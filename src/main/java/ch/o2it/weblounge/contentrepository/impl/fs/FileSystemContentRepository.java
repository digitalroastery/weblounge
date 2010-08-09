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

package ch.o2it.weblounge.contentrepository.impl.fs;

import ch.o2it.weblounge.common.content.MalformedResourceURIException;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceReader;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ResourceSerializer;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.o2it.weblounge.contentrepository.impl.ContentRepositoryServiceImpl;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

/**
 * Implementation of a content repository that lives on a filesystem.
 */
public class FileSystemContentRepository extends AbstractWritableContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileSystemContentRepository.class);

  /** Prefix for repository configuration keys */
  private static final String CONF_PREFIX = ContentRepositoryServiceImpl.OPT_PREFIX + ".fs.";

  /** Configuration key for the repository's root directory */
  public static final String OPT_ROOT_DIR = CONF_PREFIX + "root";

  /** Name of the index path element right below the repository root */
  public static final String INDEX_PATH = "index";

  /** Name of the repository path element right below the repository root */
  public static final String REPOSITORY_PATH = "repository";

  /** The repository root directory */
  protected File repositoryRoot = null;

  /** The root directory for the temporary bundle index */
  protected File idxRootDir = null;

  /** The document builder factory */
  protected final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

  /** The xml transformer factory */
  protected final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepository#connect(java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {

    // Call the super implementation
    super.connect(properties);

    // Detect the filesystem root directory
    String fsRootDir = (String) properties.get(OPT_ROOT_DIR);
    if (fsRootDir == null) {
      fsRootDir = UrlSupport.concat(new String[] {
          System.getProperty("java.io.tmpdir"),
          "weblounge",
          "repository" });
    }
    repositoryRoot = new File(fsRootDir, site.getIdentifier());
    logger.debug("Content repository root is located at {}", repositoryRoot);

    // Make sure we can create a temporary index
    idxRootDir = new File(repositoryRoot, INDEX_PATH);
    try {
      FileUtils.forceMkdir(idxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Unable to create site index at " + idxRootDir, e);
    }

    logger.info("Content repository for site '{}' connected at {}", site, repositoryRoot);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {
    if (!connected)
      throw new IllegalStateException("Repository is not connected");

    // Temporary path for rebuilt site
    File restructuredSite = new File(repositoryRoot, ".resources");
    boolean success = false;

    try {
      // Clear the current index
      index.clear();

      logger.info("Populating site index '{}'...", site);
      long time = System.currentTimeMillis();
      long resourceCount = 0;
      long resourceVersionCount = 0;

      boolean restructured = false;

      Stack<File> uris = new Stack<File>();
      String homePath = UrlSupport.concat(repositoryRoot.getAbsolutePath(), REPOSITORY_PATH);
      File siteRootDirectory = new File(homePath);
      FileUtils.forceMkdir(siteRootDirectory);

      Map<String, ResourceSerializer<?>> serializers = new HashMap<String, ResourceSerializer<?>>();

      uris.push(siteRootDirectory);
      while (!uris.empty()) {
        File dir = uris.pop();
        File[] files = dir.listFiles(new FileFilter() {
          public boolean accept(File path) {
            if (path.getName().startsWith("."))
              return false;
            return path.isDirectory() || path.getName().endsWith(".xml");
          }
        });
        if (files == null || files.length == 0)
          continue;
        boolean foundResource = false;
        for (File f : files) {
          if (f.isDirectory()) {
            uris.push(f);
          } else {
            try {
              ResourceURI uri = loadResourceURI(site, f.toURI().toURL());
              if (uri == null) {
                logger.warn("Skipping resource {}: unable to extract uri", f);
                continue;
              }

              // Look for a suitable resource reader
              String resourceType = uri.getType();
              ResourceSerializer<?> serializer = serializers.get(resourceType);
              if (serializer == null) {
                serializer = ResourceSerializerFactory.getSerializer(resourceType);
                if (serializer == null) {
                  logger.warn("Skipping resource {}: no resource serializer found for type '{}'", uri, resourceType);
                  continue;
                } else {
                  serializers.put(resourceType, serializer);
                }
              }

              // Load the resource
              Resource resource = null;
              ResourceReader<?> reader = serializer.getReader();
              try {
                InputStream is = loadResource(uri);
                resource = reader.read(uri, is);
                if (resource == null) {
                  logger.warn("Unkown error loading {}", uriToFile(uri));
                  continue;
                }
              } catch (Exception e) {
                logger.error("Error loading {}: {}", uriToFile(uri), e.getMessage());
                continue;
              }

              index.add(resource);
              resourceVersionCount++;
              if (!foundResource) {
                logger.info("Adding {} to site index", uri);
                resourceCount++;
                foundResource = true;
              }

              // Make sure the resource is in the correct place
              File expectedFile = uriToFile(uri);
              String tempPath = expectedFile.getAbsolutePath().substring(homePath.length());
              FileUtils.copyFile(f, new File(restructuredSite, tempPath));
              if (!f.equals(expectedFile)) {
                restructured = true;
              }
            } catch (Throwable t) {
              logger.error("Error indexing resource {}: {}", f, t.getMessage());
            }
          }
        }
      }

      // Move new site in place
      if (restructured) {
        File movedOldSite = new File(repositoryRoot, "resources-old");
        if (movedOldSite.exists()) {
          for (int i = 1; i < Integer.MAX_VALUE; i++) {
            movedOldSite = new File(repositoryRoot, "resources-old " + i);
            if (!movedOldSite.exists())
              break;
          }
        }
        FileUtils.moveDirectory(siteRootDirectory, movedOldSite);
        FileUtils.moveDirectory(restructuredSite, siteRootDirectory);
      }

      success = true;

      if (resourceCount > 0) {
        time = System.currentTimeMillis() - time;
        logger.info("Site index populated in {} ms", ConfigurationUtils.toHumanReadableDuration(time));
        logger.info("{} resources and {} revisions added to index", resourceCount, resourceVersionCount - resourceCount);
      }
    } catch (IOException e) {
      throw new ContentRepositoryException("Error while writing to index", e);
    } catch (MalformedResourceURIException e) {
      throw new ContentRepositoryException("Error while reading resource uri for index", e);
    } finally {
      if (!success)
        try {
          index.clear();
        } catch (IOException e) {
          logger.error("Error while trying to cleanup after failed indexing operation", e);
        }
      if (restructuredSite.exists())
        FileUtils.deleteQuietly(restructuredSite);
    }
  }

  /**
   * Returns the root directory for this repository.
   * <p>
   * The root is either equal to the repository's filesystem root or, in case
   * this repository hosts multiple sites, to the filesystem root + a uri.
   * 
   * @return the repository root directory
   */
  public File getRootDirectory() {
    return repositoryRoot;
  }

  /**
   * Returns the <code>File</code> object that is represented by
   * <code>uri</code> or <code>null</code> if the resource does not exist on the
   * filesystem.
   * 
   * @param uri
   *          the resource uri
   * @return the file
   */
  protected File uriToFile(ResourceURI uri) throws IOException {
    StringBuffer path = new StringBuffer(repositoryRoot.getAbsolutePath());
    path.append("/").append(REPOSITORY_PATH);
    String id = null;
    if (uri.getId() != null) {
      id = uri.getId();
    } else {
      id = index.getIdentifier(uri);
      if (id == null) {
        logger.debug("Uri '{}' is not part of the repository index", uri);
        return null;
      }
    }
    if (uri.getVersion() < 0) {
      logger.warn("Resource {} has no version");
    }
    path = appendIdToPath(id, path);
    path.append(File.separatorChar);
    path.append(ResourceUtils.getDocument(Math.max(uri.getVersion(), Resource.LIVE)));
    return new File(path.toString());
  }

  /**
   * Returns the resource uri's parent directory or <code>null</code> if the
   * directory does not exist on the filesystem.
   * 
   * @param uri
   *          the resource uri
   * @return the parent directory
   */
  protected File uriToDirectory(ResourceURI uri) throws IOException {
    StringBuffer path = new StringBuffer(repositoryRoot.getAbsolutePath());
    path.append("/").append(REPOSITORY_PATH);
    String id = null;
    if (uri.getId() != null) {
      id = uri.getId();
    } else {
      id = index.getIdentifier(uri);
      if (id == null) {
        logger.warn("Uri '{}' is not part of the repository index", uri);
        return null;
      }
    }
    path = appendIdToPath(uri.getId(), path);
    return new File(path.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (repositoryRoot != null)
      return repositoryRoot.hashCode();
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
    if (obj instanceof FileSystemContentRepository) {
      FileSystemContentRepository repo = (FileSystemContentRepository) obj;
      if (repositoryRoot != null) {
        return repositoryRoot.equals(repo.getRootDirectory());
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
    return "filesystem content repository " + repositoryRoot;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadResource()
   */
  @Override
  protected InputStream loadResource(ResourceURI uri) throws IOException {
    File resourceFile = uriToFile(uri);
    if (resourceFile == null || !resourceFile.isFile())
      return null;
    return new FileInputStream(resourceFile);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadResourceContent(ch.o2it.weblounge.common.content.ResourceURI,
   *      ch.o2it.weblounge.common.language.Language)
   */
  @Override
  protected InputStream loadResourceContent(ResourceURI uri, Language language)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResource(ch.o2it.weblounge.common.content.ResourceURI,
   *      long[])
   */
  @Override
  protected void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException {

    // Remove the resources
    for (long r : revisions) {
      ResourceURI resourceURI = new ResourceURIImpl(uri, r);
      File f = uriToFile(uri);
      if (f.exists() && !f.delete()) {
        logger.warn("Tried to remove non-existing resource '{}' from repository", uri);
        throw new IOException("Unable to delete resource " + resourceURI + " located at " + f + " from repository");
      }
    }

    // Get the resource's directory
    File f = uriToDirectory(uri);
    if (!f.exists()) {
      throw new IOException("Unable to get directory for resource " + uri + ", supposedly located at " + f);
    }

    // Remove the enclosing directories
    try {
      while (!REPOSITORY_PATH.equals(f.getName()) && f.listFiles().length == 0) {
        FileUtils.deleteDirectory(f);
        f = f.getParentFile();
      }
    } catch (IOException e) {
      throw new IOException("Unable to delete directory for resource " + uri, e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.o2it.weblounge.common.content.resource.Resource)
   */
  @Override
  protected void storeResource(Resource resource) throws IOException {
    File resourceUrl = uriToFile(resource.getURI());
    InputStream is = null;
    OutputStream os = null;
    try {
      FileUtils.forceMkdir(resourceUrl.getParentFile());
      if (!resourceUrl.exists())
        resourceUrl.createNewFile();
      is = new ByteArrayInputStream(resource.toXml().getBytes("UTF-8"));
      os = new FileOutputStream(resourceUrl);
      IOUtils.copy(is, os);
    } finally {
      if (is != null)
        IOUtils.closeQuietly(is);
      if (os != null)
        IOUtils.closeQuietly(os);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResourceContent(ch.o2it.weblounge.common.content.Resource,
   *      java.io.InputStream)
   */
  @Override
  protected void storeResourceContent(Resource resource, InputStream is)
      throws IOException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException {

    logger.debug("Trying to load site index from {}", idxRootDir);
    FileUtils.forceMkdir(idxRootDir);

    index = new FileSystemContentRepositoryIndex(idxRootDir);

    // Create the index if there is nothing in place so far
    if (index.getResourceCount() <= 0) {
      index();
    }

    // Make sure the version matches the implementation
    else if (index.getIndexVersion() != VersionedContentRepositoryIndex.IDX_VERSION) {
      logger.warn("Index version does not match implementation, triggering reindex");
      index();
    } 

    // Is there an existing index?
    long resourceCount = index.getResourceCount();
    long resourceVersionCount = index.getVersions();
    logger.info("Loaded existing site index from {}", idxRootDir);
    logger.info("Index contains {} resources and {} revisions", resourceCount, resourceVersionCount - resourceCount);

    return index;
  }

}
