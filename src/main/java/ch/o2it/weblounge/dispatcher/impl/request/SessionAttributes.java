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

package ch.o2it.weblounge.dispatcher.impl.request;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.History;
import ch.o2it.weblounge.common.security.AuthenticatedUser;

/**
 * The <code>SessionAttributes</code> are placed in the current session,
 * so information about user, history etc. are only evaluated once and can thereafter
 * be gathered from this object.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class SessionAttributes {

	/** Constant identifying the session attributes object within the session */
	public final static String ID = "weblounge-session";
	
	/** the current user */
	private AuthenticatedUser user_;
	
	/** the user's session history */
	private History history_;
	
	/** the current language */
	private Language language_;
	
	/** the session mode */
	private String mode_;
	
	/**
	 * Sets the user.
	 * 
	 * @param user the user
	 */
	public void setUser(AuthenticatedUser user) {
		user_ = user;
	}
	
	/**
	 * Returns the user that sent off this request.
	 * 
	 * @return the user
	 */
	public AuthenticatedUser getUser() {
		return user_;
	}

	/**
	 * Sets the session history.
	 * 
	 * @param history the history
	 */
	void setHistory(History history) {
		history_ = history;
	}
	
	/**
	 * Returns the user's session history.
	 * 
	 * @return the history
	 */
	public History getHistory() {
		return history_;
	}
	
	/**
	 * Sets the session language.
	 * 
	 * @param language the language
	 */
	public void setLanguage(Language language) {
		language_ = language;
	}
	
	/**
	 * Returns the session language.
	 * 
	 * @return the language
	 */
	public Language getLanguage() {
		return language_;
	}

	/**
	 * Sets the session mode.
	 * 
	 * @param mode the mode
	 */
	public void setMode(String mode) {
		mode_ = mode;
	}
	
	/**
	 * Returns the session mode.
	 * 
	 * @return the mode
	 */
	public String getMode() {
		return mode_;
	}
	
}