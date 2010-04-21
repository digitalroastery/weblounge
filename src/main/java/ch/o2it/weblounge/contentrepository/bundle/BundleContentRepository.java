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

package ch.o2it.weblounge.contentrepository.bundle;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.impl.content.PageReader;
import ch.o2it.weblounge.common.impl.content.PageURIImpl;
import ch.o2it.weblounge.common.impl.content.PageUtils;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
public class BundleContentRepository implements ContentRepository {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(BundleContentRepository.class);

  /** The site */
  protected Site site = null;
  
  /** The site bundle context */
  protected Bundle bundle = null;

  /** Prefix into the bundle */
  protected String bundlePathPrefix = "/repository";

  /** Prefix into the bundle */
  protected String pagesPathPrefix = bundlePathPrefix + "/pages";

  /** Prefix into the bundle */
  protected String resourcesPathPrefix = bundlePathPrefix + "/resources";

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#connect(ch.o2it.weblounge.common.site.Site, java.util.Dictionary)
   */
  public void connect(Dictionary<?, ?> properties)
      throws ContentRepositoryException {
    if (properties == null)
      throw new ContentRepositoryException("Content repository cannot be initialized without properties");
    this.site = (Site) properties.get(Site.class.getName());
    if (site == null)
      throw new ContentRepositoryException("Site was not found in connect properties");
    this.bundle = (Bundle) properties.get(Bundle.class.getName());
    if (bundle == null)
      throw new ContentRepositoryException("Bundle was not found in connect properties");
    logger.info("Content repository connected to bundle {}", bundle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#disconnect()
   */
  public void disconnect() throws ContentRepositoryException {
    logger.info("Content repository disconnected from bundle {}", bundle);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#exists(ch.o2it.weblounge.common.content.PageURI)
   */
  public boolean exists(PageURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Page uri cannot be null");
    String file = PageUtils.getDocument(uri.getVersion());
    String entryPath = UrlSupport.concat(new String[] {
      pagesPathPrefix, uri.getPath(), file 
    });
    URL url = bundle.getEntry(entryPath);
    return url != null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#exists(ch.o2it.weblounge.common.content.PageURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)
   */
  public boolean exists(PageURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException {
    // TODO: Check user and permission
    return exists(uri);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#findPages(ch.o2it.weblounge.common.content.SearchQuery)
   */
  public SearchResult[] findPages(SearchQuery query) throws ContentRepositoryException {
    // TODO: Implement a static search index for bundles
    throw new UnsupportedOperationException("Searching in bundle repositories is not supported");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#getPage(ch.o2it.weblounge.common.content.PageURI)
   */
  public Page getPage(PageURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Page uri cannot be null");
    String entryPath = UrlSupport.concat(pagesPathPrefix, uri.getPath());
    URL url = bundle.getEntry(entryPath);
    if (url == null)
      return null;
    PageReader pageReader = new PageReader();
    try {
      return pageReader.read(url.openStream(), uri);
    } catch (SAXException e) {
      throw new ContentRepositoryException("SAX error while reading page '" + uri + "'", e);
    } catch (IOException e) {
      throw new ContentRepositoryException("I/O error while reading page '" + uri + "'", e);
    } catch (ParserConfigurationException e) {
      throw new ContentRepositoryException("Parser configuration error while reading page '" + uri + "'", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#getPage(ch.o2it.weblounge.common.content.PageURI,
   *      ch.o2it.weblounge.common.user.User,
   *      ch.o2it.weblounge.common.security.Permission)
   */
  public Page getPage(PageURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException {
    // TODO: Check security
    return getPage(uri);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#getVersions(ch.o2it.weblounge.common.content.PageURI)
   */
  @SuppressWarnings({ "unchecked" })
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
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages()
   */
  public Iterator<PageURI> listPages() throws ContentRepositoryException {
    return listPages(new PageURIImpl(site, "/"), Integer.MAX_VALUE, new long[] {});
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages(long[])
   */
  public Iterator<PageURI> listPages(long[] versions)
      throws ContentRepositoryException {
    return listPages(new PageURIImpl(site, "/"), Integer.MAX_VALUE, versions);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI)
   */
  public Iterator<PageURI> listPages(PageURI uri)
      throws ContentRepositoryException {
    return listPages(uri, Integer.MAX_VALUE, new long[] {});
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, long[])
   */
  public Iterator<PageURI> listPages(PageURI uri, long[] versions)
      throws ContentRepositoryException {
    return listPages(uri, Integer.MAX_VALUE, versions);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, int)
   */
  public Iterator<PageURI> listPages(PageURI uri, int level)
      throws ContentRepositoryException {
    return listPages(uri, level, new long[] {});
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.repository.ContentRepository#listPages(ch.o2it.weblounge.common.content.PageURI, int, long[])
   */
  @SuppressWarnings("unchecked")
  public Iterator<PageURI> listPages(PageURI uri, int level, long[] versions)
      throws ContentRepositoryException {
    String entryPath = UrlSupport.concat(pagesPathPrefix, uri.getPath());
    int startLevel = StringUtils.countMatches(uri.getPath(), "/");
    List<PageURI> pages = new ArrayList<PageURI>();
    Enumeration<URL> entries = bundle.findEntries(entryPath, "*.xml", level > 0);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        URL entry = entries.nextElement();
        String path = FilenameUtils.getPath(entry.getPath());
        path = path.substring(pagesPathPrefix.length());
        int currentLevel = StringUtils.countMatches(uri.getPath(), "/");
        if (level > -1 && currentLevel > startLevel + level)
          continue;
        long version = PageUtils.getVersion(FilenameUtils.getBaseName(entry.getPath()));
        if (versions.length > 0) {
          boolean found = false;
          for (long v : versions) {
            if (v == version) {
              found = true;
              break;
            }
          }
          if (!found)
            continue;
        }
        pages.add(new PageURIImpl(site, path, version));
        logger.trace("Found version '{}' of page {}", entry, uri);
      }
    }
    return pages.iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepository#setURI(java.lang.String)
   */
  public void setURI(String repositoryURI) {
    if (repositoryURI == null)
      throw new IllegalArgumentException("Repository uri cannot be null");
    if (repositoryURI.startsWith("/"))
      throw new IllegalArgumentException("Repository uri Must be absolute");
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

}
