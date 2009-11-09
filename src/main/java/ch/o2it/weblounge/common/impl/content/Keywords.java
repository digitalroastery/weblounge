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

package ch.o2it.weblounge.common.impl.content;

import java.util.HashSet;

/**
 * Utility implementation for classes that are dealing with keywords. This
 * implementation allows keywords to only exist once, so it is realized as a
 * set.
 */
public class Keywords extends HashSet<String> {

  /** The serial version UID */
  private static final long serialVersionUID = 6293776479106369637L;

  /**
   * Returns an <code>XML</code> representation of the set of keywords, 
   * looking something like:
   * <pre>
   * &lt;keywords&gt;
   *  &lt;keyword&gt;&lt;![CDATA[[important[[&gt;&lt;/keyword&gt;
   * &lt;/keywords&gt;
   * </pre>
   * 
   * @return the <code>XML</code> representation
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    if (size() != 0) {
      b.append("<keywords>\n");
      for (String keyword : this) {
        b.append("<keyword><![CDATA[");
        b.append(keyword);
        b.append("]]></keyword>\n");
      }
      b.append("</keywords>\n");
    } else {
      b.append("<keywords/>\n");
    }
    return b.toString();
  }

}