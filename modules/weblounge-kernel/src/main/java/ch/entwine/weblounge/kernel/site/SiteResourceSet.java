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

package ch.entwine.weblounge.kernel.site;

import ch.entwine.weblounge.common.site.Site;

/**
 * This resource set defines which parts of a site can be accessed from the
 * outside and which parts are protected.
 */
public class SiteResourceSet extends ResourceSet {

  /**
   * Creates a new site resource set.
   * 
   * @param site
   *          the site
   */
  public SiteResourceSet(Site site) {
    super("^/");
    exclude("^/(?:classes|conf|doc|i18n|lib)/");
    exclude("^/site.xml");
    if (site.getSecurity() != null) {
      String configPath = site.getSecurity().toExternalForm();
      if (configPath.startsWith("file://${site.root}")) {
        exclude(configPath.substring(19));
      } else if (configPath.startsWith("file://${bundle.root}/site")) {
        exclude(configPath.substring(26));
      }
    }
  }

}
