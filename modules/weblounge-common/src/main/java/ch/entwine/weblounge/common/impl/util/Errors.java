/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.util;

/**
 * Utility class for serious problems also known as errors.
 */
public final class Errors {

  private Errors() {
  }

  /**
   * Indicates that a code execution path is not (yet) implemented.
   * 
   * @param msg
   *          the detail message
   */
  public static <A> A notImplemented(String msg) {
    throw new UnsupportedOperationException(msg);
  }

  /**
   * Indicates that a code execution path is not (yet) implemented.
   */
  public static <A> A notImplemented() {
    throw new UnsupportedOperationException();
  }

  /**
   * Indicates a match that is not expected. An example usage would be a
   * switch-statement
   * 
   * <pre>
   * switch (enum) {
   *   case OPTION_A:
   *     // ...
   *     break;
   *   case OPTION_B:
   *     // ...
   *     break;
   *   case default:
   *     // no more options expected
   *     Errors.unexpectedMatch();
   * }
   * </pre>
   */
  public static <A> A unexpectedMatch() {
    throw new Error("Unexpected match");
  }
}
