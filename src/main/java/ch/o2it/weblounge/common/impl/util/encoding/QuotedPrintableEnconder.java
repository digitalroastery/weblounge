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

package ch.o2it.weblounge.common.impl.util.encoding;

import java.io.UnsupportedEncodingException;

/**
 * QuotedPrintableEnconder
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class QuotedPrintableEnconder {

  /** the 'valid' characters */
  private static final char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  /**
   * Encodes a message body.
   * 
   * @param body
   *          the message body to encode
   * @return the encoded message body
   */
  public static StringBuffer encodeBody(String body) {
    byte buf[] = getBytes(body);
    StringBuffer sb = new StringBuffer(body.length() * 3);

    int lineLength = 0;
    for (int i = 0; i < buf.length; i++) {
      byte b = buf[i];
      if ((b >= 33 && b <= 60) || (b >= 62 && b <= 126)) {
        if (lineLength >= 75) {
          lineLength = 0;
          sb.append("=\r\n");
        }
        sb.append((char) b);
        lineLength++;
      } else if (b == '\r' || b == '\n') {
        // !TODO: check for real CRLF!
        sb.append((char) b);
        lineLength = 0;
        /*
         * } else if (c == ' ' || c == '\t') { // ~TODO: handle whitespace
         */
      } else {
        if (lineLength >= 73) {
          lineLength = 0;
          sb.append("=\r\n");
        }
        lineLength += 3;
        sb.append('=');
        sb.append(hexChars[(b >> 4) & 0xf]);
        sb.append(hexChars[b & 0xf]);
      }
    }

    return sb;
  }

  /**
   * Encodes a message header.
   * 
   * @param header
   *          the message header to encode
   * @return the encoded message header
   */
  public static String encodeHeader(String header) {
    if (!needsEncoding(header))
      return header;
    byte buf[] = getBytes(header);
    StringBuffer sb = new StringBuffer(3 * header.length());
    sb.append("=?ISO-8859-1?Q?");
    int lineLength = 0;
    for (int i = 0; i < header.length(); i++) {
      byte b = buf[i];
      // TODO: handle line length
      if ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')) {
        sb.append((char) b);
        lineLength++;
      } else if (b == ' ') {
        sb.append('_');
        lineLength++;
      } else {
        lineLength += 3;
        sb.append('=');
        sb.append(hexChars[(b >> 4) & 0xf]);
        sb.append(hexChars[b & 0xf]);
      }
    }

    sb.append("?=");
    return sb.toString();
  }

  /**
   * Check whether the given text should to be encoded.
   * 
   * @param text
   *          the text to check
   * @return <code>true</code> if the given text should be encoded
   */
  public static boolean needsEncoding(String text) {
    byte buf[] = getBytes(text);
    for (int i = 0; i < text.length(); i++) {
      byte b = buf[i];
      if (b > 126 || b < 32)
        return true;
    }
    return false;
  }

  /**
   * extracts the bytes from a String in ISO-8859-1 encoding.
   * 
   * @param s
   *          the string to extract the bytes
   * @return the bytes of the string
   */
  private static byte[] getBytes(String s) {
    try {
      return s.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      return s.getBytes();
    }
  }
}
