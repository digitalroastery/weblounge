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

package ch.o2it.weblounge.workbench;

import org.apache.commons.lang.StringUtils;

/**
 * A page suggestion includes everything that is needed to display a suggested
 * list of pages based on some input text.
 */
public class PageSuggestion extends SuggestionBase {

  /** The page id */
  protected String id = null;

  /** The page url */
  protected String url = null;

  /** The page title */
  protected String title = null;

  /**
   * Creates a new suggestion containing a page's details.
   * 
   * @param id
   *          the page identifier
   * @param url
   *          the page url
   * @param title
   *          the page title
   */
  public PageSuggestion(String id, String url, String title) {
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("Id must not be null");
    if (StringUtils.isBlank(url))
      throw new IllegalArgumentException("Url must not be null");
    if (StringUtils.isBlank(title))
      throw new IllegalArgumentException("Title must not be null");
    this.id = id;
    this.url = url;
    this.title = title;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.workbench.Suggestion#toXml(java.lang.String,
   *      java.lang.String)
   */
  public String toXml(String hint, String highlightTag) {
    StringBuffer xml = new StringBuffer();
    xml.append("<page id=\"").append(id).append("\">");
    xml.append("<title><[CDATA[").append(title).append("]]></title>");
    xml.append("<url>").append(url).append("</name>");
    xml.append("</page>");
    return xml.toString();
  }

}
