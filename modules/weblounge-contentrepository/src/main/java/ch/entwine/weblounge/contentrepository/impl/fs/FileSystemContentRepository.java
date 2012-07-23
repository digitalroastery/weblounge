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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Stack;

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
      logger.warn("Resource {} has no version", uri);
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
  protected InputStream loadResource(ResourceURI uri) throws IOException {
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
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  @Override
  protected InputStream loadResourceContent(ResourceURI uri, Language language)
      throws IOException {
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
  protected <C extends ResourceContent, R extends Resource<C>> R storeResource(
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
  protected <C extends ResourceContent, R extends Resource<C>> C storeResourceContent(
      ResourceURI uri, C content, InputStream is) throws IOException {

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
  @Override
  protected <C extends ResourceContent, R extends Resource<C>> void deleteResourceContent(
      ResourceURI uri, C content) throws IOException {
    File contentFile = uriToContentFile(uri, content);
    if (contentFile == null)
      throw new IOException("Resource content " + contentFile + " does not exist");
    FileUtils.deleteQuietly(contentFile);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#listResources(java.lang.String)
   */
  @Override
  protected List<ResourceURI> listResources(String resourceType)
      throws IOException {
    // Temporary path for rebuilt site
    String resourceDirectory = resourceType + "s";
    String homePath = UrlUtils.concat(repositorySiteRoot.getAbsolutePath(), resourceDirectory);
    File resourcesRootDirectory = new File(homePath);
    if (resourcesRootDirectory.list().length == 0) {
      logger.debug("No {}s found to index", resourceType);
      return Collections.EMPTY_LIST;
    }

    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
      return Collections.EMPTY_LIST;
    }

    List<ResourceURI> resourceURIs = new ArrayList<ResourceURI>();

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
            long version = Long.parseLong(f.getParentFile().getName());
            String id = f.getParentFile().getParentFile().getName();
            resourceURIs.add(new ResourceURIImpl(resourceType, getSite(), null, id, version));
          }
        }
      }
    } catch (Throwable t) {
      logger.error("Error reading available uris from file system: {}", t.getMessage());
      throw new IOException(t);
    }

    return resourceURIs;
  }
}
