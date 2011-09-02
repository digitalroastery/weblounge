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

import ch.entwine.weblounge.common.editor.EditingState;
import ch.entwine.weblounge.taglib.WebloungeTag;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.jsp.JspException;

/**
 * This tag inserts the code to show the workbench.
 */
public class WorkbenchTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -498800954917968929L;

  /** Path to the workbench script */
  public static final String WORKBENCH_SCRIPT = "<script src=\"/weblounge/steal/steal.js?editor,development\"></script>";

  /**
   * Writes the workbench script tag to the output.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    // if (!SecurityUtils.userHasRole(request.getUser(), SystemRole.EDITOR))
    // return super.doEndTag();

    if (request.getParameter(EditingState.WORKBENCH_PARAM) != null) {
      Cookie cookie = new Cookie(EditingState.STATE_COOKIE, "true");
      cookie.setPath("/");
      response.addCookie(cookie);
      writeWorkbenchScript();
      return super.doEndTag();
    }

    if (request.getCookies() == null)
      return super.doEndTag();
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(EditingState.STATE_COOKIE) && "true".equals(cookie.getValue())) {
        writeWorkbenchScript();
        break;
      }
    }

    return super.doEndTag();
  }

  private void writeWorkbenchScript() throws JspException {
    try {
      pageContext.getOut().write(WORKBENCH_SCRIPT);
    } catch (IOException e) {
      throw new JspException(e);
    }
  }

}
