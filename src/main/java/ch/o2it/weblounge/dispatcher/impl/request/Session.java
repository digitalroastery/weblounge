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

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session is a non-operational helper class to examine the
 * attributes of the current session.
 *
 * @author Tobias Wunden
 * @version   1.0 Sat Sep 14 2002
 * @since    WebLounge 1.0
 */ 

public class Session {

	// Logging
	
	/** the class name, used for the logging facility */
	private final static String className = Session.class.getName();
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

	/**
    * Constructor for class Session.
    */
    private Session() { }

	public static void dump(HttpSession session) {
		Enumeration ae = session.getAttributeNames();
		while (ae.hasMoreElements()) {
			String name = ae.nextElement().toString();
			String attribute = session.getAttribute(name).toString();
			log_.info(name + "=" + attribute);
		}
	}

}