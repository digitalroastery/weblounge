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

/**
 * This trigger makes sure the job is triggered at the next possible moment.
 * After this one execution, the job will not be executed again.
 * 
 * TODO: How do we call back to this trigger telling it when it has been
 * fired once?
 */
public class FireOnceTrigger implements JobTrigger {
  
  /** Has this trigger been fired? */
  protected boolean fired = false;

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#getNextExecution()
   */
  public long getNextExecution() {
    return !fired ? ONCE : NEVER;
  }

}
