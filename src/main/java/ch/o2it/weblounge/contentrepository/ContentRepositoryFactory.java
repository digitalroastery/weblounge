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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryFactory.class);

  /** The content repository service */
  private static ContentRepositoryService repositoryService = null;

  /**
   * This method is used to register the factory with a backing service
   * implementation.
   * 
   * @param service
   *          the content repository service
   */
  public void setContentRepositoryService(ContentRepositoryService service) {
    repositoryService = service;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.repository.ContentRepositoryFactory#newContentRepository(ch.o2it.weblounge.common.site.Site)
   */
  public static ContentRepository getRepository(Site site) {
    if (repositoryService == null) {
      logger.warn("Tried to access content repository without a backing service being configured");
      return null;
    }
    return repositoryService.getRepository(site);
  }

}
