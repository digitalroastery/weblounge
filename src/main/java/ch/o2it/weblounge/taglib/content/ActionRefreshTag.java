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

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.taglib.WebloungeTag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * This tag prints out a refresh statement and should therefore be placed within
 * the <tt>&lt;head&gt;</tt> section of the template.
 */
public abstract class ActionRefreshTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -544938652104194352L;

  /** Name of the refresh period parameter name */
  public static final String REFRESH_PERIOD = "refreshPeriod";

  /**
   * Does the tag processing.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);
    if (action != null) {
      if (isRefreshEnabled(action)) {
        try {
          String tag = "<meta http-equiv=\"refresh\" content=\"" + getRefreshPeriod(action) + "\">";
          pageContext.getOut().print(tag);
        } catch (IOException e) {
          /** ignore */
        }
      }
    }
    return super.doEndTag();
  }

  /**
   * Returns <code>true</code> if the refresh tag
   * 
   * @param action
   *          the action handler
   * @return <code>true</code> if refreshing is enabled
   */
  abstract boolean isRefreshEnabled(Action action);

  /**
   * Returns the refresh period in seconds.
   * 
   * @param action
   *          the action handler
   * @return the refresh period in seconds
   */
  abstract boolean getRefreshPeriod(Action action);

}
