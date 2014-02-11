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

package ch.entwine.weblounge.common.scheduler;

import java.util.Date;

/**
 * A job trigger indicates when a job is fired. A job associated with a job
 * trigger while be kept scheduled as long as
 * {@link #getNextExecutionAfter(Date)} returns a valid date or <code>0</code>,
 * in which case the job will be fired once only.
 */
public interface JobTrigger extends Cloneable {

  /**
   * Returns the next execution date and time. If the job should no longer be
   * executed, this method should return <code>null</code>. If it should be
   * executed as soon as possible, <code>date</code> should be returned.
   * 
   * @param date
   *          the earliest possible execution date
   * @return the next execution date and time
   */
  Date getNextExecutionAfter(Date date);

  /**
   * Callback by the scheduler indicating that the trigger has been fired on
   * <code>date</code>.
   * <p>
   * Note that the callback doesn't imply anything regarding the success or
   * failure of the execution of the job that was triggered.
   * 
   * @param date
   *          the firing date
   */
  void triggered(Date date);

  /**
   * Resets this trigger.
   */
  void reset();

  /**
   * Creates a clone of this trigger.
   * 
   * @see java.lang.Object#clone()
   */
  Object clone() throws CloneNotSupportedException;

}
