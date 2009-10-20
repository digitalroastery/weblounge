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

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteNotFoundException;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Facade to the <code>HttpRequest</code> object with easy-to-use access to user
 * and language information.
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since WebLounge 1.0
 */

public final class RequestSupport {

  /** The request identifier */
  private static long id = 0;

  /** The url prefix where the webapp is mapped */
  private static String urlPrefix = null;

  /** The thread - request mapping */
  private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();

  private static Pattern urlAnalyzer_ = Pattern.compile("^(.*)(work|original|index)(_[a-zA-Z]+)?\\.([a-zA-Z0-9]+)$");

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = RequestSupport.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * RequestSupport is a static class and therefore has no constructor.
   */
  private RequestSupport() {
  }

  /**
   * Triggers the creation of a request identifier.
   * 
   * @param request
   *          the servlet request
   */
  public static String setRequestId(HttpServletRequest request) {
    if (id == Long.MAX_VALUE)
      id = 0;
    else
      id++;
    String requestId = getSite(request) + "-" + id;
    request.setAttribute("request-id", requestId);
    return requestId;
  }

  /**
   * Returns the request url, which corresponds to the so called domain. The url
   * provides information on the called site, the server name, the webapp path
   * and the webapp-relative url itself.
   * 
   * @param request
   *          the http request
   * @return the url
   */
  public static WebUrl getRequestedUrl(HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    WebUrl url = attribs.getRequestedUrl();
    return (url != null) ? url : getUrl(request);
  }

  /**
   * Returns the request identifier.
   * 
   * @param request
   *          the servlet request
   * @return the request identifier
   */
  public static String getRequestId(HttpServletRequest request) {
    return (String) request.getAttribute("request-id");
  }

  /**
   * Returns the site associated with the current request or the default site,
   * if it has been configured and if <code>forward</code> is set to
   * <code>true</code>.
   * <p>
   * This method throws a <code>SiteNotFoundException</code> if the site is not
   * found.
   * 
   * @param request
   *          the <code>HttpRequest</code>
   * @param forward
   *          <code>true</code> if unmatched requests should be forwarded to the
   *          default site
   * @return the site associated with the current request
   * @throws SiteNotFoundException
   *           if the site is not found
   */
  public static Site getSite(HttpServletRequest request, boolean forward)
      throws SiteNotFoundException {
    RequestAttributes attribs = getAttributes(request);
    Site site = attribs.getSite();
    if (site != null) {
      return site;
    } else {
      throw new SiteNotFoundException(request.getServerName());
    }

    // // we didn't. So try to find the site by the server name
    // String server = request.getServerName();
    // SiteRegistry sites = (SiteRegistry)SystemRegistries.get(SiteRegistry.ID);
    // site = sites.getByServerName(server);
    //		
    // // if no site has been found, then throw the corresponding exception
    // // since this is a configuration mistake.
    //		
    // if (site == null) {
    // if (forward && sites.getDefault() != null &&
    // !SiteWatchdog.sitesLoading()) {
    // site = sites.getDefault();
    // attribs.setSite(site);
    // if (!redirects.contains(request.getServerName())) {
    // log_.warn("Forwarding requests for host '" + request.getServerName() +
    // "' to default site '" + site + "'");
    // redirects.add(request.getServerName());
    // }
    // }
    //    		
    // // no suitable site could be found. This could either be due to a missing
    // // default site, or to the fact, that sites are still being loaded.
    //   
    // else {
    // log_.debug("Site not found for host '" + server + "'");
    // throw new SiteNotFoundException(server);
    // }
    // }
    //		
    // // We found a site, so store it in the request to speed up upcoming
    // // calls to this method.
    //		
    // attribs.setSite(site);
    // log_.debug("Request matched site " + site);
    // return site;
  }

  /**
   * Returns the site associated with the current request or the default site,
   * if the hostname doesn't match any of the loaded sites.
   * <p>
   * This method throws a <code>SiteNotFoundException</code> if the site is not
   * found and no default site has been configured.
   * 
   * @param request
   *          the <code>HttpRequest</code>
   * @return the site associated with the current request
   * @throws SiteNotFoundException
   *           if the site is not found
   */
  public static Site getSite(HttpServletRequest request)
      throws SiteNotFoundException {
    return getSite(request, true);
  }

  /**
   * Sets the url. This method is used to tweak the url when passing on requests
   * to a different handler, e. g. if an action has a target url defined which
   * should be handled by the page handler, but not with the original action
   * url.
   * 
   * @param url
   *          the new url
   * @param request
   *          the request object
   */
  public static void setUrl(WebUrl url, HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    attribs.setUrl(url);
  }

  /**
   * Sets the url and version. This method is used to tweak the url when passing
   * on requests to a different handler, e. g. if an action has a target url
   * defined which should be handled by the page handler, but not with the
   * original action url.
   * 
   * @param uri
   *          the page uri
   * @param request
   *          the request object
   */
  public static void setUri(PageURI uri, HttpServletRequest request) {
    setUrl(uri.getLink(), request);
    setVersion(uri.getVersion(), request);
  }

  /**
   * Returns the request url, which corresponds to the so called domain. The url
   * provides information on the called site, the server name, the webapp path
   * and the webapp-relative url itself.
   * 
   * @param request
   *          the http request
   * @return the url
   */
  public static WebUrl getUrl(HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    WebUrl url = attribs.getUrl();
    if (url != null) {
      return url;
    }

    // We didn't. To determine the url relative to the servlet mapping
    // prefix, we have to cut off some parts

    if (urlPrefix == null) {
      String installPath = Env.getURI();
      String servletPath = Env.getServletPath();
      urlPrefix = UrlSupport.trim(UrlSupport.concat(installPath, servletPath));
    }

    String uri = request.getRequestURI();
    String urlPath = uri.substring(urlPrefix.length() - 1);
    String urlFlavor = "html";
    log_.debug("url prefix=" + urlPrefix + "; request uri=" + uri + "; url=" + urlPath);

    // Version selection

    long version = Page.LIVE;
    String mode = (String) request.getSession().getAttribute(PageManager.MODE);

    Matcher m = urlAnalyzer_.matcher(urlPath);
    if (m.matches()) {
      String versionId = m.group(2);
      version = getVersion(versionId);
      urlPath = m.group(1);
      urlFlavor = m.group(4);
    }

    // If we are within the control center, it depends on the selected tab
    // (live or edit), which version to show

    if (PageManager.MODE_LIVE.equals(mode)) {
      version = Page.LIVE;
    } else if (PageManager.MODE_WORK.equals(mode)) {
      version = Page.WORK;
    }

    Site site = getSite(request);
    url = new WebUrlImpl(site, urlPath, version);
    if (urlFlavor != null)
      ((WebUrlImpl) url).setFlavor(urlFlavor);

    // Url Lookup succeeded. Store it for later reference and return it.

    attribs.setUrl(url);
    return url;
  }

  /**
   * Sets the version. This method is used to tweak the version when passing on
   * requests to a different handler.
   * 
   * @param version
   *          the new version
   * @param request
   *          the request object
   */
  public static void setVersion(long version, HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    attribs.setVersion(version);
  }

  /**
   * Returns the request version. This method returns the version if set or
   * <code>-1</code> otherwise.
   * 
   * @param request
   *          the http request
   * @return the version
   */
  public static long getVersion(HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    return attribs.getVersion();
  }

  /**
   * Removes the url attribute from the request and thus forces reevaluation.
   * 
   * @param request
   *          the request
   */
  public static void clearUrl(HttpServletRequest request) {
    RequestAttributes attribs = getAttributes(request);
    attribs.setUrl(null);
  }

  /**
   * Returns the request attributes that are specific to weblounge.
   * 
   * @param request
   *          the http request
   * @return the weblounge request attributes
   */
  private static RequestAttributes getAttributes(HttpServletRequest request) {
    RequestAttributes attribs = (RequestAttributes) request.getAttribute(RequestAttributes.ID);
    if (attribs == null) {
      attribs = new RequestAttributes();
      request.setAttribute(RequestAttributes.ID, attribs);
    }
    return attribs;
  }

  /**
   * Dumps the request headers to <code>System.out</code>.
   * 
   * @param request
   *          the request
   */
  public static void dumpHeaders(HttpServletRequest request) {
    Enumeration hi = request.getHeaderNames();
    System.out.println("Request headers:");
    while (hi.hasMoreElements()) {
      String header = (String) hi.nextElement();
      String value = request.getHeader(header);
      System.out.println("\t" + header + ": " + value);
    }
  }

  /**
   * Returns the site that this thread is currently visiting.
   * 
   * @param thread
   *          the thread
   * @return the visited site
   */
  public static Site getSite() {
    HttpServletRequest request = RequestSupport.request.get();
    if (request != null) {
      return getSite(request);
    }
    return null;
  }

  /**
   * Returns the user that is associated with the current thread.
   * 
   * @return the visiting user
   */
  public static AuthenticatedUser getUser() {
    HttpServletRequest request = RequestSupport.request.get();
    if (request != null) {
      return SessionSupport.getUser(request);
    }
    return null;
  }

  /**
   * Returns a string representation of the request parameters.
   * 
   * @param request
   *          the request
   * @return the request parameters
   */
  public static String getParameters(WebloungeRequest request) {
    StringBuffer params = new StringBuffer();
    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      String param = (String) e.nextElement();
      String value = request.getParameter(param);
      if (params.length() > 0) {
        params.append(";");
      }
      params.append(param);
      params.append("=");
      params.append(value);
    }
    return (params.length() > 0) ? "[" + params.toString() + "]" : "[-]";
  }

  /**
   * Registers the given request with the thread.
   * 
   * @param request
   *          the request
   */
  public static void register(HttpServletRequest request) {
    RequestSupport.request.set(request);
  }

  /**
   * Removes the map entry for the current thread.
   */
  public static void deregister() {
  }

  /**
   * Returns the version for the given version identifier. Available versions
   * are:
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #WORK}</li>
   * <li>{@link #ORIGINAL}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  private static long getVersion(String version) {
    if (version.equals("index")) {
      return Page.LIVE;
    } else if (version.equals("work")) {
      return Page.WORK;
    } else if (version.equals("original")) {
      return Page.ORIGINAL;
    } else {
      try {
        return Long.parseLong(version);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

}