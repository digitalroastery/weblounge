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

import ch.o2it.weblounge.common.scheduler.JobTrigger;

import org.quartz.Calendar;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import java.util.Date;

/**
 * The <code>QuartzJobTrigger</code> wraps the Weblounge <code>JobTrigger</code>
 * to be used by the quartz scheduler.
 */
public class QuartzJobTrigger extends Trigger {

  /** Serial version uid */
  private static final long serialVersionUID = 3873501563763335486L;

  /** Weblounge job trigger */
  private JobTrigger trigger = null;

  /** True if the trigger may fire multiple times */
  boolean mayFireAgain = true;

  /** Start date */
  private Date startTime = null;

  /** End date */
  private Date endTime = null;
  
  /** Time when the trigger was last fired */
  private Date lastFireTime = null;

  /**
   * Creates a new trigger that wraps the weblounge job trigger to be used by
   * the quartz scheduler.
   * 
   * @param name
   *          the job name
   * @param group
   *          the quartz scheduler group
   * @param trigger
   *          the weblounge job trigger
   */
  public QuartzJobTrigger(String name, String group, JobTrigger trigger) {
    super(name, group);
    if (trigger == null)
      throw new IllegalArgumentException("Job trigger cannot be null");
    init(trigger);
  }

  /**
   * Initialized this trigger for use by the Quartz scheduler.
   * 
   * @param trigger
   *          the weblounge trigger
   */
  private void init(JobTrigger trigger) {
    this.trigger = trigger;
    if (trigger.getNextExecution() == 0) {
      startTime = new Date();
      endTime = startTime;
      mayFireAgain = false;
    } else {
      startTime = new Date(trigger.getNextExecution());
      endTime = null;
      mayFireAgain = true;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.SimpleTrigger#getNextFireTime()
   */
  @Override
  public Date getNextFireTime() {
    long nextExecution = trigger.getNextExecution();
    if (nextExecution == JobTrigger.NEVER) {
      return null;
    } else if (nextExecution == JobTrigger.ONCE) {
      return new Date();
    }
    return new Date(nextExecution);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#computeFirstFireTime(org.quartz.Calendar)
   */
  @Override
  public Date computeFirstFireTime(Calendar cal) {
    return getNextFireTime();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#executionComplete(org.quartz.JobExecutionContext,
   *      org.quartz.JobExecutionException)
   */
  @Override
  public int executionComplete(JobExecutionContext ctx, JobExecutionException e) {
    if (trigger.getNextExecution() <= 0)
      return INSTRUCTION_DELETE_TRIGGER ;
    else
      return INSTRUCTION_SET_TRIGGER_COMPLETE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getEndTime()
   */
  @Override
  public Date getEndTime() {
    return endTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getFinalFireTime()
   */
  @Override
  public Date getFinalFireTime() {
    return endTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getFireTimeAfter(java.util.Date)
   */
  @Override
  public Date getFireTimeAfter(Date date) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getPreviousFireTime()
   */
  @Override
  public Date getPreviousFireTime() {
    return lastFireTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getStartTime()
   */
  @Override
  public Date getStartTime() {
    return startTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#mayFireAgain()
   */
  @Override
  public boolean mayFireAgain() {
    return trigger.getNextExecution() > 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#setEndTime(java.util.Date)
   */
  @Override
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#setStartTime(java.util.Date)
   */
  @Override
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#triggered(org.quartz.Calendar)
   */
  @Override
  public void triggered(Calendar cal) {
    this.lastFireTime = new Date();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#updateAfterMisfire(org.quartz.Calendar)
   */
  @Override
  public void updateAfterMisfire(Calendar arg0) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#updateWithNewCalendar(org.quartz.Calendar, long)
   */
  @Override
  public void updateWithNewCalendar(Calendar arg0, long arg1) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#validateMisfireInstruction(int)
   */
  @Override
  protected boolean validateMisfireInstruction(int arg0) {
    // TODO Auto-generated method stub
    return false;
  }

}
