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

import java.util.ArrayList;

/**
 * Models a list of {@link Suggestion} items.
 */
public class SuggestionList<T extends Suggestion> extends ArrayList<T> {

  /** Serial version uid */
  private static final long serialVersionUID = 6983520688053355781L;

  /** The type of suggestions */
  protected String suggestionType = null;

  /** The original hint */
  protected String hint = null;

  /** Tag name used to highlight matches */
  protected String highlightTag = null;

  /**
   * Creates a new list for suggestions.
   * 
   * @param suggestionType
   *          the type of suggestions, used as the root element
   * @param hint
   *          the original hint
   * @param highlightTag
   *          name of the tag used for highlighting
   */
  public SuggestionList(String suggestionType, String hint, String highlightTag) {
    this.suggestionType = suggestionType;
    this.hint = hint;
    this.highlightTag = highlightTag;
  }

  /**
   * Returns the suggestion as xml.
   * 
   * @return the serialized version of the suggestion
   */
  public String toXml() {
    StringBuffer xml = new StringBuffer();
    xml.append("<suggestions>");
    xml.append("<seed><[CDATA[").append(hint).append("]]></seed>");
    for (T suggestion : this) {
      xml.append(suggestion.toXml(hint, highlightTag));
    }
    xml.append("</suggestions>");
    return xml.toString();
  }

}
