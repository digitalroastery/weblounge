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

package ch.o2it.weblounge.common.site;

/**
 * This interface defines the methods that have to be implemented by a
 * <code>SiteListener</code>. The listener will be informed about site relative
 * events like e. g. a site shutdown.
 */
public interface SiteListener {

  /**
   * Method used to inform listeners about a started site.
   * 
   * @param site
   *          the site that has started
   */
  void siteStarted(Site site);

  /**
   * Method used to inform listeners about a stopped site.
   * 
   * @param site
   *          the site that has been shut down
   */
  void siteStopped(Site site);

}