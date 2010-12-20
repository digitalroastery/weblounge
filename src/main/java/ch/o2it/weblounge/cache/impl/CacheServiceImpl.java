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
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.impl.user.Guest;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

  /** The ehache cache manager */
  private CacheManager cacheManager = null;

  /**
   * Creates a new cache service.
   */
  public CacheServiceImpl() {
    cacheManager = new CacheManager();
  }

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) throws Exception {
    // TODO: Add cache configuration
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    for (String cacheName : cacheManager.getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheName);
      cache.dispose();
    }
    cacheManager.shutdown();
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
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties)
      throws org.osgi.service.cm.ConfigurationException {
    if (properties == null)
      return;

    // Filter options
    String filters = (String) properties.get("filters");
    try {
      if (filters != null) {
        // TODO: Configure cache filters
      }
    } catch (Exception e) {
      throw new ConfigurationException("Error configuring the cache filters: " + e.getMessage(), e);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#resetStatistics()
   */
  public void resetStatistics() {
    for (String cacheId : cacheManager.getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheId);
      cache.setStatisticsEnabled(false);
      cache.setStatisticsEnabled(true);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#clear()
   */
  public void clear() {
    cacheManager.clearAll();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(java.lang.Iterable,
   *      ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse, long, long)
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
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(ch.o2it.weblounge.common.request.CacheHandle,
   *      ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
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

    Site site = request.getSite();
    String cacheId = site.getIdentifier();
    Cache cache = cacheManager.getCache(cacheId);
    if (cache == null)
      throw new IllegalStateException("No cache found for site '" + site + "'");

    // Try to load the content from the cache
    String cacheKey = createKey(request);
    Element element = cache.get(cacheKey);

    // If it exists, write the contents back to the response
    if (element != null && element.getValue() != null) {
      // TODO: Object should be an object with entries for headers and content
      CacheHandle content = (CacheHandle)element.getValue();
      long contentLength = element.getSerializedSize();
      response.setContentLength((int) contentLength);
      try {
        // TODO: Write headers to response first
        response.getWriter().write(content.toString());
        return true;
      } catch (IOException e) {
        logger.warn("Error writing cached response to client");
        return true; // If we can't others can't either
      }
    }

    /* start the cache transaction */
    // TODO: Do a lookup. If the cache entry is there, write the contents to the
    // response and return true. If not, start the cacheable response
    // TODO: Start the response
    // HttpServletResponse resp = CacheManager.startCacheableResponse(handle,
    // request, (HttpServletResponse) ((HttpServletResponseWrapper)
    // response).getResponse());
    // if (resp == null)
    // return true;

    /* wrap the response */
    // ((HttpServletResponseWrapper) response).setResponse(resp);

    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public boolean endResponse(WebloungeResponse response) {
    // TODO: Start respone
    // return CacheManager.endCacheableResponse(unwrapResponse(response));
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void invalidateResponse(WebloungeResponse response) {
    // TODO: Invalidate response
    // CacheManager.invalidateCacheableResponse(unwrapResponse(response));
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
    // boolean found = CacheManager.startHandle(handle,
    // unwrapResponse(response));
    // return found;
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponsePart(ch.o2it.weblounge.cache.CacheHandle,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void endResponsePart(CacheHandle handle, HttpServletResponse response) {
    // CacheManager.endHandle(handle, unwrapResponse(response));
    // TODO: Write the response part to the cache
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(java.lang.Iterable)
   */
  public Set<CacheHandle> invalidateEntry(Iterable<CacheTag> tags) {
    // TODO: Invalidate the entry
    // return CacheManager.invalidate(tags);
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(ch.o2it.weblounge.common.request.CacheHandle)
   */
  public Set<CacheHandle> invalidateEntry(CacheHandle handle) {
    // TODO: Invalidate the entry
    // return CacheManager.invalidateEntry(handle);
    return null;
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

  /**
   * Callback for OSGi that is called when a new site is registered. Upon
   * registration, a new cache will be created for this site.
   * 
   * @param site
   *          the site
   */
  void addSite(Site site) {
    String siteId = site.getIdentifier();
    cacheManager.addCache(siteId);
    Cache cache = cacheManager.getCache(siteId);
    if (cache == null)
      throw new IllegalStateException("Unable to create cache for site '" + siteId + "'");

    // Specify where to put the file
    String cacheFile = PathUtils.concat(System.getProperty("java.io.tmpdir"), "weblounge", "sites", site.getIdentifier(), "caches");
    try {
      FileUtils.forceMkdir(new File(cacheFile));
      cache.setDiskStorePath(cacheFile);
      logger.info("Created new cache file at {}", cacheFile);
    } catch (IOException e) {
      logger.error("Failed to create cache file at {}", cacheFile);
    }

    // Enable cache statistics
    cache.setStatisticsEnabled(true);

    // Warm the cache by loading the home uri in all languages
    User guest = new Guest();
    for (Language language : site.getLanguages()) {
      String cacheKey = createKey(cacheFile, guest, language, null);
      cache.load(cacheKey);
    }
  }

  void removeSite(Site site) {
    String siteId = site.getIdentifier();
    Cache cache = cacheManager.getCache(siteId);
    if (cache == null) {
      logger.warn("No cache found to disable for site '{}'", siteId);
      return;
    }
    cache.flush();
    cacheManager.removeCache(siteId);
    logger.info("Cache for site '{}' removed", siteId);
  }

  /**
   * Returns the cache key for the given request.
   * 
   * @param request
   *          the request
   * @return the cache key
   */
  protected String createKey(WebloungeRequest request) {
    String uri = request.getPathInfo();
    User user = request.getUser();
    Language language = request.getLanguage();
    Map<String, String> params = new HashMap<String, String>();
    Enumeration<?> ne = request.getParameterNames();
    while (ne.hasMoreElements()) {
      String name = (String) ne.nextElement();
      String[] values = request.getParameterValues(name);
      if (values.length == 1) {
        params.put(name, values[0]);
      } else {
        int i = 0;
        for (String value : values) {
          params.put(name + "-" + i, value);
        }
      }
    }
    return createKey(uri, user, language, params);
  }

  /**
   * Returns the cache key for the given url, user, language and request
   * parameters.
   * 
   * @param uri
   *          the request uri
   * @param user
   *          the requesting user
   * @param language
   *          the request language
   * @param params
   *          the request parameters
   * @return the cache key
   */
  protected String createKey(String uri, User user, Language language,
      Map<String, String> params) {
    StringBuffer buf = new StringBuffer(uri);
    buf.append("?user=").append(user.getLogin());
    buf.append("&language=").append(language.getIdentifier());

    // Parameters need to be sorted by key, otherwise we'll get a cache miss
    // simply because the arguments were specified in a different order
    if (params != null) {
      List<String> parameterNames = new ArrayList<String>();
      parameterNames.addAll(params.keySet());
      Collections.sort(parameterNames);    
      for (String parameterName : parameterNames) {
        buf.append("&").append(parameterName).append("=").append(params.get(parameterName));
      }
    }
    
    // TODO: Consider headers as well. Think "Accepts"

    return buf.toString();
  }

}