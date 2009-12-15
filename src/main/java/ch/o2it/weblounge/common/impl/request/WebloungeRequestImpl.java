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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.page.PageUtils;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.user.GuestImpl;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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

  /** The attribute name used to store the {@link User} in the session */
  public static final String SESSION_USER = "weblounge-user";

  /** The attribute name used to store the {@link Language} in the session */
  public static final String SESSION_LANGUAGE = "weblounge-language";

  /** The attribute name used to store the {@link Site} in the session */
  public static final String SESSION_SITE = "weblounge-site";

  /** The attribute name used to store the last {@link URL} in the session */
  public static final String SESSION_PREVIOUS_URL = "weblounge-lasturl";

  /** The language extraction regular expression */
  private static Pattern languageExtractor_ = Pattern.compile("_([a-zA-Z]+)\\.[\\w\\- ]+$");

  /** Regular expression used to take urls apart */
  private static Pattern urlAnalyzer_ = Pattern.compile("^(.*)(work|original|index)(_[a-zA-Z0-9]+)?\\.([a-zA-Z0-9]+)$");

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

  /** Url that was requested before this requested */
  protected WebUrl previousUrl = null;

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
   * Returns the requested language. The language is determined by evaluating
   * the respective request header fields.
   * 
   * @return the requested language
   */
  public Language getLanguage() {
    // Has the language been cached?
    if (language != null) {
      return language;
    }

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
      language = getSite().getDefaultLanguage();
      log_.trace("Selected default site language " + language);
    }

    getSession().setAttribute(SESSION_LANGUAGE, language);
    return language;
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
    if (url != null) {
      return url;
    }

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    String urlPrefix = null;
    String installPath = Env.getURI();
    String servletPath = Env.getServletPath();
    urlPrefix = UrlSupport.trim(UrlSupport.concat(installPath, servletPath));

    String uri = getRequestURI();
    String urlPath = uri.substring(urlPrefix.length() - 1);
    String urlFlavor = "html";
    long version = Page.LIVE;
    log_.trace("url prefix=" + urlPrefix + "; request uri=" + uri + "; url=" + urlPath);

    // Version selection
    Matcher m = urlAnalyzer_.matcher(urlPath);
    if (m.matches()) {
      urlPath = m.group(1);
      version = PageUtils.getVersion(m.group(2));
      urlFlavor = m.group(3);
    }

    this.url = new WebUrlImpl(site, urlPath, version, urlFlavor);
    this.requestedUrl = url;
    getSession(true).setAttribute(SESSION_PREVIOUS_URL, url);
    return url;
  }

  /**
   * Returns the originally requested url.
   * 
   * @return the requested url
   */
  public WebUrl getRequestedUrl() {
    if (requestedUrl != null) {
      return requestedUrl;
    }

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // If the requested url has not been stored so far, it will anyway be equal
    // to what getUrl() returns
    return getUrl();
  }

  /**
   * Returns the user's history.
   * 
   * @return the history
   */
  public WebUrl getPreviousUrl() {
    if (previousUrl != null)
      return previousUrl;

    if (site == null)
      throw new IllegalStateException("Site has not been set");
    
    // Seems that no on has shown interest in the previous url so far. Let's see
    // if we can extract it from the session
    previousUrl = (WebUrl) getSession(true).getAttribute(SESSION_PREVIOUS_URL);    
    return previousUrl;
  }

  /**
   * Returns the current user.
   * 
   * @return the user
   */
  public User getUser() {
    if (user != null) {
      return user;
    }

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Extract the user from the session (a.k.a an earlier request). Then make
    // sure the user was put there for the current site.
    user = (User) getSession(true).getAttribute(SESSION_USER);

    // if no valid user object has been found in the session, it's the
    // visitor's first access to this site. Therefore, he/she is automatically
    // being logged in as guest.
    if (user == null) {
      log_.debug("New guest at " + getLocalName());
      user = new GuestImpl();
    }

    return user;
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
    return getUrl().getVersion();
  }

  /**
   * Returns the requested output method.
   * 
   * @return the requested output method
   */
  public String getOutputMethod() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return getUrl().getFlavor();
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
   * <p/>
   * Note that you should only set <code>clearSession</code> to <code>true</code>
   * if you want to get rid of cached information to force a change in the way
   * the current user is treated, e. g. if he/she has been logged out.
   * <p/>
   * The following things are stored in the session
   * <ul>
   *  <li>The current site</li>
   *  <li>The user</li>
   *  <li>The selected language</li>
   *  <li>The previously visited url (on this site)</li>
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