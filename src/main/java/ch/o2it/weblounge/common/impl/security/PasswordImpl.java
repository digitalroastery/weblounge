/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.security.Password;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;

/**
 * Represents a password user credentials.
 */
public final class PasswordImpl implements Password {

  /** the password */
  protected String password = null;

  /** md5 digest of the password */
  protected String md5 = null;

  /** Password digest type, either plain or md5 */
  protected DigestType passwordDigestType = DigestType.plain;

  /**
   * Creates a new password.
   * 
   * @param password
   *          the password
   * @param type
   *          the digest type
   * @throws IllegalArgumentException
   *           if either one of <code>password</code> or <code>type</code> is
   *           <code>null</code>
   */
  public PasswordImpl(String password, DigestType type) {
    if (password == null)
      throw new IllegalArgumentException("Password cannot be null");
    if (type == null)
      throw new IllegalArgumentException("Password digest type cannot be null");
    this.password = password;
    this.passwordDigestType = type;

    switch (type) {
      case plain:
        try {
          this.md5 = new String(DigestUtils.md5(password.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
        break;
      case md5:
        this.md5 = password;
        break;
      default:
        throw new IllegalArgumentException("Unknown digest type " + type);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Password#getDigestType()
   */
  public DigestType getDigestType() {
    return passwordDigestType;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return md5.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Password))
      return false;
    Password pw = (Password) o;
    DigestType digestType = pw.getDigestType();
    switch (digestType) {
      case md5:
        return md5.equals(pw.getPassword());
      case plain:
        try {
          return md5.equals(DigestUtils.md5(pw.getPassword().getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
      default:
        throw new IllegalStateException("Found unknown digest type " + digestType);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Password#getPassword()
   */
  public String getPassword() {
    return password;
  }

}
