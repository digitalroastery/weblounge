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

package ch.o2it.weblounge.common.content.page;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

/**
 * This interface describes an extension for <code>HTMLHeadElement</code>s that
 * have been read from a site configuration.
 * <p>
 * Since these elements support the use of variables, the element needs to be
 * initialized with the missing information such as hostname, site and module.
 */
public interface DeclarativeHTMLHeadElement extends HTMLHeadElement {

  /**
   * Provides the tag with request information.
   * 
   * @param request
   *          the request
   * @param site
   *          the site
   * @param module
   *          the module
   * @throws IllegalStateException
   *           if the tag cannot be configured
   */
  void configure(WebloungeRequest request, Site site, Module module)
      throws IllegalStateException;

}
