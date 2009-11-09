/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.encoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

/**
 * This encoder transforms text to UTF-8. An advantage over existing methods
 * like <code>String.getBytes(encoding)</code> is that a
 * UnsupportedEncodingException will never be thrown.
 * <p>
 * Unless the method is named <code>*WithLength</code> the characters are
 * encoded without two bytes length prefixed.
 * </p>
 * <p>
 * See <a href="http://ietf.org/rfc/rfc2279.txt">RFC 2279</a> for details about
 * the UTF-8 transform format.
 * </p>
 */
public class UTF8Encoder {

  private UTF8Encoder() {
  }

  /**
   * Encode to UTF-8.
   * 
   * @param s
   *          the input string
   * @return the encoded byte array
   */
  public static byte[] encode(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Cannot convert null string to UTF-8");
    }
    return encode(s.toCharArray());
  }

  /**
   * Encodes the character array to an UTF-8 byte sequence.
   * 
   * @param chars
   *          the character array
   * @return the encoded bytes
   */
  public static byte[] encode(char[] chars) {
    if (chars == null) {
      throw new IllegalArgumentException("Cannot convert null character array to UTF-8");
    }
    byte[] result = new byte[getEncodedLength(chars)];
    int pos = 0;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c <= 0x007F) {
        result[pos++] = (byte) (c & 0xFF);
      } else if (c <= 0x07FF) {
        result[pos++] = (byte) (0xC0 | (c >> 6));
        result[pos++] = (byte) (0x80 | (c & 0x3F));
      } else {
        result[pos++] = (byte) (0xE0 | (c >> 12));
        result[pos++] = (byte) (0xC0 | (c >> 6));
        result[pos++] = (byte) (0x80 | (c & 0x3F));
      }
    }
    return result;
  }

  /**
   * Calculate the number of bytes a string would require to be encoded as
   * UTF-8.
   * 
   * @param s
   *          the input text
   * @return the length of the encoded string
   */
  public static int getEncodedLength(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Cannot calculate UTF-8 length of null string");
    }
    return getEncodedLength(s.toCharArray());
  }

  /**
   * Calculate the number of bytes an array of characters would require to be
   * encoded as UTF-8.
   * 
   * @param chars
   *          the bytes to encode, may not be <code>null</code>
   */
  public static int getEncodedLength(char[] chars) {
    if (chars == null) {
      throw new IllegalArgumentException("Cannot calculate UTF-8 length of null array");
    }

    int result = 0;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if ((c >= 0x0001) && (c <= 0x007F)) {
        result++;
      } else if (c > 0x07FF) {
        result += 3;
      } else {
        result += 2;
      }
    }
    return result;
  }

  /**
   * Write a character in UTF-8 format to any output stream.
   * 
   * @param out
   * @param c
   * @throws IOException
   */
  public static void write(OutputStream out, char c) throws IOException {
    // Writing single bytes can be expensive but creating a tiny array
    // is just as painful and churns memory.
    if (c <= 0x007F) {
      out.write((byte) (c & 0xFF));
    } else if (c <= 0x07FF) {
      out.write((byte) (0xC0 | (c >> 6)));
      out.write((byte) (0x80 | (c & 0x3F)));
    } else {
      out.write((byte) (0xE0 | (c >> 12)));
      out.write((byte) (0xC0 | (c >> 6)));
      out.write((byte) (0x80 | (c & 0x3F)));
    }
  }

  /**
   * @param out
   * @param c
   *          may not be <code>null</code>
   * @throws IOException
   */
  public static void write(OutputStream out, char[] c) throws IOException {
    if (c == null) {
      throw new IllegalArgumentException("Cannot convert null character array to UTF-8");
    }
    for (int i = 0; i < c.length; i++) {
      // TODO do block write.
      UTF8Encoder.write(out, c[i]);
    }
  }

  /**
   * Write a string as a sequence of UTF-8 characters as if writing each
   * character of the string individually. Identical to
   * <code>String.getBytes("UTF8")</code>.
   * 
   * @param out
   * @param s
   *          may not be <code>null</code>
   * @throws IOException
   */
  public static void write(OutputStream out, String s) throws IOException {
    if (s == null) {
      throw new IllegalArgumentException("Cannot convert null string to UTF-8");
    }

    // Shortcut small strings to avoid the penalty of creating a byte array
    // to write block-wise.
    int slen = s.length();
    if (slen < 15) {
      for (int i = 0; i < slen; i++) {
        write(out, s.charAt(i));
      }
      return;
    }

    out.write(encode(s));
  }

  /**
   * Write a string as a sequence of UTF-8 characters prefix by a length.
   * 
   * @param out
   * @param s
   *          the text input
   * @throws IOException
   */
  public static void writeWithLength(OutputStream out, String s)
      throws IOException {
    if (s == null) {
      throw new IllegalArgumentException("Cannot convert null string to UTF-8");
    }

    // Shortcut small strings to avoid the penalty of creating a byte array
    // to write block-wise.
    int slen = s.length();
    if (slen < 15) {
      int len = getEncodedLength(s);
      out.write((byte) ((len >>> 8) & 0xFF));
      out.write((byte) (len & 0xFF));
      for (int i = 0; i < slen; i++) {
        write(out, s.charAt(i));
      }
      return;
    }

    byte[] encoded = encode(s);
    if (encoded.length > 65535) {
      throw new UTFDataFormatException("Too many encoded bytes");
    }
    out.write((byte) ((encoded.length >>> 8) & 0xFF));
    out.write((byte) (encoded.length & 0xFF));
    out.write(encoded);

  }

}