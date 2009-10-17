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

import ch.o2it.weblounge.common.impl.security.Guest;
import ch.o2it.weblounge.common.impl.url.HistoryImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.UnsupportedLanguageException;
import ch.o2it.weblounge.common.request.History;
import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class keeps weblounge specific attributes in the session.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class SessionSupport {

	/** The language extraction regular expression */
	private static Pattern languageExtractor_ = Pattern.compile("_([a-zA-Z]+)\\.[\\w\\- ]+$");
	
	// Logging
	
	/** the class name, used for the loggin facility */
	private final static String className = SessionSupport.class.getName();
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);
	
	/**
	* Returns the user object associated with the current
	* session. If no user object can be found, a <code>guest</code>
	* user is assigned.
	* 
	* @param request the <code>HttpRequest</code>
	* @return the user associated with the current session
	*/
	public static AuthenticatedUser getUser(HttpServletRequest request) {
		SessionAttributes attribs = SessionSupport.getAttributes(request);
		AuthenticatedUser user = attribs.getUser();
		
		// if no valid user object has been found in the session, so this is the
		// visitor's first access to this site. Therefore, he/she is automatically
		// being logged in as guest.
		if (user == null)	{
			log_.debug("Assigning user = guest");
			user = new Guest(RequestSupport.getSite(request));
			attribs.setUser(user);
			return user;
		} else {
			return user;
		}
	}

	/**
	* Returns the language object associated with the current
	* session. If no language can be found, then the default
	* language for the current site is assigned.
	* 
	* @param request the <code>ServletRequest</code>
	* @return the language associated with the current session
	*/
	public static Language getLanguage(HttpServletRequest request) {
		Language language = null;
		
		// Check if language has already been evaluated. If so, it has been
		// put into the request
		
		if (request.getAttribute(Language.ID) instanceof Language) {
			language = (Language)request.getAttribute(Language.ID);
			if (language != null) {
				return language;
			}
		}
		
		// Check if there is explicit language information encoded in the
		// request url
		
		Site site = RequestSupport.getSite(request);
		SessionAttributes attribs = SessionSupport.getAttributes(request);
		Matcher m = languageExtractor_.matcher(request.getRequestURI());
		
		if (m.find()) {
			String langId = m.group(1);
			try {
			  language = site.getLanguage(langId);
				request.setAttribute(Language.ID, language);
				attribs.setLanguage(language);
				return language;
			} catch (Exception e) {
				log_.debug("no explicit language selector found");
			}
		}

		// Extract language from the session
		
		language = attribs.getLanguage();
		if (language != null) {
			request.setAttribute(Language.ID, language);
			return language;
		}
		
		// Try to get the default language from the request
		
		if (language == null) {
			Enumeration localeEnum = request.getLocales();
			while (localeEnum.hasMoreElements()) {
				Locale l = (Locale)localeEnum.nextElement();
				String languageId = l.getLanguage();
				try {
					language = site.getLanguage(languageId);
					log_.debug("Selected language " + languageId + " from user preferences");
					break;
				} catch (UnsupportedLanguageException e) { 
					log_.debug("Unsuccessfully tried language " + languageId);
				}
			}
		}
		
		// If no language has been found, the default language is selected
		
		if (language == null) {
			language = site.getDefaultLanguage();
			log_.debug("Assigning default language " + language.toString());
		}
		
		// If language still is null, then we have the case of a server
		// misconfiguration.
		
		if (language == null) {
			log_.info("Unable to assign default language in site '" + site + "'");
			return null;
		}
							
		attribs.setLanguage(language);
		request.setAttribute(Language.ID, language);
		return language;
	}

	/**
	* Returns the history object associated with the current
	* session. If no history object can be found, an empty one
	* is assigned.
	* 
	* @param request the <code>HttpRequest</code>
	* @return the history associated with the current session
	*/
	public static History getHistory(HttpServletRequest request) {
		SessionAttributes attribs = SessionSupport.getAttributes(request);
		History history = attribs.getHistory();
		if (history != null) {
			return history;
		}

		// No history has been found. This is the case if this is the first visit
		// of this user to the site.
		
		if (history == null) {
			history = new HistoryImpl(RequestSupport.getSite(request));
			history.addEntry(RequestSupport.getUrl(request));
			attribs.setHistory(history);
		}
		return history;
	}

	/**
	 * Returns the current move, which is one out of
	 * <ul>
	 * 	<li>Composer.MODE_EDIT</li>
	 * 	<li>Composer.MODE_LIVE</li>
	 * </ul>
	 * The mode either states that the user is currently observing the live
	 * view of the page or the editing view with the handles displayed
	 * 
	 * @param request the http servlet request
	 * @return the mode
	 */
	/*public static String getMode(HttpServletRequest request) {
		SessionAttributes attribs = SessionSupport.getAttributes(request);
		String mode = attribs.getMode();
		if (mode != null) {
			return mode;
		}
		
		// No mode has been set so far. Therefore, we assume mode "live"
		
		mode = Composer.MODE_LIVE;
		attribs.setMode(mode);
		return mode;
	}*/

	/**
	 * Returns the session attributes that are specific to weblounge.
	 * 
	 * @return the weblounge session attributes
	 */
	public static SessionAttributes getAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession();
		SessionAttributes attribs = (SessionAttributes)session.getAttribute(SessionAttributes.ID);
		if (attribs == null) {
			attribs = new SessionAttributes();
			session.setAttribute(SessionAttributes.ID, attribs);
		}
		return attribs;
	}

}