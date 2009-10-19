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

package ch.o2it.weblounge.dispatcher.impl.request;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * The <code>RequestAttributes</code> are placed in the current servlet request,
 * so information about url, user, site etc. are only evaluated once and can
 * thereafter be gathered from this object.
 * 
 * @author Tobias Wunden
 * @version Apr 17, 2003
 */

public class RequestAttributes {

  /** Constant identifying the request attributes object within the request */
  public final static String ID = "weblounge-request";

  /** the current site */
  private Site site_;

  /** The current url */
  private WebUrl url_;

  /** The original url (before a call to setTargetUrl()) */
  private WebUrl requestUrl_ = null;

  /** The current version */
  private long version_ = -1;

  /**
   * Creates a new request information object.
   */
  RequestAttributes() {
  }

  /**
   * Sets the site.
   * 
   * @param site
   *          the site
   */
  void setSite(Site site) {
    site_ = site;
  }

  /**
   * Returns the site that has been called using the request that is currently
   * being processed.
   * 
   * @return the requested site
   */
  public Site getSite() {
    return site_;
  }

  /**
   * Sets the url of this request.
   * 
   * @param url
   *          the url
   */
  void setUrl(WebUrl url) {
    url_ = url;
  }

  /**
   * Returns the url that has been called by the current request.
   * 
   * @return the current url
   */
  public WebUrl getUrl() {
    return url_;
  }

  /**
   * Sets the request's initial url.
   * 
   * @param url
   *          the url
   */
  void setRequestedUrl(WebUrl url) {
    requestUrl_ = url;
  }

  /**
   * Returns the request's initial url.
   * 
   * @return the requested url
   */
  public WebUrl getRequestedUrl() {
    return requestUrl_;
  }

  /**
   * Sets the request version.
   * 
   * @param version
   *          the version
   */
  public void setVersion(long version) {
    version_ = version;
  }

  /**
   * Returns the version or <code>-1</code> if no version has been set so far.
   * 
   * @return the version
   */
  public long getVersion() {
    return version_;
  }

}