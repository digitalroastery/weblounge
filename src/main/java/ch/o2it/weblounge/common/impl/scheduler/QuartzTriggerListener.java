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

package ch.o2it.weblounge.common.impl.scheduler;

import ch.o2it.weblounge.common.site.Site;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This listener keeps track of the work that the Quartz scheduler is doing.
 */
public final class QuartzTriggerListener implements TriggerListener {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(QuartzTriggerListener.class);

  /** Date formatter */
  private static DateFormat df = new SimpleDateFormat();

  /** The site */
  protected Site site = null;

  /** The listener name */
  protected String name = null;

  /**
   * Creates a new trigger listener for the given site.
   * 
   * @param site
   *          the site
   */
  public QuartzTriggerListener(Site site) {
    if (site == null)
      throw new IllegalArgumentException("Site can't be null");
    this.site = site;
    this.name = "quartz site listener " + site.getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerComplete(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext, int)
   */
  public void triggerComplete(Trigger trigger, JobExecutionContext ctx, int a) {
    String jobName = ctx.getJobDetail().getName();
    Date nextExecution = ctx.getNextFireTime();
    if (nextExecution != null)
      log_.info("Job {} finished, next execution scheduled for ", new Object[] {
          jobName,
          df.format(nextExecution) });
    else
      log_.info("Job {} finished", jobName);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerFired(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext)
   */
  public void triggerFired(Trigger trigger, JobExecutionContext ctx) {
    String jobName = ctx.getJobDetail().getName();
    log_.debug("Executing job {} of site {}", jobName, site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerMisfired(org.quartz.Trigger)
   */
  public void triggerMisfired(Trigger trigger) {
    log_.error("Failed to fire job trigger {}", trigger.getName());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#vetoJobExecution(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext)
   */
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext ctx) {
    return site.isEnabled() && site.isRunning();
  }

}
