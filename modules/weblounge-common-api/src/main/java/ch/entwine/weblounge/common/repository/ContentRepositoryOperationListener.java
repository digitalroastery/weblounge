/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.common.repository;

/**
 * This listener is called once the corresponding asynchronous content
 * repository operations have been executed.
 */
public interface ContentRepositoryOperationListener {

  /**
   * This method is called when the operation was executed successfully.
   * 
   * @param operation
   *          the operation
   */
  void executionSucceeded(ContentRepositoryOperation<?> operation);

  /**
   * This method is called when the resource update failed.
   * 
   * @param operation
   *          the operation
   * @param t
   *          the reason of failure
   */
  void executionFailed(ContentRepositoryOperation<?> operation, Throwable t);

}
