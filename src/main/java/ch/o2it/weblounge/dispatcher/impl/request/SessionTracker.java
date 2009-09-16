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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * SessionTracker is a class to examine movements and online state of 
 * site visitors.
 *
 * @author	Tobias Wunden
 * @version	1.0
 * @since	WebLounge 1.0
 */ 

public class SessionTracker implements UserListener {

	/** the active users */
	private List<User> activeUsers_;
	
	// Logging
	
	/** the class name, used for the loggin facility */
	private final static String className = SessionTracker.class.getName();
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

   /**
    * Constructor for class SessionTracker.
    */
    public SessionTracker() { 
    	activeUsers_ = new ArrayList<User>(100);
    }

   /**
    * Returns the number of active users.
    * 
    * @return the number of active users
    */
	public int getActiveUsers() {
		return activeUsers_.size();
	}
	
   /**
	* Returns <code>true</code> if <code>user</code> is online. Note
	* that due to the nature of sessions, a user may already be offlilne although
	* its session has not yet been cleared.
	* 
	* @param user the user in question
	* @return <code>true</code> if the user is online
	*/
	public boolean isOnline(User user) {
		return activeUsers_.contains(user);
	}
	
   /**
	* Returns an iteration of those users that are currently considered to be
	* online.
	* 
	* @return an iteration of active users
	*/
	public Iterator users() {
		return activeUsers_.iterator();
	}


   /* -------------------------------------------------------------
	* I M P L E M E N T A T I O N   O F   UserListener
	* -------------------------------------------------------------
	*/

   /**
    * Notification that a user has moved.
    * 
    * @param user the user that moved
    * @param url the target url
    */
	public void userMoved(User user, WebUrl url) {
		log_.debug("User " + user + " moved to " + url);
	}

   /**
    * Notification that a user has logged in.
    * 
    * @param user the user that logged in
    */
	public void userLoggedIn(User user) {
		log_.debug("User " + user + " logged in");
		if (!activeUsers_.contains(user)) {
			activeUsers_.add(user);
		}
	}

   /**
    * Notification that a user has logged out.
    * 
    * @param user the user that logged out
    */	
	public void userLoggedOut(User user) {
		log_.debug("User " + user + " logged out");
		activeUsers_.remove(user);
	}
	
}