/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.util.process;

/**
 * This exception is thrown by the {@link ProcessExecutor} and indicates that
 * the process failed.
 */
public class ProcessExcecutorException extends Exception {

  /** Serial version uid */
  private static final long serialVersionUID = -9194578448624904231L;

  /**
   * Creates a new exception.
   * 
   * @param msg
   *          the error message
   * @param cause
   *          the original cause
   */
  public ProcessExcecutorException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
