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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.cache.CacheService;
import ch.o2it.weblounge.common.site.Site;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service factory that will return a cache for each (site) bundle.
 */
public class CacheServiceFactory implements ServiceFactory {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(CacheServiceFactory.class);

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
   *      org.osgi.framework.ServiceRegistration)
   */
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    Site site = (Site)registration.getReference().getProperty("site");
    if (site == null) {
      logger.warn("No site was passed with the service creation request");
      return null;
    }
    return new CacheServiceImpl(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle,
   *      org.osgi.framework.ServiceRegistration, java.lang.Object)
   */
  public void ungetService(Bundle bundle, ServiceRegistration registration,
      Object service) {
    CacheService cacheService = (CacheService) service;
    cacheService.shutdown();
  }

}
