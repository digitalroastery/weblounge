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

package ch.entwine.weblounge.workbench.suggest;

import org.apache.commons.lang.StringUtils;

/**
 * A subject suggestion includes everything that is needed to display a
 * suggested list of subjects based on some input text.
 */
public class SimpleSuggestion extends SuggestionBase {

  /** The subject name */
  protected String suggestion = null;

  /** The type of suggestion */
  protected String type = null;

  /**
   * Creates a new suggestion containing a subject's details.
   * 
   * @param type
   *          of suggestion
   * @param suggestion
   *          the subject name
   */
  public SimpleSuggestion(String type, String suggestion) {
    if (StringUtils.isBlank(type))
      throw new IllegalArgumentException("Type must not be null");
    if (StringUtils.isBlank(suggestion))
      throw new IllegalArgumentException("Suggestion must not be null");
    this.type = type;
    this.suggestion = suggestion;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.workbench.suggest.Suggestion#toXml(java.lang.String,
   *      java.lang.String)
   */
  public String toXml(String hint, String highlightTag) {
    StringBuffer xml = new StringBuffer();
    xml.append("<").append(type).append(">");
    xml.append("<[CDATA[").append(suggestion);
    xml.append("</").append(type).append(">");
    return xml.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return suggestion;
  }

}
