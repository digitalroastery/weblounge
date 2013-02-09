/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This utility class will encode passwords to an md5 hex representation.
 */
public final class PasswordEncoder {

  /** The algorithm used by this encoder */
  private static final String algorithm = "md5";

  /** The character set to use when ecoding passwords */
  private static final Charset CHARSET = Charset.forName("UTF-8");

  private static final char[] HEX = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  /**
   * Private constructor to disallow construction of this utility class.
   */
  private PasswordEncoder() {
  }

  /**
   * Encode a clear text password.
   * 
   * @param clearText
   *          the password
   * @return the encoded password
   * @throws IllegalArgumentException
   *           if clearText or salt are null
   */
  public static String encode(String clearText) throws IllegalArgumentException {
    if (clearText == null)
      throw new IllegalArgumentException("clear text password must not be null");

    MessageDigest messageDigest = getMessageDigest();
    byte[] digest = messageDigest.digest(utf8(clearText));
    return new String(hex(digest));
  }

  /**
   * Get a MessageDigest instance for the given algorithm. Throws an
   * IllegalArgumentException if <i>algorithm</i> is unknown
   * 
   * @return MessageDigest instance
   * @throws IllegalArgumentException
   *           if NoSuchAlgorithmException is thrown
   */
  private static MessageDigest getMessageDigest()
      throws IllegalArgumentException {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("No such algorithm [" + algorithm + "]");
    }
  }

  /**
   * Get the bytes of the String in UTF-8 encoded form.
   */
  public static byte[] utf8(CharSequence string) {
    try {
      ByteBuffer bytes = CHARSET.newEncoder().encode(CharBuffer.wrap(string));
      byte[] bytesCopy = new byte[bytes.limit()];
      System.arraycopy(bytes.array(), 0, bytesCopy, 0, bytes.limit());

      return bytesCopy;
    } catch (CharacterCodingException e) {
      throw new IllegalArgumentException("Encoding failed", e);
    }
  }

  public static char[] hex(byte[] bytes) {
    final int nBytes = bytes.length;
    char[] result = new char[2 * nBytes];

    int j = 0;
    for (int i = 0; i < nBytes; i++) {
      // Char for top 4 bits
      result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
      // Bottom 4
      result[j++] = HEX[(0x0F & bytes[i])];
    }

    return result;
  }

}
