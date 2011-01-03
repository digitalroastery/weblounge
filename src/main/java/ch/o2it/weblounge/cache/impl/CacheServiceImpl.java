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
import ch.o2it.weblounge.cache.StreamFilter;
import ch.o2it.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;

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

  /** Name of the weblounge cache header */
  private static final String CACHE_KEY_HEADER = "X-Cache-Key";

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.cache";

  /** Configuration key prefix for content repository configuration */
  public static final String OPT_PREFIX = "cache";

  /** Configuration key for the persistence of the cache */
  public static final String OPT_DISK_PERSISTENT = OPT_PREFIX + ".diskPersistent";

  /** The default value for "disk persistent" configuration property */
  private static final boolean DEFAULT_DISK_PERSISTENT = false;

  /** Configuration key for the overflow to disk setting */
  public static final String OPT_OVERFLOW_TO_DISK = OPT_PREFIX + ".overflowToDisk";

  /** The default value for "overflow to disk" configuration property */
  private static final boolean DEFAULT_OVERFLOW_TO_DISK = true;

  /** Configuration key for the statistics setting */
  public static final String OPT_ENABLE_STATISTICS = OPT_PREFIX + ".statistics";

  /** The default value for "statistics enabled" configuration property */
  private static final boolean DEFAULT_STATISTICS_ENABLED = true;

  /** Configuration key for the maximum number of elements in memory */
  public static final String OPT_MAX_ELEMENTS_IN_MEMORY = OPT_PREFIX + ".maxElementsInMemory";

  /** The default value for "max elements in memory" configuration property */
  private static final int DEFAULT_MAX_ELEMENTS_IN_MEMORY = 1000;

  /** Configuration key for the maximum number of elements on disk */
  public static final String OPT_MAX_ELEMENTS_ON_DISK = OPT_PREFIX + ".maxElementsOnDisk";

  /** The default value for "max elements on disk" configuration property */
  private static final int DEFAULT_MAX_ELEMENTS_ON_DISK = 0;

  /** Configuration key for the time to idle setting */
  public static final String OPT_TIME_TO_IDLE = OPT_PREFIX + ".timeToIdle";

  /** The default value for "seconds to idle" configuration property */
  private static final int DEFAULT_TIME_TO_IDLE = 0;

  /** Configuration key for the time to live setting */
  public static final String OPT_TIME_TO_LIVE = OPT_PREFIX + ".timeToLive";

  /** The default value for "seconds to live" configuration property */
  private static final int DEFAULT_TIME_TO_LIVE = (int) (Times.MS_PER_DAY / 1000);

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
  protected CacheManager cacheManager = null;

  /** The stream filter */
  protected StreamFilter filter = null;

  /** The site */
  protected Site site = null;

  /**
   * Creates a new cache service.
   */
  public CacheServiceImpl(Site site) {
    this.site = site;
    InputStream configInputStream = null;
    try {
      // TODO: Adjust path
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
    BundleContext bundleContext = context.getBundleContext();

    logger.info("Starting cache service");

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        updated(config);
      } else {
        logger.debug("No customized configuration found for cache");
      }
    } else {
      logger.debug("No configuration admin service found while looking for cache configuration");
    }
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
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;

    // Disk persistence
    diskPersistent = ConfigurationUtils.isTrue((String) properties.get(OPT_DISK_PERSISTENT), DEFAULT_DISK_PERSISTENT);
    logger.debug("Cache persistance between reboots is {}", diskPersistent ? "on" : "off");

    // Statistics
    statisticsEnabled = ConfigurationUtils.isTrue((String) properties.get(OPT_ENABLE_STATISTICS), DEFAULT_STATISTICS_ENABLED);
    logger.debug("Cache statistics are {}", statisticsEnabled ? "enabled" : "disabled");

    // Max elements in memory
    try {
      maxElementsInMemory = ConfigurationUtils.getValue((String) properties.get(OPT_MAX_ELEMENTS_IN_MEMORY), DEFAULT_MAX_ELEMENTS_IN_MEMORY);
      logger.debug("Cache will keep {} elements in memory", maxElementsInMemory > 0 ? "up to " + maxElementsInMemory : "all");
    } catch (NumberFormatException e) {
      logger.warn("Value for cache setting '" + OPT_MAX_ELEMENTS_IN_MEMORY + "' is malformed: " + (String) properties.get(OPT_MAX_ELEMENTS_IN_MEMORY));
      logger.warn("Cache setting '" + OPT_MAX_ELEMENTS_IN_MEMORY + "' set to default value of " + DEFAULT_MAX_ELEMENTS_IN_MEMORY);
      maxElementsInMemory = DEFAULT_MAX_ELEMENTS_IN_MEMORY;
    }

    // Max elements on disk
    try {
      maxElementsOnDisk = ConfigurationUtils.getValue((String) properties.get(OPT_MAX_ELEMENTS_ON_DISK), DEFAULT_MAX_ELEMENTS_ON_DISK);
      logger.debug("Cache will keep {} elements on disk", maxElementsOnDisk > 0 ? "up to " + maxElementsOnDisk : "all");
    } catch (NumberFormatException e) {
      logger.warn("Value for cache setting '" + OPT_MAX_ELEMENTS_ON_DISK + "' is malformed: " + (String) properties.get(OPT_MAX_ELEMENTS_ON_DISK));
      logger.warn("Cache setting '" + OPT_MAX_ELEMENTS_ON_DISK + "' set to default value of " + DEFAULT_MAX_ELEMENTS_ON_DISK);
      maxElementsOnDisk = DEFAULT_MAX_ELEMENTS_ON_DISK;
    }

    // Overflow to disk
    overflowToDisk = ConfigurationUtils.isTrue((String) properties.get(OPT_OVERFLOW_TO_DISK), DEFAULT_OVERFLOW_TO_DISK);

    // Time to idle
    try {
      timeToIdle = ConfigurationUtils.getValue((String) properties.get(OPT_TIME_TO_IDLE), DEFAULT_TIME_TO_IDLE);
      logger.debug("Cache time to idle is set to ", timeToIdle > 0 ? timeToIdle + "s" : "unlimited");
    } catch (NumberFormatException e) {
      logger.warn("Value for cache setting '" + OPT_TIME_TO_IDLE + "' is malformed: " + (String) properties.get(OPT_TIME_TO_IDLE));
      logger.warn("Cache setting '" + OPT_TIME_TO_IDLE + "' set to default value of " + DEFAULT_TIME_TO_IDLE);
      timeToIdle = DEFAULT_TIME_TO_IDLE;
    }

    // Time to live
    try {
      timeToLive = ConfigurationUtils.getValue((String) properties.get(OPT_TIME_TO_LIVE), DEFAULT_TIME_TO_LIVE);
      logger.debug("Cache time to live is set to ", timeToIdle > 0 ? timeToLive + "s" : "unlimited");
    } catch (NumberFormatException e) {
      logger.warn("Value for cache setting '" + OPT_TIME_TO_LIVE + "' is malformed: " + (String) properties.get(OPT_TIME_TO_LIVE));
      logger.warn("Cache setting '" + OPT_TIME_TO_LIVE + "' set to default value of " + DEFAULT_TIME_TO_LIVE);
      timeToLive = DEFAULT_TIME_TO_LIVE;
    }

    for (String cacheId : cacheManager.getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheId);
      if (cache == null)
        continue;
      CacheConfiguration config = cache.getCacheConfiguration();
      config.setDiskPersistent(diskPersistent);
      config.setStatistics(statisticsEnabled);
      config.setMaxElementsInMemory(maxElementsInMemory);
      config.setMaxElementsOnDisk(maxElementsOnDisk);
      config.setOverflowToDisk(overflowToDisk);
      config.setTimeToIdleSeconds(timeToIdle);
      config.setTimeToLiveSeconds(timeToLive);
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
   * @see ch.o2it.weblounge.common.request.ResponseCache#preload(ch.o2it.weblounge.common.request.CacheTag[])
   */
  public void preload(CacheTag[] tags) {
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Tags cannot be null or empty");

    String cacheId = site.getIdentifier();
    Cache cache = cacheManager.getCache(cacheId);
    if (cache == null)
      throw new IllegalStateException("Cache '" + cacheId + "' is not online");

    // Get the matching keys and load the elements into the cache
    Collection<Object> keys = getKeys(cache, tags);
    for (Object key : keys) {
      cache.load(key);
    }
    logger.info("Loaded first {} elements of cache '{}' into memory", keys.size(), cacheId);
  }

  /**
   * Returns those keys from the given cache that contain at least all the tags
   * as defined in the <code>tags</code> array.
   * 
   * @param cache
   *          the cache
   * @param tags
   *          the set of tags
   * @return the collection of matching keys
   */
  private Collection<Object> getKeys(Cache cache, CacheTag[] tags) {
    // Create the parts of the key to look for
    List<String> keyParts = new ArrayList<String>(tags.length);
    for (CacheTag tag : tags) {
      StringBuffer b = new StringBuffer(tag.getName()).append(":").append(tag.getValue());
      keyParts.add(b.toString());
    }

    // Collect those keys that contain all relevant parts
    Collection<Object> keys = new ArrayList<Object>();
    key: for (Object k : cache.getKeys()) {
      String key = k.toString();
      for (String keyPart : keyParts) {
        if (!key.contains(keyPart))
          continue key;
      }
      keys.add(k);
    }

    return keys;
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
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponse(ch.o2it.weblounge.common.request.CacheTag[],
   *      ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse, long, long)
   */
  public CacheHandle startResponse(CacheTag[] uniqueTags,
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

    String cacheId = site.getIdentifier();
    Cache cache = cacheManager.getCache(cacheId);
    if (cache == null)
      throw new IllegalStateException("No cache found for site '" + site + "'");

    // Try to load the content from the cache
    Element element = cache.get(handle.getKey());

    // If it exists, write the contents back to the response
    if (element != null && element.getValue() != null) {
      CacheEntry entry = (CacheEntry) element.getValue();

      long clientCacheDate = request.getDateHeader("If-Modified-Since");
      long validTimeInSeconds = (element.getExpirationTime() - System.currentTimeMillis()) / 1000;
      String eTag = request.getHeader("If-None-Match");

      try {

        // Write the response headers
        response.setContentType(entry.getContentType());
        response.setContentLength(entry.getContent().length);

        entry.getHeaders().apply(response);

        // Add cache control headers
        response.setDateHeader("Date", System.currentTimeMillis());
        response.setDateHeader("Expires", element.getExpirationTime());
        response.setHeader("Cache-Control", "max-age=" + validTimeInSeconds + ", must-revalidate");
        response.setHeader("Etag", entry.getETag());

        // Add the X-Cache-Key header
        StringBuffer cacheKeyHeader = new StringBuffer(site.getName());
        cacheKeyHeader.append(" (").append(handle.getKey()).append(")");
        response.addHeader(CACHE_KEY_HEADER, cacheKeyHeader.toString());

        // Check the headers first. Maybe we don't need to send anything but
        // a not-modified back
        if (entry.notModified(clientCacheDate) && entry.matches(eTag)) {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
          response.getOutputStream().write(entry.getContent());
        }

        response.flushBuffer();

        return null;
      } catch (IOException e) {
        logger.warn("Error writing cached response to client");
        return null; // If we can't, others can't either
      }
    }

    cacheableResponse.startTransaction(handle, cacheId, filter);
    response.setHeader("Etag", CacheEntry.createETag(handle.getCreationDate()));
    response.setDateHeader("Expires", handle.getCreationDate() + Times.MS_PER_MIN);
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
    if (transaction == null || !transaction.isValid() || !response.isValid()) {
      logger.debug("Response to {} was invalid and is not being cached", response);
      return false;
    }

    // Make sure the cache is still available
    Cache cache = cacheManager.getCache(transaction.getCache());
    if (cache == null) {
      logger.debug("Cache for {} disappeared, response is not being cached", response);
      return false;
    }

    // Write the entry to the cache
    logger.trace("Writing response for {} to the cache", response);
    CacheEntry entry = new CacheEntry(transaction.getHandle(), transaction.getContent(), transaction.getHeaders());
    Element element = new Element(entry.getKey(), entry);
    cache.put(element);

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidate(ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void invalidate(WebloungeResponse response) {
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null || cacheableResponse.tx == null)
      return;
    cacheableResponse.invalidate();
    CacheHandle handle = cacheableResponse.tx.getHandle();
    String cache = cacheableResponse.tx.getCache();
    invalidate(handle, cache);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#startResponsePart(ch.o2it.weblounge.common.request.CacheTag[],
   *      javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse, long, long)
   */
  public CacheHandle startResponsePart(CacheTag[] uniqueTags,
      HttpServletRequest request, HttpServletResponse response, long validTime,
      long recheckTime) {

    // Is this a valid response?
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return null;

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

    // Is this a valid response?
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return false;

    // Adjust the transaction handle's recheck and valid time
    CacheHandle responseHnd = cacheableResponse.tx.getHandle();
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
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidate(ch.o2it.weblounge.common.request.CacheTag[])
   */
  public void invalidate(CacheTag[] tags) {
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Tags cannot be null or empty");
    if (site == null)
      throw new IllegalArgumentException("Site cannot be null");

    // Load the cache
    String cacheId = site.getIdentifier();
    Cache cache = cacheManager.getCache(cacheId);
    if (cache == null)
      throw new IllegalStateException("Cache '" + cacheId + "' is not online");

    // Remove the objects matched by the tags
    long removed = 0;
    for (Object key : getKeys(cache, tags)) {
      if (cache.remove(key))
        removed++;
    }

    logger.debug("Removed {} elements from cache '{}'", removed, cacheId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.ResponseCache#invalidate(ch.o2it.weblounge.common.request.CacheHandle)
   */
  public void invalidate(CacheHandle handle) {
    if (handle == null)
      throw new IllegalArgumentException("Handle cannot be null");
    invalidate(handle, site.getIdentifier());
  }

  /**
   * Removes the entry identified by <code>handle</code> from the given cache.
   * 
   * @param handle
   *          the cache handle
   * @param cacheId
   *          the cache identifier
   */
  void invalidate(CacheHandle handle, String cacheId) {
    if (handle == null)
      throw new IllegalArgumentException("Handle cannot be null");
    if (cacheId == null)
      throw new IllegalArgumentException("Cache id cannot be null");

    // Load the cache
    Cache cache = cacheManager.getCache(cacheId);
    if (cache == null)
      throw new IllegalStateException("Cache '" + cacheId + "' is not online");

    cache.remove(handle.getKey());
    logger.debug("Removed {} from cache '{}'", handle.getKey(), cacheId);
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
    cacheManager.addCache(cache);
    if (overflowToDisk)
      logger.info("Cache for site '{}' created at {}", siteId, cacheManager.getDiskStorePath());
    else
      logger.info("In-memory cache for site '{}' created");
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

}