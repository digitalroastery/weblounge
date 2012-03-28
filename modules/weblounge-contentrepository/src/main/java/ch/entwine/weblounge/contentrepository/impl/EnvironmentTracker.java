/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.site.Environment;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>EnvironmentTracker</code> watches instances of {@link Environment}
 * in the OSGi registry.
 */
public class EnvironmentTracker extends ServiceTracker {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(EnvironmentTracker.class);

  /** The environment */
  protected Environment environment = null;

  /** The content repository */
  protected AbstractContentRepository contentRepository = null;

  /**
   * Creates a new tracker for {@link Environment} instances.
   * 
   * @param context
   *          the bundle context
   * @param contentRepository
   *          content repository
   */
  EnvironmentTracker(BundleContext context,
      AbstractContentRepository contentRepository) {
    super(context, Environment.class.getName(), null);
    this.contentRepository = contentRepository;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    environment = (Environment) context.getService(reference);
    contentRepository.setEnvironment(environment);
    logger.trace("Found environment '{}'", environment);
    return environment;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    Environment environment = (Environment) service;
    logger.trace("Environment '{}' went away", environment);
    this.environment = null;
    if (reference.getBundle() != null) {
      super.removedService(reference, service);
    }
  }

  /**
   * Returns the response cache or <code>null</code> if no cache is currently
   * registered.
   * 
   * @return the cache
   */
  public Environment getEnvironment() {
    return environment;
  }

}
