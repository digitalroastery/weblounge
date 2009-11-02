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

package ch.o2it.weblounge.common.url;

import ch.o2it.weblounge.common.site.Site;

/**
 * A web url represents a url that is used to address locations within the
 * webapp, such as html pages or module actions.
 */
public interface WebUrl extends Url {

  /** Default flavors */
  public enum Flavor { html, xml, json };

  /**
   * Returns the parent site.
   * 
   * @return the parent site
   */
  Site getSite();

  /**
   * Returns the encoded url to be used when calling the url through the web
   * application. <br>
   * What this method does in detail is prepending the mount point of the
   * web application and the servlet path obtained via <code>Env.getURI()</code>
   * and <code>getServletPath()</code> to the url.
   * 
   * @return the encoded url
   */
  String getLink();

  /**
   * Returns the encoded url to be used when calling the url through the web
   * application. <br>
   * What this method does in detail is prepending the mount point of the
   * web application and the servlet path obtained via <code>Env.getURI()</code>
   * and <code>getServletPath()</code> to the url.
   * <p>
   * The parameter version is used to create links to special versions of a
   * given page. Possible values are:
   * <ul>
   * <li>live</li>
   * <li>work</li>
   * </ul>
   * 
   * @param version
   *          the requested version
   * @return the encoded url
   */
  String getLink(String version);

  /**
   * Returns the version of this url. Possible versions are:
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.page.Page#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#WORK}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#ORIGINAL}</li>
   * </ul>
   * 
   * @return the url version
   */
  long getVersion();

  /**
   * Returns the url flavor. For example, in case of "index.xml" the flavor will
   * be <code>XML</code>.
   * 
   * @return the url flavor
   */
  String getFlavor();

}