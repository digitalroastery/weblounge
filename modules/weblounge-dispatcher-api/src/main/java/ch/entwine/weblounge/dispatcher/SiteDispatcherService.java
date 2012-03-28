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

package ch.entwine.weblounge.dispatcher;

import ch.entwine.weblounge.common.site.Site;

import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This service registers sites with the
 * <code>org.osgi.service.HttpService</code>.
 */
public interface SiteDispatcherService {

  /**
   * Returns the site associated with the given server name.
   * <p>
   * Note that the server name is expected to not end with a trailing slash, so
   * please pass in <code>www.entwinemedia.com</code> instead of
   * <code>http://www.entwinemedia.com/</code>.
   * 
   * @param url
   *          the server url, e.g. <code>http://www.entwinemedia.com</code>
   * @return the site
   */
  Site findSiteByURL(URL url);

  /**
   * Returns the site with the given site identifier or <code>null</code> if no
   * such site is currently registered.
   * 
   * @param identifier
   *          the site identifier
   * @return the site
   */
  Site findSiteByIdentifier(String identifier);

  /**
   * Returns the site targeted by the given request or <code>null</code> if no
   * site can be matched to the request.
   * 
   * @param request
   *          the servlet request
   * @return the site
   */
  Site findSiteByRequest(HttpServletRequest request);

  /**
   * Returns the servlet that is registered to serve content from the specified
   * site.
   * 
   * TODO: Move
   * 
   * @param site
   *          the site
   * @return the SiteServlet
   */
  Servlet getSiteServlet(Site site);

}