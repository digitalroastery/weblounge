/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.request;

import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.ResponseCache;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
  private WeakReference<WebloungeRequest> request = null;

  /** The cache service */
  private WeakReference<ResponseCache> cache = null;

  /** The response's cache handle */
  private WeakReference<CacheHandle> cacheHandle = null;

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
   * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
   */
  @Override
  public void sendRedirect(String location) throws IOException {
    super.sendRedirect(location);
    responseStatus = HttpServletResponse.SC_MOVED_PERMANENTLY;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int,
   *      java.lang.String)
   */
  @Override
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
  @Override
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
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
   */
  @Override
  public void setStatus(int sc) {
    super.setStatus(sc);
    responseStatus = sc;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#getStatus()
   */
  public int getStatus() {
    return responseStatus;
  }

  /**
   * Sets the associated request object.
   * 
   * @param request
   *          the request
   */
  public void setRequest(WebloungeRequest request) {
    this.request = new WeakReference<WebloungeRequest>(request);
  }

  /**
   * Sets the service that is used to cache responses to clients.
   * 
   * @param cache
   *          the cache
   */
  public void setResponseCache(ResponseCache cache) {
    this.cache = new WeakReference<ResponseCache>(cache);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean addTag(String name, String value) {
    boolean result = false;
    if (cacheHandle != null && cacheHandle.get() != null) {
      result = cacheHandle.get().addTag(name, value);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<CacheTag> tags) {
    boolean result = false;
    if (cacheHandle != null && cacheHandle.get() != null) {
      result = cacheHandle.get().addTags(tags);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean addTag(CacheTag tag) {
    boolean result = false;
    if (cacheHandle != null && cacheHandle.get() != null) {
      result = cacheHandle.get().addTag(tag);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#clearTags()
   */
  public void clearTags() {
    if (cacheHandle != null && cacheHandle.get() != null) {
      cacheHandle.get().clearTags();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean containsTag(CacheTag tag) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().containsTag(tag);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String name) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().containsTag(name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean containsTag(String name, String value) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().containsTag(name, value);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#isTagged()
   */
  public boolean isTagged() {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().isTagged();
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean removeTag(CacheTag tag) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().removeTag(tag);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTags(java.lang.String)
   */
  public boolean removeTags(String name) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().removeTags(name);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean removeTag(String name, String value) {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().removeTag(name, value);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#tags()
   */
  public Iterator<CacheTag> tags() {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().tags();
    }
    return new ArrayList<CacheTag>().iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#getTags()
   */
  public CacheTag[] getTags() {
    if (cacheHandle != null && cacheHandle.get() != null) {
      return cacheHandle.get().getTags();
    }
    return new CacheTag[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#startResponse(ch.entwine.weblounge.common.request.CacheTag[],
   *      long, long)
   */
  public boolean startResponse(CacheTag[] tags, long validTime, long recheckTime)
      throws IllegalStateException {
    if (!isValid || cache == null)
      return false;
    ResponseCache cache = this.cache.get();
    if (cache == null)
      return false;
    if (cacheHandle != null)
      throw new IllegalStateException("The response is already being cached");

    // Is the response in the cache?
    CacheHandle hdl = cache.startResponse(tags, request.get(), this, validTime, recheckTime);
    if (hdl == null)
      return true;

    // It's not, meaning we need to do the processing ourselves
    cacheHandle = new WeakReference<CacheHandle>(hdl);
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#endResponse()
   */
  public void endResponse() throws IllegalStateException {
    try {
      if (cache == null)
        return;
      ResponseCache cache = this.cache.get();
      if (cache == null)
        return;
      if (cacheHandle == null || cacheHandle.get() == null)
        return;

      // End the response and have the output sent back to the client
      cache.endResponse(this);
    } finally {
      try {
        flushBuffer();
      } catch (IOException e) {
        // The client closed the connection
      }
      cacheHandle = null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#setClientRevalidationTime(long)
   */
  public void setClientRevalidationTime(long recheckTime) {
    if (cacheHandle == null)
      return;
    CacheHandle hdl = cacheHandle.get();
    if (hdl == null)
      return;
    hdl.setClientRevalidationTime(Math.min(recheckTime, hdl.getClientRevalidationTime()));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#getClientRevalidationTime()
   */
  public long getClientRevalidationTime() {
    if (cacheHandle == null)
      return 0;
    CacheHandle hdl = cacheHandle.get();
    if (hdl == null)
      return 0;
    return hdl.getClientRevalidationTime();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#setCacheExpirationTime(long)
   */
  public void setCacheExpirationTime(long validTime) {
    if (cacheHandle == null)
      return;
    CacheHandle hdl = cacheHandle.get();
    if (hdl == null)
      return;
    hdl.setCacheExpirationTime(Math.min(validTime, hdl.getCacheExpirationTime()));

    // The recheck time can't be longer than the valid time
    setClientRevalidationTime(validTime);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#isCached()
   */
  public boolean isCached() {
    return cache != null && cache.get() != null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#getCacheExpirationTime()
   */
  public long getCacheExpirationTime() {
    if (cacheHandle == null)
      return 0;
    CacheHandle hdl = cacheHandle.get();
    if (hdl == null)
      return 0;
    return hdl.getCacheExpirationTime();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#invalidate()
   */
  public void invalidate() {
    isValid = false;
    if (cache != null) {
      ResponseCache c = cache.get();
      if (c != null) {
        c.invalidate(this);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeResponse#isValid()
   */
  public boolean isValid() {
    return isValid;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (request == null || request.get() == null)
      return super.toString();
    return request.get().toString();
  }

}