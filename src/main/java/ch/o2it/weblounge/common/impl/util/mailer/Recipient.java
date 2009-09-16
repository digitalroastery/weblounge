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

import ch.o2it.weblounge.common.impl.util.encoding.QuotedPrintableEnconder;

import java.io.Serializable;

/**
 * Encapsulates a recipient of an email.
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class Recipient implements Serializable {

  /** the email address */
  protected String email;

  /** the name */
  protected String name;

  /**
   * Creates a new recipient.
   * 
   * @param email
   *          the email address of the recipient
   * @param name
   *          the name of the recipient
   * 
   * @throws InvalidAddressException
   *           if the address is invalid
   */
  protected Recipient(String email, String name) throws InvalidAddressException {
    if (email == null)
      throw new NullPointerException("No email address");
    if (!checkEmail(email))
      throw new InvalidAddressException("Invalid email address", email);
    this.email = email;
    this.name = name;
  }

  /**
   * Gets the email address of the recipient.
   * 
   * @return the email address of the recipient
   */
  public String getEmail() {
    return email;
  }

  /**
   * Gets the name of the recipient.
   * 
   * @return the name of the recipient
   */
  public String getName() {
    return name;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    if (name != null)
      return QuotedPrintableEnconder.encodeHeader(name) + " <" + email + ">";
    return email;
  }

  /**
   * Formats the recipient and appends it to the buffer.
   * 
   * @param buf
   *          the <code>StringBuffer</code> the recipient should be written to
   */
  public void format(StringBuffer buf) {
    buf.append(toString());
  }

  /**
   * Checks whether the given email address is valid.
   * 
   * @param email
   *          ethe email address to validate
   * @return <code>true</code> iff the email is valid
   */
  public static boolean checkEmail(String email) {
    int index;
    if ((index = email.indexOf('@')) < 1 || email.indexOf('@', index + 1) != -1 || index >= email.length() - 1)
      return false;
    // TODO: filter more invalid chars
    for (index = 0; index < email.length(); ++index)
      if (email.charAt(index) <= (char) 32 || email.charAt(index) >= (char) 127)
        return false;
    return true;
  }
}