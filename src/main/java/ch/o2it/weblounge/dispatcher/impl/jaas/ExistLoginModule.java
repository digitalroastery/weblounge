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

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.o2it.weblounge.common.impl.security.jaas.AbstractLoginModule;
import ch.o2it.weblounge.common.request.WebloungeRequest;

/**
 * Implementation of a login module which will look up the users and passwords
 * from the exist database.
 */
public class ExistLoginModule extends AbstractLoginModule {

	// Logging

	/** the class name, used for the logging facility */
	private final static String className = ExistLoginModule.class.getName();

	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * @see ch.o2it.weblounge.core.security.jaas.AbstractLoginModule#checkUserAndPassword()
	 */
	protected boolean checkUserAndPassword() throws LoginException {
		// TODO: implement
//		if (callbackHandler instanceof HttpAuthCallback) {
//			WebloungeRequest request = ((HttpAuthCallback)callbackHandler).getRequest();
//
//			// Connect to database
//			DBXMLDatabase db = (DBXMLDatabase)ServiceManager.getEnabledSystemService(DBXMLDatabase.ID);
//			if (db == null) {
//				log_.warn("Unable to authenticate with eXist database: xmldb service is not available!");
//				return false;
//			}
//			
//			// Get root collection
//			Collection c = db.getCollection("/", false);
//			if (c == null) {
//				log_.warn("Unable to authenticate with eXist database: root collection is not available!");
//				return false;
//			}
//
//			UserManagementService service;
//			try {
//				service = (UserManagementService)c.getService("UserManagementService", "1.0");
//				User existUser = service.getUser(username);
//				if (existUser != null) {
//					String p = existUser.getPassword();
//					String pass = new String(password);
//					if ((p != null && p.equals(existUser.digest(pass))) || (p == null && password.length == 0)) {
//						user = new ExistUserImpl(username, pass, ((HttpAuthCallback)callbackHandler).getRequest().getSite());
//						return checkPath(request);
//					} else {
//						throw new FailedLoginException("Wrong password");
//					}
//				}
//				return false;
//			} catch (XMLDBException e) {
//				return false;
//			} finally {
//				db.returnCollection(c);
//			}
//		} else {
//			log_.warn("Exist login module received unknown callback handler!");
//			return false;
//		}
		return false;
	}
	
	/**
	 * Checks if the database user tries to access a path different from the xmlrpc
	 * servlet path.
	 * 
	 * @param request the weblounge request
	 * @return <code>true</code> if the path accessed is valid
	 * @throws LoginException if the path is invalid
	 */
	private boolean checkPath(WebloungeRequest request) throws LoginException {
		// TODO: Check this
//		RequestHandlerRegistry handlers = (RequestHandlerRegistry)SystemRegistries.get(RequestHandlerRegistry.ID);
//		ServletRequestHandler servletHandler = (ServletRequestHandler)handlers.get(ServletRequestHandler.ID);
//		if (servletHandler == null) {
//			throw new LoginException("Login to database is not possible: Servlet handler not found!");
//		}
//		if (!(servletHandler.getServlet(request) instanceof XmlRpcServlet)) {
//			throw new LoginException("Login is restricted to database");
//		}
//		return true;
		return true;
	}

	/**
	 * Returns a string identifier for this login context. The identifier is used to store and identify
	 *  user profile data in the weblounge database.
	 *
	 * @return the login module identifier
	 */
	public String getNamespace() {
		return "exist-admins";
	}

}