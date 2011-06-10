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

package ch.entwine.weblounge.common.request;

/**
 * Common flavor definitions for requests.
 */
public enum RequestFlavor {

  /**
   * <code>ANY</code> is the flavor of choice if no concrete flavor can be
   * determined.
   */
  ANY,

  /**
   * <code>HTML</code> or <code>XHTML</code> output as produced by Java server
   * pages or actions by default.
   */
  HTML,

  /**
   * The <code>XML</code> flavor is produced for pages or other resources like
   * user and group information.
   */
  XML,

  /**
   * <code>JSON</code> or <tt>JavaScript Object Notation</tt> is suited for data
   * processing on the client side where the client usually is a web browser.
   */
  JSON;

  /**
   * Returns a lower case string of this flavor.
   * 
   * @return the flavor in lower case
   */
  public String toExtension() {
    return this.toString().toLowerCase();
  }

  /**
   * Returns a request flavor by matching <code>value</code> against the
   * available flavors.
   * 
   * @param value
   *          the value
   * @return the request flavor
   * @throws IllegalArgumentException
   *           if <code>value</code> cannot be converted to a
   *           <code>RequestFlavor</code>
   */
  public static RequestFlavor parseString(String value)
      throws IllegalArgumentException {
    if (HTML.toString().equalsIgnoreCase(value))
      return HTML;
    else if (XML.toString().equalsIgnoreCase(value))
      return XML;
    else if (JSON.toString().equalsIgnoreCase(value))
      return JSON;
    throw new IllegalArgumentException("Request flavor " + value + " is unknown");
  }

}