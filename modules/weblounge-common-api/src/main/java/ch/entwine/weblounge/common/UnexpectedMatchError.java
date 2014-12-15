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

package ch.entwine.weblounge.common;

import static java.lang.String.format;

/**
 * Error that indicates a match that is not expected. An example usage would be
 * a switch-statement
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
 *     throw new UnexpectedMatchError(enum.toString())
 * }
 * </pre>
 */
public class UnexpectedMatchError extends Error {

  private static final long serialVersionUID = 6070792041091151166L;

  private final String match;

  /**
   * Creates an instance of {@code UnexpectedMatchException}.
   * 
   * @param match
   *          the name of the unexpected match
   */
  public UnexpectedMatchError(String match) {
    this.match = match;
  }

  @Override
  public String getMessage() {
    return format("Reached unexpected match '%s'!", match);
  }

}
