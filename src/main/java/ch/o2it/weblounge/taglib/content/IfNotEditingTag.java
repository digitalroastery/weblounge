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

import javax.servlet.jsp.JspException;

/**
 * Tag to provide support for &lt;ifnotediting&gt; tag implementations.
 */
public class IfNotEditingTag extends EditingCheckTag {

  /** Serial version uid */
  private static final long serialVersionUID = -3271519218946469394L;

  /**
   * This method is called if the start of a <code>&lt;ifnotediting&gt;</code>
   * tag is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    if (isEditing()) {
      return SKIP_BODY;
    } else {
      super.doStartTag();
      return EVAL_BODY_INCLUDE;
    }
  }

}
