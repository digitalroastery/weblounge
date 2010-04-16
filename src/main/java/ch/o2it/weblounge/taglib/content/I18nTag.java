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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.taglib.WebloungeTag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * This tag inserts a <code>generator</code> meta tag into the head section of
 * an html or jsp page.
 */
public class I18nTag extends WebloungeTag {

  private static final long serialVersionUID = 5791260790900823852L;

  /** The i18n key */
  protected String i18n = null;

  /**
   * Sets the i18n attribute.
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#setName(java.lang.String)
   */
  public void setName(String name) {
    try {
      this.i18n = (String) ExpressionUtil.evalNotNull("i18n", "name", name, String.class, this, pageContext);
    } catch (NullAttributeException ex) {
      this.i18n = "";
    } catch (JspException ex) {
      // TODO: add error handling
    }
  }

  /**
   * Writes the generator tag to the output.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().write(I18n.toHTML(i18n, request.getLanguage(), request.getSite()));
    } catch (IOException e) {
    }
    super.doEndTag();
    return EVAL_PAGE;
  }

}