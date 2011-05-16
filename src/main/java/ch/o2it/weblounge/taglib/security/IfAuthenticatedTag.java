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

package ch.o2it.weblounge.taglib.security;

import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * Checks if a user is authenticated. The body content is only evaluated, if the
 * user is logged in.
 */
public class IfAuthenticatedTag extends WebloungeTag {

  /** serial version uid */
  private static final long serialVersionUID = 5576649176077112454L;

  /**
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    super.doStartTag();
    User user = getRequest().getUser();
    if (!(user instanceof AuthenticatedUser))
      return SKIP_BODY;
    return EVAL_BODY_INCLUDE;
  }

}
