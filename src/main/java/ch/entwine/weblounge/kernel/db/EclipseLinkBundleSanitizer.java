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

package ch.entwine.weblounge.kernel.db;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for bundles using JPA (defined by the existence of
 * <code>META-INF/persistence.xml</code>), and refreshes the persistence
 * provider bundle when they are updated.
 * <p>
 * This service is needed since the EclipseLink implementation of JPA does not
 * behave well when bundles using it reload.
 */
public class EclipseLinkBundleSanitizer implements BundleListener {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(EclipseLinkBundleSanitizer.class);

  /** The bundle context */
  protected BundleContext bundleContext = null;

  /**
   * Callback from OSGi that is executed on service activation.
   * 
   * @param bundleContext
   *          the bundle context
   */
  void activate(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
    bundleContext.addBundleListener(this);
  }

  /**
   * Callback from OSGi that is executed on service deactivation.
   */
  void deactivate() {
    bundleContext.removeBundleListener(this);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
   */
  public void bundleChanged(BundleEvent event) {
    if (event.getType() != BundleEvent.UPDATED)
      return;

    // Make sure the updated bundle is using JPA
    Bundle bundle = event.getBundle();
    if (bundle.getEntry("/META-INF/persistence.xml") == null) {
      logger.debug("Updated bundle {} is not using JPA", bundle.getSymbolicName());
      return;
    }

    // If there is no persistence provider, there is no need to do anything
    ServiceReference jpaProviderRef = bundleContext.getServiceReference("javax.persistence.spi.PersistenceProvider");
    if (jpaProviderRef == null) {
      logger.debug("No persistence provider found");
      return;
    }

    // Refresh the persistence provider bundle
    Bundle jpaBundle = jpaProviderRef.getBundle();
    try {
      jpaBundle.update();
      logger.info("Updated the JPA provider bundle {}", jpaBundle.getSymbolicName());
    } catch (BundleException e) {
      logger.info("Failed to update the JPA provider bundle: {}", e);
    }
  }

}
