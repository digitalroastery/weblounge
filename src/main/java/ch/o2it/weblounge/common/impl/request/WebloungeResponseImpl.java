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

import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.ResponseCache;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.io.IOException;
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

  /** Flag for invalidated responses that should not be cached */
  private boolean isValid = true;

  /** True if an error has been reported */
  private boolean hasError = false;

  /** Response status */
  private int responseStatus = SC_OK;

  /** Associated HTTP request object */
  private WebloungeRequest request = null;

  /** The cache service */
  private ResponseCache cache = null;

  /** The response part's cache handle */
  private Stack<CacheHandle> cacheHandles = null;

  /**
   * Creates a new <code>HttpServletResponse</code> wrapper around the original
   * response object.
   * 
   * @param response
   *          the response
   */
  public WebloungeResponseImpl(HttpServletResponse response) {
    super(response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int,
   *      java.lang.String)
   */
  public void sendError(int error, String msg) throws IOException {
    hasError = true;
    responseStatus = error;
    super.sendError(error, msg);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
   */
  public void sendError(int error) throws IOException {
    sendError(error, null);
  }

  /**
   * Returns <code>true</code> if an error code has been sent back to the
   * client.
   * 
   * @return <code>true</code> if an error code has been sent to the client
   */
  public boolean hasError() {
    return hasError;
  }

  /**
   * Returns the value of the response status. The value will match
   * <code>SC_OK</code> as long as no error has been reported.
   * 
   * @return the status code
   */
  public int getResponseStatus() {
    return responseStatus;
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
  public void setResponseCache(ResponseCache cache) {
    this.cache = cache;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#addTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean addTag(String name, String value) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTag(name, value);
      result |= cacheHandles.peek().addTag(name, value);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<CacheTag> tags) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTags(tags);
      result |= cacheHandles.peek().addTags(tags);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#addTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean addTag(CacheTag tag) {
    boolean result = false;
    if (cacheHandles != null && cacheHandles.size() > 0) {
      result |= cacheHandles.firstElement().addTag(tag);
      result |= cacheHandles.peek().addTag(tag);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#clearTags()
   */
  public void clearTags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      cacheHandles.peek().clearTags();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean containsTag(CacheTag tag) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().containsTag(tag);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String name) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().containsTag(name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#isTagged()
   */
  public boolean isTagged() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().isTagged();
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#removeTag(ch.o2it.weblounge.common.content.Tag)
   */
  public boolean removeTag(CacheTag tag) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().removeTag(tag);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#removeTags(java.lang.String)
   */
  public boolean removeTags(String name) {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().removeTags(name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#tags()
   */
  public Iterator<CacheTag> tags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().tags();
    }
    return new ArrayList<CacheTag>().iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#getTags()
   */
  public CacheTag[] getTags() {
    if (cacheHandles != null && cacheHandles.size() > 0) {
      return cacheHandles.peek().getTags();
    }
    return new CacheTag[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#startResponse(ch.o2it.weblounge.common.request.CacheTag[],
   *      long, long)
   */
  public boolean startResponse(CacheTag[] tags, long validTime, long recheckTime)
      throws IllegalStateException {
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
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#startResponsePart(ch.o2it.weblounge.common.request.CacheTag[],
   *      long, long)
   */
  public boolean startResponsePart(CacheTag[] uniqueTags, long validTime,
      long recheckTime) {
    if (!isValid || cache == null)
      return false;
    if (cacheHandles == null)
      throw new IllegalStateException("Cache root entry is missing");

    // Do a cache lookup
    CacheHandle handle = null;
    handle = cache.startResponsePart(uniqueTags, request, this, validTime, recheckTime);

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

    // End the response and have the output sent back to the client
    try {
      if (cacheHandles.size() > 1)
        throw new IllegalStateException("Unfinished response parts detected");
      cache.endResponse(this);
    } finally {
      cacheHandles.clear();
      cacheHandles = null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#endResponsePart()
   */
  public void endResponsePart() {
    if (!isValid || cache == null)
      return;
    if (cacheHandles == null || cacheHandles.size() < 1)
      throw new IllegalStateException("No response part has been started");
    CacheHandle handle = cacheHandles.pop();
    cache.endResponsePart(handle, this);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#setRecheckTime(long)
   */
  public void setRecheckTime(long recheckTime) {
    if (cacheHandles == null)
      return;
    CacheHandle h = cacheHandles.peek();
    if (h != null) {
      h.setRecheck(recheckTime);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#setValidTime(long)
   */
  public void setValidTime(long validTime) {
    if (cacheHandles == null)
      return;
    CacheHandle h = cacheHandles.peek();
    if (h != null) {
      h.setExpires(validTime);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.WebloungeResponse#invalidate()
   */
  public void invalidate() {
    isValid = false;
    if (cacheHandles != null) {
      cacheHandles.clear();
      cacheHandles = null;
    }
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