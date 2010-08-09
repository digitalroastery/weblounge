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

import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * Tag to provide support for &lt;ifversion&gt; tag implementations.
 */
public class VersionCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -4213051219560296442L;

  /** The element identifier */
  private String version = null;

  /**
   * Sets the version, which is one of
   * <ul>
   * <li>live</li>
   * <li>work</li>
   * <li>original</li>
   * </ul>
   * 
   * @param value
   *          the version identifier name
   */
  public final void setVersion(String value) {
    version = value;
  }

  /**
   * This method is called if the start of a <code>&lt;ifelement&gt;</code> tag
   * is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    long version = request.getVersion();
    if (this.version == ResourceUtils.getVersionString(version)) {
      return EVAL_BODY_INCLUDE;
    } else {
      return SKIP_BODY;
    }
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   */
  public void reset() {
    version = null;
  }

}
