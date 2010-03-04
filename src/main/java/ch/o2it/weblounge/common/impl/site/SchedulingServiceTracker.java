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

package ch.o2it.weblounge.common.impl.site;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>SchedulingServiceTracker</code> watches OSGi services that
 * implement the {@link Quartz} interface and registers and unregisters a site
 * with the first service implementation to come.
 */
public final class SchedulingServiceTracker extends ServiceTracker {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(SchedulingServiceTracker.class);

  /** The tracking site */
  private SiteImpl site = null;

  /**
   * Creates a new <code>SchedulingServiceTracker</code> that will, upon an
   * appearing <code>SchedulingService</code> implementation, register
   * the scheduling service with the site.
   * 
   * @param context
   *          the bundle context
   * @param site
   *          the site
   */
  SchedulingServiceTracker(BundleContext context, SiteImpl site) {
    super(context, Scheduler.class.getName(), null);
    this.site = site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    Scheduler scheduler = (Scheduler) context.getService(reference);
    log_.debug("Registering site {} with {}", site, scheduler.getClass().getName());
    site.setScheduler(scheduler);
    log_.debug("Registered {} with site {}", scheduler, site);
    return scheduler;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#modifiedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void modifiedService(ServiceReference reference, Object service) {
    log_.info("Job scheduler was modified");
    super.modifiedService(reference, service);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    log_.debug("Job scheduler disabled for site {}", site);
    site.removeScheduler();
    super.removedService(reference, service);
  }

}
