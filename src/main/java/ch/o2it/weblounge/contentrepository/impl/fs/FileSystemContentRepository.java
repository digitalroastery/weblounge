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

import ch.o2it.weblounge.common.content.MalformedPageURIException;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.impl.content.PageReader;
import ch.o2it.weblounge.common.impl.content.PageURIImpl;
import ch.o2it.weblounge.common.impl.content.PageUtils;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Implementation of a content repository that lives on a filesystem.
 */
public class FileSystemContentRepository extends AbstractWritableContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileSystemContentRepository.class);

  /** Prefix for repository configuration keys */
  private static final String OPT_PREFIX = FileSystemContentRepository.class.getName();

  /** Configuration key for the repository's root directory */
  public static final String OPT_ROOT_DIR = OPT_PREFIX + ".directory";

  /** Configuration key for the repository uri */
  public static final String OPT_ROOT_URI = OPT_PREFIX + ".uri";

  /** The repository root directory */
  protected File filesystemRoot = null;

  /** The site root directory inside the repository */
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
    filesystemRoot = new File(fsRootDir);
    logger.debug("Content repository root is located at {}", filesystemRoot);

    // Consider the site uri when building the repository uri
    if (rootURI == null) {
      setURI(site.getIdentifier());
      repositoryRoot = new File(filesystemRoot, getURI());
    }

    // Make sure we can create a temporary index
    idxRootDir = new File(PathSupport.concat(new String[] {
        System.getProperty("java.io.tmpdir"),
        "weblounge",
        "tmp",
        "index",
        getSite().getIdentifier() }));
    try {
      FileUtils.deleteQuietly(idxRootDir);
      FileUtils.forceMkdir(idxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Unable to create temporary site index at " + idxRootDir, e);
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
    
    try {
      // Clear the current search index
      index.clear();
  
      // Recreate the index
      int repositoryRootIndex = repositoryRoot.getAbsolutePath().length();
      Stack<File> stack = new Stack<File>();
      stack.push(repositoryRoot);
      logger.info("Starting reindex of " + this);
      while (!stack.empty()) {
        File dir = stack.pop();
        for (File f : dir.listFiles()) {
          if (f.isDirectory()) {
            stack.push(f);
          } else {
            String path = f.getAbsolutePath().substring(repositoryRootIndex);
            String repositoryPath = FilenameUtils.getPath(path);
            long version = PageUtils.getVersion(path);
            PageURI pageURI = new PageURIImpl(site, repositoryPath, version);
            index.add(pageURI);
            logger.debug("Adding {} to repository index");
          }
        }
      }
    } catch (IOException e) {
      throw new ContentRepositoryException(e);
    }
    logger.info("Finished reindex of " + this);
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
   * <code>uri</code> or <code>null</code> if the page does not exist on the
   * filesystem.
   * 
   * @param uri
   *          the page uri
   * @return the file
   */
  protected File uriToFile(PageURI uri) throws IOException {
    StringBuffer path = new StringBuffer(repositoryRoot.getAbsolutePath());
    String id = null;
    if (uri.getId() != null) {
      id = uri.getId();
    } else {
      id = index.toId(uri);
      if (id == null) {
        logger.warn("Uri '{}' is not part of the repository index", uri);
        return null;
      }
    }
    path = appendIdToPath(uri.getId(), path);
    path.append(File.separatorChar);
    path.append(PageUtils.getDocument(uri.getVersion()));
    return new File(path.toString());
  }

  /**
   * Returns the page uri's parent directory or <code>null</code> if the
   * directory does not exist on the filesystem.
   * 
   * @param uri
   *          the page uri
   * @return the parent directory
   */
  protected File uriToDirectory(PageURI uri) throws IOException {
    StringBuffer path = new StringBuffer(repositoryRoot.getAbsolutePath());
    String id = null;
    if (uri.getId() != null) {
      id = uri.getId();
    } else {
      id = index.toId(uri);
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
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadPage()
   */
  @Override
  protected Page loadPage(PageURI uri) throws IOException {
    File pageFile = uriToFile(uri);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(pageFile);
      PageReader pageReader = new PageReader();
      Page page = pageReader.read(fis, uri);
      return page;
    } catch (IOException e) {
      throw new IOException("Error accessing page " + uri + " at " + pageFile, e);
    } catch (SAXException e) {
      throw new RuntimeException("Error parsing page " + uri + " at " + pageFile, e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Parser configuration error while trying to parse page " + uri + " at " + pageFile, e);
    } finally {
      if (fis != null)
        IOUtils.closeQuietly(fis);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deletePage(ch.o2it.weblounge.common.content.PageURI, long[])
   */
  @Override
  protected void deletePage(PageURI uri, long[] revisions) throws IOException {

    // Remove the pages
    for (long r : revisions) {
      PageURI pageURI = new PageURIImpl(uri, r);
      File f = uriToFile(uri);
      if (f.exists() && !f.delete()) {
        logger.warn("Tried to remove non-existing page '{}' from repository", uri);
        throw new IOException("Unable to delete page " + pageURI + " located at " + f + " from repository");
      }
    }

    // Get the page's directory
    File f = uriToDirectory(uri);
    if (!f.exists()) {
      throw new IOException("Unable to get directory for page " + uri + ", supposedly located at " + f);
    }

    // Remove the file
    try {
      if (f.listFiles().length == 0)
        FileUtils.deleteDirectory(f);
    } catch (IOException e) {
      throw new IOException("Unable to delete directory for page " + uri, e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storePage(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.content.Page)
   */
  @Override
  protected void storePage(PageURI uri, Page page) throws IOException {
    File pageUrl = uriToFile(uri);
    InputStream is = null;
    OutputStream os = null;
    try {
      FileUtils.forceDelete(pageUrl);
      is = new ByteArrayInputStream(page.toXml().getBytes());
      os = new FileOutputStream(pageUrl);
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
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#updatePage(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.content.Page)
   */
  @Override
  protected void updatePage(PageURI uri, Page page) throws IOException {
    storePage(uri, page);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException {
    
    FileSystemContentRepositoryIndex index = null;
    
    // If there is an existing index, try to load it
    if (idxRootDir.exists()) {
      logger.debug("Trying to load site index from {}", idxRootDir);
      index = new FileSystemContentRepositoryIndex(idxRootDir);
      logger.info("Loaded site index containing {} pages in {} versions", index.getPages(), index.getVersions());
      return index;
    }

    FileUtils.forceMkdir(idxRootDir);
    index = new FileSystemContentRepositoryIndex(idxRootDir);

    try {
      logger.info("Populating site index '{}'...", site);
      long time = System.currentTimeMillis();
      long pageCount = 0;
      long pageVersionCount = 0;
      
      Stack<File> uris = new Stack<File>();
      String homePath = UrlSupport.concat(repositoryRoot.getAbsolutePath(), "pages");
      uris.push(new File(PathSupport.trim(homePath)));
      while (!uris.empty()) {
        File dir = uris.pop();
        File[] files = dir.listFiles();
        boolean foundPage = false;
        for (File f : files) {
          if (f.isDirectory()) {
            uris.push(f);
          } else {
            String path = f.getAbsolutePath().substring(homePath.length());
            long version = PageUtils.getVersion(FilenameUtils.getBaseName(f.getName()));
            PageURI uri = new PageURIImpl(site, path, version);
            index.add(uri);
            pageVersionCount ++;
            if (!foundPage) {
              logger.info("Indexing {}", path);
              pageCount ++;
              foundPage = true;
            }
          }
        }
      }
      
      time = System.currentTimeMillis() - time;
      logger.info("Site index populated in {}", ConfigurationUtils.toHumanReadableDuration(time));
      logger.info("{} pages and {} revisions added to index", pageCount, pageVersionCount);
    } catch (MalformedPageURIException e) {
      throw new ContentRepositoryException("Error while reading page uri for index", e);
    }

    return index;
  }

}
