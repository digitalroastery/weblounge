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

package ch.o2it.weblounge.common.impl.request;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.ResponseCache;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Default implementation of the <code>WebloungeResponse</code>.
 */
public class WebloungeResponseImpl extends HttpServletResponseWrapper implements WebloungeResponse {

  /** the response state */
  private int state_ = STATE_SYSTEM_INITIALIZING;

  /** Flag for invalidated responses that should not be cached */
  private boolean isValid = false;

  /** HTTP error code */
  private int httpError = SC_OK;

  /** HTTP error message */
  private String httpErrorMsg = null;

  /** Associated HTTP request object */
  private WebloungeRequest request = null;

  /** The cache service */
  private ResponseCache cache = null;

  /** The response part's cache handle */
  private Stack<CacheHandle> cacheHandles = null;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = WebloungeResponseImpl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new <code>HttpServletResponse</code> wrapper around the original
   * response object.
   * 
   * @param response
   *          the response
   */
  public WebloungeResponseImpl(HttpServletResponse response) {
    super(response);
    state_ = STATE_SYSTEM_INITIALIZING;
    httpError = SC_OK;
  }

  /**
   * Sets the response state.
   * 
   * @param state
   *          the response state
   */
  public void setState(int state) {
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
   * Returns <code>true</code> if a request listener detects that a precondition
   * failed and triggered the <code>sendError</code> method.
   * 
   * @return <code>true</code> if a precondition failed
   */
  public boolean preconditionFailed() {
    return state_ == STATE_PRECONDITION_FAILED;
  }

  /**
   * Returns <code>true</code> if a request listener detects that a error
   * occurred while processing the request.
   * 
   * @return <code>true</code> if an error has occurred
   */
  public boolean processingFailed() {
    return state_ == STATE_PROCESSING_FAILED;
  }

  /**
   * Method to be called when an error is detected while processing the request.
   * <p>
   * <b>Note:</b> Call <code>super.sendError(error, msg)<code> when
   * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
   * 
   * @param error
   *          the HTTP error code
   * @param msg
   *          the error message
   * @see javax.servlet.http.HttpServletResponse#sendError(int,
   *      java.lang.String)
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
        state_ = STATE_PRECONDITION_FAILED;
        break;
      case STATE_SYSTEM_INITIALIZING:
        state_ = STATE_PRECONDITION_FAILED;
        break;

      case STATE_SITE_PROCESSING:
        notifySite = true;
        state_ = STATE_PROCESSING_FAILED;
        break;
      case STATE_SYSTEM_PROCESSING:
        state_ = STATE_PROCESSING_FAILED;
        break;
    }

    httpError = error;
    httpErrorMsg = msg;
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
        site = request.getSite();
        site.requestFailed(request, this, httpError);
      } catch (SiteNotFoundException e) {
        log_.warn("Site request failure notification failed for " + request);
      }
    }

  }

  /**
   * Method to be called when an error is detected while processing the request.
   * <p>
   * <b>Note:</b> Call <code>super.sendError(error)<code> when
   * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
   * 
   * @param error
   *          the HTTP error code
   * @see javax.servlet.http.HttpServletResponse#sendError(int)
   */
  public void sendError(int error) {
    sendError(error, null);
  }

  /**
   * Returns the <code>HTTP</code> error code.
   * 
   * @return the error code
   */
  public int getError() {
    return httpError;
  }

  /**
   * Returns the <code>HTTP</code> error message, which may be <code>null</code>
   * .
   * 
   * @return the error message
   */
  String getErrorMessage() {
    return httpErrorMsg;
  }

  /**
   * Sets the associated request object.
   * 
   * @param request
   *          the request
   */
  public void setRequest(WebloungeRequest request) {
    this.request = request;
  }

  /**
   * Sets the service that is used to cache responses to clients.
   * 
   * @param cache
   *          the cache
   */
  public void setCache(ResponseCache cache) {
    this.cache = cache;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#addTag(java.lang.String,
   *      java.lang.Object)
   */
  public boolean addTag(String name, Object value) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTag(name, value);
      result |= cacheHandles.peek().addTag(name, value);
    }
    return result;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<Tag> tags) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTags(tags);
      result |= cacheHandles.peek().addTags(tags);
    }
    return result;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#addTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean addTag(Tag tag) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTag(tag);
      result |= cacheHandles.peek().addTag(tag);
    }
    return result;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#clearTags()
   */
  public void clearTags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      cacheHandles.peek().clearTags();
    }
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean containsTag(Tag tag) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().containsTag(tag);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String name) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().containsTag(name);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean containsTag(String name, String value) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().containsTag(name, value);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#isTagged()
   */
  public boolean isTagged() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().isTagged();
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#removeTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean removeTag(Tag tag) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().removeTag(tag);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#removeTags(java.lang.String)
   */
  public boolean removeTags(String name) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().removeTags(name);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#removeTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean removeTag(String name, String value) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().removeTag(name, value);
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#tags()
   */
  public Iterator<Tag> tags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().tags();
    }
    return new ArrayList<Tag>().iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#getTags()
   */
  public Tag[] getTags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().getTags();
    }
    return new Tag [] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#startResponse(java.lang.Iterable,
   *      long, long)
   */
  public boolean startResponse(Iterable<Tag> tags, long validTime,
      long recheckTime) throws IllegalStateException {
    if (!isValid || cache == null)
      return false;
    if (cacheHandles != null)
      throw new IllegalStateException("The response is already being cached");
    
    // Is the response in the cache?
    CacheHandle hdl = cache.startResponse(tags, request, this, validTime, recheckTime);
    if (hdl == null)
      return true;
    
    // It's not
    cacheHandles = new Stack<CacheHandle>();
    cacheHandles.push(hdl);
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#startResponsePart(java.lang.Iterable,
   *      long, long)
   */
  public boolean startResponsePart(Iterable<Tag> uniqueTags, long validTime,
      long recheckTime) {
    if (!isValid || cache == null)
      return false;
    if (cacheHandles == null)
      throw new IllegalStateException("Cache root entry is missing");

    // Do a cache lookup
    CacheHandle handle = null;
    try {
      handle = cache.startResponsePart(uniqueTags, this, validTime, recheckTime);
    } catch (Exception e) {
      log_.warn("Error starting response part in cache", e);
      return false;
    }

    // Is the response part in the cache?
    if (handle == null)
      return true;

    // It's not in the cache. Make sure the stack is set up and push the
    // handle onto of it.
    cacheHandles.push(handle);
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#endResponse()
   */
  public void endResponse() throws IllegalStateException {
    if (!isValid || cache == null)
      return;
    if (cacheHandles == null)
      throw new IllegalStateException("Cache root entry is missing");
    if (cacheHandles.size() > 1)
      throw new IllegalStateException("Unfinished response parts detected");
    
    // End the response and have the output sent back to the client
    try {
      cache.endResponse(this);
    } catch (Exception e) {
      log_.warn("Error sending end of response to cache", e);
    } finally {
      cacheHandles.clear();
      cacheHandles = null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#endResponsePart(ch.o2it.weblounge.common.request.CacheHandle)
   */
  public void endResponsePart() {
    if (!isValid || cache == null)
      return;
    if (cacheHandles == null || cacheHandles.size() < 1)
      throw new IllegalStateException("No response part has been started");
    CacheHandle handle = cacheHandles.pop();
    try {
      cache.endResponsePart(handle, this);
    } catch (Exception e) {
      log_.warn("Error sending end of response part to cache", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#invalidate()
   */
  public void invalidate() {
    isValid = false;
    cacheHandles.clear();
    cacheHandles = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#isValid()
   */
  public boolean isValid() {
    return isValid;
  }

}