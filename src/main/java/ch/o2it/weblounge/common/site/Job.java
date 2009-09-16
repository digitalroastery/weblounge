/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

/**
 * Describes a cron job that can be registered with the cron daemon for periodic
 * execution.
 */
public interface Job {

  /** Constant used for jobs that start immediately */
  final static long NOW = 0;

  /** Constant used for jobs that don't want to be executed */
  final static long NEVER = -1;

  /**
   * Sets the job name.
   * 
   * @param name
   *          the name
   */
  void setName(String name);

  /**
   * Returns the job identifier.
   * 
   * @return the job identifier
   */
  String getIdentifier();

  /**
   * Returns the job name.
   * 
   * @return the job name
   */
  String getName();

  /**
   * Sets the job description.
   * 
   * @param description
   *          the description
   */
  void setDescription(String description);

  /**
   * Returns a job description or <code>null</code> if no description is
   * available.
   * 
   * @return the job description
   */
  String getDescription();

  /**
   * Returns the start time of the job in milliseconds or <code>NOW</code> for
   * jobs that start immediately. Jobs that don't want to get scheduled again
   * should return <code>CANCEL</code>.
   * 
   * @return the start time of the job
   */
  long getNextExecution();

  /**
   * This method is called every time the job is executed.
   */
  void work();

  /**
   * Returns the string representation of this job, which is equal to the value
   * returned by <code>getName()</code>.
   * 
   * @see java.lang.Object#toString()
   */
  String toString();

}