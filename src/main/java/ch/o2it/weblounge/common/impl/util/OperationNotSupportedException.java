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

package ch.o2it.weblounge.common.impl.util;

/**
 * Exception that will be thrown if an operation (method) is called in a state
 * where it is not possible to execute this method.
 * 
 * @author Tobias Wunden
 * @version 1.0 Thu Jul 11 2002
 * @since WebLounge 1.0
 */

public class OperationNotSupportedException extends RuntimeException {

  /** The serial version id */
  private static final long serialVersionUID = 3799604976734247015L;

  /**
   * Constructor for class OperationNotSupportedException.
   */
  public OperationNotSupportedException() {
    super();
  }

  /**
   * Constructor for class OperationNotSupportedException.
   */
  public OperationNotSupportedException(String message) {
    super(message);
  }

}