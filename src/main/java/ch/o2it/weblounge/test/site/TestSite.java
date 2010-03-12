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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger;
import ch.o2it.weblounge.common.impl.scheduler.PeriodicJobTrigger;
import ch.o2it.weblounge.common.impl.site.SiteImpl;

import org.osgi.service.component.ComponentContext;

/**
 * Implementation of the <code>Site</code> API that hosts the weblounge
 * integration test suite.
 */
public class TestSite extends SiteImpl {

  /** Serial version UID */
  private static final long serialVersionUID = -241760236588222514L;

  /**
   * Creates a new test site implementation.
   */
  public TestSite() {
    setAutoStart(true);
    addHostName("localhost");
    setDescription("Weblounge Test Site");
  }

  /**
   * Callback from the OSGi environment to activate the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   */
  @Override
  public void activate(ComponentContext context) throws Exception {
    super.activate(context);
    addJob("startup", SiteStartupJob.class, null, new CronJobTrigger("@restart"));
    addJob("greeter", GreeterJob.class, null, new PeriodicJobTrigger(60000));
    addModule(new TestModule());
  }

  /**
   * Callback from the OSGi environment to deactivate the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   */
  @Override
  public void deactivate(ComponentContext context) throws Exception {
    super.deactivate(context);
    removeJob("startup");
    removeJob("greeter");
  }

}
