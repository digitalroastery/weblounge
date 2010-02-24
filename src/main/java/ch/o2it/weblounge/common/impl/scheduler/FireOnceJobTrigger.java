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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This trigger makes sure the job is triggered at the next possible moment.
 * After this one execution, the job will not be executed again.
 */
public class FireOnceJobTrigger implements JobTrigger {
  
  Logger logger = LoggerFactory.getLogger(FireOnceJobTrigger.class);

  /** First date that the trigger was asked for */
  protected Date fireDate = null;

  /** Has this trigger been fired? */
  protected boolean fired = false;

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (fired) {
      logger.debug("Fired one-time job trigger {} was asked for additional fire dates", this);
      return null;
    } else if (fireDate != null) {
      logger.debug("One-time job trigger {} was asked for additional fire dates", this);
      return null;
    }
    fireDate = date;
    return date;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#triggered(java.util.Date)
   */
  public void triggered(Date date) {
    if (fired)
      throw new IllegalStateException("This trigger should be fired once only");
    fired = true;
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
