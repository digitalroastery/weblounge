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

package ch.entwine.weblounge.cache;

/**
 * An interface used to filter character streams. The method
 * <code>filter()</code> will get called multiple times for successive parts of
 * the input stream.<br>
 * 
 * The <code>StreamFilter</code> may keep an internal cache and/or state
 * information about its filtering process and is free to return as much or as
 * few of the filtered stream for each invocation of the <code>filter()</code>
 * method as it likes.<br>
 * 
 * The <code>StreamFilter</code> must return all remaining/cached parts of the
 * filtered stream upon a call to the <code>flush()</code> method.<br>
 * 
 * The <code>StreamFilter</code> must discard all its internal state information
 * upon a call to the <code>close()</code> method. Successive calls to the
 * <code>filter()</code> method will be on a new input stream.
 */
public interface StreamFilter {

  /**
   * Filters the next part of the character stream.
   * 
   * @param buffer
   *          the next portion of the input stream
   * @param contentType
   *          the stream's content type
   * @return the filtered character stream or <code>null</code> if no filtered
   *         output should be generated
   * 
   */
  StringBuffer filter(StringBuffer buffer, String contentType);

  /**
   * Indicates that the stream must be flushed. The <code>StreamFilter</code>
   * must return all remaining output.
   * 
   * @return the remaining output of the filtered stream or <code>null</code> if
   *         no further output remains
   */
  StringBuffer flush();

  /**
   * Indicates the end of the input stream and. The <code>StreamFilter</code>
   * must discard all its state information about the filtering process.
   */
  void close();

}
