/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.page.MalformedPageURIException;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * Implementation of the {@link PageURI} interface.
 */
public class PageURIImpl implements PageURI {

  /** The page identifier */
  String id = null;

  /** The associated site */
  Site site = null;

  /** The path */
  String path = null;

  /** The page */
  long version = Page.LIVE;

  /** The link */
  private transient WebUrlImpl link_ = null;

  PageURIImpl(Site site) {
    this.site = site;
  }

  /**
   * Creates a new {@link PageURI} from the given request, which is used to
   * determine <code>site</code>, <code>path</code> and <code>version</code>.
   * 
   * @param request
   *          the request
   */
  public PageURIImpl(WebloungeRequest request) {
    this(request.getSite(), request.getUrl().getPath(), request.getVersion());
  }

  /**
   * Creates a new {@link PageURI} pointing to the live version of the page
   * identified by <code>site</code> and <code>path</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   */
  public PageURIImpl(Site site, String path) throws MalformedPageURIException {
    this(site, path, Page.LIVE, null);
  }

  /**
   * Creates a new {@link PageURI} pointing to a specific version of the page
   * identified by <code>site</code>, <code>path</code> and <code>version</code>
   * .
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param version
   *          the version
   */
  public PageURIImpl(Site site, String path, long version)
      throws MalformedPageURIException {
    this(site, path, version, null);
  }

  /**
   * Creates a new {@link PageURI} pointing to a specific version of the page
   * identified by <code>id<code>, <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param version
   *          the version
   * @param id
   *          the page identifier
   */
  public PageURIImpl(Site site, String path, long version, String id)
      throws MalformedPageURIException {
    if (path == null)
      throw new IllegalArgumentException("Argument 'path' must not be null");
    if (site == null)
      throw new IllegalArgumentException("Argument 'site' must not be null");
    if (!path.startsWith("/"))
      throw new MalformedPageURIException("Path can't be relative: " + path);
    this.site = site;
    this.id = id;
    this.path = path;
    this.version = version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getId()
   */
  public String getId() throws MalformedPageURIException {
    return id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getLink()
   */
  public WebUrl getLink() throws MalformedPageURIException {
    if (link_ == null) {
      link_ = new WebUrlImpl(site, path);
    }
    return link_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getParentURI()
   */
  public PageURI getParentURI() throws MalformedPageURIException {
    if ("/".equals(path))
      return null;
    String p = path;
    if (p.endsWith("/"))
      p = path.substring(0, path.length() - 1);
    int lastSeparator = path.lastIndexOf('/');
    p = path.substring(0, lastSeparator);
    return new PageURIImpl(site, p, version, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getPath()
   */
  public String getPath() throws MalformedPageURIException {
    return path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getSite()
   */
  public Site getSite() throws MalformedPageURIException {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getVersion()
   */
  public long getVersion() throws MalformedPageURIException {
    return version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.PageURI#getVersion(long)
   */
  public PageURI getVersion(long version) throws MalformedPageURIException {
    return new PageURIImpl(site, path, version);
  }

}