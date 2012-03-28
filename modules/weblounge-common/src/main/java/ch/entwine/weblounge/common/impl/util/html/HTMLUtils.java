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

package ch.entwine.weblounge.common.impl.util.html;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.StringWriter;

/**
 * Utility class that facilitates dealing with <code>HTML</code> code.
 */
public final class HTMLUtils {

  /**
   * This is a utility class and must not be instantiated.
   */
  private HTMLUtils() {
    // Nothing to do here
  }

  /**
   * <p>
   * Escapes the characters in the <code>String</code> passed and writes the
   * result to the <code>Writer</code> passed.
   * </p>
   * 
   * @param writer
   *          The <code>Writer</code> to write the results of the escaping to.
   *          Assumed to be a non-null value.
   * @param str
   *          The <code>String</code> to escape. Assumed to be a non-null value.
   * 
   * @see #escapeHtml(String)
   */
  public static String escapeHtml(String str) {
    StringWriter writer = createStringWriter(str);
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (c > 0x7F) {
        writer.write("&#");
        writer.write(Integer.toString(c, 10));
        writer.write(';');
      } else {
        writer.write(c);
      }
    }
    return writer.toString();
  }

  /**
   * <p>
   * Unescapes the escaped entities in the <code>String</code> passed and writes
   * the result to the <code>Writer</code> passed.
   * </p>
   * 
   * @param writer
   *          The <code>Writer</code> to write the results to; assumed to be
   *          non-null.
   * @param str
   *          The source <code>String</code> to unescape; assumed to be
   *          non-null.
   * @return the unescaped string
   * @see #escapeHtml(String)
   */
  public static String unescape(String str) {
    return StringEscapeUtils.unescapeHtml(str);
  }

  /**
   * Make the StringWriter 10% larger than the source String to avoid growing
   * the writer
   * 
   * @param str
   *          The source string
   * @return A newly created StringWriter
   */
  private static StringWriter createStringWriter(String str) {
    return new StringWriter((int) (str.length() + (str.length() * 0.1)));
  }

}
