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

  /** Number of times to fire */
  private long repeatCount = -1;

  /** Number of times the trigger has been fired */
  private long triggerCount = 0;

  /** The period in milliseconds */
  private long period = -1;

  /** Flag indicating whether to wait a period for the first execution */
  private boolean startImmediately = false;
  
  /** True if this is the first execution */
  private boolean firstExecution = true;

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
    this(period, new Date(), null, false);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts <code>period</code> milliseconds from now.
   * 
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startImmediately
   *          <code>true</code> if the first execution should be right now
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, boolean startImmediately)
      throws IllegalArgumentException {
    this(period, new Date(), null, startImmediately);
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
   *          the start time of the job
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime)
      throws IllegalArgumentException {
    this(period, startTime, null, -1, false);
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
   *          the start time of the job
   * @param startImmediately
   *          <code>true</code> if the first execution should be right now
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime,
      boolean startImmediately) throws IllegalArgumentException {
    this(period, startTime, null, -1, startImmediately);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time and ends at or before
   * <code>endTime</code>. If <code>endTime</code> is set to <code>null</code>,
   * this trigger will fire forever or until it has reached the repeat count set
   * using <code>setRepeatCount()</code>.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job
   * @param endTime
   *          the end time of the job
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime, Date endTime)
      throws IllegalArgumentException {
    this(period, startTime, endTime, -1, false);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time and ends at or before
   * <code>endTime</code>. If <code>endTime</code> is set to <code>null</code>,
   * this trigger will fire forever or until it has reached the repeat count set
   * using <code>setRepeatCount()</code>.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job
   * @param endTime
   *          the end time of the job
   * @param startImmediately
   *          <code>true</code> if the first execution should be right now
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime, Date endTime,
      boolean startImmediately) throws IllegalArgumentException {
    this(period, startTime, endTime, -1, startImmediately);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time and ends at or before
   * <code>endTime</code>. If <code>endTime</code> is set to <code>null</code>,
   * this trigger will fire forever or until it has reached the repeat count set
   * using <code>setRepeatCount()</code>.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job
   * @param endTime
   *          the end time of the job
   * @param repeat
   *          number of times that this trigger should be executed
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime, Date endTime,
      long repeat) throws IllegalArgumentException {
    this(period, startTime, endTime, repeat, false);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time and ends at or before
   * <code>endTime</code>. If <code>endTime</code> is set to <code>null</code>,
   * this trigger will fire forever or until it has reached the repeat count set
   * using <code>setRepeatCount()</code>.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in milliseconds at which the job should be executed
   * @param startTime
   *          the start time of the job
   * @param endTime
   *          the end time of the job
   * @param repeat
   *          number of times that this trigger should be executed
   * @param startImmediately
   *          <code>true</code> if the first execution should be right now
   * @throws IllegalArgumentException
   *           if the period is smaller are equal to zero
   */
  public PeriodicJobTrigger(long period, Date startTime, Date endTime,
      long repeat, boolean startImmediately) throws IllegalArgumentException {
    if (period <= 1)
      throw new IllegalArgumentException("Period needs to be a positive integer");
    this.period = period;
    this.startTime = startTime != null ? startTime.getTime() : System.currentTimeMillis();
    this.endTime = endTime != null ? endTime.getTime() : -1;
    this.repeatCount = repeat;
    this.startImmediately = startImmediately;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (endTime != -1 && date.getTime() >= endTime)
      return null;
    if (repeatCount != -1 && triggerCount >= repeatCount)
      return null;
    if (date.getTime() < startTime)
      return new Date(startTime);

    long nextExecution = 0;
    long triggerCount = (long) Math.floor((date.getTime() - startTime) / period);
    if (triggerCount == 0 && startImmediately && firstExecution)
      nextExecution = startTime;
    else
      nextExecution = startTime + (triggerCount + 1) * period;
    firstExecution = false;
    return new Date(nextExecution);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#triggered(java.util.Date)
   */
  public void triggered(Date date) {
    triggerCount++;
    if (endTime > -1 && date.getTime() > date.getTime())
      throw new IllegalStateException("Trigger was not supposed to be fired after " + new Date(endTime));
    if (repeatCount > -1 && triggerCount > repeatCount)
      throw new IllegalStateException("Trigger was not supposed to called more than " + repeatCount + " times");
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

  /**
   * Returns <code>true</code> if the first execution is right now rather than
   * after the first period of time has passed.
   * 
   * @return the startImmediately <code>true</code> if the trigger fires right
   *         now for the first time
   */
  public boolean getStartsImmediately() {
    return startImmediately;
  }

  /**
   * Sets the first trigger execution to either <code>now</code> or
   * <code>now + period</code>.
   * 
   * @param startImmediately
   *          <code>true</code> to execute the trigger right now for the first
   *          time
   */
  public void setStartImmediately(boolean startImmediately) {
    this.startImmediately = startImmediately;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Periodic job trigger [period=" + period + " ms]";
  }

}
