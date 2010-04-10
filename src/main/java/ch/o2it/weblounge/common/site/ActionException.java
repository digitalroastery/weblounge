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
 * <p>
 * If one of the constructors with a status code is chosen, then the action
 * handler that is executing the action will properly report that status to the
 * client.
 */
public class ActionException extends Exception {

  /** Serial version id */
  private static final long serialVersionUID = 1L;

  /** The http status code that the handler should send back to the client */
  private int statusCode = -1;

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
   * Creates a new <code>ActionException</code> with the given <code>HTTP</code>
   * status code and error message.
   * 
   * @param httpStatusCode
   *          the status code to send back to the client
   * @param message
   *          the error message
   */
  public ActionException(int httpStatusCode, String message) {
    this(httpStatusCode, message, null);
  }

  /**
   * Creates a new <code>ActionException</code> with the given <code>HTTP</code>
   * status code.
   * 
   * @param httpStatusCode
   *          the status code to send back to the client
   */
  public ActionException(int httpStatusCode) {
    this(httpStatusCode, null, null);
  }

  /**
   * Creates a new <code>ActionException</code> with the given <code>HTTP</code>
   * status code and originating from <code>cause</code>.
   * 
   * @param httpStatusCode
   *          the status code to send back to the client
   * @param cause
   *          the original error
   */
  public ActionException(int httpStatusCode, Throwable cause) {
    this(httpStatusCode, null, cause);
  }

  /**
   * Creates a new <code>ActionException</code> originating from
   * <code>cause</code>.
   * 
   * @param cause
   *          the original error
   */
  public ActionException(Throwable cause) {
    this(-1, null, cause);
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
    this(-1, message, cause);
  }

  /**
   * Creates a new <code>ActionException</code> with the given <code>HTTP</code>
   * status code, error message and the indicated original cause of failure.
   * 
   * @param httpStatusCode
   *          the status code to send back to the client
   * @param message
   *          the error message
   * @param cause
   *          the original error
   */
  public ActionException(int httpStatusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = httpStatusCode;
  }

  /**
   * Returns the http status code or <code>-1</code> if no status code has been
   * set.
   * 
   * @return the http status code
   */
  public int getStatusCode() {
    return statusCode;
  }

}