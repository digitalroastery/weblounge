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

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import javax.servlet.jsp.JspException;

/**
 * Checks if the user meets all role requirements. If this is the case, the tag body
 * is included, otherwise it is discarded.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class IfRoleTag extends RoleCheckTag {
	
	/** serial version uid */
  private static final long serialVersionUID = -1617400257793452113L;

  /**
	 * Process the start tag for this instance and check wether all role requirements
	 * are met.
	 * 
	 * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();

		WebloungeRequest request = getRequest(); 
		User user = request.getUser();
		Site site = request.getSite();
		if (hasOneOf(user, site) && hasAllOf(user, site))
			return EVAL_BODY_INCLUDE;
		else
			return SKIP_BODY;
	}
	
}