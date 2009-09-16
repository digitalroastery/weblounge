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

import ch.o2it.weblounge.common.site.Job;

/**
 * Describes a Cron Job that can be registered with the Cron Daemon for periodic
 * execution.
 * 
 * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */

public abstract class AbstractJob implements Job {

  /** constant used for jobs that start immediately */
  public final static long NOW = 0;

  /** constant used for jobs that don't want to be executed */
  public final static long NEVER = -1;

  /** used to allocate unique ids */
  private static int sid = 0;

  /** the unique job id */
  Integer id = new Integer(++sid);

  /** the name of the job */
  protected String name = null;

  /** the job description */
  protected String description = null;

  /**
   * Sets the job name.
   * 
   * @param name
   *          the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the job identifier.
   * 
   * @return the job identifier
   */
  public String getIdentifier() {
    return id.toString();
  }

  /**
   * Returns the job name.
   * 
   * @return the job name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the job description.
   * 
   * @param description
   *          the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns a job description or <code>null</code> if no description is
   * available.
   * 
   * @return the job description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the start time of the job in milliseconds or <code>NOW</code> for
   * jobs that start immediately. Jobs that don't want to get scheduled again
   * should return <code>CANCEL</code>.
   * 
   * @return the start time of the job
   */
  public abstract long getNextExecution();

  /**
   * This method is called everytime the job is executed.
   */
  public abstract void work();

  /**
   * Returns the string representation of this job, which is equal to the value
   * returned by <code>getName()</code>.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }

}
