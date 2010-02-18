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

import java.util.Date;

/**
 * This trigger will fire a job periodically. The next period starts when the
 * job's last execution finished.
 */
public class PeriodicJobTrigger implements JobTrigger {

  /** The start time */
  private long startTime = -1;

  /** The end time */
  private long endTime = -1;

  /** The last execution time */
  private long lastExecution = -1;

  /** Number of times to fire */
  private long repeatCount = -1;

  /** Number of times the trigger has been fired */
  private long triggerCount = -1;

  /** The period in milliseconds */
  private long period = -1;

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts <code>period</code> milliseconds from now.
   * 
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period) throws IllegalArgumentException {
    this(period, System.currentTimeMillis(), -1);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job in milliseconds
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   * @throws IllegalArgumentException
   *           if the start time is in the past
   */
  public PeriodicJobTrigger(long period, long startTime)
      throws IllegalArgumentException {
    this(period, startTime, -1);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time and ends at or before
   * <code>endTime</code>. If <code>endTime</code> is set to <code>-1</code>,
   * this trigger will fire forever or until it has reached the repeat count
   * set using <code>setRepeatCount()</code>.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job in milliseconds
   * @param endTime
   *          the end time of the job in milliseconds
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   * @throws IllegalArgumentException
   *           if the start time is in the past
   */
  public PeriodicJobTrigger(long period, long startTime, long endTime)
      throws IllegalArgumentException {
    if (period <= 1)
      throw new IllegalArgumentException("Period needs to be a positive integer");
    if (startTime < System.currentTimeMillis())
      throw new IllegalArgumentException("Start time must be in the future");
    this.period = period;
    this.startTime = startTime;
    this.endTime = endTime;
    this.lastExecution = startTime - period;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (endTime != -1 && date.getTime() > endTime)
      return null;
    if (repeatCount != -1 && triggerCount >= repeatCount)
      return null;
    long now = Math.max(startTime, date.getTime());
    long nextExecution = lastExecution;
    while (nextExecution <= now) {
      nextExecution += period;
    }
    lastExecution = nextExecution;
    triggerCount ++;
    return new Date(nextExecution);
  }

  /**
   * Returns the period in milliseconds.
   * 
   * @return the period
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets the period for this trigger.
   * 
   * @param period
   *          the new period for this periodic job in milliseconds
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public void setPeriod(long period) {
    if (period <= 1)
      throw new IllegalArgumentException("Period needs to be a positive integer");
    this.period = period;
  }

  /**
   * Sets the number of times that this trigger will fire, given that it doesn't
   * reach the end time.
   * 
   * @param repeatCount
   *          number of times to fire
   */
  public void setRepeatCount(long repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * Returns the number of times that this trigger will fire or <code>-1</code>
   * if it should fire indefinitely or until it reaches the end time.
   * 
   * @return the number of times to fire
   */
  public long getRepeatCount() {
    return repeatCount;
  }

}
