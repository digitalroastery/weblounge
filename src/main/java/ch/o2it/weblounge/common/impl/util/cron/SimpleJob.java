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

/**
 * A basic a-periodic job that can be scheduled by the cron daemon. Simply
 * create a subclass that implements the <code>run()</code> method and schedule
 * it with the daemon using its <code>addJob()</code> method.
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 * @see Daemon#addJob(AbstractJob)
 */
public abstract class SimpleJob extends AbstractJob {

  /** the start time of the job */
  protected long nextExecution = NOW;

  /**
   * Creates a new <code>SimpleJob</code> with the given name. The job starts
   * immediately.
   * 
   * @param name
   *          the name of the job
   */
  public SimpleJob(String name) {
    this(name, NOW);
  }

  /**
   * Creates a new <code>SimpleJob</code> with the given name. The job starts
   * execution at the given time.
   * 
   * @param name
   *          the name of the job
   * @param startTime
   *          the start time of the job in [ms]
   */
  public SimpleJob(String name, long startTime) {
    this(name, "", startTime);
  }

  /**
   * Creates a new <code>SimpleJob</code> with the given name and description.
   * The job starts execution at the given time.
   * 
   * @param name
   *          the name of the job
   * @param description
   *          the job description
   * @param startTime
   *          the start time of the job in [ms]
   */
  public SimpleJob(String name, String description, long startTime) {
    if (name == null)
      throw new IllegalArgumentException("Job name must not be null!");
    this.name = name;
    this.description = description;
    this.nextExecution = startTime;
  }

  /**
   * @see AbstractJob#getNextExecution()
   */
  public long getNextExecution() {
    return nextExecution;
  }

}