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

import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.site.Site;

/**
 * A web url represents a url that is used to address locations within the web
 * application such as pages, binary resources or module actions. It not only
 * contains information about the path but also the version of the page or
 * resource it is pointing to as well as the flavor.
 * <p>
 * Following is a list of supported urls:
 * <ul>
 * <li><code>http://www.example.org/</code></li>
 * <li><code>http://www.example.org/test</code></li>
 * <li><code>http://www.example.org/test/index.(html|json|xml)</code></li>
 * <li><code>http://www.example.org/test/index_de.(html|json|xml)</code></li>
 * <li><code>http://www.example.org/test/127.(html|json|xml)</code></li>
 * </ul>
 */
public interface WebUrl extends Url {

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the encoded url.
   * 
   * @return the encoded url
   */
  String getLink();

  /**
   * Returns a link to the indicated version of this url.
   * 
   * @param version
   *          the requested version
   * @return the encoded url
   * @see #getVersion()
   */
  String getLink(long version);

  /**
   * Returns a link to the indicated flavored version of this url.
   * 
   * @param version
   *          the requested version
   * @param flavor
   *          the requested flavor
   * @return the encoded url
   */
  String getLink(long version, String flavor);

  /**
   * Returns a link to the indicated flavored live version of this url.
   * 
   * @param flavor
   *          the requested flavor
   * @return the encoded url
   */
  String getLink(String flavor);

  /**
   * Returns the version of this url. Possible versions are:
   * <ul>
   * <li>{@link ch.o2it.weblounge.common.page.Page#LIVE}</li>
   * <li>{@link ch.o2it.weblounge.common.page.Page#WORK}</li>
   * </ul>
   * 
   * @return the url version
   */
  long getVersion();

  /**
   * Returns the url flavor. For example, in case of "index.xml" the flavor will
   * be <code>XML</code>.
   * <p>
   * It is good practice to use the enumeration at
   * {@link RequestFlavor} when passing around and comparing flavors.
   * 
   * @return the url flavor
   */
  String getFlavor();

}