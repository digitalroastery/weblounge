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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <code>MD5</code> is used to ease the creation and verification of
 * <code>md5</code> hashes.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class MD5 {

  /** The available hexadecimal characters */
  private static String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = MD5.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Returns the md5-encoded password or the plain password, if the encoding
   * didn't succeed. For the creation of the message digest, the system is asked
   * for the appropriate algorithm.
   * 
   * @param passwd
   *          The password
   * @return md5-encrypted password
   */
  public static String md(String passwd) {
    if (passwd == null) {
      return passwd;
    }
    MessageDigest md5 = null;
    String digest = passwd;
    try {
      md5 = MessageDigest.getInstance("MD5");
      md5.update(passwd.getBytes());
      byte[] digestData = md5.digest();
      digest = byteArrayToHex(digestData);
    } catch (NoSuchAlgorithmException e) {
      log_.warn("MD5 not supported. Using plain string as password!");
    } catch (Exception e) {
      log_.warn("Digest creation failed. Using plain string as password!");
    }
    return digest;
  }

  /**
   * Appends the given byte with its hexadecimal representation to the buffer
   * <code>buf</code>
   * 
   * @param buf
   *          the string buffer
   * @param b
   *          the byte
   */
  private static void byteToHex(StringBuffer buf, byte b) {
    int n = b;
    if (n < 0) {
      n = 256 + n;
    }
    int d1 = n / 16;
    int d2 = n % 16;
    buf.append(hex[d1]);
    buf.append(hex[d2]);
  }

  /**
   * Converts the byte array <code>b[]</code> into its hexadecimal
   * representation and returns it as a string.
   * 
   * @param b
   *          The byte array
   * @return the hexadecimal representation of the byte array
   */
  public static String byteArrayToHex(byte[] b) {
    StringBuffer buf = new StringBuffer(b.length * 2);
    for (int i = 0; i < b.length; i++) {
      byteToHex(buf, b[i]);
    }
    return buf.toString();
  }

}