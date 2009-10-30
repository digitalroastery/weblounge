/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.request;

import ch.o2it.weblounge.common.impl.security.Guest;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.History;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * This class is a wrapper to the <code>HttpServletRequest</code> with weblounge
 * specific functionality enhancements, e. g. to get access to the requested
 * site or language.
 */
public class WebloungeRequestImpl extends HttpServletRequestWrapper implements WebloungeRequest {

  /** Logging facility */
  private Logger log_ = LoggerFactory.getLogger(WebloungeRequestImpl.class);

  /** The attribute name used to store the {@link User} in the session */
  public static final String SESSION_USER = "weblounge-user";

  /** The attribute name used to store the {@link Language} in the session */
  public static final String SESSION_LANGUAGE = "weblounge-language";

  /** The attribute name used to store the {@link Site} in the session */
  public static final String SESSION_SITE = "weblounge-site";

  /** The request counter */
  private static long requestCounter_ = 0L;

  /** The request identifier */
  protected String id = null;

  /** Target site of this request */
  protected Site site = null;

  /** User of this request */
  protected User user = null;

  /** Language used for this site and request */
  protected Language language = null;

  /** Target url */
  protected WebUrl url = null;

  /** Url that was originally requested */
  protected WebUrl requestedUrl = null;

  /**
   * Creates a new wrapper for <code>request</code>.
   * 
   * @param request
   *          the request to wrap.
   */
  public WebloungeRequestImpl(HttpServletRequest request) {
    super(request);
  }

  /**
   * Initializes reloading of cached request information.
   */
  public void revalidate() {
    user = null;
  }

  /**
   * Returns the requested language. The language is determined by evaluating
   * the respective request header fields.
   * 
   * @return the requested language
   */
  public Language getLanguage() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return SessionSupport.getLanguage((HttpServletRequest) getRequest());
  }

  /**
   * Returns the requested site.
   * 
   * @return the requested site
   */
  public Site getSite() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return site;
  }

  /**
   * Returns the requested url.
   * 
   * @return the requested url
   */
  public WebUrl getUrl() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Has the url been cached?
    if (url != null) {
      return url;
    }

    return RequestSupport.getUrl((HttpServletRequest) getRequest());
  }

  /**
   * Returns the originally requested url.
   * 
   * @return the requested url
   */
  public WebUrl getRequestedUrl() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Has the requested url been cached?
    if (requestedUrl != null) {
      return requestedUrl;
    }    
    
    return RequestSupport.getRequestedUrl((HttpServletRequest) getRequest());
  }

  /**
   * Returns the current user.
   * 
   * @return the user
   */
  public User getUser() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Has the user been cached?
    if (user != null) {
      return user;
    }

    // Extract the user from the session (a.k.a an earlier request). Then make
    // sure the user was put there for the current site.
    user = (User) getSession(true).getAttribute(SESSION_USER);

    // if no valid user object has been found in the session, it's the
    // visitor's first access to this site. Therefore, he/she is automatically
    // being logged in as guest.
    if (user == null) {
      log_.info("New guest at " + getLocalName());
      user = new Guest(getSite());
    }

    return user;
  }

  /**
   * Returns the user's history.
   * 
   * @return the history
   */
  public History getHistory() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return SessionSupport.getHistory((HttpServletRequest) getRequest());
  }

  /**
   * Returns the requested version. Possible values are:
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.page.Page#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#WORK}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#ORIGINAL}</li>
   * </ul>
   * 
   * @return the requested version
   */
  public long getVersion() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    long version = RequestSupport.getVersion((HttpServletRequest) getRequest());
    return (version < 0) ? getUrl().getVersion() : version;
  }

  /**
   * Returns the requested output method.
   * 
   * @return the requested output method
   */
  public String getOutputMethod() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // TODO: Make method selection dynamic
    return "html";
  }

  /**
   * Tells the request what site it is serving. This call updates the site in
   * the user's session, since it's being used by {@link #getUser()} and other
   * methods.
   * 
   * @param site
   *          the site
   */
  public void init(Site site) {
    this.site = site;

    // Update the request counter
    id = "[" + requestCounter_ + "]";
    if (requestCounter_ == Long.MAX_VALUE)
      requestCounter_ = 0L;
    else
      requestCounter_ ++;

    // Handle changes to the users session that might be required should he/she
    // be surfing on another site
    HttpSession session = getSession(true);
    Site oldSite = (Site)session.getAttribute(SESSION_SITE);
    if (!site.equals(oldSite)) {
      session.removeAttribute(SESSION_USER);
      session.removeAttribute(SESSION_LANGUAGE);
      session.setAttribute(SESSION_SITE, site);
    }
  }

  /**
   * Method to reset this request object, forcing it to release any cached
   * information.
   */
  public void reset() {
    site = null;
    user = null;
    language = null;
    url = null;
    requestedUrl = null;
  }

  /**
   * Returns <code>true</code> if <code>o</code> represents the same request
   * object.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof WebloungeRequestImpl) {
      return ((WebloungeRequestImpl) o).id.equals(id);
    }
    return false;
  }

  /**
   * Returns the hash code for this request.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Returns a string representation of this request.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return id;
  }

}