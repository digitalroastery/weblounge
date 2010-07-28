/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.doc;

/**
 * Represents a possible output format for an endpoint.
 */
public class Format {

  /** JSON output format */
  public static String JSON = "json";

  /** XML output format */
  public static String XML = "xml";

  /** JSON namespace url */
  public static String JSON_URL = "http://www.json.org/";

  /** XML namespace url */
  public static String XML_URL = "http://www.w3.org/XML/";

  /** The format identifier */
  private String name = null;

  /** Format description */
  private String description = null;

  /** Namespace url */
  private String url = null;

  /**
   * Creates a new format.
   * 
   * @param name
   *          the format name (e.g. json)
   * @param description
   *          an optional description related to this format
   * @param url
   *          an optional url pointing to additional format documentation
   */
  public Format(String name, String description, String url) {
    if (!EndpointDocumentation.isValid(name)) {
      throw new IllegalArgumentException("name must not be null and must be alphanumeric");
    }
    this.name = name;
    this.description = description;
    this.url = url;
  }

  /**
   * Creates the standard <code>JSON</code> format.
   * 
   * @return the <code>JSON</code> format
   */
  public static Format json() {
    return new Format(JSON, null, JSON_URL);
  }

  /**
   * Creates the standard <code>XML</code> format.
   * 
   * @return the <code>XML</code> format
   */
  public static Format xml() {
    return new Format(XML, null, XML_URL);
  }

  /**
   * Creates the standard <code>JSON</code> format along with the given
   * description.
   * 
   * @param description
   *          the format description
   * @return the <code>JSON</code> format
   */
  public static Format json(String description) {
    return new Format(JSON, description, JSON_URL);
  }

  /**
   * Creates the standard <code>XML</code> format along with the given
   * description.
   * 
   * @param description
   *          the format description
   * @return the <code>XML</code> format
   */
  public static Format xml(String desc) {
    return new Format(XML, desc, XML_URL);
  }

  /**
   * Returns the format name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the format description or <code>null</code> if no description was
   * given.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the url providing addition format documentation or
   * <code>null</code> if no url was specified.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name + ":(" + url + ")";
  }

}
