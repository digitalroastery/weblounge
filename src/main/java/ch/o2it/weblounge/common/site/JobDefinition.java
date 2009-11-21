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
 * Definition for jobs within weblounge.
 * 
 * TODO: The interface currently relies on quartz, which is bad
 */
public interface JobDefinition {

  /**
   * Returns the {@link JobDetail} that holds the jobs name, the scheduler group
   * as well as the job implementation. 
   * 
   * @return the job
   */
  // JobDetail getJob();
  
  /**
   * Returns the trigger that determines when the job should be executed.
   * 
   * @return the trigger
   */
  // Trigger getTrigger();
  
}