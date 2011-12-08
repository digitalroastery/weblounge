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

package ch.entwine.weblounge.cache.impl;

import ch.entwine.weblounge.cache.CacheListener;
import ch.entwine.weblounge.cache.CacheService;
import ch.entwine.weblounge.cache.StreamFilter;
import ch.entwine.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /** Name of the weblounge cache tags header */
  private static final String CACHE_TAGS_HEADER = "X-Cache-Tags";

  /** Configuration key prefix for content repository configuration */
  public static final String OPT_PREFIX = "cache";

  /** Configuration key for the enabled state of the cache */
  public static final String OPT_ENABLE = OPT_PREFIX + ".enable";

  /** The default value for "enable" configuration property */
  private static final boolean DEFAULT_ENABLE = true;

  /** Configuration key for the debugging option */
  public static final String OPT_DEBUG = OPT_PREFIX + ".debug";

  /** The default value for "debug" configuration property */
  private static final boolean DEFAULT_DEBUG = true;

  /** Configuration key for the cache identifier */
  public static final String OPT_ID = OPT_PREFIX + ".id";

  /** Configuration key for the cache name */
  public static final String OPT_NAME = OPT_PREFIX + ".name";

  /** Configuration key indicating that a clear() operation is required */
  public static final String OPT_CLEAR = OPT_PREFIX + ".clear";

  /** Configuration key for the path to the cache's disk store */
  public static final String OPT_DISKSTORE_PATH = OPT_PREFIX + ".diskStorePath";

  /** Configuration key for the persistence of the cache */
  public static final String OPT_DISK_PERSISTENT = OPT_PREFIX + ".diskPersistent";

  /** The default value for "disk persistent" configuration property */
  private static final boolean DEFAULT_DISK_PERSISTENT = false;

  /** Configuration key for the overflow to disk setting */
  public static final String OPT_OVERFLOW_TO_DISK = OPT_PREFIX + ".overflowToDisk";

  /** The default value for "overflow to disk" configuration property */
  private static final boolean DEFAULT_OVERFLOW_TO_DISK = false;

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

  /** Identifier for the default cache */
  private static final String DEFAULT_CACHE = "site";

  /** The ehache cache manager */
  protected CacheManager cacheManager = null;

  /** True if the cache is enabled */
  protected boolean enabled = true;

  /** True if additional response headers are enabled */
  protected boolean debug = false;

  /** The stream filter */
  protected StreamFilter filter = null;

  /** Cache identifier */
  protected String id = null;

  /** Cache name */
  protected String name = null;

  /** Path to the local disk store */
  protected String diskStorePath = null;

  /**
   * True to indicate that everything went fine with the setup of the disk store
   */
  protected boolean diskStoreEnabled = true;

  /** Transactions that are currently being processed */
  protected Map<String, CacheTransaction> transactions = null;

  /** List of registered cache listeners */
  protected List<CacheListener> cacheListeners = null;

  /**
   * Creates a new cache with the given identifier and name.
   * 
   * @param id
   *          the cache identifier
   * @param name
   *          the cache name
   * @param diskStorePath
   *          the cache's disk store
   */
  public CacheServiceImpl(String id, String name, String diskStorePath) {
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("Cache id cannot be blank");
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("Cache name cannot be blank");
    this.id = id;
    this.name = name;
    this.diskStorePath = diskStorePath;
    this.transactions = new HashMap<String, CacheTransaction>();
    this.cacheListeners = new ArrayList<CacheListener>();
    init(id, name, diskStorePath);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.cache.CacheService#addCacheListener(ch.entwine.weblounge.cache.CacheListener)
   */
  public void addCacheListener(CacheListener listener) {
    if (cacheListeners.contains(listener))
      cacheListeners.add(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.cache.CacheService#removeCacheListener(ch.entwine.weblounge.cache.CacheListener)
   */
  public void removeCacheListener(CacheListener listener) {
    cacheListeners.remove(listener);
  }

  /**
   * Initializes the cache service with an identifier, a name and a path to the
   * local disk store (if applicable).
   * 
   * @param id
   *          the cache identifier
   * @param name
   *          the cache name
   * @param diskStorePath
   *          path to the local disk store
   */
  private void init(String id, String name, String diskStorePath) {
    InputStream configInputStream = null;
    try {
      configInputStream = getClass().getClassLoader().getResourceAsStream(CACHE_MANAGER_CONFIG);
      Configuration cacheManagerConfig = ConfigurationFactory.parseConfiguration(configInputStream);
      cacheManagerConfig.getDiskStoreConfiguration().setPath(diskStorePath);
      cacheManager = new CacheManager(cacheManagerConfig);
      cacheManager.setName(id);
    } finally {
      IOUtils.closeQuietly(configInputStream);
    }

    // Check the path to the cache
    if (StringUtils.isNotBlank(diskStorePath)) {
      File file = new File(diskStorePath);
      try {
        if (!file.exists())
          FileUtils.forceMkdir(file);
        if (!file.isDirectory())
          throw new IOException();
        if (!file.canWrite())
          throw new IOException();
      } catch (IOException e) {
        logger.warn("Unable to create disk store for cache '{}' at {}", id, diskStorePath);
        logger.warn("Persistent cache will be disabled for '{}'", id);
        diskPersistent = false;
        diskStoreEnabled = false;
      }
    } else {
      diskStoreEnabled = false;
    }

    // Configure the cache
    CacheConfiguration cacheConfig = new CacheConfiguration();
    cacheConfig.setName(DEFAULT_CACHE);
    cacheConfig.setDiskPersistent(diskPersistent && diskStoreEnabled);
    cacheConfig.setOverflowToDisk(overflowToDisk && diskStoreEnabled);
    if (overflowToDisk && diskStoreEnabled) {
      cacheConfig.setDiskStorePath(diskStorePath);
      cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
    }
    cacheConfig.setEternal(false);
    cacheConfig.setMaxElementsInMemory(maxElementsInMemory);
    cacheConfig.setStatistics(statisticsEnabled);
    cacheConfig.setTimeToIdleSeconds(timeToIdle);
    cacheConfig.setTimeToLiveSeconds(timeToLive);

    Cache cache = new Cache(cacheConfig);
    cacheManager.addCache(cache);
    if (overflowToDisk)
      logger.info("Cache extension for site '{}' created at {}", id, cacheManager.getDiskStorePath());
    else
      logger.info("In-memory cache for site '{}' created", id);

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.cache.CacheService#shutdown()
   */
  public void shutdown() {
    if (cacheManager == null)
      return;
    for (String cacheName : cacheManager.getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheName);
      cache.dispose();
    }
    cacheManager.shutdown();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.cache.CacheService#getIdentifier()
   */
  public String getIdentifier() {
    return id;
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

    // Do we need to clear the cache?
    boolean clear = ConfigurationUtils.isTrue((String) properties.get(OPT_CLEAR), false);
    if (clear) {
      clear();
    }

    // Enabled status
    enabled = ConfigurationUtils.isTrue((String) properties.get(OPT_ENABLE), DEFAULT_ENABLE);
    logger.debug("Cache is {}", diskPersistent ? "enabled" : "disabled");

    debug = ConfigurationUtils.isTrue((String) properties.get(OPT_DEBUG), DEFAULT_DEBUG);
    logger.debug("Cache is {}", diskPersistent ? "enabled" : "disabled");

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
      config.setOverflowToDisk(overflowToDisk && diskStoreEnabled);
      if (overflowToDisk && diskStoreEnabled) {
        config.setMaxElementsOnDisk(maxElementsOnDisk);
      }
      config.setDiskPersistent(diskPersistent && diskStoreEnabled);
      config.setStatistics(statisticsEnabled);
      config.setMaxElementsInMemory(maxElementsInMemory);
      config.setTimeToIdleSeconds(timeToIdle);
      config.setTimeToLiveSeconds(timeToLive);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#resetStatistics()
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
   * @see ch.entwine.weblounge.common.request.ResponseCache#clear()
   */
  public void clear() {
    cacheManager.clearAll();
    logger.info("Cache '{}' cleared", id);
    for (CacheListener listener : cacheListeners) {
      listener.cacheCleared();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#preload(ch.entwine.weblounge.common.request.CacheTag[])
   */
  public void preload(CacheTag[] tags) {
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Tags cannot be null or empty");

    Cache cache = cacheManager.getCache(DEFAULT_CACHE);

    // Get the matching keys and load the elements into the cache
    Collection<Object> keys = getKeysForPrimaryTags(cache, tags);
    for (Object key : keys) {
      cache.load(key);
    }
    logger.info("Loaded first {} elements of cache '{}' into memory", keys.size(), id);
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
  private Collection<Object> getKeysForPrimaryTags(Cache cache, CacheTag[] tags) {
    // Create the parts of the key to look for
    List<String> keyParts = new ArrayList<String>(tags.length);
    for (CacheTag tag : tags) {
      StringBuffer b = new StringBuffer(tag.getName()).append("=").append(tag.getValue());
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
   * @see ch.entwine.weblounge.common.request.ResponseCache#createCacheableResponse(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public HttpServletResponse createCacheableResponse(
      HttpServletRequest request, HttpServletResponse response) {
    return new CacheableHttpServletResponse(response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#startResponse(ch.entwine.weblounge.common.request.CacheTag[],
   *      ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse, long, long)
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
   * @see ch.entwine.weblounge.common.request.ResponseCache#startResponse(ch.entwine.weblounge.common.request.CacheHandle,
   *      ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public CacheHandle startResponse(CacheHandle handle,
      WebloungeRequest request, WebloungeResponse response) {

    // Check whether the response has been properly wrapped
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null) {
      throw new IllegalStateException("Cached response is not properly wrapped");
    }

    // While disabled, don't do lookups but return immediately
    if (!enabled) {
      cacheableResponse.startTransaction(handle, filter);
      return handle;
    }

    // Make sure the cache is still alive
    if (cacheManager.getStatus() != Status.STATUS_ALIVE) {
      logger.debug("Cache '{}' has unexpected status '{}'", request.getSite().getIdentifier(), cacheManager.getStatus());
      return null;
    }

    // Load the cache
    Cache cache = cacheManager.getCache(DEFAULT_CACHE);
    if (cache == null)
      throw new IllegalStateException("No cache found for site '" + id + "'");

    // Try to load the content from the cache
    Element element = cache.get(new CacheEntryKey(handle.getKey()));

    // If it exists, write the contents back to the response
    if (element != null && element.getValue() != null) {
      try {
        logger.debug("Answering {} from cache '{}'", request, id);
        writeCacheEntry(element, handle, request, response);
        return null;
      } catch (IOException e) {
        logger.debug("Error writing cached response to client");
        return null;
      }
    }

    // Make sure that there are no two transactions producing the same content.
    // If there is a transaction already working on specific content, have
    // this transaction simply wait for the outcome.
    synchronized (transactions) {
      CacheTransaction tx = transactions.get(handle.getKey());
      if (tx != null) {
        try {
          logger.debug("Waiting for cache transaction {} to be finished", request);
          while (transactions.containsKey(handle.getKey())) {
            transactions.wait();
          }
        } catch (InterruptedException e) {
          // Done sleeping!
        }
      }

      // The cache might have been shut down in the meantime
      if (cacheManager.getStatus() == Status.STATUS_ALIVE) {
        element = cache.get(handle.getKey());
      } else {
        logger.debug("Cache '{}' changed status to '{}'", request.getSite().getIdentifier(), cacheManager.getStatus());
      }

      if (element == null) {
        tx = cacheableResponse.startTransaction(handle, filter);
        transactions.put(handle.getKey(), tx);
        logger.debug("Starting work on cached version of {}", request);
      }
    }

    // If we were waiting for an active cache transaction, let's try again
    if (element != null && element.getValue() != null) {
      try {
        logger.debug("Answering {} from cache '{}'", request, id);
        writeCacheEntry(element, handle, request, response);
        return null;
      } catch (IOException e) {
        logger.warn("Error writing cached response to client");
        return null;
      }
    }

    // Apparently, we need to get it done ourselves
    return handle;
  }

  /**
   * Writes the cache element to the response, setting the cache headers
   * according to the settings found on the element.
   * 
   * @param element
   *          the cache contents
   * @param handle
   *          the cache handle
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws IOException
   *           if writing the cache contents to the response fails
   */
  private void writeCacheEntry(Element element, CacheHandle handle,
      WebloungeRequest request, WebloungeResponse response) throws IOException {
    CacheEntry entry = (CacheEntry) element.getValue();
    CacheEntryKey key = (CacheEntryKey) element.getKey();

    long clientCacheDate = request.getDateHeader("If-Modified-Since");
    long expirationDate = element.getCreationTime() + entry.getClientRevalidationTime();
    long revalidationTimeInSeconds = entry.getClientRevalidationTime() / 1000;
    String eTag = request.getHeader("If-None-Match");

    boolean isModified = !entry.notModified(clientCacheDate) && !entry.matches(eTag);

    // Write the response headers
    if (isModified) {
      entry.getHeaders().apply(response);
      response.setHeader("Cache-Control", "private, max-age=" + revalidationTimeInSeconds + ", must-revalidate");
      response.setContentType(entry.getContentType());
      response.setCharacterEncoding(entry.getEncoding());
      response.setContentLength(entry.getContent().length);
    }

    // Set the current date
    response.setDateHeader("Date", System.currentTimeMillis());

    // This header must be set, otherwise it defaults to
    // "Thu, 01-Jan-1970 00:00:00 GMT"
    response.setDateHeader("Expires", expirationDate);
    response.setHeader("ETag", entry.getETag());

    // Add the X-Cache-Key header
    if (debug) {
      StringBuffer cacheKeyHeader = new StringBuffer(name);
      cacheKeyHeader.append(" (").append(handle.getKey()).append(")");
      response.addHeader(CACHE_KEY_HEADER, cacheKeyHeader.toString());
    }

    // Add the X-Cache-Tags header
    if (debug) {
      key = (CacheEntryKey) element.getKey();
      StringBuffer cacheTagsHeader = new StringBuffer(name);
      cacheTagsHeader.append(" (").append(key.getTags()).append(")");
      response.addHeader(CACHE_TAGS_HEADER, cacheTagsHeader.toString());
    }

    // Check the headers first. Maybe we don't need to send anything but
    // a not-modified back
    if (isModified) {
      response.getOutputStream().write(entry.getContent());
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    response.flushBuffer();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#endResponse(ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public boolean endResponse(WebloungeResponse response) {
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null)
      return false;

    // Discard any cached content while disabled
    if (!enabled)
      return true;

    // Make sure the cache is still available and active
    if (cacheManager.getStatus() != Status.STATUS_ALIVE) {
      logger.debug("Cache '{}' has unexpected status '{}'", cacheManager.getName(), cacheManager.getStatus());
      return false;
    }

    // Load the cache
    Cache cache = cacheManager.getCache(DEFAULT_CACHE);
    if (cache == null) {
      logger.debug("Cache for {} was deactivated, response is not being cached", response);
      return false;
    }

    // Finish writing the element
    CacheTransaction tx = cacheableResponse.endOutput();

    // Is the response ready to be cached?
    if (tx == null) {
      logger.debug("Response to {} was not associated with a transaction", response);
      return false;
    }

    // Important note: Do not return prior to this try block if there is a
    // transaction associated with the request.
    try {

      // Is the response ready to be cached?
      if (tx.isValid() && response.isValid() && response.getStatus() == HttpServletResponse.SC_OK) {
        logger.trace("Writing response for {} to the cache", response);
        CacheHandle cacheHdl = tx.getHandle();
        String encoding = cacheableResponse.getCharacterEncoding();
        CacheEntry entry = new CacheEntry(cacheHdl, tx.getContent(), encoding, tx.getHeaders());
        Element element = new Element(new CacheEntryKey(cacheHdl), entry);
        element.setTimeToLive((int) (cacheHdl.getCacheExpirationTime() / 1000));
        cache.put(element);

        // Write cache relevant headers
        long expirationDate = System.currentTimeMillis() + entry.getClientRevalidationTime();
        long revalidationTimeInSeconds = entry.getClientRevalidationTime() / 1000;

        // Send the cache directive
        response.setHeader("Cache-Control", "private, max-age=" + revalidationTimeInSeconds + ", must-revalidate");

        // Set the current date
        response.setDateHeader("Date", System.currentTimeMillis());

        // This header must be set, otherwise it defaults to
        // "Thu, 01-Jan-1970 00:00:00 GMT"
        response.setDateHeader("Expires", expirationDate);
        response.setHeader("ETag", entry.getETag());

        // Inform listeners
        for (CacheListener listener : cacheListeners) {
          listener.cacheEntryAdded(cacheHdl);
        }
      } else if (tx.isValid() && response.isValid()) {
        logger.trace("Skip caching of response for {} to the cache: {}", response, response.getStatus());
        response.setDateHeader("Expires", System.currentTimeMillis() + tx.getHandle().getCacheExpirationTime());
      } else {
        logger.debug("Response to {} was invalid and is not being cached", response);
      }

      return tx.isValid() && response.isValid();

    } finally {

      // Mark the current transaction as finished and notify anybody who was
      // waiting for it to be finished
      synchronized (transactions) {
        transactions.remove(tx.getHandle().getKey());
        logger.debug("Caching of {} finished", response);
        transactions.notifyAll();
      }

      try {
        if (!response.isCommitted())
          response.flushBuffer();
      } catch (IOException e) {
        String message = e.getMessage();
        // This is debug, as the client may have closed the connection
        logger.debug("Error flushing response: {}", message);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#invalidate(ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public void invalidate(WebloungeResponse response) {
    CacheableHttpServletResponse cacheableResponse = unwrapResponse(response);
    if (cacheableResponse == null || cacheableResponse.getTransaction() == null)
      return;
    cacheableResponse.invalidate();
    CacheTransaction tx = cacheableResponse.getTransaction();
    invalidate(tx.getHandle());
    logger.debug("Removed {} from cache '{}'", response, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#invalidate(ch.entwine.weblounge.common.request.CacheTag[],
   *      boolean)
   */
  public void invalidate(CacheTag[] tags, boolean partialMatches) {
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Tags cannot be null or empty");

    // Load the cache
    Cache cache = cacheManager.getCache(DEFAULT_CACHE);

    // Inform listeners
    for (CacheListener listener : cacheListeners) {
      listener.cacheSetInvalidated(tags);
    }

    // Remove the objects matched by the tags
    long removed = 0;
    for (Object key : getKeysForTags(cache, tags, partialMatches)) {
      if (cache.remove(key))
        removed++;
    }

    logger.debug("Removed {} elements from cache '{}'", removed, id);
  }

  /**
   * Returns those keys from the given cache that contain all or any of the tags
   * as defined in the <code>tags</code> array.
   * 
   * @param cache
   *          the cache
   * @param tags
   *          the set of tags
   * @param partialMatches
   *          <code>true</code> to include partial matches, where only one of
   *          the tag matches instead of all
   * @return the collection of matching keys
   */
  private Collection<Object> getKeysForTags(Cache cache, CacheTag[] tags,
      boolean partialMatches) {
    // Create the parts of the key to look for
    List<String> keyParts = new ArrayList<String>(tags.length);
    for (CacheTag tag : tags) {
      StringBuffer b = new StringBuffer(tag.getName()).append("=").append(tag.getValue());
      keyParts.add(b.toString());
    }

    // Collect those keys that contain all relevant parts
    Collection<Object> cacheKeys = new ArrayList<Object>();
    key: for (Object k : cache.getKeys()) {
      String key = ((CacheEntryKey) k).tags;
      for (String keyPart : keyParts) {
        if (!key.contains(keyPart) && !partialMatches) {
          continue key;
        } else if (key.contains(keyPart)) {
          cacheKeys.add(k);
          continue key;
        }
      }
    }

    return cacheKeys;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.ResponseCache#invalidate(ch.entwine.weblounge.common.request.CacheHandle)
   */
  public void invalidate(CacheHandle handle) {
    if (handle == null)
      throw new IllegalArgumentException("Handle cannot be null");

    // Make sure the cache is still available and active
    if (cacheManager.getStatus() != Status.STATUS_ALIVE) {
      logger.debug("Cache '{}' has unexpected status '{}'", cacheManager.getName(), cacheManager.getStatus());
      return;
    }

    // Load the cache
    Cache cache = cacheManager.getCache(DEFAULT_CACHE);
    if (cache == null) {
      logger.debug("Cache for {} was deactivated, response is not being invalidated");
      return;
    }

    cache.remove(handle.getKey());

    // Mark the current transaction as finished and notify anybody that was
    // waiting for it to be finished
    synchronized (transactions) {
      transactions.remove(handle.getKey());
      transactions.notifyAll();
    }

    logger.debug("Removed {} from cache '{}'", handle.getKey(), id);

    // Inform listeners
    for (CacheListener listener : cacheListeners) {
      listener.cacheEntryRemoved(handle);
    }

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
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}