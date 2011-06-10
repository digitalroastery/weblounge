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

package ch.entwine.weblounge.common;

/**
 * This exception is thrown if any part of the system is improperly configured.
 */
public class ConfigurationException extends RuntimeException {

  /** Serial version UID */
  private static final long serialVersionUID = 6322156095419046390L;

  /**
   * Constructor for class ConfigurationException.
   */
  public ConfigurationException() {
    super();
  }

  /**
   * Constructor for class ConfigurationException.
   * 
   * @param msg
   *          the error message
   */
  public ConfigurationException(String msg) {
    super(msg);
  }

  /**
   * Creates a new <code>ConfigurationException</code>, giving
   * <code>message</code> as the reason for this exception and <code>t</code> as
   * the exception that has originally been risen.
   * 
   * @param message
   *          exception description
   * @param t
   *          the original exception
   */
  public ConfigurationException(String message, Throwable t) {
    super(message, t);
  }

}