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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.language.Language;

/**
 * Default implementation of a resource content.
 */
public class ResourceContentImpl implements ResourceContent {

  /** The content's language */
  protected Language language = null;
  
  /**
   * Creates a new resource content representation.
   * 
   * @param language
   *          the content language
   */
  protected ResourceContentImpl(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.ResourceContent#setLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.ResourceContent#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * Callback for subclasses that need to add additional information to the file
   * content representation. Implementations should append their data to the
   * <code>StringBuffer</code> and return it once they're done.
   * 
   * @param xml
   *          the string buffer
   * @return the modified string buffer
   */
  protected StringBuffer addXml(StringBuffer xml) {
    return xml;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.ResourceContent#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<content language=\"").append(getLanguage().getIdentifier()).append("\">");
    addXml(buf);
    buf.append("</content>");
    return buf.toString();
  }

}
