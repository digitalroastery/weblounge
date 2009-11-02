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
 * A <code>ActionException</code> is thrown if an exceptional state is
 * reached when executing an <code>Action</code> to create the output for
 * either a page or a single page element.
 */
public class ActionException extends RuntimeException {

  /** Serial version id */
  private static final long serialVersionUID = 1L;

  /** Action name, e. g. <code>XSLAction</code> */
  private Action handler_ = null;

  /** Rendering method, e. g. <code>HTML</code>" */
  private String method_ = null;

  /** The exception that lead to this one */
  private Throwable exception_ = null;

  /**
   * Creates a new <code>ActionException</code> providing the information
   * passed by the parameters.
   * 
   * @param handler
   *          the handler, e. g. <code>XLSElementAction</code>
   * @param method
   *          the output method, e. g. <code>HTML</code>
   */
  public ActionException(Action handler, String method) {
    handler_ = handler;
    method_ = method;
  }

  /**
   * Creates a new <code>ActionException</code> providing the information
   * passed by the parameters.
   * 
   * @param handler
   *          the handler, e. g. <code>XLSElementAction</code>
   * @param method
   *          the output method, e. g. <code>HTML</code>
   * @param t
   *          the exception caught when executing the renderer
   */
  public ActionException(Action handler, String method,
      Throwable t) {
    this(handler, method);
    exception_ = t;
  }

  /**
   * Returns the handler that raised this exception.
   * 
   * @return the handler
   */
  public Action getAction() {
    return handler_;
  }

  /**
   * Returns the output method, e. g. <code>HTML</code>.
   * 
   * @return the output method
   */
  public String getRenderingMethod() {
    return method_;
  }

  /**
   * Returns the exception that lead to this <code>ActionException</code>
   * .
   * 
   * @return the reason for this exception
   */
  public Throwable getReason() {
    return exception_;
  }

}