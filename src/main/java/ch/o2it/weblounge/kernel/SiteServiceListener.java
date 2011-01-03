/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.kernel;

import ch.o2it.weblounge.common.site.Site;

import org.osgi.framework.ServiceReference;

/**
 * Site service listener informs implementers about appearing and disappearing
 * site services.
 * <p>
 * Use
 * {@link SiteManager#addSiteBundleListener(ch.o2it.weblounge.common.site.SiteListener)}
 * and
 * {@link SiteManager#removeSiteBundleListener(ch.o2it.weblounge.common.site.SiteListener)}
 * to register to and unregister from these kinds of events.
 */
public interface SiteServiceListener {

  /**
   * Informs listeners about a new site, located in the given bundle. Note that
   * upon this callback, the site has not yet been started.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the site's service reference
   */
  void siteAppeared(Site site, ServiceReference reference);

  /**
   * Informs listeners about a site that has been shut down. Note that upon this
   * callback, the site has not yet been shut down.
   * 
   * @param site
   *          the site
   */
  void siteDisappeared(Site site);

}
