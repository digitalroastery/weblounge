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

package ch.entwine.weblounge.taglib.content;

import ch.entwine.weblounge.common.site.I18nDictionary;
import ch.entwine.weblounge.taglib.WebloungeTag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * This tag inserts the value of the given <code>i18n</code> key into the page.
 */
public class I18nTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 5791260790900823852L;

  /** The i18n key */
  protected String i18nKey = null;

  /**
   * Sets the i18n attribute.
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#setName(java.lang.String)
   */
  public void setKey(String name) throws JspException {
    if (name == null)
      throw new JspException("i18n name cannot be null");
    i18nKey = name;
  }

  /**
   * Writes the i18n value to the output.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    try {
      I18nDictionary dictionary = request.getSite().getI18n();
      pageContext.getOut().write(dictionary.getAsHTML(i18nKey, request.getLanguage()));
    } catch (IOException e) {
      throw new JspException(e);
    }
    return super.doEndTag();
  }

}
