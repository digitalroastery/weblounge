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

package ch.entwine.weblounge.cache.impl.filter;

import ch.entwine.weblounge.cache.StreamFilter;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This {@link StreamFilter} removes special characters from the output stream
 * and replaces them with their proper HTML representation.
 */
public class SpecialChars implements StreamFilter {

  /**
   * @see ch.entwine.weblounge.api.request.StreamFilter#filter(java.lang.StringBuffer,
   *      java.lang.String)
   */
  public StringBuffer filter(StringBuffer buffer, String contentType) {
    if ("text/html".equals(contentType))
      return new StringBuffer(StringEscapeUtils.escapeHtml(buffer.toString()));
    return buffer;
  }

  /**
   * @see ch.entwine.weblounge.api.request.StreamFilter#flush()
   */
  public StringBuffer flush() {
    return null;
  }

  /**
   * @see ch.entwine.weblounge.api.request.StreamFilter#close()
   */
  public void close() {
  }

}