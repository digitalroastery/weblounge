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

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.editor.EditingState;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.SecurityUtils;
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
  public static final String WORKBENCH_SCRIPT = "<script src=\"/%1$2s/steal/steal.js?editor,%2$2s\"></script>";

  /** Development identifier for steal */
  private static final String STEAL_DEVELOPMENT = "development";

  /** Production identifier for steal */
  private static final String STEAL_PRODUCTION = "production";

  /** Default path to the workbench ui */
  private static final String DEFAULT_WORKBENCH_PATH = "weblounge";

  /** The workbench path */
  private String workbenchPath = DEFAULT_WORKBENCH_PATH;

  /**
   * Sets the workbench path.
   * 
   * @param workbenchPath
   *          the path to the workbench ui
   */
  public void setUri(String workbenchPath) {
    this.workbenchPath = workbenchPath;
  }

  /**
   * Writes the workbench script tag to the output.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    boolean isMockRequest = RequestUtils.isMockRequest(request);

    // If the user does not have editing rights, make sure the
    // cookie is deleted. This is likely to be the case after the user
    // has hit the logout button or after the session has expired.

    boolean isEditor = SecurityUtils.userHasRole(request.getUser(), SystemRole.EDITOR);

    if (!isEditor && !isMockRequest) {

      // Make sure the cookie is removed on the client side
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie c : cookies) {
          if (EditingState.STATE_COOKIE.equals(c.getName())) {
            Cookie cookie = new Cookie(EditingState.STATE_COOKIE, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
          }
        }
      }

      // Make sure to remove the session flag
      request.getSession().removeAttribute(EditingState.STATE_COOKIE);

      return super.doEndTag();
    }

    if (request.getParameter(EditingState.WORKBENCH_PREVIEW_PARAM) != null) {
      return super.doEndTag();
    }

    // Is the ?edit parameter in place?
    boolean enterWorkbench = request.getParameter(EditingState.WORKBENCH_PARAM) != null;
    if (enterWorkbench && !isMockRequest) {
      Cookie cookie = new Cookie(EditingState.STATE_COOKIE, "true");
      cookie.setPath("/");
      response.addCookie(cookie);
      request.getSession().setAttribute(EditingState.STATE_COOKIE, Boolean.TRUE);
      writeWorkbenchScript(getRequest(), getResponse());
      return super.doEndTag();
    } else if (RequestUtils.isEditingState(request)) {
      writeWorkbenchScript(request, response);
    }

    return super.doEndTag();
  }

  private void writeWorkbenchScript(WebloungeRequest request,
      WebloungeResponse response) throws JspException {

    // Determine the environment for the steal.js script
    String environment = null;
    switch (request.getEnvironment()) {
      case Development:
        environment = STEAL_DEVELOPMENT;
        break;
      default:
        environment = STEAL_PRODUCTION;
    }

    Page page = (Page) request.getAttribute(WebloungeRequest.PAGE);

    // Add the current path to header for the editor
    response.addHeader("path", page.getPath());

    try {
      pageContext.getOut().write("<script> window.currentLanguage = '" + request.getLanguage().getIdentifier() + "';");
      pageContext.getOut().write("window.currentPagePath = '" + page.getPath() + "';</script>");
      pageContext.getOut().write(String.format(WORKBENCH_SCRIPT, workbenchPath, environment));
    } catch (IOException e) {
      throw new JspException(e);
    }
  }

}
