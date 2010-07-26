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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.cache.CacheService;
import ch.o2it.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Set;

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Default implementation of the <code>CacheService</code> that is used to store
 * rendered pages and page elements so that they may be served out of the cache
 * upon the next request.
 * <p>
 * The service itself has no logic implemented besides configuring, starting and
 * stopping the cache. The actual caching is provided by the
 * <code>CacheManager</code>.
 */
public class CacheServiceImpl implements CacheService, ManagedService {

  /** The configured cache size */
  private long cacheSize_ = CacheManager.DEFAULT_CACHE_SIZE;

  /** Logging facility provided by log4j */
  private final static Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

  /**
   * Creates a new cache service.
   */
  public CacheServiceImpl() {
    cacheSize_ = CacheManager.DEFAULT_CACHE_SIZE;
  }

  /**
   * Activates the cache.
   * 
   * @param context
   * @throws Exception
   */
  public void activate(ComponentContext context) throws Exception {
    CacheManager.setEnabled(true);
  }

  /**
   * Deactivates the cache.
   * 
   * @param context
   * @throws Exception
   */
  public void deactivate(ComponentContext context) throws Exception {
    CacheManager.shutdown();
  }

  /**
   * Configures the caching service. Available options are:
   * <ul>
   * <li><code>size</code> - the maximum cache size</li>
   * <li><code>filters</code> - the name of the output filters</li>
   * </ul>
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties)
      throws org.osgi.service.cm.ConfigurationException {
    if (properties == null)
      return;
    String cacheSize = (String) properties.get("size");
    if (cacheSize != null) {
      try {
        cacheSize_ = Long.parseLong(cacheSize);
        CacheManager.setMaxCacheSize(cacheSize_);
      } catch (Exception e) {
        throw new ConfigurationException("Error configuring the cache size: " + e.getMessage(), e);
      }
    }

    // Filter options
    String filters = (String) properties.get("filters");
    try {
      if (filters != null) {
        CacheManager.setFilters(filters);
      }
    } catch (Exception e) {
      throw new ConfigurationException("Error configuring the cache filters: " + e.getMessage(), e);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#setSize(long)
   */
  public void setSize(long size) {
    CacheManager.setMaxCacheSize(size);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#resetStatistics()
   */
  public void resetStatistics() {
    CacheManager.resetStats();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#clear()
   */
  public void clear() {
    CacheManager.emptyCache();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(java.lang.Iterable, ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse, long, long)
   */
  public CacheHandle startResponse(Iterable<CacheTag> uniqueTags,
      WebloungeRequest request, WebloungeResponse response, long validTime,
      long recheckTime) {
    CacheHandle hdl = new TaggedCacheHandle(uniqueTags, validTime, recheckTime);
    return startResponse(hdl, request, response) ? null : hdl;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(ch.o2it.weblounge.common.request.CacheHandle, ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public boolean startResponse(CacheHandle handle, WebloungeRequest request,
      WebloungeResponse response) {

    /* check whether the response has already been wrapped */
    if (unwrapResponse(response) != null) {
      logger.warn("Response already wrapped!");
      return false;
    } else if (!(response instanceof HttpServletResponseWrapper)) {
      logger.warn("Cached response is not properly wrapped");
      return false;
    }
    
    /* start the cache transaction */
    HttpServletResponse resp = CacheManager.startCacheableResponse(handle, request, (HttpServletResponse) ((HttpServletResponseWrapper) response).getResponse());
    if (resp == null)
      return true;

    /* wrap the response */
      ((HttpServletResponseWrapper) response).setResponse(resp);

    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public boolean endResponse(WebloungeResponse response) {
    return CacheManager.endCacheableResponse(unwrapResponse(response));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void invalidateResponse(WebloungeResponse response) {
    CacheManager.invalidateCacheableResponse(unwrapResponse(response));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponsePart(java.lang.Iterable,
   *      javax.servlet.http.HttpServletResponse, long, long)
   */
  public CacheHandle startResponsePart(Iterable<CacheTag> uniqueTags,
      HttpServletResponse response, long validTime, long recheckTime) {
    CacheHandle hdl = new TaggedCacheHandle(uniqueTags, validTime, recheckTime);
    return startResponsePart(hdl, response) ? null : hdl;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponsePart(ch.o2it.weblounge.cache.CacheHandle,
   *      javax.servlet.http.HttpServletResponse)
   */
  public boolean startResponsePart(CacheHandle handle,
      HttpServletResponse response) {
    boolean found = CacheManager.startHandle(handle, unwrapResponse(response));
    return found;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponsePart(ch.o2it.weblounge.cache.CacheHandle,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void endResponsePart(CacheHandle handle, HttpServletResponse response) {
    CacheManager.endHandle(handle, unwrapResponse(response));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(java.lang.Iterable)
   */
  public Set<CacheHandle> invalidateEntry(Iterable<CacheTag> tags) {
    return CacheManager.invalidate(tags);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(ch.o2it.weblounge.common.request.CacheHandle)
   */
  public Set<CacheHandle> invalidateEntry(CacheHandle handle) {
    return CacheManager.invalidateEntry(handle);
  }

  /**
   * Extracts the <code>CacheableServletResponse</code> from its wrapper(s).
   * 
   * @param response
   *          the original response
   * @return the wrapped <code>CacheableServletResponse</code> or
   *         <code>null</code> if the response is not cacheable
   */
  private static CacheableHttpServletResponse unwrapResponse(
      ServletResponse response) {
    while (response != null) {
      if (response instanceof CacheableHttpServletResponse)
        return (CacheableHttpServletResponse) response;
      if (!(response instanceof ServletResponseWrapper))
        break;
      response = ((ServletResponseWrapper) response).getResponse();
    }
    return null;
  }

}