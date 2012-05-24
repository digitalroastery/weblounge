/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.request;

import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
  private Logger logger = LoggerFactory.getLogger(WebloungeRequestImpl.class);

  /** The language extraction regular expression */
  private static final Pattern LANG_EXTRACTOR_REGEX = Pattern.compile("_([a-zA-Z]+)\\.[\\w\\- ]+$");

  /** The request counter */
  private static long requestCounter = 0L;

  /** The request identifier */
  protected String id = null;

  /** Target site of this request */
  protected Site site = null;

  /** The site servlet */
  protected Servlet siteServlet = null;

  /** User of this request */
  protected User user = null;

  /** Language used for this site and request */
  protected Language language = null;

  /** Language found in the session when the request started */
  protected Language sessionLanguage = null;

  /** Target url */
  protected WebUrlImpl url = null;

  /** Url that was originally requested */
  protected WebUrlImpl requestedUrl = null;

  /** The request environment */
  protected Environment environment = null;

  /**
   * Creates a new wrapper for <code>request</code>.
   * 
   * @param request
   *          the request to wrap.
   * @param servlet
   *          the servlet used to serve content out of the request's site
   * @param environment
   *          the environment
   */
  public WebloungeRequestImpl(HttpServletRequest request, Servlet servlet,
      Environment environment) {
    super(request);
    HttpSession session = request.getSession();
    if (session != null)
      this.sessionLanguage = (Language) session.getAttribute(LANGUAGE);
    if (environment == null)
      throw new IllegalArgumentException("Environment must not be null");
    this.environment = environment;
    this.siteServlet = servlet;
  }

  /**
   * Creates a new wrapper for <code>request</code>.
   * 
   * @param request
   *          the request to wrap.
   * @param environment
   *          the environment
   */
  public WebloungeRequestImpl(HttpServletRequest request,
      Environment environment) {
    this(request, null, environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getEnvironment()
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getLanguage()
   */
  public Language getLanguage() {
    // Has the language been cached?
    if (language != null)
      return language;

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // The language might very well be encoded as part of the path, so asking
    // for the url might give us the language for free.
    if (url == null)
      url = (WebUrlImpl) getUrl();

    // There is no url without a path, so we can safely assume that we will
    // get an object back form getUrl().
    language = url.getLanguage();

    // Take a look at the url, where language information might be encoded,
    // e. g. index_en.xml
    if (language == null) {
      Matcher m = LANG_EXTRACTOR_REGEX.matcher(getRequestURI());
      if (m.find()) {
        String languageCandidate = m.group(1);
        try {
          language = LanguageUtils.getLanguage(languageCandidate);
          logger.trace("Selected language " + language + " from request uri");
        } catch (UnknownLanguageException e) {
          logger.trace("'{}' does not represent a langauge", languageCandidate);
        }
      }
    }

    // Get hold of the session
    HttpSession session = getSession(true);

    // Extract the language from the session (a.k.a an earlier request). Then
    // make sure the language was put there for the current site.
    if (language == null && session != null) {
      language = (Language) session.getAttribute(LANGUAGE);
    }

    // If the url didn't contain language information, or referenced a language
    // that the site doesn't support, let's go for the user's browser
    // preferences
    if (language == null) {
      Enumeration<?> localeEnum = getLocales();
      while (localeEnum.hasMoreElements()) {
        String languageId = ((Locale) localeEnum.nextElement()).getLanguage();
        language = site.getLanguage(languageId);
        if (language != null) {
          logger.trace("Selected language " + languageId + " from browser preferences");
          break;
        }
      }
    }

    // Still no valid language? Let's go with the site default.
    if (language == null) {
      language = site.getDefaultLanguage();
      logger.trace("Selected default site language " + language);
    }

    // This really looks like a configuration disaster!
    if (language == null) {
      language = new LanguageImpl(Locale.getDefault());
      logger.trace("Selected default system language " + language);
    }

    // Store the current selection in the session
    if (language != null && session != null)
      session.setAttribute(LANGUAGE, language);

    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getSessionLanguage()
   */
  public Language getSessionLanguage() {
    if (sessionLanguage == null)
      return getLanguage();
    return sessionLanguage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getSite()
   */
  public Site getSite() {
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getUrl()
   */
  public WebUrl getUrl() {
    if (url != null)
      return url;
    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // Let's create a url. The constructor will try to populate as many fields
    // on the url as possible, including flavor, language and version which
    // might all be encoded in the path.
    this.url = new WebUrlImpl(site, getRequestURI());

    this.requestedUrl = url;
    return url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getRequestedUrl()
   */
  public WebUrl getRequestedUrl() {
    if (requestedUrl != null)
      return requestedUrl;

    if (site == null)
      throw new IllegalStateException("Site has not been set");

    // If the requested url has not been stored so far, it will anyway be equal
    // to what getUrl() returns
    requestedUrl = (WebUrlImpl) getUrl();
    return requestedUrl;
  }

  /**
   * Sets this request's user.
   * 
   * @param user
   *          the user
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Sets this request's language.
   * 
   * @param language
   *          the language
   */
  public void setLanguage(Language language) {
    this.language = language;
    this.getSession().setAttribute(LANGUAGE, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getUser()
   */
  public User getUser() {
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getVersion()
   */
  public long getVersion() {
    if (url != null)
      return url.getVersion();
    return ((WebUrlImpl) getUrl()).getVersion();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getFlavor()
   */
  public RequestFlavor getFlavor() {
    RequestFlavor flavor = null;
    if (url != null)
      flavor = url.getFlavor();
    else
      flavor = ((WebUrlImpl) getUrl()).getFlavor();
    // TODO: Look at accepts-header (text/json, text/xml, text/...)
    return flavor != null ? flavor : RequestFlavor.ANY;
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
    id = Long.toString(requestCounter);
    if (requestCounter == Long.MAX_VALUE)
      requestCounter = 0L;
    else
      requestCounter++;

    // Handle changes to the users session that might be required should he/she
    // be surfing on another site
    HttpSession session = getSession(false);
    if (session == null)
      return;

    Site oldSite = (Site) session.getAttribute(SITE);
    if (!site.equals(oldSite)) {
      clearSession();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletRequestWrapper#getRequestDispatcher(java.lang.String)
   */
  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    final String servletPath = UrlUtils.concat("/weblounge-sites/", site.getIdentifier());
    if (siteServlet != null && path.startsWith(servletPath)) {
      return new RequestDispatcher() {
        public void include(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
          siteServlet.service(request, response);
        }

        public void forward(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
          siteServlet.service(request, response);
        }
      };
    } else {
      return super.getRequestDispatcher(path);
    }
  }

  /**
   * Method to reset this request object, forcing it to release any cached
   * information.
   * <p>
   * Note that you should only set <code>clearSession</code> to
   * <code>true</code> if you want to get rid of cached information to force a
   * change in the way the current user is treated, e. g. if he/she has been
   * logged out.
   * <p>
   * The following things are stored in the session
   * <ul>
   * <li>The current site</li>
   * <li>The selected language</li>
   * </ul>
   * 
   * @param clearSession
   *          <code>true</code> to remove cached information as well
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
    HttpSession session = getSession(false);
    if (session == null)
      return;
    session.removeAttribute(LANGUAGE);
    session.setAttribute(SITE, site);
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
      return url + " (" + id + ")";
    else
      return getRequestURI();
  }

}