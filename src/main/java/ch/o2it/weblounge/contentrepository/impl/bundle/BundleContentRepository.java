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
import ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Content repository that reads pages and resources from the site's
 * <code>OSGi</code> bundle. Since a bundle is a read only archive, it does not
 * implement the extended <code>WritableContentRepository</code> interface.
 * <p>
 * The implementation assumes a prefix to all page and resource uris of
 * <code>/repository</code>, which means that pages are expected to live under
 * <code>/repository/pages</code> while resources are expected under
 * <code>/repository/resources</code>. You may want to change these assumptions
 * using {@link #setURI(String)}, {@link #setPagesURI(String)} or
 * {@link #setResourcesURI()}.
 */
public class BundleContentRepository extends AbstractContentRepository {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(BundleContentRepository.class);

  /** The site bundle context */
  protected Bundle bundle = null;

  /** Prefix into the bundle */
  protected String bundlePathPrefix = "/repository";

  /** Prefix into the bundle */
  protected String pagesPathPrefix = bundlePathPrefix + "/pages";

  /** Prefix into the bundle */
  protected String resourcesPathPrefix = bundlePathPrefix + "/resources";

  /** The root directory for the temporary bundle index */
  protected File idxRootDir = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#connect(ch.o2it.weblounge.common.site.Site,
   *      java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {

    super.connect(properties);

    this.bundle = (Bundle) properties.get(Bundle.class.getName());
    if (bundle == null)
      throw new ContentRepositoryException("Bundle was not found in connect properties");

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

    logger.info("Content repository connected to bundle {}", bundle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#disconnect()
   */
  @Override
  public void disconnect() throws ContentRepositoryException {
    super.disconnect();
    if (idxRootDir != null && idxRootDir.exists()) {
      logger.info("Removing temporary site index at {}", idxRootDir);
      FileUtils.deleteQuietly(idxRootDir);
    }
  }

  /**
   * Returns the bundle or <code>null</code> if no bundle has been set so far.
   * 
   * @return the bundle
   */
  public Bundle getBundle() {
    return bundle;
  }

  /**
   * {@inheritDoc}
   * 
   * TODO: Move to BundleContentRepositoryIndex
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#getVersions(ch.o2it.weblounge.common.content.PageURI)
   */
  @SuppressWarnings( { "unchecked" })
  public PageURI[] getVersions(PageURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Page uri cannot be null");
    String entryPath = UrlSupport.concat(pagesPathPrefix, uri.getPath());
    Enumeration<URL> entries = bundle.findEntries(entryPath, "*.xml", false);
    List<PageURI> versions = new ArrayList<PageURI>();
    while (entries.hasMoreElements()) {
      URL entry = entries.nextElement();
      String filename = FilenameUtils.getBaseName(entry.getPath());
      long version = PageUtils.getVersion(filename);
      if (version != -1) {
        logger.trace("Found version '{}' of page {}", entry, uri);
        versions.add(new PageURIImpl(uri, version));
      }
    }
    return versions.toArray(new PageURI[versions.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI,
   *      int, long)
   */
  @SuppressWarnings("unchecked")
  public Iterator<PageURI> listPages(PageURI uri, int level, long version)
      throws ContentRepositoryException {
    String entryPath = UrlSupport.concat(pagesPathPrefix, uri.getPath());
    int startLevel = StringUtils.countMatches(uri.getPath(), "/");
    List<PageURI> pages = new ArrayList<PageURI>();
    Enumeration<URL> entries = bundle.findEntries(entryPath, "*.xml", level > 0);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        URL entry = entries.nextElement();
        String path = FilenameUtils.getPath(entry.getPath());
        path = path.substring(pagesPathPrefix.length() - 1);
        int currentLevel = StringUtils.countMatches(uri.getPath(), "/");
        if (level > -1 && level < Integer.MAX_VALUE && currentLevel > (startLevel + level))
          continue;
        long v = PageUtils.getVersion(FilenameUtils.getBaseName(entry.getPath()));
        if (version != -1 && v != version)
          continue;
        try {
          String id = getPageId(entry);
          pages.add(new PageURIImpl(getSite(), path, v, id));
          logger.trace("Found version '{}' of page {}", entry, uri);
        } catch (IOException e) {
          throw new ContentRepositoryException("Unable to read id from page at " + entry, e);
        }
      }
    }
    return pages.iterator();
  }

  /**
   * Returns the page id or <code>null</code> if no page id could be found on
   * the specified document.
   */
  private String getPageId(URL url) throws IOException {
    BufferedInputStream is = new BufferedInputStream(url.openStream());
    InputStreamReader reader = new InputStreamReader(is);
    CharBuffer buf = CharBuffer.allocate(512);
    reader.read(buf);
    Pattern p = Pattern.compile(".*id=\"([a-z0-9-]*)\".*");
    String s = new String(buf.array());
    s = s.replace('\n', ' ');
    Matcher m = p.matcher(s);
    if (m.matches()) {
      return m.group(1);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#setURI(java.lang.String)
   */
  public void setURI(String repositoryURI) {
    super.setURI(repositoryURI);
    this.bundlePathPrefix = repositoryURI;
    this.pagesPathPrefix = UrlSupport.concat(bundlePathPrefix, "/pages");
    this.resourcesPathPrefix = UrlSupport.concat(bundlePathPrefix, "/resources");
  }

  /**
   * Sets the pages uri to the specified value.
   * <p>
   * Note that this uri must be absolute. Also note that it will be overwritten
   * by subsequent calls to <code>setURI()</code>.
   * 
   * @param pagesURI
   *          the uri to the pages section of the repository
   * @throws IllegalArgumentException
   *           if the uri is either <code>null</code> or relative instead of
   *           absolute
   */
  public void setPagesURI(String pagesURI) {
    if (pagesURI == null)
      throw new IllegalArgumentException("Pages uri cannot be null");
    if (pagesURI.startsWith("/"))
      throw new IllegalArgumentException("Pages uri Must be absolute");
    this.pagesPathPrefix = pagesURI;
  }

  /**
   * Sets the resources uri to the specified value.
   * <p>
   * Note that this uri must be absolute. Also note that it will be overwritten
   * by subsequent calls to <code>setURI()</code>.
   * 
   * @param resourcesURI
   *          the uri to the pages section of the repository
   * @throws IllegalArgumentException
   *           if the uri is either <code>null</code> or relative instead of
   *           absolute
   */
  public void setResourcesURI(String resourcesURI) {
    if (resourcesURI == null)
      throw new IllegalArgumentException("Pages uri cannot be null");
    if (resourcesURI.startsWith("/"))
      throw new IllegalArgumentException("Pages uri Must be absolute");
    this.resourcesPathPrefix = resourcesURI;
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

    try {
      logger.info("Populating temporary site index '{}'...", site);
      long time = System.currentTimeMillis();
      long pageCount = 0;
      long pageVersionCount = 0;
      PageURI previousURI = null;
      PageURI homeURI = new PageURIImpl(getSite(), "/");
      Iterator<PageURI> pi = listPages(homeURI, Integer.MAX_VALUE, -1);
      while (pi.hasNext()) {
        PageURI uri = pi.next();
        index.add(uri);
        pageVersionCount++;
        if (previousURI != null && !previousURI.getPath().equals(uri.getPath())) {
          logger.info("Indexing {}", uri.getPath());
          pageCount++;
        }
        previousURI = uri;
      }
      time = System.currentTimeMillis() - time;
      logger.info("Site index populated in {}", ConfigurationUtils.toHumanReadableDuration(time));
      logger.info("{} pages and {} revisions added to index", pageCount, pageVersionCount);
    } catch (MalformedPageURIException e) {
      throw new ContentRepositoryException("Error while reading page uri for index", e);
    }

    return index;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#loadPage()
   */
  @Override
  protected Page loadPage(PageURI uri) throws IOException {
    String uriPath = uri.getPath();

    // This repository is path based, so let's make sure we have a path
    // or get one, if that's not the case.
    if (uriPath == null) {
      uriPath = index.toPath(uri);
      if (uriPath == null)
        return null;
    }

    String entryPath = UrlSupport.concat(new String[] {
        pagesPathPrefix,
        uriPath,
        PageUtils.getDocument(uri.getVersion()) });
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    try {
      PageReader pageReader = new PageReader();
      return pageReader.read(url.openStream(), uri);
    } catch (SAXException e) {
      throw new RuntimeException("SAX error while reading page '" + uri + "'", e);
    } catch (IOException e) {
      throw new IOException("I/O error while reading page '" + uri + "'", e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Parser configuration error while reading page '" + uri + "'", e);
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
    if (obj instanceof BundleContentRepository) {
      BundleContentRepository repo = (BundleContentRepository) obj;
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
    return "bundle content repository " + bundle.toString();
  }

}
