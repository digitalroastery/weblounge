/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
package ch.entwine.weblounge.kernel.http;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.entwine.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.entwine.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.kernel.site.SiteManager;

/**
 * This servlet filter puts information about the request into the logging
 * context.
 */
public class WebloungeLoggingFilter extends AbstractWebloungeFilter {

  /** Key of the Weblounge site in the logging context */
  public static final String MDC_KEY_WEBLOUNGE_SITE = "weblounge.site";
  
  /** Key of the Weblounge user in the logging context */
  public static final String MDC_KEY_WEBLOUNGE_USER = "weblounge.user";
  
  /** Key of the Weblounge language in the logging context */
  public static final String MDC_KEY_WEBLOUNGE_LANGUAGE = "weblounge.language";
  
  /** Key of the request session in the logging context */
  public static final String MDC_KEY_REQUEST_SESSION = "request.session";
  
  /** Key of the request method in the logging context */
  public static final String MDC_KEY_REQUEST_METHOD = "request.method";
  
  /** Key of the request parameters in the logging context */
  public static final String MDC_KEY_REQUEST_PARAMETERS = "request.parameters";
  
  /** Key of the request uri in the logging context */
  public static final String MDC_KEY_REQUEST_URI = "request.uri";

  /** The environment */
  private Environment env = null;

  /** The site manager */
  private SiteManager siteManager = null;

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeLoggingFilter.class);

  @Override
  public void doFilter(WebloungeRequestImpl request, WebloungeResponseImpl response,
      FilterChain chain) throws IOException, ServletException {

    if (request.getSite() != null) {
      MDC.put(MDC_KEY_WEBLOUNGE_SITE, request.getSite().getIdentifier());
      logger.trace("Put '{}' with key '{}' into logging context", request.getSite().getIdentifier(), MDC_KEY_WEBLOUNGE_SITE);
    }
    if (request.getUser() != null) {
      MDC.put(MDC_KEY_WEBLOUNGE_USER, request.getUser().getLogin());
      logger.trace("Put '{}' with key '{}' into logging context", request.getUser().getLogin(), MDC_KEY_WEBLOUNGE_USER);
    }
    if (request.getLanguage() != null) {
      MDC.put(MDC_KEY_WEBLOUNGE_LANGUAGE, request.getLanguage().getIdentifier());
      logger.trace("Put '{}' with key '{}' into logging context", request.getLanguage().getIdentifier(), MDC_KEY_WEBLOUNGE_LANGUAGE);
    }
    if (request.getSession() != null) {
      MDC.put(MDC_KEY_REQUEST_SESSION, request.getSession().getId());
      logger.trace("Put '{}' with key '{}' into logging context", request.getSession().getId(), MDC_KEY_REQUEST_SESSION);
    }
    if (request.getRequestURI() != null) {
      MDC.put(MDC_KEY_REQUEST_URI, request.getRequestURI());
      logger.trace("Put '{}' with key '{}' into logging context", request.getRequestURI(), MDC_KEY_REQUEST_URI);
    }
    if (!request.getParameterMap().isEmpty()) {
      MDC.put(MDC_KEY_REQUEST_PARAMETERS, request.getParameterMap().toString());
      logger.trace("Put '{}' with key '{}' into logging context", request.getParameterMap().toString(), MDC_KEY_REQUEST_PARAMETERS);
    }
    if (request.getMethod() != null) {
      MDC.put(MDC_KEY_REQUEST_METHOD, request.getMethod());
      logger.trace("Put '{}' with key '{}' into logging context", request.getMethod(), MDC_KEY_REQUEST_METHOD);
    }

    logger.debug("Populated Weblounge logging context");

    // Pass on to the next filter in the chain
    chain.doFilter(request.getRequest(), response.getResponse());
  }

  @Override
  public String toString() {
    return "Weblounge Logging Context Filter";
  }

  @Override
  protected Environment getEnvironment() {
    return env;
  }

  @Override
  protected SiteManager getSiteManager() {
    return siteManager;
  }

  /**
   * OSGi callback to set the environment service
   * 
   * @param environment
   *          the environment service
   */
  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

  /**
   * OSGi callback to set the site manager service.
   * 
   * @param siteManager
   *          the site manager
   */
  protected void setSiteManager(SiteManager siteManager) {
    this.siteManager = siteManager;
  }

}
