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

package ch.entwine.weblounge.common.repository;

/**
 * This exception is thrown when a resource is about to be removed from the
 * content repository that is still being referenced by another resource.
 */
public class ReferentialIntegrityException extends ContentRepositoryException {

  /** Serial version uid */
  private static final long serialVersionUID = -8768389027972761077L;

  /**
   * Creates a new referential integrity exception with the given error message.
   * 
   * @param message
   *          the error message
   */
  public ReferentialIntegrityException(String message) {
    super(message);
  }

  /**
   * Creates a new referential integrity exception with the given cause.
   * 
   * @param cause
   *          the reason of failure
   */
  public ReferentialIntegrityException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new referential integrity exception with the given error message
   * and cause.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the reason of failure
   */
  public ReferentialIntegrityException(String message, Throwable cause) {
    super(message, cause);
  }

}
