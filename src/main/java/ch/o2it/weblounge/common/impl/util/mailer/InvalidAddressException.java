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

package ch.o2it.weblounge.common.impl.util.mailer;

/**
 * InvalidAddressException
 * 
 * @version $Revision: 1059 $
 * @author Daniel Steiner
 */

public class InvalidAddressException extends EmailException {

  /** The serial version UID */
  private static final long serialVersionUID = 5078521209466813291L;

  /** the address that caused the exception */
  private String email_;

  /**
   * Creates a new <code>InvalidAddressException</code>.
   * 
   * @param message
   *          a message describing the exception
   * @param email
   *          the address that caused the exception
   */
  public InvalidAddressException(String message, String email) {
    super(message);
    email_ = email;
  }

  /**
   * Creates a new <code>InvalidAddressException</code>.
   * 
   * @param email
   *          the address that caused the exception
   */
  public InvalidAddressException(String email) {
    super();
    email_ = email;
  }

  /**
   * Returns the address that caused the exception.
   * 
   * @return the address that caused the exception
   */
  public String getEmail() {
    return email_;
  }
}