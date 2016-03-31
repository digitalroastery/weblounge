/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.contentrepository.impl;

import static ch.entwine.weblounge.common.site.Environment.Development;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.GeneralResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSelector;
import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.repository.WritableContentRepository;
import ch.entwine.weblounge.common.search.SearchIndex;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Abstract implementation for read-only content repositories.
 */
public abstract class AbstractContentRepository implements ContentRepository {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(AbstractContentRepository.class);

  /** The repository type */
  protected String type = null;

  /** Index into this repository */
  protected ContentRepositoryIndex index = null;

  /** The site */
  protected Site site = null;

  /** Flag indicating the connected state */
  protected boolean connected = false;

  /** Flag indicating the initializing state */
  protected boolean initializing = false;

  /** Flag indicating the write access */
  protected boolean readOnly = false;

  /** Flag indicating the indexing state */
  protected boolean indexing = false;

  /** The document builder factory */
  protected final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

  /** The xml transformer factory */
  protected final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /** Regular expression to match the resource id, path and version */
  protected static final Pattern resourceHeaderRegex = Pattern.compile(".*<\\s*([\\w]*) .*id=\"([a-z0-9-]*)\".*path=\"([^\"]*)\".*version=\"([^\"]*)\".*");

  /** The environment */
  protected Environment environment = Environment.Production;

  /** The resource serializer service */
  protected ResourceSerializerService resourceSerializer = null;

  /** The search index service */
  protected SearchIndex searchIndex = null;

  /** The image style tracker */
  private ImageStyleTracker imageStyleTracker = null;

  /** The image preview generators */
  protected List<ImagePreviewGenerator> imagePreviewGenerators = new ArrayList<ImagePreviewGenerator>();

  /** The resources for which preview generation is due */
  private final Map<ResourceURI, PreviewOperation> previews = new HashMap<ResourceURI, PreviewOperation>();

  /** Prioritized list of preview rendering operations */
  private final Queue<PreviewOperation> previewOperations = new LinkedBlockingQueue<PreviewOperation>();

  /** The preview operations that are being worked on at the moment */
  private final List<PreviewOperation> currentPreviewOperations = new ArrayList<PreviewOperation>();

  /** The maximum number of concurrent preview operations */
  private final int maxPreviewOperations = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

  /**
   * Creates a new instance of the content repository.
   * 
   * @param type
   *          the repository type
   */
  public AbstractContentRepository(String type) {
    this.type = type;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly || !(this instanceof WritableContentRepository);
  }

  @Override
  public void connect(Site site) throws ContentRepositoryException {
    if (connected)
      throw new IllegalStateException("Content repository has already been started");
    if (site == null)
      throw new ContentRepositoryException("Site must not be null");
    this.site = site;

    try {
      index = loadIndex();
    } catch (IOException e) {
      throw new ContentRepositoryException("Error loading repository index", e);
    }

    Bundle bundle = loadBundle(site);
    if (bundle != null) {
      imageStyleTracker = new ImageStyleTracker(bundle.getBundleContext());
      imageStyleTracker.open();
    }

    connected = true;

    // Make sure previews are available as defined
    updatePreviews();
  }

  @Override
  public void disconnect() throws ContentRepositoryException {

    // Stop ongoing image preview generation
    synchronized (currentPreviewOperations) {
      logger.info("Stopping preview generation");
      previewOperations.clear();
      previews.clear();
    }

    // Close the image style tracker
    if (imageStyleTracker != null) {
      imageStyleTracker.close();
      imageStyleTracker = null;
    }

    // Close the index and mark the content repository as offline
    try {
      connected = false;
      if (index != null)
        index.close();
    } catch (IOException e) {
      throw new ContentRepositoryException("Error closing repository index", e);
    }
  }

  @Override
  public boolean isIndexing() {
    return indexing;
  }

  @Override
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return index.exists(uri);
  }

  @Override
  public boolean existsInAnyVersion(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return index.existsInAnyVersion(uri);
  }

  @Override
  public ResourceURI getResourceURI(String resourceId)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    ResourceURI uri = new GeneralResourceURIImpl(getSite(), null, resourceId);
    if (!index.exists(uri))
      return null;
    uri.setType(index.getType(uri));
    uri.setPath(index.getPath(uri));
    return uri;
  }

  @Override
  public SearchResult find(SearchQuery query) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return searchIndex.getByQuery(query);
  }

  @Override
  public List<String> suggest(String dictionary, String seed, int count)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return searchIndex.suggest(dictionary, seed, false, count, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource<?>> R get(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Check if the resource is available
    if (!index.exists(uri))
      return null;

    // Make sure we have the correct resource type
    if (uri.getType() == null) {
      uri.setType(index.getType(uri));
    } else if (!uri.getType().equals(index.getType(uri))) {
      return null;
    }
    if (uri.getIdentifier() == null && StringUtils.isNotBlank(uri.getPath())) {
      uri.setIdentifier(index.getIdentifier(uri));
    }

    // Load the resource
    SearchQuery q = new SearchQueryImpl(site).withVersion(uri.getVersion()).withIdentifier(uri.getIdentifier());
    SearchResult result = searchIndex.getByQuery(q);

    if (result.getDocumentCount() > 0) {
      ResourceSearchResultItem searchResultItem = (ResourceSearchResultItem) result.getItems()[0];
      InputStream is = null;
      try {
        ResourceSerializer<?, ?> serializer = getSerializerByType(uri.getType());
        if (serializer == null) {
          logger.warn("No resource serializer for type '{}' found", uri.getType());
          throw new ContentRepositoryException("No resource serializer for type '" + uri.getType() + "' found");
        }
        ResourceReader<?, ?> reader = serializer.getReader();
        is = IOUtils.toInputStream(searchResultItem.getResourceXml(), "utf-8");
        return (R) reader.read(is, site);
      } catch (Throwable t) {
        logger.error("Error loading {}: {}", uri, t.getMessage());
        throw new ContentRepositoryException(t);
      } finally {
        IOUtils.closeQuietly(is);
      }

    } else {

      try {
        Resource<?> resource = null;
        InputStream is = null;
        try {
          InputStream resourceStream = loadResource(uri);
          if (resourceStream == null) {
            return null;
          }
          is = new BufferedInputStream(resourceStream);
          ResourceSerializer<?, ?> serializer = getSerializerByType(uri.getType());
          ResourceReader<?, ?> reader = serializer.getReader();
          resource = reader.read(is, site);
        } catch (Throwable t) {
          String version = ResourceUtils.getVersionString(uri.getVersion());
          throw new IOException("Error reading " + version + " version of " + uri + " (" + uri.getIdentifier() + ")", t);
        } finally {
          IOUtils.closeQuietly(is);
        }

        if (resource == null) {
          logger.error("Index inconsistency detected: version '{}' of {} does not exist on disk", ResourceUtils.getVersionString(uri.getVersion()), uri);
          return null;
        }

        return (R) resource;
      } catch (IOException e) {
        logger.error("Error loading {}: {}", uri, e.getMessage());
        throw new ContentRepositoryException(e);
      }
    }

  }

  @Override
  public InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    return loadResourceContent(uri, language);
  }

  @Override
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    long[] revisions = index.getRevisions(uri);
    ResourceURI[] uris = new ResourceURI[revisions.length];
    int i = 0;
    for (long r : revisions) {
      uris[i++] = new ResourceURIImpl(uri, r);
    }
    return uris;
  }

  @Override
  public long getResourceCount() throws ContentRepositoryException {
    return index != null ? index.getResourceCount() : -1;
  }

  @Override
  public long getVersionCount() throws ContentRepositoryException {
    return index != null ? index.getRevisionCount() : -1;
  }

  /**
   * Appends the identifier of the form <code>x-y-z-u-v</code> to
   * <code>path</code> as in <code>/&lt;int&gt;/&lt;int&gt;/id</code>, with the
   * "/" being the platform's file separator.
   * 
   * @param id
   *          the identifier
   * @param path
   *          the root path
   * @return the path
   */
  protected StringBuffer appendIdToPath(String id, StringBuffer path) {
    if (id == null)
      throw new IllegalArgumentException("Identifier must not be null");
    path.append(idToDirectory(id));
    return path;
  }

  /**
   * Returns the identifier of the form <code>x-y-z-u-v</code> as a path as in
   * <code>/&lt;int&gt;/&lt;int&gt;/id</code>, with the "/" being the platform's
   * file separator.
   * 
   * @param id
   *          the identifier
   * @return the path
   */
  protected String idToDirectory(String id) {
    if (id == null)
      throw new IllegalArgumentException("Identifier must not be null");
    String[] elements = id.split("-");
    StringBuffer path = new StringBuffer();

    // convert first part of uuid to long and apply modulo 100
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[0], 16) % 100));

    // convert second part of uuid to long and apply modulo 10
    path.append(File.separatorChar);
    path.append(String.valueOf(Long.parseLong(elements[1], 16) % 10));

    // append the full uuid as the actual directory
    path.append(File.separatorChar);
    path.append(id);

    return path.toString();
  }

  /**
   * Returns the site that is associated with this repository.
   * 
   * @return the site
   */
  protected Site getSite() {
    return site;
  }

  @Override
  public Collection<ResourceURI> list(ResourceSelector selector)
      throws ContentRepositoryException {

    int index = -1;
    int selected = 0;

    Collection<ResourceURI> uris = null;
    Collection<ResourceURI> result = new ArrayList<ResourceURI>();

    List<?> selectedTypes = Arrays.asList(selector.getTypes());
    List<?> forbiddenTypes = Arrays.asList(selector.getWithoutTypes());
    List<?> selectedIds = Arrays.asList(selector.getIdentifiers());
    List<?> selectedVersions = Arrays.asList(selector.getVersions());

    try {
      uris = listResources();
    } catch (IOException e) {
      logger.error("Error reading available uris: {}", e.getMessage());
      throw new ContentRepositoryException(e);
    }

    for (ResourceURI uri : uris) {

      // Rule out types that we don't need
      if (!selectedTypes.isEmpty() && !selectedTypes.contains(uri.getType()))
        continue;
      if (!forbiddenTypes.isEmpty() && forbiddenTypes.contains(uri.getType()))
        continue;

      // Rule out resources we are not interested in
      if (!selectedIds.isEmpty() && !selectedIds.contains(uri.getIdentifier()))
        continue;
      if (!selectedVersions.isEmpty() && !selectedVersions.contains(uri.getVersion()))
        continue;

      index++;

      // Skip everything below the offset
      if (index < selector.getOffset())
        continue;

      result.add(uri);
      selected++;

      // Only collect as many items as we need
      if (selector.getLimit() > 0 && selected == selector.getLimit())
        break;
    }

    return result;
  }

  /**
   * Returns <code>true</code> if the repository is connected and started.
   * 
   * @return <code>true</code> if the repository is started
   */
  protected boolean isStarted() {
    return connected;
  }

  /**
   * Lists the resources in the content repository.
   * 
   * @return the list of resources
   * @throws ContentRepositoryException
   *           if loading metadata from the repository fails
   * @throws IOException
   *           if listing the resources fails
   */
  protected abstract Collection<ResourceURI> listResources()
      throws ContentRepositoryException, IOException;

  /**
   * Loads and returns the resource from the repository.
   * 
   * @param uri
   *          the resource uri
   * @return the resource
   * @throws ContentRepositoryException
   *           if loading metadata from the repository fails
   * @throws IOException
   *           if the resource could not be loaded
   */
  protected abstract InputStream loadResource(ResourceURI uri)
      throws ContentRepositoryException, IOException;

  /**
   * Returns the input stream to the resource content identified by
   * <code>uri</code> and <code>language</code> or <code>null</code> if no such
   * resource exists.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the content language
   * @return the resource contents
   * @throws ContentRepositoryException
   *           if loading metadata from the repository fails
   * @throws IOException
   *           if opening the stream to the resource failed
   */
  protected abstract InputStream loadResourceContent(ResourceURI uri,
      Language language) throws ContentRepositoryException, IOException;

  /**
   * Loads the repository index. Depending on the concrete implementation, the
   * index might be located in the repository itself or at any other storage
   * location. It might even be an in-memory index, in which case the repository
   * implementation is in charge of populating the index.
   * 
   * @return the index
   * @throws IOException
   *           if reading or creating the index fails
   * @throws ContentRepositoryException
   *           if populating the index fails
   */
  protected abstract ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException;

  @Override
  public int hashCode() {
    if (site != null)
      return site.hashCode();
    else
      return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractContentRepository) {
      AbstractContentRepository repo = (AbstractContentRepository) obj;
      if (site != null) {
        return site.equals(repo.getSite());
      } else {
        return super.equals(obj);
      }
    }
    return false;
  }

  /**
   * Returns the resource that is located at the indicated url.
   * 
   * @param uri
   *          the resource uri
   * @param contentUrl
   *          location of the resource file
   * @return the resource
   */
  protected Resource<?> loadResource(ResourceURI uri, URL contentUrl)
      throws IOException {
    InputStream is = null;
    try {
      is = new BufferedInputStream(contentUrl.openStream());
      ResourceSerializer<?, ?> serializer = getSerializerByType(uri.getType());
      ResourceReader<?, ?> reader = serializer.getReader();
      return reader.read(is, site);
    } catch (Throwable t) {
      throw new IOException("Error reading resource from " + contentUrl);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Returns the resource uri or <code>null</code> if no resource id and/or path
   * could be found on the specified document. This method is intended to serve
   * as a utility method when importing resources.
   * 
   * @param site
   *          the resource uri
   * @param contentUrl
   *          location of the resource file
   * @return the resource uri
   */
  protected ResourceURI loadResourceURI(Site site, URL contentUrl)
      throws IOException {

    InputStream is = null;
    InputStreamReader reader = null;
    try {
      is = new BufferedInputStream(contentUrl.openStream());
      reader = new InputStreamReader(is);
      CharBuffer buf = CharBuffer.allocate(1024);
      reader.read(buf);
      String s = new String(buf.array());
      s = s.replace('\n', ' ');
      Matcher m = resourceHeaderRegex.matcher(s);
      if (m.matches()) {
        long version = ResourceUtils.getVersion(m.group(4));
        return new ResourceURIImpl(m.group(1), site, m.group(3), m.group(2), version);
      }
      return null;
    } finally {
      if (reader != null)
        reader.close();
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Replaces templates inside the property value with their corresponding value
   * from the system properties and environment.
   * 
   * @param v
   *          the original property value
   * @return the processed value
   */
  protected Object processPropertyTemplates(Object v) {
    if (v == null || !(v instanceof String))
      return v;

    String value = (String) v;

    // Do variable replacement using the system properties
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      StringBuffer envKey = new StringBuffer("\\$\\{").append(entry.getKey()).append("\\}");
      value = value.replaceAll(envKey.toString(), entry.getValue().toString());
    }

    // Do variable replacement using the system environment
    for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
      StringBuffer envKey = new StringBuffer("\\$\\{").append(entry.getKey()).append("\\}");
      value = value.replaceAll(envKey.toString(), entry.getValue());
    }

    return value;
  }

  /**
   * Tries to find the site's bundle in the OSGi service registry and returns
   * it, <code>null</code> otherwise.
   * 
   * @param site
   *          the site
   * @return the bundle
   */
  protected Bundle loadBundle(Site site) {
    Bundle bundle = FrameworkUtil.getBundle(this.getClass());
    if (bundle == null)
      return null;
    BundleContext bundleCtx = bundle.getBundleContext();
    if (bundleCtx == null) {
      logger.debug("Bundle {} does not have a bundle context associated", bundle);
      return null;
    }
    String siteClass = Site.class.getName();
    try {
      ServiceReference[] refs = bundleCtx.getServiceReferences(siteClass, null);
      if (refs == null || refs.length == 0)
        return null;
      for (ServiceReference ref : refs) {
        Site s = (Site) bundleCtx.getService(ref);
        if (s == site)
          return ref.getBundle();
      }
      return null;
    } catch (InvalidSyntaxException e) {
      // Can't happen
      logger.error("Error trying to locate the site's bundle", e);
      return null;
    }
  }

  @Override
  public void createPreviews() throws ContentRepositoryException {
    Collection<ResourceURI> uris = null;
    logger.debug("Starting preview generation");

    // Load the uris
    try {
      uris = listResources();
    } catch (IOException e) {
      logger.warn("Error retrieving list of resources: {}", e.getMessage());
      return;
    }

    // Initiate preview generation
    for (ResourceURI uri : uris) {
      Resource<?> resource = get(uri);
      if (resource == null) {
        logger.warn("Skipping missing {} for preview generation", uri);
        continue;
      }
      createPreviews(resource, site.getLanguages());
    }

  }

  /**
   * Iterates over the existing image styles and determines whether at least one
   * style has changed or is missing the previews.
   * 
   * @throws ContentRepositoryException
   *           if preview generation fails
   */
  protected void updatePreviews() throws ContentRepositoryException {

    // Compile the full list of image styles
    if (imageStyleTracker == null) {
      logger.info("Skipping preview generation: image styles are unavailable");
      return;
    }

    final List<ImageStyle> allStyles = new ArrayList<ImageStyle>();

    // Add the global image styles that have the preview flag turned on
    for (ImageStyle s : imageStyleTracker.getImageStyles()) {
      allStyles.add(s);
    }

    // Add the site's preview image styles as well as
    for (Module m : getSite().getModules()) {
      for (ImageStyle s : m.getImageStyles()) {
        allStyles.add(s);
      }
    }

    // Check whether the image styles still match the current definition. If
    // not, remove the produced previews and recreate them.
    boolean styleHasChanged = false;
    boolean styleIsMissing = false;

    for (ImageStyle s : allStyles) {
      File baseDir = ImageStyleUtils.getDirectory(site, s);
      File definitionFile = new File(baseDir, "style.xml");

      // Try and read the file on disk
      if (definitionFile.isFile()) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc;
        ImageStyle style;
        try {
          docBuilder = docBuilderFactory.newDocumentBuilder();
          doc = docBuilder.parse(definitionFile);
          style = ImageStyleImpl.fromXml(doc.getFirstChild());

          // Is the style still the same?
          boolean stylesMatch = s.getWidth() == style.getWidth();
          stylesMatch = stylesMatch && s.getHeight() == style.getHeight();
          stylesMatch = stylesMatch && s.getScalingMode().equals(style.getScalingMode());
          stylesMatch = stylesMatch && s.getContexts().size() == style.getContexts().size();
          for (String ctx : s.getContexts()) {
            stylesMatch = stylesMatch && s.createPreview(ctx) == style.createPreview(ctx);
          }
          styleHasChanged = styleHasChanged || !stylesMatch;
        } catch (ParserConfigurationException e) {
          logger.error("Error setting up image style parser: {}", e.getMessage());
        } catch (SAXException e) {
          logger.error("Error parsing image style {}: {}", definitionFile, e.getMessage());
        } catch (IOException e) {
          logger.error("Error reading image style {}: {}", definitionFile, e.getMessage());
        }
      } else {
        boolean previewsForAnyContext = false;
        for (String ctx : s.getContexts()) {
          previewsForAnyContext |= s.createPreview(ctx);
        }
        if (previewsForAnyContext) {
          logger.debug("No previews found for image style '{}'", s.getIdentifier());
          styleIsMissing = true;
        }
      }

      // The current definition is no longer valid
      if (styleHasChanged) {
        logger.info("Image style '{}' has changed, removing existing previews from {}", s.getIdentifier(), baseDir);
        FileUtils.deleteQuietly(baseDir);
        if (!baseDir.mkdirs()) {
          logger.error("Error creating image style directory {}", baseDir);
          continue;
        }
      }

      // Store the new definition
      if (!definitionFile.isFile() || styleHasChanged) {
        try {
          definitionFile.getParentFile().mkdirs();
          definitionFile.createNewFile();
          FileUtils.copyInputStreamToFile(IOUtils.toInputStream(s.toXml(), "UTF-8"), definitionFile);
        } catch (IOException e) {
          logger.error("Error creating image style defintion file at {}", definitionFile, e.getMessage());
          continue;
        }
      } else {
        logger.debug("Image style {} still matching the current definition", s.getIdentifier());
      }
    }

    if (styleHasChanged || styleIsMissing) {
      if (environment.equals(Development)) {
        logger.info("Missing or outdated previews found. Skipping preview generation for current environment 'development'");
        return;
      }
      logger.info("Triggering creation of missing and outdated previews");
      createPreviews();
    } else {
      logger.debug("Preview images for {} are still up to date", site.getIdentifier());
    }
  }

  /**
   * Creates the previews for this resource in all languages and for all known
   * image styles. The implementation ensures that there is only one preview
   * renderer running per resource.
   * 
   * @param resource
   *          the resource
   * @param languages
   *          the languages to build the previews for
   */
  protected void createPreviews(final Resource<?> resource,
      Language... languages) {

    ResourceURI uri = resource.getURI();

    // Compile the full list of image styles
    if (imageStyleTracker == null) {
      logger.info("Skipping preview generation for {}: image styles are unavailable", uri);
      return;
    }

    final List<ImageStyle> previewStyles = new ArrayList<ImageStyle>();

    // Add the global image styles that have the preview flag turned on
    for (ImageStyle s : imageStyleTracker.getImageStyles()) {
      if (s.createPreview(resource.getURI().getType())) {
        previewStyles.add(s);
        logger.debug("Preview images will be generated for {}", s);
      } else {
        logger.debug("Preview image generation will be skipped for {}", s);
      }
    }

    // Add the site's preview image styles as well as
    for (Module m : getSite().getModules()) {
      for (ImageStyle s : m.getImageStyles()) {
        if (s.createPreview(resource.getURI().getType())) {
          previewStyles.add(s);
          logger.debug("Preview images will be generated for {}", s);
        } else {
          logger.debug("Preview image generation will be skipped for {}", s);
        }
      }
    }

    // If no language has been specified, we create the preview for all
    // languages
    if (languages == null || languages.length == 0) {
      languages = uri.getSite().getLanguages();
    }

    // Create the previews
    PreviewOperation previewOp = null;
    synchronized (currentPreviewOperations) {

      // is there an existing operation for this resource? If so, simply update
      // it and be done.
      previewOp = previews.get(uri);
      if (previewOp != null) {
        PreviewGeneratorWorker worker = previewOp.getWorker();
        if (worker != null) {
          logger.info("Canceling current preview generation for {} in favor of more recent data", uri);
          worker.cancel();
        }
      }

      // Otherwise, a new preview generator needs to be started.
      previewOp = new PreviewOperation(resource, Arrays.asList(languages), previewStyles, ImageStyleUtils.DEFAULT_PREVIEW_FORMAT);

      // Make sure nobody is working on the same resource at the moment
      if (currentPreviewOperations.contains(previewOp)) {
        logger.debug("Queing concurring creation of preview for {}", uri);
        previews.put(uri, previewOp);
        previewOperations.add(previewOp);
        return;
      }

      // If there is enough being worked on already, there is nothing we can do
      // right now, the work will be picked up later on
      if (currentPreviewOperations.size() >= maxPreviewOperations) {
        logger.debug("Queing creation of preview for {}", uri);
        previews.put(uri, previewOp);
        previewOperations.add(previewOp);
        logger.debug("Preview generation queue now contains {} resources", previews.size());
        return;
      }

      // It seems like it is safe to start the preview generation
      currentPreviewOperations.add(previewOp);
      PreviewGeneratorWorker previewWorker = new PreviewGeneratorWorker(this, previewOp.getResource(), environment, previewOp.getLanguages(), previewOp.getStyles(), previewOp.getFormat());
      previewOp.setWorker(previewWorker);
      Thread t = new Thread(previewWorker);
      t.setPriority(Thread.MIN_PRIORITY);
      t.setDaemon(true);

      logger.debug("Creating preview of {}", uri);
      t.start();
    }
  }

  /**
   * Callback for the preview renderer to indicate a finished rendering
   * operation.
   * 
   * @param resource
   *          the resource
   */
  void previewCreated(Resource<?> resource) {
    synchronized (currentPreviewOperations) {

      // Do the cleanup
      for (Iterator<PreviewOperation> i = currentPreviewOperations.iterator(); i.hasNext();) {
        PreviewOperation op = i.next();
        Resource<?> r = op.getResource();
        if (r.equals(resource)) {
          logger.debug("Preview creation of {} finished", r.getURI());
          i.remove();
          PreviewOperation o = previews.get(r.getURI());
          // In the meantime, someone may have canceled this operation and
          // created a new one
          if (op == o)
            previews.remove(r.getURI());
          break;
        }
      }

      // Is there more work to do?
      if (!previewOperations.isEmpty() && currentPreviewOperations.size() < maxPreviewOperations) {

        // Get the next operation and do the bookkeeping
        PreviewOperation op = previewOperations.remove();
        Resource<?> r = op.getResource();
        currentPreviewOperations.add(op);

        // Finally start the generation
        PreviewGeneratorWorker previewWorker = new PreviewGeneratorWorker(this, r, environment, op.getLanguages(), op.getStyles(), op.getFormat());
        op.setWorker(previewWorker);
        Thread t = new Thread(previewWorker);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);

        logger.debug("Starting creation of preview of {}", r.getURI());
        logger.trace("There are {} more preview operations waiting", previewOperations.size());
        logger.trace("Currently using {} out of {} preview creation slots", currentPreviewOperations.size(), maxPreviewOperations);
        t.start();
      } else {
        logger.debug("No more resources queued for preview creation");
      }
    }
  }

  /**
   * Deletes the previews for this resource in all languages and for all known
   * image styles.
   * 
   * @param resource
   *          the resource
   */
  protected void deletePreviews(Resource<?> resource) {
    deletePreviews(resource, null);
  }

  /**
   * Deletes the previews for this resource in the given languages and for all
   * known image styles.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   */
  protected void deletePreviews(Resource<?> resource, Language language) {
    // Compile the full list of image styles
    List<ImageStyle> styles = new ArrayList<ImageStyle>();
    if (imageStyleTracker != null)
      styles.addAll(imageStyleTracker.getImageStyles());
    for (Module m : getSite().getModules()) {
      styles.addAll(Arrays.asList(m.getImageStyles()));
    }

    for (ImageStyle style : styles) {
      File styledImage = null;

      // Create the path to a sample image
      if (language != null) {
        styledImage = ImageStyleUtils.getScaledFile(resource, language, style);
      } else {
        styledImage = ImageStyleUtils.getScaledFile(resource, LanguageUtils.getLanguage("en"), style);
        styledImage = styledImage.getParentFile();
      }

      // Remove the parent's directory, which will include the specified
      // previews
      File dir = styledImage.getParentFile();
      logger.debug("Deleting previews in {}", dir.getAbsolutePath());
      FileUtils.deleteQuietly(dir);
    }
  }

  /**
   * Returns the current environment.
   * 
   * @return the environment
   */
  protected Environment getEnvironment() {
    return environment;
  }

  /**
   * This method is called right after initialization of the content repository
   * and sets the environment.
   * 
   * @param environment
   *          the environment
   */
  public void setEnvironment(Environment environment) {
    if (environment == null)
      throw new IllegalStateException("Environment has not been set");
    this.environment = environment;
  }

  /**
   * Returns the resource serializer for the given type or <code>null</code> if
   * no such serializer is registered.
   * 
   * @param type
   *          the resource type
   * @return the serializer
   */
  protected ResourceSerializer<?, ?> getSerializerByType(String type) {
    if (resourceSerializer == null)
      throw new IllegalStateException("Serializer service has not been set");
    return resourceSerializer.getSerializerByType(type);
  }

  /**
   * Returns the resource serializer for the given mime type or
   * <code>null</code> if no such serializer is registered.
   * 
   * @param mimeType
   *          the mime type
   * @return the serializer
   */
  protected ResourceSerializer<?, ?> getSerializerByMimeType(String mimeType) {
    if (resourceSerializer == null)
      throw new IllegalStateException("Serializer service has not been set");
    return resourceSerializer.getSerializerByMimeType(mimeType);
  }

  /**
   * Returns the set of available resource serializers.
   * 
   * @return the set serializer
   */
  protected Set<ResourceSerializer<?, ?>> getSerializers() {
    return resourceSerializer.getSerializers();
  }

  /**
   * This method is called right after initialization of the content repository
   * and is used to register the factory with a backing service implementation.
   * 
   * @param service
   *          the resource serializer service
   */
  public void setSerializer(ResourceSerializerService service) {
    resourceSerializer = service;
  }

  public void setSearchIndex(SearchIndex index) {
    searchIndex = index;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (imagePreviewGenerators) {
      imagePreviewGenerators.add(generator);
      Collections.sort(imagePreviewGenerators, new Comparator<PreviewGenerator>() {
        public int compare(PreviewGenerator a, PreviewGenerator b) {
          return Integer.valueOf(b.getPriority()).compareTo(a.getPriority());
        }
      });
    }
  }

  /**
   * Removes the preview generator from the list of registered preview
   * generators.
   * 
   * @param generator
   *          the generator
   */
  void removePreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (imagePreviewGenerators) {
      imagePreviewGenerators.remove(generator);
    }
  }

  /**
   * Data structure that is used to hold all relevant information for preview
   * generation of a given resource.
   */
  private static final class PreviewOperation {

    /** The resource to be rendered */
    private Resource<?> resource = null;

    /** List of languages that need to be rendered */
    private final List<Language> languages = new ArrayList<Language>();

    /** List of image styles that need to be rendered */
    private final List<ImageStyle> styles = new ArrayList<ImageStyle>();

    /** Name of the preview image format */
    private String format = null;

    /** Worker that is in charge of conducting this operation */
    private PreviewGeneratorWorker worker = null;

    /**
     * Creates a new representation of a preview generation.
     */
    public PreviewOperation(Resource<?> resource, List<Language> languages,
        List<ImageStyle> styles, String format) {
      this.resource = resource;
      this.languages.addAll(languages);
      this.styles.addAll(styles);
      this.format = format;
    }

    /**
     * Sets the worker that is in charge of conducting this operation.
     * 
     * @param worker
     *          the worker
     */
    void setWorker(PreviewGeneratorWorker worker) {
      this.worker = worker;
    }

    /**
     * Returns the worker that is in charge of this operation.
     * 
     * @return the worker
     */
    PreviewGeneratorWorker getWorker() {
      return this.worker;
    }

    @Override
    public int hashCode() {
      return resource.hashCode();
    }

    @Override
    public boolean equals(Object op) {
      return resource.equals(((PreviewOperation) op).getResource());
    }

    /**
     * Returns the resource that is to be rendered.
     * 
     * @return the resource
     */
    public Resource<?> getResource() {
      return resource;
    }

    /**
     * Returns the languages that need preview generation.
     * 
     * @return the language
     */
    public List<Language> getLanguages() {
      return languages;
    }

    /**
     * Returns the image styles.
     * 
     * @return the styles
     */
    public List<ImageStyle> getStyles() {
      return styles;
    }

    /**
     * @return the format
     */
    public String getFormat() {
      return format;
    }

  }

}
