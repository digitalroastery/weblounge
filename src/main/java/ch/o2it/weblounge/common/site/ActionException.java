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
 * A <code>ActionException</code> is thrown if an exceptional state is reached
 * when executing an <code>Action</code> to create the output for either a page
 * or a single page element.
 */
public class ActionException extends Exception {

  /** Serial version id */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new <code>ActionException</code> with the given error message.
   * 
   * @param message
   *          the error message
   */
  public ActionException(String message) {
    super(message);
  }

  /**
   * Creates a new <code>ActionException</code> originating from
   * <code>cause</code>.
   * 
   * @param cause
   *          the original error
   */
  public ActionException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new <code>ActionException</code> with the given error message and
   * the indicated original reason.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the original error
   */
  public ActionException(String message, Throwable cause) {
    super(message, cause);
  }

}