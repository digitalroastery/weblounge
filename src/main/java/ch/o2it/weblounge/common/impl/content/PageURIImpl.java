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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.MalformedPageURIException;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.impl.url.UrlImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * Implementation of the {@link PageURI} interface.
 */
public class PageURIImpl extends UrlImpl implements PageURI {

  /** Serial version UID */
  private static final long serialVersionUID = -686750395794924219L;

  /** The page identifier */
  String id = null;

  /** The associated site */
  Site site = null;

  /** The page */
  long version = Page.LIVE;

  /**
   * Constructor for a URI pointing to the live version of the root document.
   * 
   * @param site
   *          the associated site
   */
  public PageURIImpl(Site site) {
    this(site, "/", Page.LIVE, null);
  }

  /**
   * Creates a new {@link PageURI} from the given request, which is used to
   * determine <code>site</code>, <code>path</code> and <code>version</code>.
   * 
   * @param request
   *          the request
   */
  public PageURIImpl(WebloungeRequest request) {
    this(request.getSite(), request.getUrl().getPath(), request.getVersion(), null);
  }

  /**
   * Creates a new {@link PageURI} from the given url, which is used to
   * determine <code>site</code> and <code>path</code>. The uri will default to
   * the live version.
   * 
   * @param url
   *          the url
   */
  public PageURIImpl(WebUrl url) {
    this(url.getSite(), url.getPath(), Page.LIVE, null);
  }

  /**
   * Creates a new {@link PageURI} that is equal to <code>uri</code> except for
   * the version which is switched to <code>version</code>.
   * 
   * @param uri
   *          the uri
   * @param version
   *          the version
   */
  public PageURIImpl(PageURI uri, long version) {
    this(uri.getSite(), uri.getPath(), version, uri.getId());
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
   * @param id
   *          the page identifier
   */
  public PageURIImpl(Site site, String path, String id)
      throws MalformedPageURIException {
    this(site, path, Page.LIVE, id);
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
    super(path, '/');
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (!path.startsWith("/"))
      throw new MalformedPageURIException(path);
    this.site = site;
    this.id = id;
    this.version = version;
  }

  /**
   * Sets the page identifier.
   * 
   * @param id
   *          the identifier
   */
  public void setIdentifier(String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getId()
   */
  public String getId() throws MalformedPageURIException {
    return id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getParentURI()
   */
  public PageURI getParentURI() throws MalformedPageURIException {
    String parentPath = getParentPath();
    if (parentPath == null)
      return null;
    return new PageURIImpl(site, parentPath, version, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getPath()
   */
  public String getPath() throws MalformedPageURIException {
    return path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getSite()
   */
  public Site getSite() throws MalformedPageURIException {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getVersion()
   */
  public long getVersion() throws MalformedPageURIException {
    return version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageURI#getVersion(long)
   */
  public PageURI getVersion(long version) throws MalformedPageURIException {
    return new PageURIImpl(site, path, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return path.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageURI) {
      PageURI uri = (PageURI) obj;
      if (!site.equals(uri.getSite()))
        return false;
      if (!path.equals(uri.getPath()))
        return false;
      if (version != uri.getVersion())
        return false;
      return true;
    }
    return super.equals(obj);
  }

}