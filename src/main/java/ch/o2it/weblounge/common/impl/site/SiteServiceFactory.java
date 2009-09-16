/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.site.SiteService;
import ch.o2it.weblounge.site.impl.SiteServiceImpl;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Comment SiteServiceFactory
 */
public class SiteServiceFactory implements ManagedServiceFactory {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(SiteServiceFactory.class);

  /** List of created site services */
  private Map<String, SiteService> siteServices = null;
  
  /**
   * Creates a new factory for site services.
   */
  public SiteServiceFactory() {
    siteServices = new HashMap<String, SiteService>();
    log_.info("Creating new site service factory");
  }
  
  /**
   * {@inheritDoc}
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  public void deleted(String pid) {
    SiteService service = (SiteService) siteServices.remove(pid);
    service.stop();
  }

  /**
   * {@inheritDoc}
   * @see org.osgi.service.cm.ManagedServiceFactory#getName()
   */
  public String getName() {
    return "ch.o2it.weblounge.siteservicefactory";
  }

  /**
   * {@inheritDoc}
   * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
   */
  public void updated(String pid, Dictionary properties) throws ConfigurationException {

    //updating the configuration of the pid service
    if (siteServices.containsKey(pid)) {
      log_.info("Updating configuration for site " + pid);
      SiteService service = (SiteService) siteServices.get(pid);
      service.stop();
      service.setLoginEnabled(!service.isLoginEnabled());
      service.setIdentifier(pid);
      service.start();
    }

    //new configuration
    else {
      String siteId = (String)properties.get("id");
      log_.info("Creating new site " + siteId);
      SiteService service = new SiteServiceImpl();
      service.setIdentifier(siteId);
      service.start();
      siteServices.put(pid, service);
    }
 
  }

}