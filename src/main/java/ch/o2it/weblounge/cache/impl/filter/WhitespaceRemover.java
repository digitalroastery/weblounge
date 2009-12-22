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

package ch.o2it.weblounge.cache.impl.filter;

import ch.o2it.weblounge.cache.StreamFilter;

/**
 * This filter removes whitespace from html output. Using this filter will
 * significantly reduce the amount of characters in the output, but will also
 * lead to the output being on one single line, since newline and return
 * characters will be removed as well.
 */
public class WhitespaceRemover implements StreamFilter {

  /** remember whether we have seen a whitespace character */
  private boolean lastWhite = true;

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#filter(java.lang.StringBuffer,
   *      java.lang.String)
   */
  public StringBuffer filter(StringBuffer buffer, String contentType) {
    StringBuffer b = new StringBuffer(buffer.length());
    for (int i = 0; i < buffer.length(); i++) {
      char c = buffer.charAt(i);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
        if (!lastWhite)
          b.append(' ');
        lastWhite = true;
      } else {
        b.append(c);
        lastWhite = false;
      }
    }
    return b;
  }

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#flush()
   */
  public StringBuffer flush() {
    return null;
  }

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#close()
   */
  public void close() {
    lastWhite = true;
  }

}