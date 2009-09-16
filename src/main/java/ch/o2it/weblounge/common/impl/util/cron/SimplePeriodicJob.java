/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.impl.util.cron;

public abstract class SimplePeriodicJob extends PeriodicJob {

  /** true if the job may be suspended */
  protected boolean suspendable;

  /** the start time of the job */
  private long nextExecution_ = NOW;

  /** the period of the job */
  private long period_;

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts immediately.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in [ms] at which the job should be executed
   */
  public SimplePeriodicJob(String name, long period) {
    this(name, period, NOW);
  }

  /**
   * Creates a new <code>PeriodicJob</code> with the given name and period. The
   * job starts execution at the given time.
   * 
   * @param name
   *          the name of the job
   * @param period
   *          the period in [ms] at which the job should be executed
   * @param startTime
   *          the start time of the job in [ms]
   */
  public SimplePeriodicJob(String name, long period, long startTime) {
    if (name == null)
      throw new NullPointerException();
    this.name = name;
    this.period_ = period;
    this.nextExecution_ = startTime;
  }

  /**
   * @see AbstractJob#getNextExecution()
   */
  public final long getNextExecution() {
    long now = System.currentTimeMillis();
    while (nextExecution_ < now) {
      nextExecution_ += period_;
    }
    return nextExecution_;
  }

  /**
   * This implementation returns <code>true</code> which makes the job
   * suspendable.
   * 
   * @see ch.o2it.weblounge.common.impl.util.cron.PeriodicJob#isSuspendable()
   */
  public boolean isSuspendable() {
    return true;
  }

  /**
   * Sets the sunspendable flag of this job.
   * 
   * @param suspendable
   *          <code>true</code> to make the job suspendable
   */
  public void setSuspendable(boolean suspendable) {
    this.suspendable = suspendable;
  }

  /**
   * @see AbstractJob#getPeriod_()
   */
  public final long getPeriod_() {
    return period_;
  }

  /**
   * Sets the priod for this periodic job.
   * 
   * @param period
   *          the new period for this periodic job in [ms]
   */
  public final void setPeriod_(long period) {
    this.period_ = period;
  }

}