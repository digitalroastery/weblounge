/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl.jaas;

import ch.o2it.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.security.jaas.AbstractLoginModule;
import ch.o2it.weblounge.common.impl.security.jaas.HttpAuthCallback;
import ch.o2it.weblounge.common.impl.user.WebloungeUserImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.WebloungeUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Arrays;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Implementation of the Weblounge login module, which will login users
 * according the the Weblounge user database.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public class WebloungeLoginModule extends AbstractLoginModule {
	
	/** The weblounge login module namespace */
	public final static String WEBLOUNGE_REALM = "weblounge";

	// Logging

	/** the class name, used for the logging facility */
	private final static String className = WebloungeLoginModule.class.getName();

	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * @see ch.o2it.weblounge.core.security.jaas.AbstractLoginModule#checkUserAndPassword()
	 */
	protected boolean checkUserAndPassword() throws LoginException {
		Site site = null;
		if (callbackHandler instanceof HttpAuthCallback) {
			site = ((HttpAuthCallback)callbackHandler).getRequest().getSite();
			user = site.getUser(username);
			if (user != null) {
				if (!((WebloungeUser)user).isEnabled()) {
					throw new FailedLoginException("Login disabled");
				}
				succeeded = ((WebloungeUser)user).checkPassword(new String(password));
				if (succeeded) {
					return true;
				} else {
					throw new FailedLoginException("Wrong password");
				}
			}
			return false;
		}
		log_.error("Login module was presented with an unknown callback handler!");
		return false;
	}

	/**
	 * Logout the user.
	 * <p>
	 * This method removes the <code>Principal</code> that was added by
	 * the <code>commit</code> method.
	 * 
	 * @exception LoginException if the logout fails.
	 * @return true in all cases since this <code>LoginModule</code> should
	 *                not be ignored.
	 */
	public boolean logout() throws LoginException {
		if (!super.logout())
			return false;
		
		// Remove user from session
		if (callbackHandler != null && callbackHandler instanceof HttpAuthCallback) {
			WebloungeRequest request = ((HttpAuthCallback)callbackHandler).getRequest();
			request.getSession(true).removeAttribute(WebloungeRequestImpl.SESSION_USER);
		}		
		return true;
	}
	
	/**
	 * <p>
	 * This method is called if the LoginContext's overall authentication
	 * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
	 * LoginModules succeeded).
	 * <p>
	 * If this LoginModule's own authentication attempt succeeded (checked by
	 * retrieving the private state saved by the <code>login</code> method),
	 * then this method associates a <code>User</code> with the
	 * <code>Subject</code> located in the <code>LoginModule</code>. If
	 * this LoginModule's own authentication attempted failed, then this method
	 * removes any state that was originally saved.
	 * 
	 * @exception LoginException if the commit fails.
	 * @return true if this LoginModule's own login and commit attempts
	 *                succeeded, or false otherwise.
	 */
	public boolean commit() throws LoginException {
		if (!succeeded) {
			return false;
		} else {
			subject.getPublicCredentials().add(SystemRole.GUEST);
			subject.getPublicCredentials().addAll(Arrays.asList(user.getGroups()));
			subject.getPublicCredentials().add(user.getRoles());
		}
		return super.commit();
	}
	
	/**
	 * Returns a string identifier for this login context. The identifier is used to store and identify
	 *  user profile data in the weblounge database.
	 *
	 * @return the login module identifier
	 */
	public String getNamespace() {
		return "weblounge";
	}

}