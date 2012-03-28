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

package ch.entwine.weblounge.kernel;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for the commons bundle, which will set up common services
 * like a Quartz scheduler.
 */
public class WebloungeKernel {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeKernel.class);

  /** The weblounge scheduler */
  private Scheduler scheduler = null;

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    logger.info("Starting common weblounge services", this);

    // Start and register the quartz scheduler
    try {
      logger.info("Starting job scheduler");
      StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
      scheduler = schedulerFactory.getScheduler();
      scheduler.start();
      bundleContext.registerService(Scheduler.class.getName(), scheduler, null);
    } catch (SchedulerException e) {
      logger.error("Error starting job scheduler: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    logger.info("Stopping common weblounge services", this);
    scheduler.shutdown();
    logger.info("Job scheduler stopped");
  }

}
