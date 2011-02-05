/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.contentrepository.impl.bundle;

import ch.o2it.weblounge.common.content.MalformedResourceURIException;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceReader;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ResourceSerializer;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Content repository that reads pages and resources from the site's
 * <code>OSGi</code> bundle. Since a bundle is a read only archive, it does not
 * implement the extended <code>WritableContentRepository</code> interface.
 * <p>
 * The implementation assumes a prefix to all page and resource uris of
 * <code>/repository</code>, which means that pages are expected to live under
 * <code>/repository/pages</code> while resources are expected under
 * <code>/repository/resources</code>. You may want to change these assumptions
 * using {@link #setBundlePathPrefix(String)}, {@link #setPagesURI(String)} or
 * {@link #setResourcesURI()}.
 */
public class BundleContentRepositoryImpl extends AbstractContentRepository implements ManagedService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(BundleContentRepositoryImpl.class);

  /** The repository type */
  public static final String TYPE = "ch.o2it.weblounge.contentrepository.bundle";

  /** Prefix for repository configuration keys */
  private static final String CONF_PREFIX = "contentrepository.bundle.";

  /** Option to cleanup temporary bundle index on shutdown */
  private static final String OPT_CLEANUP = CONF_PREFIX + "cleanup";

  /** The site bundle context */
  protected Bundle bundle = null;

  /** Prefix into the bundle */
  protected String bundlePathPrefix = "/repository";

  /** The root directory for the temporary bundle index */
  protected File idxRootDir = null;

  /** Flag to indicate whether temporary indices should be removed on shutdown */
  protected boolean cleanupTemporaryIndex = false;

  /**
   * Creates a new instance of the bundle content repository.
   */
  public BundleContentRepositoryImpl() {
    super(TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#connect(ch.o2it.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {
    idxRootDir = new File(PathUtils.concat(new String[] {
        System.getProperty("java.io.tmpdir"),
        "repository",
        getSite().getIdentifier(),
        "index", }));
    try {
      FileUtils.forceMkdir(idxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Unable to create temporary site index at " + idxRootDir, e);
    }
    
    super.connect(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#disconnect()
   */
  @Override
  public void disconnect() throws ContentRepositoryException {
    super.disconnect();
    if (idxRootDir != null && idxRootDir.exists() && cleanupTemporaryIndex) {
      logger.info("Removing temporary site index at {}", idxRootDir);
      FileUtils.deleteQuietly(idxRootDir);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;

    // Cleanup after shutdown?
    if (properties.get(OPT_CLEANUP) != null) {
      cleanupTemporaryIndex = ConfigurationUtils.isTrue((String) properties.get(OPT_CLEANUP));
      logger.info("Bundle content repository indices will {} removed on shutdown", (cleanupTemporaryIndex ? "be" : "not be"));
    }
  }

  /**
   * Sets the bundle that this repository connects to.
   * 
   * @param bundle
   *          the bundle
   */
  public void setBundle(Bundle bundle) {
    this.bundle = bundle;
  }

  /**
   * Returns the bundle that contains the site.
   * 
   * @return the site bundle
   */
  protected Bundle getBundle() {
    return bundle;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.ch.o2it.weblounge.common.content.repository.ContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)
   */
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Page uri cannot be null");
    try {
      List<ResourceURI> uris = new ArrayList<ResourceURI>();
      long[] versions = index.getRevisions(uri);
      for (long version : versions) {
        uris.add(new ResourceURIImpl(uri, version));
      }
      return uris.toArray(new ResourceURI[uris.size()]);
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI,
   *      int, long)
   */
  @SuppressWarnings("unchecked")
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version)
      throws ContentRepositoryException {
    String entryPath = uri.getPath();
    if (!entryPath.startsWith("/"))
      throw new IllegalArgumentException("Resource uri must be absolute");
    int startLevel = StringUtils.countMatches(uri.getPath(), "/");
    List<ResourceURI> resources = new ArrayList<ResourceURI>();
    Enumeration<URL> entries = bundle.findEntries(entryPath, "*.xml", level > 0);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        URL entry = entries.nextElement();
        int currentLevel = StringUtils.countMatches(uri.getPath(), "/");
        if (level > -1 && level < Integer.MAX_VALUE && currentLevel > (startLevel + level))
          continue;
        long v = ResourceUtils.getVersion(FilenameUtils.getBaseName(entry.getPath()));
        if (version != -1 && v != version)
          continue;
        try {
          ResourceURI resourceURI = loadResourceURI(uri.getSite(), entry);
          if (resourceURI == null)
            throw new IllegalStateException("Resource " + entry + " has no uri");
          resources.add(resourceURI);
          logger.trace("Found revision '{}' of resource {}", v, entry);
        } catch (IOException e) {
          throw new ContentRepositoryException("Unable to read id from resource at " + entry, e);
        }
      }
    }
    return resources.iterator();
  }

  /**
   * Sets the path that identifies the repository root in the bundle. If nothing
   * is specified, the default value of <code>/repository</code> will be used.
   * 
   * @param repositoryURI
   *          the path to the repository
   * @throws IllegalArgumentException
   *           if the uri is <code>null</code>
   * @throws IllegalArgumentException
   *           if the uri is not absolute
   */
  public void setBundlePathPrefix(String repositoryURI) {
    if (repositoryURI == null)
      throw new IllegalArgumentException("Repository uri cannot be null");
    if (repositoryURI.startsWith("/"))
      throw new IllegalArgumentException("Repository uri Must be absolute");
    this.bundlePathPrefix = repositoryURI;
  }

  /**
   * Returns the path that identifies the repository root in the bundle.
   * 
   * @return the path to the repository
   */
  public String getBundlePathPrefix() {
    return bundlePathPrefix;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException {

    logger.debug("Creating temporary site index at {}", idxRootDir);
    FileUtils.forceMkdir(idxRootDir);

    BundleContentRepositoryIndex index = null;
    index = new BundleContentRepositoryIndex(idxRootDir);
    boolean success = false;

    // Make sure the version matches the implementation
    if (index.getIndexVersion() != VersionedContentRepositoryIndex.INDEX_VERSION) {
      logger.warn("Index version does not match implementation, triggering reindex");
    }

    // Is there an existing index?
    else if (index.getResourceCount() > 0) {
      long resourceCount = index.getResourceCount();
      long revisionCount = index.getRevisionCount();
      logger.info("Loaded exising site index from {}", idxRootDir);
      logger.info("Index contains {} resources and {} revisions", resourceCount, revisionCount - resourceCount);
      return index;
    }

    try {
      logger.info("Populating temporary site index '{}'...", site);
      long time = System.currentTimeMillis();
      long resourceCount = 0;
      long revisionCount = 0;
      ResourceURI previousURI = null;

      // Add all known resource types to the index
      Set<ResourceSerializer<?, ?>> serializers = ResourceSerializerFactory.getSerializers();
      for (ResourceSerializer<?, ?> serializer : serializers) {
        String resourcePath = "/" + serializer.getType() + "s";
        ResourceURI resourceRootURI = new ResourceURIImpl(serializer.getType(), getSite(), resourcePath);
        Iterator<ResourceURI> pi = list(resourceRootURI, Integer.MAX_VALUE, -1);
        while (pi.hasNext()) {
          ResourceURI uri = pi.next();

          // Load the resource
          Resource<?> resource = null;
          InputStream is = null;
          try {
            ResourceReader<?, ?> reader = serializer.getReader();
            is = openStreamToResource(uri);
            resource = reader.read(is, site);
            if (resource == null) {
              logger.warn("Unkown error loading resource {}", uri);
              continue;
            }
          } catch (Exception e) {
            logger.error("Error loading resource '{}' from bundle: {}", uri, e.getMessage());
            continue;
          } finally {
            IOUtils.closeQuietly(is);
          }

          // Add it to the index
          index.add(resource);
          revisionCount++;
          if (previousURI != null && !previousURI.getPath().equals(uri.getPath())) {
            logger.info("Adding {}:{} to site index", site, uri.getPath());
            resourceCount++;
          }
          previousURI = uri;
        }
      }

      success = true;

      if (resourceCount > 0) {
        time = System.currentTimeMillis() - time;
        logger.info("Site index populated in {} ms", ConfigurationUtils.toHumanReadableDuration(time));
        logger.info("{} resources and {} revisions added to index", resourceCount, revisionCount - resourceCount);
      }
    } catch (MalformedResourceURIException e) {
      throw new ContentRepositoryException("Error while reading resource uri for index", e);
    } finally {
      if (!success) {
        try {
          index.clear();
        } catch (IOException e) {
          logger.error("Error while trying to cleanup after failed indexing operation", e);
        }
      }
    }

    return index;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadPage()
   */
  @Override
  protected InputStream openStreamToResource(ResourceURI uri)
      throws IOException {
    String uriPath = uri.getPath();

    // This repository is path based, so let's make sure we have a path
    // or get one, if that's not the case.
    if (uriPath == null) {
      uriPath = index.getPath(uri);
      if (uriPath == null)
        return null;
    }

    String entryPath = UrlUtils.concat(uriPath, ResourceUtils.getDocument(uri.getVersion()));
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    try {
      return url.openStream();
    } catch (IOException e) {
      throw new IOException("I/O error while reading page '" + uri + "'", e);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResourceContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.language.Language)
   */
  @Override
  protected InputStream openStreamToResourceContent(ResourceURI uri,
      Language language) throws IOException {
    String uriPath = uri.getPath();

    // This repository is path based, so let's make sure we have a path
    // or get one, if that's not the case.
    if (uriPath == null) {
      uriPath = index.getPath(uri);
      if (uriPath == null)
        return null;
    }

    String entryPath = UrlUtils.concat(uriPath, ResourceUtils.getDocument(uri.getVersion()));
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    try {
      return url.openStream();
    } catch (IOException e) {
      throw new IOException("I/O error while reading page '" + uri + "'", e);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (bundle != null)
      return bundle.getSymbolicName().hashCode();
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
    if (obj instanceof BundleContentRepositoryImpl) {
      BundleContentRepositoryImpl repo = (BundleContentRepositoryImpl) obj;
      if (bundle != null) {
        return bundle.equals(repo.getBundle());
      } else {
        return super.equals(obj);
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return bundle.toString();
  }

}
