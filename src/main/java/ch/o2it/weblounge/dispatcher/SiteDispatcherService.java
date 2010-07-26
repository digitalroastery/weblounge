/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher;

import ch.o2it.weblounge.common.site.Site;

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
   * please pass in <code>www.o2it.ch</code> instead of
   * <code>www.o2it.ch/</code>.
   * 
   * @param serverName
   *          the server name, e.g. <code>www.o2it.ch</code>
   * @return the site
   */
  Site findSiteByName(String serverName);

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