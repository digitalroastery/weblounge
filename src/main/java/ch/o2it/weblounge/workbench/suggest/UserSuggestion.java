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

package ch.o2it.weblounge.workbench.suggest;

import org.apache.commons.lang.StringUtils;

/**
 * A user suggestion includes everything that is needed to display a suggested
 * list of users based on some input text.
 */
public class UserSuggestion extends SuggestionBase {

  /** The user id */
  protected String id = null;

  /** The user's full name */
  protected String name = null;

  /**
   * Creates a new suggestion containing a user's details.
   * 
   * @param id
   *          the user identifier
   * @param name
   *          the user's full name
   */
  public UserSuggestion(String id, String name) {
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("Id must not be null");
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("Name must not be null");
    this.id = id;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.workbench.suggest.Suggestion#toXml(java.lang.String,
   *      java.lang.String)
   */
  public String toXml(String hint, String highlightTag) {
    StringBuffer xml = new StringBuffer();
    xml.append("<user id=\"").append(id).append("\">");
    xml.append("<name><[CDATA[").append(name).append("]]></name>");
    xml.append("</user>");
    return xml.toString();
  }

}
