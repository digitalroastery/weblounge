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

import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.user.AuthenticatedUser;
import ch.o2it.weblounge.common.user.User;

import javax.servlet.jsp.JspException;

/**
 * Checks if the user meets all permission requirements. If this is the case,
 * the tag body is included, otherwise it is discarded.
 */
public class IfPermissionTag extends PermissionCheckTag {

  /** Serial version uid */
  private static final long serialVersionUID = -2589529325057150975L;

  /**
   * Process the start tag for this instance and check whether all permission
   * requirements are met.
   * 
   * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    Securable secured = getSecured();
    User user = request.getUser();
    if (user instanceof AuthenticatedUser) {
      AuthenticatedUser authenticatedUser = (AuthenticatedUser)user;
      if (secured != null && secured.check(getPermissions(), authenticatedUser))
        return EVAL_BODY_INCLUDE;
      else
        return SKIP_BODY;
    }
    return SKIP_BODY;
  }

}
