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

package ch.entwine.weblounge.taglib.security;

import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

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