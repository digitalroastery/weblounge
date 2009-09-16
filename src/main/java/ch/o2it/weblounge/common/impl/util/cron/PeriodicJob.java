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
 * A periodic job that can be scheduled the cron daemon. Jobs implementing this
 * interface will be asked by the cron daemon repeatedly for its next execution
 * time.
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 * @see Daemon#addJob(AbstractJob)
 */

public abstract class PeriodicJob extends AbstractJob {

  /**
   * Returns <code>true</code> if the job may be suspended. For jobs that are,
   * the user interface may provide means to suspend or even stop job execution.
   * 
   * @return <code>true</code> if the job may be suspended
   */
  abstract boolean isSuspendable();

}