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

package ch.o2it.weblounge.common.impl.request;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.user.Guest;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /** The language extraction regular expression */
  private static Pattern languageExtractor_ = Pattern.compile("_([a-zA-Z]+)\\.[\\w\\- ]+$");

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
  public void validate() {
    user = null;
    url = null;
    language = null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getLanguage()
   */
  public Language getLanguage() {
    // Has the language been cached?
    if (language != null)
      return language;

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Extract the language from the session (a.k.a an earlier request). Then
    // make sure the language was put there for the current site.
    language = (Language) getSession(true).getAttribute(SESSION_LANGUAGE);

    // If no language has been found in the session, it's the visitor's first
    // access to this site. First thing we do is take a look at the url, where
    // language information might be encoded, e. g. index_en.xml
    if (language == null) {
      Matcher m = languageExtractor_.matcher(getRequestURI());
      if (m.find()) {
        language = LanguageSupport.getLanguage(m.group(1));
        log_.trace("Selected language " + language + " from request uri");
      }
    }

    // If the url didn't contain language information, or referenced a language
    // that the site doesn't support, let's go for the user's browser
    // preferences
    if (language == null) {
      Enumeration<?> localeEnum = getLocales();
      while (localeEnum.hasMoreElements()) {
        String languageId = ((Locale) localeEnum.nextElement()).getLanguage();
        if ((language = site.getLanguage(languageId)) != null) {
          log_.trace("Selected language " + languageId + " from browser preferences");
          break;
        }
      }
    }

    // Still no valid language? Let's go with the site default.
    if (language == null) {
      language = site.getDefaultLanguage();
      log_.trace("Selected default site language " + language);
    }

    // Wow, that's really a configuration disaster!
    if (language == null) {
      language = new LanguageImpl(Locale.getDefault());
      log_.trace("Selected default system language " + language);
    }

    getSession().setAttribute(SESSION_LANGUAGE, language);
    return language;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getSite()
   */
  public Site getSite() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return site;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getUrl()
   */
  public WebUrl getUrl() {
    if (url != null)
      return url;
    if (site == null)
      throw new IllegalStateException("Site has not been set");
    this.url = new WebUrlImpl(site, getRequestURI());
    this.requestedUrl = url;
    return url;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getRequestedUrl()
   */
  public WebUrl getRequestedUrl() {
    if (requestedUrl != null)
      return requestedUrl;

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // If the requested url has not been stored so far, it will anyway be equal
    // to what getUrl() returns
    requestedUrl = getUrl();
    return requestedUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getUser()
   */
  public User getUser() {
    if (user != null)
      return user;

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Extract the user from the session (a.k.a an earlier request). Then make
    // sure the user was put there for the current site.
    user = (User) getSession(true).getAttribute(SESSION_USER);

    // if no valid user object has been found in the session, it's the
    // visitor's first access to this site. Therefore, he/she is automatically
    // being logged in as guest.
    if (user == null) {
      log_.debug("New guest at {}", getLocalName());
      user = new Guest();
    }

    return user;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getVersion()
   */
  public long getVersion() {
    if (url != null)
      return url.getVersion();
    return getUrl().getVersion();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeRequest#getFlavor()
   */
  public RequestFlavor getFlavor() {
    RequestFlavor flavor = null;
    if (url != null)
      flavor = url.getFlavor();
    else
      flavor = getUrl().getFlavor();
    return flavor != null ? flavor : RequestFlavor.HTML;
  }

  /**
   * Tells the request which site it is serving. This call updates the site in
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
      requestCounter_++;

    // Handle changes to the users session that might be required should he/she
    // be surfing on another site
    HttpSession session = getSession(true);
    Site oldSite = (Site) session.getAttribute(SESSION_SITE);
    if (!site.equals(oldSite)) {
      clearSession();
    }
  }

  /**
   * Method to reset this request object, forcing it to release any cached
   * information.
   * <p>
   * Note that you should only set <code>clearSession</code> to <code>true</code>
   * if you want to get rid of cached information to force a change in the way
   * the current user is treated, e. g. if he/she has been logged out.
   * <p>
   * The following things are stored in the session
   * <ul>
   *  <li>The current site</li>
   *  <li>The user</li>
   *  <li>The selected language</li>
   * </ul>
   * 
   * @param clearSession <code>true</code> to remove cached information as well
   */
  public void reset(boolean clearSession) {
    site = null;
    user = null;
    language = null;
    url = null;
    requestedUrl = null;
    if (clearSession) {
      clearSession();
    }
  }
  
  /**
   * Utility method used to clear the attributes stored by the url in the user's
   * session.
   */
  private void clearSession() {
    HttpSession session = getSession(true);
    session.removeAttribute(SESSION_USER);
    session.removeAttribute(SESSION_LANGUAGE);
    session.setAttribute(SESSION_SITE, site);
  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    if (url != null)
      return url + " [" + id + "]";
    else
      return getRequestURI();
  }

}