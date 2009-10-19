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

import ch.o2it.weblounge.common.impl.security.Guest;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.History;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class is a wrapper to the <code>HttpServletRequest</code> with weblounge
 * specific functionality enhancements, e. g. to get access to the requested
 * site or language.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class WebloungeRequestImpl extends HttpServletRequestWrapper implements WebloungeRequest {

  /** The request counter */
  private static long requestCounter_;

  /** The request identifier */
  private String id_;

  /** whether the authentication header has aleady been evaluated */
  private boolean authEvaluated_;

  /** the user of this request */
  private AuthenticatedUser user_;

  /**
   * Creates a new wrapper for <code>request</code>.
   * 
   * @param request
   *          the request to wrap.
   */
  public WebloungeRequestImpl(HttpServletRequest request) {
    super(request);
    id_ = "[" + requestCounter_ + "]";
    requestCounter_++;
    if (requestCounter_ == Long.MAX_VALUE)
      requestCounter_ = 0L;
    authEvaluated_ = false;
  }

  /**
   * Initializes reloading of cached request information.
   */
  public void revalidate() {
    user_ = null;
  }

  /**
   * Returns the requested language. The language is determined by evaluating
   * the respective request header fields.
   * 
   * @return the requested language
   */
  public Language getLanguage() {
    return SessionSupport.getLanguage((HttpServletRequest) getRequest());
  }

  /**
   * Returns the requested site.
   * 
   * @return the requested site
   */
  public Site getSite() {
    return RequestSupport.getSite((HttpServletRequest) getRequest());
  }

  /**
   * Returns the requested url.
   * 
   * @return the requested url
   */
  public WebUrl getUrl() {
    return RequestSupport.getUrl((HttpServletRequest) getRequest());
  }

  /**
   * Returns the originally requested url.
   * 
   * @return the requested url
   */
  public WebUrl getRequestedUrl() {
    return RequestSupport.getRequestedUrl((HttpServletRequest) getRequest());
  }

  /**
   * Returns the current user.
   * 
   * @return the user
   */
  public AuthenticatedUser getUser() {
    if (user_ != null) {
      return user_;
    }

    // check if an authenticated user is stored in session
    AuthenticatedUser user = SessionSupport.getUser((HttpServletRequest) getRequest());
    if (user.isAuthenticated()) {
      user_ = user;
      return user;
    }

    // check whether the authentication has already been evaluated
    if (authEvaluated_)
      return user;
    authEvaluated_ = true;

    // TODO: Redesign! Request should not know about authentication service
    // // Check if the authentication service is enabled
    // HttpAuthCallback ctxt = new HttpAuthCallback(this, null);
    // if (ctxt.getAuthorizationHeader() != null) {
    // AuthenticationService service =
    // (AuthenticationService)getSite().getService(AuthenticationService.ID,
    // true);
    // if (service != null && service.isEnabled()) {
    // user = service.login(ctxt);
    // if (user != null) {
    // SessionAttributes attribs =
    // (SessionAttributes)getSession().getAttribute(SessionAttributes.ID);
    // attribs.setUser(user);
    // user_ = user;
    // return user;
    // }
    // }
    // }
    user_ = new Guest(getSite());
    return user_;
  }

  /**
   * Returns the user's history.
   * 
   * @return the history
   */
  public History getHistory() {
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
    long version = RequestSupport.getVersion((HttpServletRequest) getRequest());
    return (version < 0) ? getUrl().getVersion() : version;
  }

  /**
   * Returns the requested output method.
   * 
   * @return the requested output method
   */
  public String getOutputMethod() {
    // PENDING: Make method selection dynamic
    return "html";
  }

  /**
   * Returns <code>true</code> if <code>o</code> represents the same request
   * object.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof WebloungeRequestImpl) {
      return ((WebloungeRequestImpl) o).id_.equals(id_);
    }
    return false;
  }

  /**
   * Returns the hash code for this request.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return id_.hashCode();
  }

  /**
   * Returns a string representation of this request.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return id_;
  }

}