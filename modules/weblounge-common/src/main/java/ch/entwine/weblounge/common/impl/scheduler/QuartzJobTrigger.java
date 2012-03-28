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

package ch.entwine.weblounge.common.impl.scheduler;

import ch.entwine.weblounge.common.scheduler.JobTrigger;

import org.quartz.Calendar;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * The <code>QuartzJobTrigger</code> wraps the Weblounge <code>JobTrigger</code>
 * to be used by the quartz scheduler.
 */
public final class QuartzJobTrigger extends Trigger {

  /** Serial version uid */
  private static final long serialVersionUID = 3873501563763335486L;

  /**
   * <p>
   * Instructs the <code>{@link org.quartz.Scheduler}</code> that upon a
   * mis-fire situation, the <code>{@link QuartzJobTrigger}</code> wants to be
   * fired now by <code>Scheduler</code>.
   * </p>
   * 
   * <p>
   * <i>NOTE:</i> This instruction should typically only be used for 'one-shot'
   * (non-repeating) Triggers. If it is used on a trigger with a repeat count >
   * 0 then it is equivalent to the instruction
   * <code>{@link #MISFIRE_INSTRUCTION_RESCHEDULE}
   * </code>.
   * </p>
   */
  public static final int MISFIRE_INSTRUCTION_FIRE_NOW = 1;

  /**
   * <p>
   * Instructs the <code>{@link org.quartz.Scheduler}</code> that upon a
   * mis-fire situation, the <code>{@link QuartzJobTrigger}</code> wants to be
   * re-scheduled. This does obey the <code>Trigger</code> end-time however, so
   * if the next fire time is after the end-time the <code>Trigger</code> will
   * not fire again.
   * </p>
   * 
   * <p>
   * <i>NOTE:</i> This instruction could cause the <code>Trigger</code> to go to
   * the 'COMPLETE' state after rescheduling, if all the repeat-fire-times where
   * missed.
   * </p>
   */
  public static final int MISFIRE_INSTRUCTION_RESCHEDULE = 2;

  /** Do not schedule past this year (used to prevent endless loops) */
  private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = 2299;

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(QuartzJobTrigger.class);

  /** Weblounge job trigger */
  private JobTrigger trigger = null;

  /** True if the trigger may fire multiple times */
  private boolean mayFireAgain = true;

  /** Start date */
  private Date startTime = null;

  /** End date */
  private Date endTime = null;

  /** Time when the trigger should be fired next */
  private Date nextFireTime = null;

  /** Time when the trigger was last fired */
  private Date previousFireTime = null;

  /** Number of times that this trigger has been fired */
  private int timesTriggered = 0;

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
    this.trigger = trigger;

    // Initialize the settings
    Date now = new Date();
    startTime = trigger.getNextExecutionAfter(now);
    mayFireAgain = startTime != null || trigger.getNextExecutionAfter(startTime) != null;
  }

  /**
   * Returns the weblounge trigger.
   * 
   * @return the trigger
   */
  JobTrigger getOriginalTrigger() {
    return trigger;
  }

  /**
   * Sets the next fire time.
   * 
   * @param date
   *          the date
   */
  public void setNextFireTime(Date date) {
    this.nextFireTime = date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.QuartzJobTrigger#getNextFireTime()
   */
  @Override
  public Date getNextFireTime() {
    return nextFireTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#computeFirstFireTime(org.quartz.Calendar)
   */
  @Override
  public Date computeFirstFireTime(Calendar calendar) {
    Date firstFireTime = getStartTime();

    while (firstFireTime != null && calendar != null && !calendar.isTimeIncluded(firstFireTime.getTime())) {
      firstFireTime = getFireTimeAfter(firstFireTime);

      if (firstFireTime == null)
        break;

      // avoid infinite loop
      java.util.Calendar c = java.util.Calendar.getInstance();
      c.setTime(firstFireTime);
      if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
        return null;
      }
    }

    nextFireTime = (mayFireAgain) ? firstFireTime : null;
    return firstFireTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#executionComplete(org.quartz.JobExecutionContext,
   *      org.quartz.JobExecutionException)
   */
  @Override
  public int executionComplete(JobExecutionContext context,
      JobExecutionException result) {
    if (result != null && result.refireImmediately()) {
      return INSTRUCTION_RE_EXECUTE_JOB;
    }

    if (result != null && result.unscheduleFiringTrigger()) {
      return INSTRUCTION_SET_TRIGGER_COMPLETE;
    }

    if (result != null && result.unscheduleAllTriggers()) {
      return INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE;
    }

    if (!mayFireAgain()) {
      return INSTRUCTION_DELETE_TRIGGER;
    }

    return INSTRUCTION_NOOP;
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
    try {
      return trigger.getNextExecutionAfter(date);
    } catch (Throwable t) {
      logger.error("Job implementation threw exception when asked for next trigger date", t);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#getPreviousFireTime()
   */
  @Override
  public Date getPreviousFireTime() {
    return previousFireTime;
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
    return mayFireAgain && getNextFireTime() != null;
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
  public void triggered(Calendar calendar) {
    try {
      timesTriggered++;
      previousFireTime = nextFireTime;
      trigger.triggered(previousFireTime);
    } catch (Throwable t) {
      logger.error("Job implementation threw exception on trigger callback", t);
    } finally {
      nextFireTime = getFireTimeAfter(nextFireTime);
    }

    // Make sure the next fire time is not explicitly excluded
    while (nextFireTime != null && calendar != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

      nextFireTime = getFireTimeAfter(nextFireTime);
      if (nextFireTime == null)
        break;

      // avoid infinite loop
      java.util.Calendar c = java.util.Calendar.getInstance();
      c.setTime(nextFireTime);
      if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
        nextFireTime = null;
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#updateAfterMisfire(org.quartz.Calendar)
   */
  @Override
  public void updateAfterMisfire(Calendar calendar) {
    int instr = getMisfireInstruction();
    if (instr == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY) {
      if (!mayFireAgain) {
        instr = MISFIRE_INSTRUCTION_FIRE_NOW;
      } else {
        instr = MISFIRE_INSTRUCTION_RESCHEDULE;
      }
    } else if (instr == MISFIRE_INSTRUCTION_FIRE_NOW && mayFireAgain) {
      instr = MISFIRE_INSTRUCTION_RESCHEDULE;
    }

    // Reschedule?
    if (instr == MISFIRE_INSTRUCTION_FIRE_NOW) {
      setNextFireTime(new Date());
    } else if (instr == MISFIRE_INSTRUCTION_RESCHEDULE) {
      Date newFireTime = getFireTimeAfter(new Date());
      while (newFireTime != null && calendar != null && !calendar.isTimeIncluded(newFireTime.getTime())) {
        newFireTime = getFireTimeAfter(newFireTime);

        if (newFireTime == null)
          break;

        // avoid infinite loop
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(newFireTime);
        if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
          newFireTime = null;
        }
      }
      setNextFireTime(newFireTime);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#updateWithNewCalendar(org.quartz.Calendar, long)
   */
  @Override
  public void updateWithNewCalendar(Calendar calendar, long misfireThreshold) {
    nextFireTime = getFireTimeAfter(previousFireTime);

    if (nextFireTime == null || calendar == null) {
      return;
    }

    Date now = new Date();
    while (nextFireTime != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

      nextFireTime = getFireTimeAfter(nextFireTime);

      if (nextFireTime == null)
        break;

      // avoid infinite loop
      java.util.Calendar c = java.util.Calendar.getInstance();
      c.setTime(nextFireTime);
      if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
        nextFireTime = null;
      }

      if (nextFireTime != null && nextFireTime.before(now)) {
        long diff = now.getTime() - nextFireTime.getTime();
        if (diff >= misfireThreshold) {
          nextFireTime = getFireTimeAfter(nextFireTime);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Trigger#validateMisfireInstruction(int)
   */
  @Override
  protected boolean validateMisfireInstruction(int misfireInstruction) {
    if (misfireInstruction < MISFIRE_INSTRUCTION_SMART_POLICY) {
      return false;
    } else if (misfireInstruction > MISFIRE_INSTRUCTION_RESCHEDULE) {
      return false;
    }
    return true;
  }

  /**
   * Get the number of times the <code>QuartzJobTrigger</code> has already
   * fired.
   * 
   * @return the number of times that this trigger has been fired
   */
  public int getTimesTriggered() {
    return timesTriggered;
  }

}
