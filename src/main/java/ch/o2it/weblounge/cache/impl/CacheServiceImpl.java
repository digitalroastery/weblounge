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
import ch.o2it.weblounge.common.Times;
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
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.io.IOUtils;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  /** Path to cache configuration */
  private static final String CACHE_MANAGER_CONFIG = "/ehcache/config.xml";

  /** The default value for "disk persistent" configuration property */
  private static final boolean DEFAULT_DISK_PERSISTENT = true;

  /** The default value for "overflow to disk" configuration property */
  private static final boolean DEFAULT_OVERFLOW_TO_DISK = true;

  /** The default value for "statistics enabled" configuration property */
  private static final boolean DEFAULT_STATISTICS_ENABLED = true;

  /** The default value for "max elements in memory" configuration property */
  private static final int DEFAULT_MAX_ELEMENTS_IN_MEMORY = 1000;

  /** The default value for "max elements on disk" configuration property */
  private static final int DEFAULT_MAX_ELEMENTS_ON_DISK = 10000;

  /** The default value for "seconds to idle" configuration property */
  private static final int DEFAULT_TIME_TO_IDLE = (int) (Times.MS_PER_HOUR * 1000);

  /** The default value for "seconds to live" configuration property */
  private static final int DEFAULT_TIME_TO_LIVE = (int) (Times.MS_PER_DAY * 1000);

  /** Make the cache persistent between reboots? */
  protected boolean diskPersistent = DEFAULT_DISK_PERSISTENT;

  /** Write overflow elements from memory to disk? */
  protected boolean overflowToDisk = DEFAULT_OVERFLOW_TO_DISK;

  /** Maximum number of elements in memory */
  protected int maxElementsInMemory = DEFAULT_MAX_ELEMENTS_IN_MEMORY;

  /** Maximum number of elements in memory */
  protected int maxElementsOnDisk = DEFAULT_MAX_ELEMENTS_ON_DISK;

  /** Number of seconds for an element to live from its last access time */
  protected int timeToIdle = DEFAULT_TIME_TO_IDLE;

  /** Number of seconds for an element to live from its creation date */
  protected int timeToLive = DEFAULT_TIME_TO_LIVE;

  /** Whether cache statistics are enabled */
  protected boolean statisticsEnabled = DEFAULT_STATISTICS_ENABLED;

  /** The ehache cache manager */
  private CacheManager cacheManager = null;

  /**
   * Creates a new cache service.
   */
  public CacheServiceImpl() {
    InputStream configInputStream = null;
    try {
      configInputStream = getClass().getClassLoader().getResourceAsStream(CACHE_MANAGER_CONFIG);
      cacheManager = new CacheManager(configInputStream);
    } finally {
      IOUtils.closeQuietly(configInputStream);
    }
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
    // TODO: Get and apply cache configuration
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
   * @see ch.o2it.weblounge.common.request.ResponseCache#createCacheableResponse(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public HttpServletResponse createCacheableResponse(
      HttpServletRequest request, HttpServletResponse response) {
    return new CacheableHttpServletResponse(response);
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
    return startResponse(hdl, request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(ch.o2it.weblounge.common.request.CacheHandle,
   *      ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public CacheHandle startResponse(CacheHandle handle,
      WebloungeRequest request, WebloungeResponse response) {

    // check whether the response has been properly wrapped
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null) {
      throw new IllegalStateException("Cached response is not properly wrapped");
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
      CacheEntry entry = (CacheEntry) element.getValue();
      try {

        // Write the response headers
        response.setContentType(entry.getHeaders().getContentType());
        response.setContentLength(entry.getContent().length);
        entry.getHeaders().apply(response);
        
        // Write the response body
        response.getOutputStream().write(entry.getContent());
        response.flushBuffer();

        return null;
      } catch (IOException e) {
        logger.warn("Error writing cached response to client");
        return null; // If we can't, others can't either
      }
    }

    cacheableResponse.startTransaction(handle, request, response, null);
    cacheableResponse.tx.hnd = handle;
    cacheableResponse.tx.cache = cacheId;
    cacheableResponse.tx.cacheKey = cacheKey;
    return handle;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public boolean endResponse(WebloungeResponse response) {
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return false;

    // Finish writing the element
    CacheTransaction transaction = cacheableResponse.endOutput();

    // Is the response ready to be cached?
    if (transaction == null || transaction.invalidated || !response.isValid()) {
      logger.debug("Response to {} was invalid and is not cached", transaction.req);
      return false;
    }

    // Make sure the cache is still available
    Cache cache = cacheManager.getCache(transaction.cache);
    if (cache == null) {
      logger.debug("Cache for {} disappeared, response is not cached", transaction.req);
      return false;
    }

    // Write the entry to the cache
    logger.trace("Writing response for {} to the cache", transaction.req);
    CacheEntry entry = new CacheEntry(transaction.os.getBuffer(), transaction.headers);
    Element element = new Element(transaction.cacheKey, entry);
    cache.put(element);

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateResponse(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void invalidateResponse(WebloungeResponse response) {
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return;
    cacheableResponse.tx.invalidated = true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponsePart(java.lang.Iterable,
   *      javax.servlet.http.HttpServletResponse, long, long)
   */
  public CacheHandle startResponsePart(Iterable<CacheTag> uniqueTags,
      HttpServletResponse response, long validTime, long recheckTime) {
     CacheHandle hdl = new TaggedCacheHandle(uniqueTags, validTime,
     recheckTime);
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
    
    // Is this a valid response?
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return false;

    // Adjust the transaction handle's recheck and valid time 
    CacheHandle responseHnd = cacheableResponse.tx.hnd;
    if (handle.getExpires() < responseHnd.getExpires())
      responseHnd.setExpires(handle.getExpires());
    if (handle.getRecheck() < responseHnd.getRecheck())
      responseHnd.setRecheck(handle.getRecheck());

    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#endResponsePart(ch.o2it.weblounge.cache.CacheHandle,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void endResponsePart(CacheHandle handle, HttpServletResponse response) {
    // Nothing to do
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(java.lang.Iterable)
   */
  public Set<CacheHandle> invalidateEntry(Iterable<CacheTag> tags) {
    // TODO: Invalidate the entry
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidateEntry(ch.o2it.weblounge.common.request.CacheHandle)
   */
  public Set<CacheHandle> invalidateEntry(CacheHandle handle) {
    // TODO: Invalidate the entry
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
    String cacheFile = PathUtils.concat(System.getProperty("java.io.tmpdir"), "weblounge", "sites", siteId, "cache");

    // Configure the cache
    CacheConfiguration cacheConfig = new CacheConfiguration();
    cacheConfig.setName(siteId);
    cacheConfig.setDiskStorePath(cacheFile);
    cacheConfig.setEternal(false);
    cacheConfig.setDiskPersistent(diskPersistent);
    cacheConfig.setMaxElementsInMemory(maxElementsInMemory);
    cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
    cacheConfig.setOverflowToDisk(overflowToDisk);
    cacheConfig.setStatistics(statisticsEnabled);
    cacheConfig.setTimeToIdleSeconds(timeToIdle);
    cacheConfig.setTimeToLiveSeconds(timeToLive);

    Cache cache = new Cache(cacheConfig);
    cacheManager.addCache(siteId);
    if (overflowToDisk)
      logger.info("Cache for site '{}' created at {}", siteId, cacheManager.getDiskStorePath());
    else
      logger.info("In-memory cache for site '{}' created");

    // Warm the cache by loading the home uri in all languages
    User guest = new Guest();
    for (Language language : site.getLanguages()) {
      String cacheKey = createKey(cacheFile, guest, language, null);
      cache.load(cacheKey);
    }
  }

  /**
   * Callback for OSGi that is called if a site is unregistered from the service
   * registry. This method makes sure that the associated cache is properly shut
   * down and disposed.
   * 
   * @param site
   *          the site
   */
  void removeSite(Site site) {
    String siteId = site.getIdentifier();
    Cache cache = cacheManager.getCache(siteId);
    if (cache == null) {
      logger.warn("No cache found to disable for site '{}'", siteId);
      return;
    }
    cache.dispose();
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
    if (uri == null)
      uri = "/";
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