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
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteNotFoundException;

/**
 * Wrapper for a request that is passing the weblounge content management
 * system.
 */

public class WebloungeResponseImpl extends HttpServletResponseWrapper implements WebloungeResponse {
	
	/** the response state */
	private int state_;
	
	/** Flag for invalidated responses that should not be cached */
	private boolean invalidated = false;
	
	/** the http error code */
	private int httpError_ = 200;
	
	/** the http error message */
	private String httpErrorMsg_ = null;
	
	/** the servlet request */
	private WebloungeRequest request_ = null;

	// Logging
	
	/** the class name, used for the logging facility */
	private final static String className = WebloungeResponseImpl.class.getName();
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * Creates a new <code>HttpServletResponse</code> wrapper around
	 * the original response object.
	 * 
	 * @param response the response
	 */
	public WebloungeResponseImpl(HttpServletResponse response) {
		super(response);
		state_ = STATE_SYSTEM_INITIALIZING;
	}

	/**
	 * Sets the response state.
	 * 
	 * @param state the response state
	 */
	void setState(int state) {
		state_ = state;
	}
	
	/**
	 * Returns the response state.
	 * 
	 * @return the response state
	 */
	int getState() {
		return state_;
	}
	
	/**
	 * Returns <code>true</code> if a request listener detetcts that a
	 * precondition failed and triggered the <code>sendError</code>
	 * method.
	 * 
	 * @return <code>true</code> if a precondition failed
	 */
	public boolean preconditionFailed() {
		return state_ == STATE_PRECONDITION_FAILED;
	}

	/**
	 * Returns <code>true</code> if a request listener detetcts that a
	 * error occured while processing the request.
	 * 
	 * @return <code>true</code> if an error has occured
	 */
	public boolean processingFailed() {
		return state_ == STATE_PROCESSING_FAILED;
	}

	/**
	 * Method to be called when an error is detected while processing
	 * the request.
	 * <p>
	 * <b>Note:</b> Call <code>super.sendError(error, msg)<code> when
	 * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
	 * 
	 * @param error the HTTP error code
	 * @param msg the error message
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int error, String msg) {
		boolean notifySite = false;
		switch (state_) {
			
			// We already had an error. Therefore ignore any other
			// error sending
			case STATE_PRECONDITION_FAILED:
			case STATE_PROCESSING_FAILED:
				return;
			
			case STATE_SITE_INITIALIZING:
				notifySite = true;
			case STATE_SYSTEM_INITIALIZING:
				state_ = STATE_PRECONDITION_FAILED;
				break;

			case STATE_SITE_PROCESSING:
				notifySite = true;
			case STATE_SYSTEM_PROCESSING:
				state_ = STATE_PROCESSING_FAILED;
				break;
		}
		
		httpError_ = error;
		httpErrorMsg_ = msg;
		try {
			if (msg == null)
				super.sendError(error);
			else
				super.sendError(error, msg);
			log_.debug("Error '" + msg + "' written to response");
		} catch (Exception e) {
			log_.error("I/O Error when sending back error message " + error + "!");
		}

		Site site = null;
		if (notifySite) {
			try {
				site = request_.getSite();
				site.requestFailed(request_, this, httpError_);
			} catch (SiteNotFoundException e) {
				log_.warn("Site request failure notification failed for " + request_);
			}
		}

	}

	/**
	 * Method to be called when an error is detected while processing
	 * the request.
	 * <p>
	 * <b>Note:</b> Call <code>super.sendError(error, msg)<code> when
	 * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
	 * 
	 * @param error the HTTP error code
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int error) {
		sendError(error, null);
	}
	
	/**
	 * Returns the http error code.
	 * 
	 * @return the error code
	 */
	int getError() {
		return httpError_;
	}
	
	/**
	 * Returns the http error message, which may be <code>null</code>.
	 * 
	 * @return the error message
	 */
	String getErrorMessage() {
		return httpErrorMsg_;
	}
	
	/**
	 * Sets the associated request object.
	 * 
	 * @param request the request
	 */
	void setRequest(WebloungeRequest request) {
		request_ = request;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#addTag(java.lang.String, java.lang.Object)
	 */
	public boolean addTag(String name, Object value) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#addTags(java.util.Collection)
	 */
	public boolean addTags(Collection<Tag> tags) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#addTag(ch.o2it.weblounge.common.content.Tag)
	 */
	public boolean addTag(Tag tag) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#clearTags()
	 */
	public void clearTags() { }

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#containsTag(ch.o2it.weblounge.common.content.Tag)
	 */
	public boolean containsTag(Tag tag) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String)
	 */
	public boolean containsTag(String name) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String, java.lang.String)
	 */
	public boolean containsTag(String name, String value) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#getTags(ch.o2it.weblounge.common.content.Tag[])
	 */
	public Tag[] getTags(Tag[] tag) {
		return new Tag[] {};
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#isTagged()
	 */
	public boolean isTagged() {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#removeTag(ch.o2it.weblounge.common.content.Tag)
	 */
	public boolean removeTag(Tag tag) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#removeTag(java.lang.String)
	 */
	public boolean removeTag(String name) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#removeTag(java.lang.String, java.lang.String)
	 */
	public boolean removeTag(String name, String value) {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.content.Taggable#tags()
	 */
	public Iterator<Tag> tags() {
		return (new ArrayList<Tag>()).iterator();
	}

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Taggable#getTags()
   */
  public Tag[] getTags() {
    return new Tag[]{};
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#invalidate()
   */
  public void invalidate() {
    this.invalidated = true;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#isValid()
   */
  public boolean isValid() {
    return !invalidated;
  }

}