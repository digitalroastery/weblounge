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

package ch.entwine.weblounge.contentrepository.impl.fs;

import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.IndexOperation;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.operation.IndexOperationImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
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
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

/**
 * Implementation of a content repository that lives on a filesystem.
 */
public class FileSystemContentRepository extends AbstractWritableContentRepository implements ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileSystemContentRepository.class);

  /** The repository type */
  public static final String TYPE = "ch.entwine.weblounge.contentrepository.filesystem";

  /** Prefix for repository configuration keys */
  private static final String CONF_PREFIX = "contentrepository.fs.";

  /** Configuration key for the repository's root directory */
  public static final String OPT_ROOT_DIR = CONF_PREFIX + "root";

  /** Name of the system property containing the root directory */
  public static final String PROP_ROOT_DIR = "weblounge.sitesdatadir";

  /** Default directory root directory name */
  public static final String ROOT_DIR_DEFAULT = "sites-data";

  /** Name of the index path element right below the repository root */
  public static final String INDEX_PATH = "index";

  /** The repository storage root directory */
  protected File repositoryRoot = null;

  /** The repository root directory */
  protected File repositorySiteRoot = null;

  /** The root directory for the temporary bundle index */
  protected File idxRootDir = null;

  /** Flag to indicate off-site indexing */
  protected boolean indexingOffsite = false;

  /** The document builder factory */
  protected final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

  /** The xml transformer factory */
  protected final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /**
   * Creates a new instance of the file system content repository.
   */
  public FileSystemContentRepository() {
    super(TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {

    // Detect the filesystem root directory
    String fsRootDir = null;
    if (StringUtils.isNotBlank(System.getProperty(PROP_ROOT_DIR)))
      fsRootDir = System.getProperty(PROP_ROOT_DIR);
    else if (properties != null && StringUtils.isNotBlank((String) properties.get(OPT_ROOT_DIR)))
      fsRootDir = (String) properties.get(OPT_ROOT_DIR);
    else
      fsRootDir = PathUtils.concat(System.getProperty("java.io.tmpdir"), ROOT_DIR_DEFAULT);

    repositoryRoot = new File(fsRootDir);
    if (site != null)
      repositorySiteRoot = new File(repositoryRoot, site.getIdentifier());
    logger.debug("Content repository storage root is located at {}", repositoryRoot);

    // Make sure we can create a temporary index
    try {
      FileUtils.forceMkdir(repositoryRoot);
    } catch (IOException e) {
      throw new ConfigurationException(OPT_ROOT_DIR, "Unable to create repository storage at " + repositoryRoot, e);
    }

    logger.debug("Content repository configured");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {
    repositorySiteRoot = new File(repositoryRoot, site.getIdentifier());
    logger.debug("Content repository root is located at {}", repositorySiteRoot);

    // Make sure we can create a temporary index
    idxRootDir = new File(repositorySiteRoot, INDEX_PATH);
    try {
      FileUtils.forceMkdir(idxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Unable to create site index at " + idxRootDir, e);
    }

    // Tell the super implementation
    super.connect(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {

    if (indexing || indexingOffsite) {
      logger.warn("Ignoring additional index request for {}", this);
      return;
    }

    readOnly = true;
    logger.info("Switching site '{}' to read only mode", site);

    String newIdxRootDirName = idxRootDir.getName() + "-new-" + UUID.randomUUID().toString();
    File newIdxRootDir = new File(idxRootDir.getParentFile(), newIdxRootDirName);
    FileSystemContentRepositoryIndex newIndex = null;

    // Create the new index
    try {
      logger.info("Creating new index at {}", newIdxRootDir);
      FileUtils.forceMkdir(newIdxRootDir);
      newIndex = new FileSystemContentRepositoryIndex(newIdxRootDir);
      indexingOffsite = true;
      rebuildIndex(newIndex);
    } catch (IOException e) {
      indexingOffsite = false;
      try {
        FileUtils.forceDelete(newIdxRootDir);
      } catch (IOException e1) {
        logger.error("Error removing incomplete new index at {}: {}", newIdxRootDir, e.getMessage());
      }
      throw new ContentRepositoryException("Error creating index " + site.getIdentifier(), e);
    } finally {
      try {
        if (newIndex != null)
          newIndex.close();
      } catch (IOException e) {
        throw new ContentRepositoryException("Error closing new index " + site.getIdentifier(), e);
      }
    }

    String oldIdxRootDirName = idxRootDir.getName() + "-old-" + UUID.randomUUID().toString();
    File oldIdxRootDir = new File(idxRootDir.getParentFile(), oldIdxRootDirName);

    try {
      indexing = true;
      index.close();
      logger.info("Moving new index to place {}", idxRootDir);
      FileUtils.moveDirectory(idxRootDir, oldIdxRootDir);
      FileUtils.moveDirectory(newIdxRootDir, idxRootDir);
      index = new FileSystemContentRepositoryIndex(idxRootDir);
      logger.info("Removing old index at {}", oldIdxRootDir);
      FileUtils.forceDelete(oldIdxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Error clearing index " + site.getIdentifier(), e);
    } finally {
      indexing = false;
      indexingOffsite = false;
      logger.info("Switching site '{}' back to write mode", site);
      readOnly = false;
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#indexAsynchronously()
   */
  public IndexOperation indexAsynchronously() throws ContentRepositoryException {
    IndexOperation op = new IndexOperationImpl();
    processor.enqueue(op);
    return op;
  }

  /**
   * Creates a new content repository index at the given location as specified
   * by <code>idx</code>.
   * 
   * @param idx
   *          the index
   * @throws ContentRepositoryException
   *           if indexing fails
   */
  private void buildIndex(ContentRepositoryIndex idx)
      throws ContentRepositoryException {
    boolean oldReadOnly = readOnly;
    readOnly = true;
    indexing = true;

    if (!oldReadOnly)
      logger.info("Switching site '{}' to read only mode", site);

    rebuildIndex(idx);

    indexing = false;
    if (!oldReadOnly)
      logger.info("Switching site '{}' back to write mode", site);
    readOnly = oldReadOnly;
  }

  /**
   * Creates a new content repository index at the given location as specified
   * by <code>idx</code>.
   * 
   * @param idx
   *          the index
   * @throws ContentRepositoryException
   *           if indexing fails
   */
  private void rebuildIndex(ContentRepositoryIndex idx)
      throws ContentRepositoryException {
    boolean success = true;

    try {
      // Clear the current index, which might be null if the site has not been
      // started yet.
      if (idx == null)
        idx = loadIndex(idxRootDir);

      logger.info("Creating site index '{}'...", site);
      long time = System.currentTimeMillis();
      long resourceCount = 0;

      // Index each and every known resource type
      Set<ResourceSerializer<?, ?>> serializers = ResourceSerializerFactory.getSerializers();
      if (serializers == null) {
        logger.warn("Unable to index {} while no resource serializers are registered", this);
        return;
      }
      for (ResourceSerializer<?, ?> serializer : serializers) {
        long added = index(idx, serializer.getType());
        if (added > 0)
          logger.info("Added {} {}s to index", added, serializer.getType().toLowerCase());
        resourceCount += added;
      }

      if (resourceCount > 0) {
        time = System.currentTimeMillis() - time;
        logger.info("Site index populated in {} ms", ConfigurationUtils.toHumanReadableDuration(time));
        logger.info("{} resources added to index", resourceCount);
      }
    } catch (IOException e) {
      success = false;
      throw new ContentRepositoryException("Error while writing to index", e);
    } catch (MalformedResourceURIException e) {
      success = false;
      throw new ContentRepositoryException("Error while reading resource uri for index", e);
    } finally {
      if (!success) {
        try {
          idx.clear();
        } catch (IOException e) {
          logger.error("Error while trying to cleanup after failed indexing operation", e);
        }
      }
    }
  }

  /**
   * This method indexes a certain type of resources and expects the resources
   * to be located in a subdirectory of the site directory named
   * <tt>&lt;resourceType&gt;s<tt>.
   * 
   * @param idx
   *          the content repository index
   * @param resourceType
   *          the resource type
   * @return the number of resources that were indexed
   * @throws IOException
   *           if accessing a file fails
   */
  protected long index(ContentRepositoryIndex idx, String resourceType)
      throws IOException {
    // Temporary path for rebuilt site
    String resourceDirectory = resourceType + "s";
    String homePath = UrlUtils.concat(repositorySiteRoot.getAbsolutePath(), resourceDirectory);
    File resourcesRootDirectory = new File(homePath);
    FileUtils.forceMkdir(resourcesRootDirectory);
    if (resourcesRootDirectory.list().length == 0) {
      logger.debug("No {}s found to index", resourceType);
      return 0;
    }

    logger.info("Populating site index '{}' with {}s...", site, resourceType);

    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
      return 0;
    }

    // Clear previews directory
    logger.info("Removing cached preview images");
    File previewsDir = new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images"));
    FileUtils.deleteQuietly(previewsDir);

    File restructuredResources = new File(repositorySiteRoot, "." + resourceDirectory);
    long resourceCount = 0;
    long resourceVersionCount = 0;
    boolean restructured = false;

    try {
      Stack<File> uris = new Stack<File>();
      uris.push(resourcesRootDirectory);
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
        for (File f : files) {
          if (f.isDirectory()) {
            uris.push(f);
          } else {
            try {
              Resource<?> resource = null;
              ResourceURI uri = null;
              ResourceReader<?, ?> reader = serializer.getReader();
              InputStream is = null;
              boolean foundResource = false;

              // Read the resource
              try {
                is = new FileInputStream(f);
                resource = reader.read(is, site);
                if (resource == null) {
                  logger.warn("Unkown error loading {}", f);
                  continue;
                }
                uri = resource.getURI();
              } catch (Throwable t) {
                logger.error("Error loading {}: {}", f, t.getMessage());
                continue;
              } finally {
                IOUtils.closeQuietly(is);
              }

              idx.add(resource);
              resourceVersionCount++;

              // Make sure the resource is in the correct place
              File expectedFile = uriToFile(uri);
              String tempPath = expectedFile.getAbsolutePath().substring(homePath.length());
              File indexedFile = new File(restructuredResources, tempPath);
              FileUtils.copyFile(f, indexedFile);
              if (!f.equals(expectedFile)) {
                restructured = true;
              }

              // See if there are files that need to be copied as well. We add
              // the "found" flag in case that there are multiple versions of
              // this resource (we don't want to copy the files more than once).
              if (!foundResource) {
                for (ResourceContent c : resource.contents()) {
                  if (c.getFilename() == null)
                    throw new IllegalStateException("Found content without filename in " + resource);
                  Language l = c.getLanguage();
                  String filename = l.getIdentifier() + "." + FilenameUtils.getExtension(c.getFilename());
                  String srcFile = PathUtils.concat(f.getParentFile().getAbsolutePath(), filename);
                  File sourceFile = new File(srcFile);
                  if (!sourceFile.exists()) {
                    if (c.getExternalLocation() == null)
                      logger.warn("Found file resource {} with missing content!", resource.getIdentifier());
                    continue;
                  }
                  String destFile = PathUtils.concat(indexedFile.getParentFile().getAbsolutePath(), filename);
                  FileUtils.copyFile(new File(srcFile), new File(destFile));
                }
                logger.info("Indexing {} {}", resourceType, uri.toString());
                resourceCount++;
                foundResource = true;
              }

            } catch (Throwable t) {
              logger.error("Error indexing {} {}: {}", new Object[] {
                  resourceType,
                  f,
                  t.getMessage() });
            }
          }
        }
      }

      // Move restructured resources in place
      if (restructured) {
        String oldResourcesDirectory = resourceDirectory + "-old";
        File movedOldResources = new File(repositorySiteRoot, oldResourcesDirectory);
        if (movedOldResources.exists()) {
          for (int i = 1; i < Integer.MAX_VALUE; i++) {
            movedOldResources = new File(repositorySiteRoot, oldResourcesDirectory + " " + i);
            if (!movedOldResources.exists())
              break;
          }
        }
        FileUtils.moveDirectory(resourcesRootDirectory, movedOldResources);
        FileUtils.moveDirectory(restructuredResources, resourcesRootDirectory);
      }

      // Log the work
      if (resourceCount > 0) {
        logger.info("{} {}s and {} revisions added to index", new Object[] {
            resourceCount,
            resourceType,
            resourceVersionCount - resourceCount });
      }

    } finally {
      if (restructuredResources.exists()) {
        FileUtils.deleteQuietly(restructuredResources);
      }
    }

    return resourceCount;
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
    return repositorySiteRoot;
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
    StringBuffer path = new StringBuffer(repositorySiteRoot.getAbsolutePath());
    if (uri.getType() == null)
      throw new IllegalArgumentException("Resource uri has no type");
    path.append("/").append(uri.getType()).append("s");
    String id = null;
    if (uri.getIdentifier() != null) {
      id = uri.getIdentifier();
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

    // Build the path
    path = appendIdToPath(id, path);
    path.append(File.separatorChar);
    path.append(uri.getVersion());
    path.append(File.separatorChar);

    // Add the document name
    path.append(ResourceUtils.getDocument(Resource.LIVE));
    return new File(path.toString());
  }

  /**
   * Returns the <code>File</code> object that is represented by
   * <code>uri</code> and <code>content</code> or <code>null</code> if the
   * resource or the resource content does not exist on the filesystem.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @return the content file
   * @throws IOException
   *           if the file cannot be accessed not exist
   */
  protected File uriToContentFile(ResourceURI uri, ResourceContent content)
      throws IOException {
    File resourceDirectory = uriToDirectory(uri);
    File resourceRevisionDirectory = new File(resourceDirectory, Long.toString(uri.getVersion()));

    // Construct the filename
    String fileName = content.getLanguage().getIdentifier();
    String fileExtension = FilenameUtils.getExtension(content.getFilename());
    if (!"".equals(fileExtension)) {
      fileName += "." + fileExtension;
    }
    File contentFile = new File(resourceRevisionDirectory, fileName);
    return contentFile;
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
    StringBuffer path = new StringBuffer(repositorySiteRoot.getAbsolutePath());
    if (uri.getType() == null)
      throw new IllegalArgumentException("Resource uri has no type");
    path.append("/").append(uri.getType()).append("s");
    String id = null;
    if (uri.getIdentifier() != null) {
      id = uri.getIdentifier();
    } else {
      id = index.getIdentifier(uri);
      if (id == null) {
        logger.warn("Uri '{}' is not part of the repository index", uri);
        return null;
      }
      uri.setIdentifier(id);
    }
    path = appendIdToPath(uri.getIdentifier(), path);
    return new File(path.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (repositorySiteRoot != null)
      return repositorySiteRoot.hashCode();
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
      if (repositorySiteRoot != null) {
        return repositorySiteRoot.equals(repo.getRootDirectory());
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
    return repositorySiteRoot.getAbsolutePath();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadResource()
   */
  @Override
  protected InputStream openStreamToResource(ResourceURI uri)
      throws IOException {
    if (uri.getType() == null) {
      uri.setType(index.getType(uri));
    }
    File resourceFile = uriToFile(uri);
    if (resourceFile == null || !resourceFile.isFile())
      return null;
    return new FileInputStream(resourceFile);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  @Override
  protected InputStream openStreamToResourceContent(ResourceURI uri,
      Language language) throws IOException {
    File resourceFile = uriToFile(uri);
    if (resourceFile == null)
      return null;

    // Look for the localized file
    File resourceDirectory = resourceFile.getParentFile();
    final String filenamePrefix = language.getIdentifier() + ".";
    File[] localizedFiles = resourceDirectory.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isFile() && f.getName().startsWith(filenamePrefix);
      }
    });

    // Make sure everything looks consistent
    if (localizedFiles.length == 0)
      return null;
    else if (localizedFiles.length > 1)
      logger.warn("Inconsistencies found in resource {} content {}", language, uri);

    // Finally return the content
    return new FileInputStream(localizedFiles[0]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResource(ch.entwine.weblounge.common.content.ResourceURI,
   *      long[])
   */
  @Override
  protected void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException {

    // Remove the resources
    File resourceDir = uriToDirectory(uri);
    for (long r : revisions) {
      File f = new File(resourceDir, Long.toString(r));
      if (f.exists()) {
        try {
          FileUtils.deleteDirectory(f);
        } catch (IOException e) {
          throw new IOException("Unable to delete revision " + r + " of resource " + uri + " located at " + f + " from repository");
        }
      }
    }

    // Remove the resource directory itself if there are no more resources
    try {
      File f = resourceDir;
      while (!uri.getType().equals(f.getName()) && f.listFiles().length == 0) {
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
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.entwine.weblounge.common.content.resource.Resource)
   */
  @Override
  protected <T extends ResourceContent, R extends Resource<T>> R storeResource(
      R resource) throws IOException {
    File resourceUrl = uriToFile(resource.getURI());
    InputStream is = null;
    OutputStream os = null;
    try {
      FileUtils.forceMkdir(resourceUrl.getParentFile());
      if (!resourceUrl.exists())
        resourceUrl.createNewFile();
      is = new ByteArrayInputStream(resource.toXml().getBytes("utf-8"));
      os = new FileOutputStream(resourceUrl);
      IOUtils.copy(is, os);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
    }
    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  @Override
  protected <T extends ResourceContent> T storeResourceContent(ResourceURI uri,
      T content, InputStream is) throws IOException {

    if (is == null)
      return content;

    File contentFile = uriToContentFile(uri, content);
    OutputStream os = null;
    try {
      FileUtils.forceMkdir(contentFile.getParentFile());
      if (!contentFile.exists())
        contentFile.createNewFile();
      os = new FileOutputStream(contentFile);
      IOUtils.copyLarge(is, os);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
    }

    // Set the size
    content.setSize(contentFile.length());

    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  protected <T extends ResourceContent> void deleteResourceContent(
      ResourceURI uri, T content) throws IOException {
    File contentFile = uriToContentFile(uri, content);
    if (contentFile == null)
      throw new IOException("Resource content " + contentFile + " does not exist");
    FileUtils.deleteQuietly(contentFile);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException {
    logger.debug("Trying to load site index from {}", idxRootDir);
    return loadIndex(idxRootDir);
  }

  /**
   * Loads the index from a given directory on the filesystem.
   * 
   * @param idxRoot
   *          the root directory
   * @return the content repository
   * @throws IOException
   *           if reading from the filesystem fails
   * @throws ContentRepositoryException
   *           if creating the content repository index fails
   */
  protected ContentRepositoryIndex loadIndex(File idxRoot) throws IOException,
      ContentRepositoryException {

    ContentRepositoryIndex idx = null;

    logger.debug("Trying to load site index from {}", idxRoot);

    // Is this a new index?
    boolean created = !idxRoot.exists() || idxRoot.list().length == 0;
    FileUtils.forceMkdir(idxRoot);

    // Add content if there is any
    idx = new FileSystemContentRepositoryIndex(idxRoot);

    // Create the idx if there is nothing in place so far
    if (idx.getResourceCount() <= 0) {
      buildIndex(idx);
    }

    // Make sure the version matches the implementation
    else if (idx.getIndexVersion() < VersionedContentRepositoryIndex.INDEX_VERSION) {
      logger.info("Index needs to be updated, triggering reindex");
      buildIndex(idx);
    } else if (idx.getIndexVersion() != VersionedContentRepositoryIndex.INDEX_VERSION) {
      logger.warn("Index needs to be downgraded, triggering reindex");
      buildIndex(idx);
    }

    // Is there an existing idx?
    if (created) {
      logger.info("Created site idx at {}", idxRoot);
    } else {
      long resourceCount = idx.getResourceCount();
      long resourceVersionCount = idx.getRevisionCount();
      logger.info("Loaded site idx with {} resources and {} revisions from {}", new Object[] {
          resourceCount,
          resourceVersionCount - resourceCount,
          idxRoot });
    }

    return idx;
  }

}
