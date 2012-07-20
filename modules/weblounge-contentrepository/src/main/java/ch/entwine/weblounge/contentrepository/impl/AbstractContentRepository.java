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

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.GeneralResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

/**
 * Abstract implementation for read-only content repositories.
 */
public abstract class AbstractContentRepository implements ContentRepository {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractContentRepository.class);

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

  /** The image preview generators */
  protected List<ImagePreviewGenerator> imagePreviewGenerators = new ArrayList<ImagePreviewGenerator>();

  /**
   * Creates a new instance of the content repository.
   * 
   * @param type
   *          the repository type
   */
  public AbstractContentRepository(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#isReadOnly()
   */
  public boolean isReadOnly() {
    return readOnly || !(this instanceof WritableContentRepository);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
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

    connected = true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
    if (!connected)
      throw new IllegalStateException("Cannot stop a disconnected content repository");
    try {
      connected = false;
      index.close();
    } catch (IOException e) {
      throw new ContentRepositoryException("Error closing repository index", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#isIndexing()
   */
  public boolean isIndexing() {
    return indexing;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#exists(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    try {
      return index.exists(uri);
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#existsInAnyVersion(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean existsInAnyVersion(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    try {
      return index.existsInAnyVersion(uri);
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getResourceURI(java.lang.String)
   */
  public ResourceURI getResourceURI(String resourceId)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    try {
      ResourceURI uri = new GeneralResourceURIImpl(getSite(), null, resourceId);
      if (!index.exists(uri))
        return null;
      uri.setType(index.getType(uri));
      uri.setPath(index.getPath(uri));
      return uri;
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#find(ch.entwine.weblounge.common.content.SearchQuery)
   */
  public SearchResult find(SearchQuery query) throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return index.find(query);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws ContentRepositoryException
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#suggest(java.lang.String,
   *      java.lang.String, int)
   */
  public List<String> suggest(String dictionary, String seed, int count)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return index.suggest(dictionary, seed, false, count, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @SuppressWarnings("unchecked")
  public <R extends Resource<?>> R get(
      ResourceURI uri)
          throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Check if the resource is available
    try {
      if (!index.exists(uri)) {
        return null;
      }
    } catch (IOException e) {
      logger.error("Error looking up uri {}: {}", uri, e.getMessage());
      throw new ContentRepositoryException(e);
    }

    // Make sure we have the correct resource type
    try {
      if (uri.getType() == null) {
        uri.setType(index.getType(uri));
      } else if (!uri.getType().equals(index.getType(uri))) {
        return null;
      }
      if (uri.getIdentifier() == null && StringUtils.isNotBlank(uri.getPath())) {
        uri.setIdentifier(index.getIdentifier(uri));
      }
    } catch (IOException e) {
      logger.error("Error looking up type for {}: {}", uri, e.getMessage());
      throw new ContentRepositoryException(e);
    }

    // Load the resource
    SearchQuery q = new SearchQueryImpl(site).withVersion(uri.getVersion()).withIdentifier(uri.getIdentifier());
    SearchResult result = index.find(q);

    if (result.getDocumentCount() > 0) {
      ResourceSearchResultItem searchResultItem = (ResourceSearchResultItem) result.getItems()[0];
      InputStream is = null;
      try {
        ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(uri.getType());
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
          ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(uri.getType());
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException {
    return loadResourceContent(uri, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getVersions(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    try {
      long[] revisions = index.getRevisions(uri);
      ResourceURI[] uris = new ResourceURI[revisions.length];
      int i = 0;
      for (long r : revisions) {
        uris[i++] = new ResourceURIImpl(uri, r);
      }
      return uris;
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public Iterator<ResourceURI> list(ResourceURI uri)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");
    return list(uri, Integer.MAX_VALUE, -1);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      long[])
   */
  public Iterator<ResourceURI> list(ResourceURI uri, long version)
      throws ContentRepositoryException {
    return list(uri, Integer.MAX_VALUE, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      int)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level)
      throws ContentRepositoryException {
    return list(uri, level, -1);
  }

  /**
   * {@inheritDoc}
   * 
   * This implementation uses the index to get the list.
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI,
   *      int, long)
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version)
      throws ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    return index.list(uri, level, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getResourceCount()
   */
  public long getResourceCount() {
    return index != null ? index.getResourceCount() : -1;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepository#getVersionCount()
   */
  public long getVersionCount() {
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

  /**
   * Returns <code>true</code> if the repository is connected and started.
   * 
   * @return <code>true</code> if the repository is started
   */
  protected boolean isStarted() {
    return connected;
  }

  /**
   * Loads and returns the resource from the repository.
   * 
   * @param uri
   *          the resource uri
   * @return the resource
   * @throws IOException
   *           if the resource could not be loaded
   */
  protected abstract InputStream loadResource(ResourceURI uri)
      throws IOException;

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
   * @throws IOException
   *           if opening the stream to the resource failed
   */
  protected abstract InputStream loadResourceContent(ResourceURI uri,
      Language language) throws IOException;

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

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (site != null)
      return site.hashCode();
    else
      return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(uri.getType());
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

  /**
   * Callback from OSGi to set the environment.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
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

}
