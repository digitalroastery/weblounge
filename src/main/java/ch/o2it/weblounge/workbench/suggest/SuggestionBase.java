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
 * Base class for suggestions with support for hint highlighting.
 */
public abstract class SuggestionBase implements Suggestion {

  /**
   * Returns the text with the original hint emphasized using the given
   * delimiter as tag names.
   * <p>
   * For example, if <code>hello world</code> is passed in as the text,
   * <code>emph</code> as <code>delimiter</code> and the original hint was
   * <code>ll</code>, this method would return
   * <code>he&lt;emph&gt;ll&lt;/emph&gt;o world</code>.
   * <p>
   * Note that both, <code>hint</code> and <code>delimiter</code> may be
   * <code>null</code>, in which case the original text or <code>null</code>,
   * respectively, should be returned.
   * 
   * @param text
   *          the text to highlight
   * @param hint
   *          the original hint
   * @param delimiter
   *          the delimiter tag name
   * @return the highlighted text
   */
  protected String highlight(String text, String hint, String delimiter) {
    if (StringUtils.isBlank(text) || StringUtils.isBlank(delimiter))
      return text;
    String lowercasedText = text.toLowerCase();
    int position = lowercasedText.indexOf(hint);
    if (position >= 0) {
      StringBuffer result = new StringBuffer();
      int startOfRest = 0;
      while (position >= 0) {
        result.append(text.substring(startOfRest, position));
        result.append("<").append(delimiter).append(">");
        result.append(text.substring(position, position + hint.length()));
        result.append("</").append(delimiter).append(">");
        startOfRest = position + hint.length();
        position = lowercasedText.indexOf(hint, startOfRest);
      }
      result.append(text.substring(startOfRest));
      return result.toString();
    } else {
      return text;
    }
  }

}
