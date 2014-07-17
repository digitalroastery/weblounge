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

package ch.entwine.weblounge.kernel.security;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is in charge of installing a {@link java.lang.Security} manager
 * that implements access restrictions with regards to Weblounge resources.
 */
public final class SecurityServiceImpl {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

  /** The default implementation of the security manager */
  private SecurityManager defaultSecurityManager = null;

  /**
   * Callback for OSGi upon component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    logger.info("Activating default security manager of type {}", WebloungeSecurityManager.class.getName());
    defaultSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new WebloungeSecurityManager(defaultSecurityManager));
  }

  /**
   * Callback for OSGi upon component inactivation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    if (defaultSecurityManager != null)
      logger.info("Resetting to system security manager of type {}", defaultSecurityManager.getClass().getName());
    else
      logger.info("Removing Weblounge system security manager");
    System.setSecurityManager(defaultSecurityManager);
  }

  /**
   * Sets the security manager, overwriting the default security manager of type
   * {@link WebloungeSecurityManager}.
   * 
   * @param securityManager
   *          the security manager
   */
  void setSecurityManager(SecurityManager securityManager) {
    logger.info("Activating security manager of type ", securityManager.getClass().getName());
    System.setSecurityManager(securityManager);
  }

  /**
   * Removes the security manager and installs the default security manager of
   * type {@link WebloungeSecurityManager}.
   * 
   * @param securityManager
   *          the security manager
   */
  void removeSecurityManager(SecurityManager securityManager) {
    logger.info("Removing security manager of type ", securityManager.getClass().getName());
    logger.info("Activating default security manager of type ", WebloungeSecurityManager.class.getName());
    System.setSecurityManager(defaultSecurityManager);
  }

}
