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

package ch.o2it.weblounge.contentrepository;

import ch.o2it.weblounge.common.site.Site;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the <code>ContentRepositoryFactory</code>. The
 * factory will create instances of type <code>ContentRepository</code>
 * according to the service configuration.
 * <p>
 * Using the key <code>ch.o2it.weblounge.contentrepository</code>, the concrete
 * implementation can be specified. If this property cannot be found, an
 * instance of the <code>BundleContentRepository</code> will be created which
 * will serve pages and resources from the site's bundle.
 */
public class ContentRepositoryFactory {

  /** The registered content repositories */
  private static Map<Site, ContentRepository> repositories = new HashMap<Site, ContentRepository>();

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepositoryFactory#newContentRepository(ch.o2it.weblounge.common.site.Site)
   */
  public static ContentRepository getRepository(Site site) {
    return repositories.get(site);
  }

  /**
   * Registers site and content repository with the factory.
   * 
   * @param site
   *          the site
   * @param repository
   *          the site's content repository
   */
  static void register(Site site, ContentRepository repository) {
    repositories.put(site, repository);
  }

  /**
   * Unregisters the site from the factory, returning the associated content
   * repository.
   * 
   * @param site
   *          the site
   */
  static ContentRepository unregister(Site site) {
    return repositories.remove(site);
  }

}
