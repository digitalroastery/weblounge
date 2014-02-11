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

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.scheduler.JobTrigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This trigger makes sure the job is triggered at the next possible moment.
 * After this one execution, the job will not be executed again.
 */
public class FireOnceJobTrigger implements JobTrigger {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FireOnceJobTrigger.class);

  /** First date that the trigger was asked for */
  protected Date fireDate = null;

  /** Has this trigger been fired? */
  protected boolean fired = false;

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (fired) {
      logger.debug("Fired one-time job trigger {} was asked for additional fire dates", this);
      return null;
    } else if (fireDate != null && !fireDate.equals(date)) {
      logger.warn("One-time job trigger {} was asked for additional fire dates", this);
      return null;
    }
    fireDate = new Date(date.getTime() + 1 * Times.MS_PER_SECOND);
    return date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.scheduler.JobTrigger#triggered(java.util.Date)
   */
  public void triggered(Date date) {
    if (fired)
      throw new IllegalStateException("This trigger should be fired once only");
    fired = true;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.scheduler.JobTrigger#reset()
   */
  public void reset() {
    fireDate = null;
    fired = false;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Fire once job trigger";
  }

}
