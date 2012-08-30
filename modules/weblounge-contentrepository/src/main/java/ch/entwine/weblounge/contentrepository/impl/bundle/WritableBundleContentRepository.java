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

package ch.entwine.weblounge.contentrepository.impl.bundle;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Represents a bundle repository that is writable. OSGi bundles are not
 * writable, so this content repository behaves like a
 * <code>FileSystemContentRepository</code>, while the initial content is copied
 * from the respective bundle.
 */
public class WritableBundleContentRepository extends FileSystemContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WritableBundleContentRepository.class);

  /** The repository type */
  public static final String TYPE = "ch.entwine.weblounge.contentrepository.bundle";

  /** Option specifying the root directory */
  public static final String OPT_ROOT_DIR = BundleContentRepository.CONF_PREFIX + "root";

  /** Prefix into the bundle */
  protected String bundlePathPrefix = "/repository";

  /** The site's bundle */
  protected Bundle bundle = null;

  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {

    // Don't have the super implementation automatically create a home page
    // for us. Otherwise, the creation of the index from the bundle won't work.
    createHomepage = false;

    // Initialize the repository and the repository index
    super.connect(site);

    try {
      initializing = true;

      // Find the site's bundle
      bundle = loadBundle(site);
      if (bundle == null)
        throw new ContentRepositoryException("Unable to locate bundle for site '" + site + "'");

      // Add the bundle contents to the index
      if (getResourceCount() == 0)
        indexBundleContents();

      // If there was no homepage as part of the bundle, create it
      createHomepage();

    } finally {
      initializing = false;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void updated(Dictionary properties) throws ConfigurationException {

    // Since the bundle content repository is based on the file system content
    // repository, the configuration properties need to be adjusted
    String rootDirPath = (String) properties.get(OPT_ROOT_DIR);
    if (StringUtils.isNotBlank(rootDirPath)) {
      properties.put(FileSystemContentRepository.OPT_ROOT_DIR, PathUtils.trim(rootDirPath));
      logger.info("Bundle content repository data will be stored at {}", rootDirPath);
    }

    super.updated(properties);
  }

  /**
   * Initializes the content repository by loading the resources from the bundle
   * and adding it to the repository index.
   */
  protected void indexBundleContents() throws ContentRepositoryException {

    // Are the serializers already up and running?
    Set<ResourceSerializer<?, ?>> serializers = ResourceSerializerFactory.getSerializers();
    if (serializers == null) {
      logger.warn("Unable to index {} while no resource serializers are registered", this);
      return;
    }

    // See if there are any resources. If that's the case, then we don't need to
    // do anything. If not, we need to copy everything that's currently in the
    // bundle.
    for (ResourceSerializer<?, ?> serializer : serializers) {
      String resourceDirectoryPath = UrlUtils.concat(repositorySiteRoot.getAbsolutePath(), serializer.getType() + "s");
      File resourceDirectory = new File(resourceDirectoryPath);
      if (resourceDirectory.isDirectory() && resourceDirectory.list().length > 0) {
        logger.debug("Found existing {}s for site '{}' at {}", new Object[] {
            serializer.getType(),
            site,
            resourceDirectoryPath });
        return;
      }
    }

    // If there is no content repository at the target location, copy the
    // initial bundle contents to the filesystem. Otherwise, keep working with
    // what's there already.
    logger.info("Loading resources for '{}' from bundle '{}'", site, bundle.getSymbolicName());
    try {
      for (Iterator<ResourceURI> pi = getResourceURIsFromBundle(); pi.hasNext();) {
        ResourceURI uri = pi.next();

        try {
          Resource<?> resource = loadResourceFromBundle(uri);
          if (resource == null) {
            throw new ContentRepositoryException("Unable to load " + uri.getType() + " " + uri + " from bundle");
          }

          // Update the uri, it now contains the id in addition to just the path
          uri = resource.getURI();

          // Make sure we are not updating existing resources, since this is the
          // first time import.
          if (exists(uri)) {
            throw new ContentRepositoryException("Error adding resource " + uri + " to repository: a resource with id '" + uri.getIdentifier() + "' or path '" + uri.getPath() + "' already exists");
          }

          logger.info("Loading {} {}:{}", new Object[] {
              uri.getType(),
              site,
              uri });
          Set<? extends ResourceContent> content = resource.contents();
          if (content.size() == 0) {
            put(resource);
          } else {
            for (ResourceContent c : content)
              resource.removeContent(c.getLanguage());
            put(resource);
            for (ResourceContent c : content) {
              InputStream is = null;
              try {
                is = loadResourceContentFromBundle(uri, c);
                if (is == null && c.getExternalLocation() == null)
                  throw new ContentRepositoryException("Resource content " + c + " missing from repository");
                putContent(uri, c, is);
              } finally {
                IOUtils.closeQuietly(is);
              }
            }
          }
        } catch (IOException e) {
          logger.error("Error reading " + uri.getType() + " " + uri + ": " + e.getMessage(), e);
          throw new ContentRepositoryException(e);
        }
      }
    } catch (ContentRepositoryException e) {
      cleanupAfterFailure();
      throw e;
    }

    // Log index statistics to console
    long resourceCount = index.getResourceCount();
    long resourceVersionCount = index.getRevisionCount();
    logger.info("Index contains {} resources and {} revisions", resourceCount, resourceVersionCount - resourceCount);
  }

  /**
   * Closes the index and removes the bundle directory from disk.
   */
  private void cleanupAfterFailure() {
    try {
      index.close();
      FileUtils.deleteDirectory(repositorySiteRoot);
      logger.error("Site index and repository directory have been reset");
    } catch (IOException e2) {
      logger.error("Unable to clean up index and repository directory " + repositorySiteRoot);
    }
  }

  /**
   * Loads all resources from the bundle and returns their uris as an iterator.
   * 
   * @return the resource uris
   * @throws ContentRepositoryException
   *           if reading from the repository fails
   */
  @SuppressWarnings("unchecked")
  protected Iterator<ResourceURI> getResourceURIsFromBundle()
      throws ContentRepositoryException {

    List<ResourceURI> resourceURIs = new ArrayList<ResourceURI>();

    // For every serializer, try to load the resources
    Set<ResourceSerializer<?, ?>> serializers = ResourceSerializerFactory.getSerializers();
    for (ResourceSerializer<?, ?> serializer : serializers) {
      String resourceDirectory = serializer.getType() + "s";
      String resourcePathPrefix = UrlUtils.concat(bundlePathPrefix, resourceDirectory);
      Enumeration<URL> entries = bundle.findEntries(resourcePathPrefix, "*.xml", true);
      if (entries != null) {
        while (entries.hasMoreElements()) {
          URL entry = entries.nextElement();
          String path = FilenameUtils.getPath(entry.getPath());
          path = path.substring(resourcePathPrefix.length() - 1);
          long v = ResourceUtils.getVersion(FilenameUtils.getBaseName(entry.getPath()));
          ResourceURI resourceURI = new ResourceURIImpl(serializer.getType(), site, path, v);
          resourceURIs.add(resourceURI);
          logger.trace("Found revision '{}' of {} {}", new Object[] {
              v,
              resourceURI.getType(),
              entry });
        }
      }
    }

    return resourceURIs.iterator();
  }

  /**
   * Loads the specified resource from the bundle rather than from the content
   * repository.
   * 
   * @param uri
   *          the uri
   * @return the resource
   * @throws IOException
   *           if reading the resource fails
   */
  protected Resource<?> loadResourceFromBundle(ResourceURI uri)
      throws IOException {
    String uriPath = uri.getPath();
    if (uriPath == null)
      throw new IllegalArgumentException("Resource uri needs a path");
    String entryPath = UrlUtils.concat(bundlePathPrefix, uri.getType() + "s", uriPath, ResourceUtils.getDocument(uri.getVersion()));
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    try {
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(uri.getType());
      if (serializer == null) {
        logger.warn("Unable to read {} {}: no serializer found", uri.getType(), uri);
        return null;
      }
      ResourceReader<?, ?> resourceReader = serializer.getReader();
      return resourceReader.read(url.openStream(), site);
    } catch (SAXException e) {
      throw new RuntimeException("SAX error while reading " + uri.getType() + " '" + uri + "'", e);
    } catch (IOException e) {
      throw new IOException("I/O error while reading " + uri.getType() + " '" + uri + "'", e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Parser configuration error while reading " + uri.getType() + " '" + uri + "'", e);
    } catch (Throwable t) {
      throw new IllegalStateException(t);
    }
  }

  /**
   * Loads the specified resource from the bundle rather than from the content
   * repository.
   * 
   * @param uri
   *          the uri
   * @return the resource
   * @throws IOException
   *           if reading the resource fails
   */
  protected InputStream loadResourceContentFromBundle(ResourceURI uri,
      ResourceContent content) throws IOException {
    String uriPath = uri.getPath();
    if (uriPath == null)
      throw new IllegalArgumentException("Resource uri needs a path");
    String documentName = content.getLanguage().getIdentifier();
    if (!"".equals(FilenameUtils.getExtension(content.getFilename())))
      documentName += "." + FilenameUtils.getExtension(content.getFilename());
    String entryPath = UrlUtils.concat(bundlePathPrefix, uri.getType() + "s", uriPath, documentName);
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    return url.openStream();
  }

}
