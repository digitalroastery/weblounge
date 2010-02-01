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

import ch.o2it.weblounge.common.site.JobTrigger;

/**
 * This trigger will fire a job periodically. The next period starts when the
 * job's last execution finished.
 */
public class PeriodicJobTrigger implements JobTrigger {

  /** The next execution time */
  private long nextExecution = -1;

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
    if (period <= 1)
      throw new IllegalArgumentException("Period needs to be a positive integer");
    this.period = period;
    this.nextExecution = System.currentTimeMillis() + period;
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
    if (period <= 1)
      throw new IllegalArgumentException("Period needs to be a positive integer");
    if (startTime < System.currentTimeMillis())
      throw new IllegalArgumentException("Start time must be in the future");
    this.period = period;
    this.nextExecution = startTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.JobTrigger#getNextExecution()
   */
  public long getNextExecution() {
    long now = System.currentTimeMillis();
    while (nextExecution < now) {
      nextExecution += period;
    }
    return nextExecution;
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

}
