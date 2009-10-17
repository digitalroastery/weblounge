/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.security.SystemPermission;

/**
 * Specialized security context for a pagelet. This implementation adds the proper
 * name and default values.
 */
public class PageletSecurityContextImpl extends PermissionSecurityContext {

	/**
	 * Creates a new security context for a pagelet with the specified url.
	 * 
	 * @param module the pagelet module
	 * @param id the pagelet id
	 */
	public PageletSecurityContextImpl(String module, String id) {
		addDefaultValues();
	}

	/**
	 * Adds the default authorities to their respective permissions.
	 */
	private void addDefaultValues() {
		allowDefault(SystemPermission.READ, SystemRole.GUEST);
		allowDefault(SystemPermission.TRANSLATE, SystemRole.TRANSLATOR);
		allowDefault(SystemPermission.WRITE, SystemRole.EDITOR);
		allowDefault(SystemPermission.MANAGE, SystemRole.EDITOR);
		allowDefault(SystemPermission.PUBLISH, SystemRole.PUBLISHER);
	}

}