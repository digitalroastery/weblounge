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

import ch.o2it.weblounge.cache.CacheHandle;
import ch.o2it.weblounge.cache.CacheService;
import ch.o2it.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.dispatcher.impl.request.WebloungeResponseImpl;

import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Set;

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <code>Cache</code> implements the caching service that is used to 
 * store rendered pages so that they may be served much faster than if
 * they would have been if rendered again.
 * <p>
 * The service class itself has no logic implemented besides configuring,
 * starting and stopping the cache. The caching is provided by the
 * <code>CacheManager</code>.
 */
public class CacheServiceImpl implements CacheService, ManagedService {

	/** The configured cache size */
	private long cacheSize_ = CacheManager.DEFAULT_CACHE_SIZE;
	
	// Logging
	
	/** Class name, used for the logging facility */
	private final static String className = CacheServiceImpl.class.getName();
	
	/** Logging facility provided by log4j */
	private final static Logger log_ = LoggerFactory.getLogger(className);
	
	public CacheServiceImpl() {
	  
	}

  /**
   * {@inheritDoc}
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws org.osgi.service.cm.ConfigurationException {
    // TODO: Read and set cache size
    // TODO: Read and set stream filters
  }

	/**
	 * Configures the caching service. Available options are:
	 * <ul>
	 * 	<li><code>size</code> - the maximum cache size</li>
	 * 	<li><code>filters</code> - the name of the output filters</li>
	 * </ul>
	 * @see ch.o2it.weblounge.core.service.SystemServiceImpl#configure(ch.o2it.weblounge.api.service.ServiceConfiguration)
	 */
	public void init() throws ConfigurationException {
		try {
			cacheSize_ = Long.parseLong(config.getOption("size"));
			CacheManager.setMaxCacheSize(cacheSize_);
		} catch (Exception e) {
			throw new ConfigurationException("Error configuring the cache size: " + e.getMessage(), e);
		}
		
		// Filter options
		try {
			CacheManager.setFilters(config.getOption("filters"));
		} catch (Exception e) {
			throw new ConfigurationException("Error configuring the cache filters: " + e.getMessage(), e);
		}
	}

	/**
	 * Starts the caching service by enabling the <code>CacheManager</code>.
	 * @see ch.o2it.weblounge.core.service.SystemServiceImpl#start()
	 */
	@Override
	protected void startService() {
		CacheManager.setEnabled(true);
		CacheSupport.configure();
	}

	/**
	 * Stops the caching service by disabling the <code>CacheManager</code>.
	 * @see ch.o2it.weblounge.core.service.SystemServiceImpl#stop()
	 */
	@Override
	protected void stopService() {
		CacheManager.shutdown();
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#startResponse(java.lang.Iterable, ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse, long, long)
   */
	public CacheHandle startResponse(Iterable<Tag> uniqueTags, WebloungeRequest request, WebloungeResponse response, long validTime, long recheckTime) {
		CacheHandle hdl = new TaggedCacheHandle(uniqueTags, validTime, recheckTime);
		return startResponse(hdl, request, response) ? null : hdl;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#startResponse(ch.o2it.weblounge.cache.CacheHandle, ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
	public boolean startResponse(CacheHandle handle, WebloungeRequest request, WebloungeResponse response) {

		/* check whether the response has already been wrapped */
		if (unwrapResponse(response) != null) {
			log_.warn("Response already wrapped!");
			return false;
		}

		/* start the cache transaction */
		HttpServletResponse resp = CacheManager.startCacheableResponse(
				handle, 
				request,
				(HttpServletResponse)((HttpServletResponseWrapper)response).getResponse()
		);
		if (resp == null)
			return true;
		
		/* wrap the response */
		((HttpServletResponseWrapper)response).setResponse(resp);

		/* tell response about current cache handle */
		if (response instanceof WebloungeResponseImpl) {
			((WebloungeResponseImpl)response).startCacheHandle(handle);
		}

		return false;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#endResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
	public boolean endResponse(WebloungeResponse response) {
		if (response instanceof WebloungeResponseImpl) {
			((WebloungeResponseImpl)response).endCacheHandle();
		}
		return CacheManager.endCacheableResponse(unwrapResponse(response));
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#invalidateResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
	public void invalidateResponse(WebloungeResponse response) {
		CacheManager.invalidateCacheableResponse(unwrapResponse(response));
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#startResponsePart(java.lang.Iterable, javax.servlet.http.HttpServletResponse, long, long)
   */
	public CacheHandle startResponsePart(Iterable<Tag> uniqueTags, HttpServletResponse response, long validTime, long recheckTime) {
		CacheHandle hdl = new TaggedCacheHandle(uniqueTags, validTime, recheckTime);
		return startResponsePart(hdl, response) ? null : hdl;
	}
	
	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#startResponsePart(ch.o2it.weblounge.cache.CacheHandle, javax.servlet.http.HttpServletResponse)
   */
	public boolean startResponsePart(CacheHandle handle, HttpServletResponse response) {
		boolean found = CacheManager.startHandle(handle, unwrapResponse(response));
		if (!found && response instanceof WebloungeResponseImpl) {
			((WebloungeResponseImpl)response).startCacheHandle(handle);
		}
		return found;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#endResponsePart(ch.o2it.weblounge.cache.CacheHandle, javax.servlet.http.HttpServletResponse)
   */
	public void endResponsePart(CacheHandle handle, HttpServletResponse response) {
		if (response instanceof WebloungeResponseImpl) {
			((WebloungeResponseImpl)response).endCacheHandle();
		}
		CacheManager.endHandle(handle, unwrapResponse(response));
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#invalidate(java.lang.Iterable)
   */
	public Set<CacheHandle> invalidate(Iterable<Tag> tags) {
		return CacheManager.invalidate(tags);
	}
	
	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#invalidateEntry(ch.o2it.weblounge.cache.CacheHandle)
   */
	public Set<CacheHandle> invalidateEntry(CacheHandle handle) {
		return CacheManager.invalidateEntry(handle);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#setSize(long)
   */
	public void setSize(long size) {
		CacheManager.setMaxCacheSize(size);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#resetStatistics()
   */
	public void resetStatistics() {
		CacheManager.resetStats();
	}
	
	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.cache.impl.CacheService1#empty()
   */
	public void empty() {
		CacheManager.emptyCache();
	}

	/**
	 * Extracts the <code>CacheableServletResponse</code> from its wrapper(s).
	 * 
	 * @param response the original response
	 * @return the wrapped <code>CacheableServletResponse</code> or 
	 * <code>null</code> if the response is not cacheable
	 */
	private static CacheableHttpServletResponse unwrapResponse(ServletResponse response) {
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