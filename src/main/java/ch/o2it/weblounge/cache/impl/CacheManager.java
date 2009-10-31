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

import ch.o2it.weblounge.cache.StreamFilter;
import ch.o2it.weblounge.cache.impl.filter.FilterChain;
import ch.o2it.weblounge.cache.impl.index.CacheIndexMapMap;
import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.impl.util.classloader.PluginLoader;
import ch.o2it.weblounge.common.request.CacheHandle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO Comment CacheManager
 */
public class CacheManager {

  /** the default character encoding */
  public static final String DEFAULT_ENCODING = "ISO-8859-1";

  /** The default cache size maximum */
  public final static long DEFAULT_CACHE_SIZE = 10 * 1024 * 1024;

  /** various statistical values */
  /* cache update events */
  public static final int CACHE_INSERT = 0;
  public static final int CACHE_EXPIRE = 1;
  public static final int CACHE_REMOVE = 2;
  public static final int CACHE_REPLACE = 3;
  public static final int CACHE_INVALIDATE = 4;
  public static final int CACHE_MODIFY = 5;

  /* cache transaction events */
  public static final int CACHE_TRANSACTION_START = 6;
  public static final int CACHE_TRANSACTION_COMPLETE = 7;
  public static final int CACHE_TRANSACTION_SKIP = 8;

  /* cache waiting events */
  public static final int CACHE_WAIT_START = 9;
  public static final int CACHE_WAIT_TIMEOUT = 10;
  public static final int CACHE_WAIT_SUCCESS = 11;
  public static final int CACHE_WAIT_FAIL = 12;

  /* the number of 'real counters' */
  public static final int NOF_COUNTERS = 13;

  /* additional statistical values */
  public static final int CACHE_SIZE = 20;
  public static final int CACHE_MAX_SIZE = 21;
  public static final int CACHE_TRANSACTION_ABORT = 22;
  public static final int RESPONSE_GENERATED = 23;
  public static final int CACHE_ENTRIES = 24;
  public static final int CACHE_WAIT_ACTIVE = 25;

  /** the statistics counters */
  private static long[] stats;

  /** statistics for the various cache element classes */
  private static Map<Class<? extends CacheHandle>, TypeStats> typeStats;

  /**
   * Holds the cached objects. Each Object is identified by a
   * <code>CacheHandle</code> which should implement meaningful
   * <code>hashCode</code> and <code>equals</code> methods.
   */
  private static final Map<CacheHandle, CacheEntry> cache = new HashMap<CacheHandle, CacheEntry>();

  /** Holds the LRU list. Use the <code>cache</code>-lock to manipulate! */
  static final CacheEntry lru = new CacheEntry(null, 0, 0, 0, null, null, null, null);

  /** the cache index */
  private static final CacheIndexMapMap index = new CacheIndexMapMap();

  /** Holds the current size of the cache in bytes */
  private static long cacheSize = 0;

  /** Holds the maximum size of the cache in bytes. Defaults to 10MB */
  private static long maxCacheSize = DEFAULT_CACHE_SIZE;

  /** indicates whether the cache is enabled */
  private static boolean enabled;

  /** the active cache transactions */
  private static Map<CacheHandle, CacheHandle> activeHandles = new HashMap<CacheHandle, CacheHandle>();

  /** active output filter */
  private static Class<? extends StreamFilter> filters[];

  /** all output filters */
  private static Map<String, Class<? extends StreamFilter>> allFilters;

  /** the logging facility provided by log4j */
  private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

  // initialize the class
  static {
    resetStats();
    lru.prev = lru;
    lru.next = lru;
  }

  /**
   * Method startCacheableResponse.
   * 
   * @param rootHnd
   * @param req
   * @param resp
   * @return HttpServletResponse
   */
  static HttpServletResponse startCacheableResponse(CacheHandle rootHnd,
      HttpServletRequest req, HttpServletResponse resp) {

    /* a cacheable servlet response can only be started once */
    if (resp instanceof CacheableHttpServletResponse)
      return resp;

    /* check whether the cache is enabled */
    if (!enabled) {
      log.info("Cache is not enabled");
      return resp;
    }

    /* find the root handle in the cache */
    CacheEntry entry = getCacheEntry(rootHnd);

    /*
     * wait for active transactions NOTE: this looks a bit ugly but it really
     * works, trust me!
     */
    if (entry == null) {
      synchronized (activeHandles) {
        while ((activeHandles.get(rootHnd)) != null) {
          incStats(CACHE_WAIT_START);
          try {
            activeHandles.wait();
          } catch (InterruptedException e) {
            incStats(CACHE_WAIT_TIMEOUT);
          }
        }
        activeHandles.put(rootHnd, rootHnd);
      }
      incStats(entry == null ? CACHE_WAIT_FAIL : CACHE_WAIT_SUCCESS);
    }

    if (entry != null) {
      /* handle the request */
      log.debug("Lookup for " + rootHnd + " succeeded (hit)");
      incStats(rootHnd.getClass(), true);
      incStats(CACHE_TRANSACTION_SKIP);

      /* apply the original response meta information to the new response */
      if (entry.meta != null)
        entry.meta.apply(resp);

      /* analyze the request */
      Http11ResponseType type = Http11ProtocolHandler.analyzeRequest(req, entry.modified, entry.recheck + System.currentTimeMillis(), entry.buf.length);

      /* generate response */
      Http11ProtocolHandler.generateResponse(resp, type, entry.buf);

      return null;
    }

    /* start a new response */
    log.debug("Lookup for " + rootHnd + " failed (miss)");
    incStats(rootHnd.getClass(), false);
    incStats(CACHE_TRANSACTION_START);

    StreamFilter filter = null;
    if (filters != null && filters.length != 0)
      try {
        if (filters.length == 1)
          filter = filters[0].newInstance();
        else {
          StreamFilter f[] = new StreamFilter[filters.length];
          for (int i = 0; i < f.length; i++)
            f[i] = filters[i].newInstance();
          filter = new FilterChain(f);
        }
      } catch (Exception e) {
        log.error("Unable to create filter: " + e.getMessage());
      }
    return new CacheableHttpServletResponse(new CacheTransaction(rootHnd, req, resp, filter));

  }

  /**
   * Method startHandle.
   * 
   * @param hnd
   * @param resp
   * @return boolean
   */
  static boolean startHandle(CacheHandle hnd, CacheableHttpServletResponse resp) {
    if (!enabled) {
      log.info("Cache is not enabled");
      return false;
    }
    if (resp == null) {
      log.info("Response is not cacheable");
      return false;
    }

    /* check whether the element already exists in the cache */
    CacheEntry e = getCacheEntry(hnd);
    if (e == null) {
      incStats(hnd.getClass(), false);
      resp.cacheMiss(hnd);
      log.debug("Lookup for " + hnd + " failed (miss)");
      return false;
    }

    incStats(hnd.getClass(), true);
    resp.cacheHit(hnd, e.buf);
    log.debug("Lookup for " + hnd + " succeeded (hit)");
    return true;
  }

  /**
   * Method endHandle.
   * 
   * @param hnd
   * @param resp
   */
  static void endHandle(CacheHandle hnd, CacheableHttpServletResponse resp) {
    if (!enabled) {
      log.info("Cache is not enabled");
      return;
    }
    if (resp == null) {
      log.info("Response is not cacheable");
      return;
    }
    resp.endEntry(hnd);
  }

  /**
   * Method endCacheableResponse.
   * 
   * @param resp
   */
  static boolean endCacheableResponse(CacheableHttpServletResponse resp) {
    if (resp == null)
      return true;

    incStats(CACHE_TRANSACTION_COMPLETE);
    CacheTransaction tx = resp.endOutput();

    /* get the generated response object */
    CacheEntry entry = getCacheEntry(tx.hnd);
    if (entry == null) {
      log.debug("Page not in cache after transaction completion. Writing temporary response.");
      /* create a temporary cache entry */
      entry = new CacheEntry(tx.hnd, System.currentTimeMillis(), 0, System.currentTimeMillis(), tx.os.getBuffer(), null, null, null);
    }

    /* wake all requests waiting for this transaction */
    synchronized (activeHandles) {
      CacheHandle hnd = activeHandles.remove(tx.hnd);
      if (hnd == null) {
        if (!resp.isInvalidated())
          log.warn("Active transaction not found in endCacheableResponse()");
        // log.warn("Active transaction not found in endCacheableResponse()",
        // new IllegalStateException());
      } else
        activeHandles.notifyAll();
    }

    /* analyze the request */
    Http11ResponseType type = Http11ProtocolHandler.analyzeRequest(tx.req, entry.modified, entry.recheck + System.currentTimeMillis(), entry.buf.length);

    /* finally, generate the response */
    if (!resp.isCommitted()) {
      return Http11ProtocolHandler.generateResponse(tx.resp, type, entry.buf);
    }
    return true;
  }

  /**
   * Method invalidateCacheableResponse.
   * 
   * @param resp
   */
  static void invalidateCacheableResponse(CacheableHttpServletResponse resp) {
    if (resp == null)
      return;

    /* wake all requests waiting for this transaction */
    synchronized (activeHandles) {
      CacheHandle hnd = activeHandles.remove(resp.tx.hnd);
      if (hnd == null) {
        if (!resp.isInvalidated())
          log.warn("Active transaction not found in invalidateCacheableResponse()");
        // log.warn("Active transaction not found in invalidateCacheableResponse()",
        // new IllegalStateException());
      } else {
        activeHandles.notifyAll();
      }
    }

    resp.invalidateOutput();
  }

  /**
   * Method incStats.
   * 
   * @param c
   * @param hit
   */
  private static void incStats(Class<? extends CacheHandle> c, boolean hit) {
    synchronized (typeStats) {
      /* find the types statistics */
      TypeStats s = typeStats.get(c);
      if (s == null) {
        s = new TypeStats(c.getName());
        typeStats.put(c, s);
      }
      /* update the stats */
      if (hit)
        ++s.hit;
      else
        ++s.miss;
    }
  }

  /**
   * Method incStats.
   * 
   * @param stat
   */
  private static void incStats(int stat) {
    if (stat >= 0 && stat < NOF_COUNTERS)
      ++stats[stat];
  }

  /**
   * Method invalidate.
   * 
   * @param tags
   * @return
   */
  static Set<CacheHandle> invalidate(Iterable<Tag> tags) {
    Set<CacheHandle> handles = new HashSet<CacheHandle>();
    for (CacheHandle hnd : index.lookup(tags)) {
      invalidateEntry(hnd, true, handles);
    }
    return handles;
  }

  /**
   * Method invalidateEntry.
   * 
   * @param hnd
   */
  static Set<CacheHandle> invalidateEntry(CacheHandle hnd) {
    Set<CacheHandle> handles = new HashSet<CacheHandle>();
    invalidateEntry(hnd, true, handles);
    return handles;
  }

  /**
   * Method invalidateEntry.
   * 
   * @param hnd
   * @param children
   */
  private static void invalidateEntry(CacheHandle hnd, boolean children,
      Set<CacheHandle> handles) {
    log.debug("Trying to invalidate " + hnd);
    synchronized (cache) {
      CacheEntry e = cache.remove(hnd);
      if (e != null) {
        handles.add(e.key);

        /* update the LRU list */
        e.unlink();

        /* remove from index */
        index.removeEntry(e.key);

        /* update the statistics */
        cacheSize -= e.buf.length;
        incStats(CACHE_INVALIDATE);
        log.debug("Entry " + e + " invalidated");

        /* remove parent entry */
        log.debug("Invalidating parents...");
        for (Iterator<CacheHandle> iter = e.parents.iterator(); iter.hasNext();)
          invalidateEntry(iter.next(), false, handles);

        /* remove child entries */
        if (children && e.children != null) {
          log.debug("Invalidating children...");
          for (Iterator<CacheHandle> iter = e.children.iterator(); iter.hasNext();)
            invalidateEntry(iter.next(), true, handles);
        }
        /* cleanup parent-child relationships */
        cleanupRelationships(e, false);
      }
    }
  }

  /**
   * Method cleanupRelationships.
   * 
   * @param e
   */
  private static void cleanupRelationships(CacheEntry e, boolean childrenOnly) {
    if (!childrenOnly) {
      for (CacheHandle p : e.parents) {
        CacheEntry pe = cache.get(p);
        if (pe != null && pe.children != null) {
          pe.children.remove(e.key);
        }
      }
    }
    if (e.children != null) {
      for (CacheHandle c : e.children) {
        CacheEntry ce = cache.get(c);
        if (ce != null)
          ce.parents.remove(e.key);
      }
    }
  }

  /**
   * Method getCacheEntry.
   * 
   * @param hnd
   * @return CacheEntry
   */
  private static CacheEntry getCacheEntry(CacheHandle hnd) {
    /* get the cache entry */
    CacheEntry e = cache.get(hnd);
    if (e == null)
      return null;

    /* check whether the object is still valid */
    if (e.expires < System.currentTimeMillis()) {
      log.debug(hnd + " is no longer valid");
      synchronized (cache) {
        if (cache.remove(hnd) != null) {
          e.unlink();
          index.removeEntry(e.key);
          cleanupRelationships(e, false);
          cacheSize -= e.buf.length;
          incStats(CACHE_EXPIRE);
        } else {
          log.debug("Unable to remove " + hnd);
        }
      }
      return null;
    }

    /* set the expiration time of the handle */
    // !TODO: Why is/was this line needed
    // hnd.setValid(e.valid);

    /* update LRU list */
    synchronized (cache) {
      e.unlink();
      e.link();
    }

    /* return the cached object */
    return e;
  }

  /**
   * Method insert.
   * 
   * @param hnd
   * @param buf
   * @param modified
   * @param parent
   * @param children
   * @param meta
   */
  static void insert(CacheHandle hnd, byte buf[], long modified,
      CacheHandle parent, List<CacheHandle> children,
      CachedResponseMetaInfo meta) {

    /* check whether the object is still valid and fits in the cache */
    if (enabled && (buf.length <= maxCacheSize)) {
      synchronized (cache) {
        /* make sure there's enough space to hold the entry */
        ensureFreeSpace(buf.length);

        /* add the new object */
        CacheEntry e = new CacheEntry(hnd, hnd.getExpires() + System.currentTimeMillis(), hnd.getRecheck(), modified, buf, parent, children, meta);
        CacheEntry old = cache.put(hnd, e);
        e.link();
        index.addEntry(e.key);
        cacheSize += buf.length;
        if (old != null) {
          old.unlink();
          index.removeEntry(old.key);
          cleanupRelationships(old, false);
          cacheSize -= old.buf.length;
          incStats(CACHE_REPLACE);
          log.debug("Replaced " + hnd + " in cache");
        } else {
          incStats(CACHE_INSERT);
          log.debug("Wrote " + hnd + " to cache");
        }
      }
    } else {
      log.debug("Element " + hnd + " is not valid or too big");
    }
  }

  /**
   * Method modify.
   * 
   * @param hnd
   * @param parent
   */
  static void modify(CacheHandle hnd, CacheHandle parent) {
    CacheEntry e = getCacheEntry(hnd);
    if (e == null || parent == null)
      return;

    synchronized (e.parents) {
      for (Iterator<CacheHandle> iter = e.parents.iterator(); iter.hasNext();) {
        CacheHandle p = iter.next();
        if (parent == p)
          return;
        if (parent.equals(p)) {
          log.debug("Replacing parent " + p + " of " + hnd + " with " + parent);
          e.parents.remove(p);
          e.parents.add(parent);
          incStats(CACHE_MODIFY);
          return;
        }
      }
    }
    log.debug("Adding parent " + parent + " to " + hnd);
    e.parents.add(parent);
    incStats(CACHE_MODIFY);

  }

  /**
   * Method ensureFreeSpace.
   * 
   * @param bytes
   */
  private static void ensureFreeSpace(long bytes) {
    synchronized (cache) {
      while (cacheSize + bytes > maxCacheSize && lru.prev != lru) {
        CacheEntry e = lru.prev;
        /* remove the element */
        if (cache.remove(e.key) != null) {
          incStats(CACHE_REMOVE);
          cacheSize -= e.buf.length;
        } else {
          // log.error("LRU element could not be removed from cache. Element lost...");
          // This should not occur any more...
          log.error("LRU element could not be removed from cache. Element lost, flushing cache. The bug seems to be more serious than expected!");
          // TODO: Fix parent - children relationship
          emptyCache();
          return;
        }
        e.unlink();
        index.removeEntry(e.key);
        cleanupRelationships(e, false);
      }
    }
  }

  /**
   * Clears the cache.
   */
  static void emptyCache() {
    log.debug("Flushing cache...");
    synchronized (cache) {
      cache.clear();
      cacheSize = 0;
      lru.next = lru.prev = lru;
      index.clear();
    }
  }

  /**
   * Shuts down the cache.
   */
  public static void shutdown() {
    setEnabled(false);
    emptyCache();
    resetStats();
  }

  /**
   * Returns the TypeStats.
   * 
   * @return iterator
   */
  public static Iterator<TypeStats> getTypeStatistics() {
    return typeStats.values().iterator();
  }

  /**
   * Sets the enabled.
   * 
   * @param enabled
   *          The enabled to set
   */
  static void setEnabled(boolean enabled) {
    CacheManager.enabled = enabled;
    log.debug("The cache is now " + (enabled ? "enabled" : "disabled"));
  }

  /**
   * Returns the enabled.
   * 
   * @return boolean
   */
  public static boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the maxCacheSize.
   * 
   * @param maxCacheSize
   *          The maxCacheSize to set
   */
  static void setMaxCacheSize(long maxCacheSize) {
    CacheManager.maxCacheSize = maxCacheSize;
    ensureFreeSpace(0);
    log.debug("New cache size is now " + maxCacheSize + " bytes");
  }

  /**
   * Sets the output filters.
   * 
   * @param filter
   *          a coma separated list of filter names
   */
  @SuppressWarnings("unchecked")
  static void setFilters(String filter) {
    if (filter == null)
      return;
    if (allFilters == null) {
      allFilters = new HashMap<String, Class<? extends StreamFilter>>();
      Class<? extends StreamFilter> all[] = PluginLoader.findPlugins("ch.o2it.weblounge.service.cache.filter", null, new String[] { "ch.o2it.weblounge.api.request.StreamFilter" }, CacheManager.class.getClassLoader());
      for (int i = 0; i < all.length; i++) {
        String name = all[i].getName();
        int j = -1;
        if ((j = name.lastIndexOf('.')) >= 0)
          name = name.substring(j + 1);
        log.debug("found filter: " + name);
        allFilters.put(name, all[i]);
      }
    }
    StringTokenizer st = new StringTokenizer(filter, ",");
    List<Class<? extends StreamFilter>> l = new ArrayList<Class<? extends StreamFilter>>();
    while (st.hasMoreTokens()) {
      String tok = st.nextToken().trim();
      if (tok.length() == 0)
        continue;
      Class<? extends StreamFilter> c = allFilters.get(tok);
      if (c == null)
        log.warn("ignoring unknown filter: " + tok);
      else {
        l.add(c);
        log.debug("using filter: " + tok);
      }
    }
    filters = new Class[l.size()];
    for (int i = 0; i < filters.length; i++)
      filters[i] = l.get(i);
  }

  /**
   * Method resetStats.
   */
  static void resetStats() {
    log.debug("Resetting statistics...");
    typeStats = new HashMap<Class<? extends CacheHandle>, TypeStats>();
    stats = new long[NOF_COUNTERS];
  }

  /**
   * Method getStatistics.
   * 
   * @param stat
   * @return the statistic value
   */
  public static long getStatistics(int stat) {
    if (stat >= 0 && stat < NOF_COUNTERS)
      return stats[stat];
    switch (stat) {
    case CACHE_SIZE:
      return cacheSize;
    case CACHE_MAX_SIZE:
      return maxCacheSize;
    case CACHE_TRANSACTION_ABORT:
      return stats[CACHE_TRANSACTION_START] - stats[CACHE_TRANSACTION_COMPLETE];
    case RESPONSE_GENERATED:
      return stats[CACHE_TRANSACTION_COMPLETE] + stats[CACHE_TRANSACTION_SKIP];
    case CACHE_ENTRIES:
      return cache.size();
    case CACHE_WAIT_ACTIVE:
      return stats[CACHE_WAIT_START] - (stats[CACHE_WAIT_SUCCESS] + stats[CACHE_WAIT_FAIL]);
    }
    return -1;
  }

  /**
   * Dumps the complete cache hierarchy of a cache element identified by the
   * given cache handle. If the cache element if part of multiple hierarchies,
   * only the first hierarchy is dumped. <br>
   * <br>
   * 
   * **WARNING** **WARNING** **WARNING** **WARNING** **WARNING**:<br>
   * This operation is expensive and locks the whole cache while generating the
   * output. Do not use this in regular debugging code since it can drastically
   * reduce the overall system performance. Use the following method if you
   * really must dump cache entries in logs:
   * 
   * <pre>
   * if (log.isDebugEnabled())
   *   log.debug(dumpHierarchy(hnd));
   * </pre>
   * 
   * @param hnd
   *          the cache handle to identify a cache hierarchy
   * @return a string respresentation of the cache hierarchy
   */
  public static String dumpHierarchy(CacheHandle hnd) {
    synchronized (cache) {
      CacheEntry e = null;
      while ((e = cache.get(hnd)) != null && e.parents.size() > 0)
        hnd = e.parents.get(0);
      if (e == null)
        return null;
      StringBuffer sb = new StringBuffer();
      dumpHierarchy(sb, hnd, 0);
      return sb.toString();
    }
  }

  /**
   * Dumps the whole cache hierarchy to the associated logger.
   */
  public static void dumpCacheAsError() {
    log.error("Full cache dump...\n\n" + dumpCache() + "done!");
  }

  /**
   * Dumps the class names along with the number of occurences in the cache.
   * 
   * @return a mapping between cache handle type and occurence
   */
  public static Map<Class<? extends CacheHandle>, Long> dumpCacheTypes() {
    synchronized (cache) {
      Map<Class<? extends CacheHandle>, Long> types = new HashMap<Class<? extends CacheHandle>, Long>();
      for (Iterator<Map.Entry<CacheHandle, CacheEntry>> i = cache.entrySet().iterator(); i.hasNext();) {
        Map.Entry<CacheHandle, CacheEntry> e = i.next();
        CacheHandle handle = e.getKey();
        Long typeCount = types.remove(handle.getClass());
        typeCount = new Long(typeCount == null ? 1 : typeCount.longValue() + 1);
        types.put(handle.getClass(), typeCount);
      }
      return types;
    }
  }

  /**
   * Dumps the whole cache hierarchy.
   * 
   * @return a string representation of the whole cache hierarchy
   */
  public static String dumpCache() {
    synchronized (cache) {
      Map<Class<? extends CacheHandle>, Long> types = new HashMap<Class<? extends CacheHandle>, Long>();
      StringBuffer sb = new StringBuffer();
      for (Iterator<Map.Entry<CacheHandle, CacheEntry>> i = cache.entrySet().iterator(); i.hasNext();) {
        Map.Entry<CacheHandle, CacheEntry> e = i.next();
        CacheHandle handle = e.getKey();
        Long typeCount = types.remove(handle.getClass());
        typeCount = new Long(typeCount == null ? 1 : typeCount.longValue() + 1);
        types.put(handle.getClass(), typeCount);
        if (e.getValue().parents.size() != 0)
          continue;
        dumpHierarchy(sb, handle, 0);
        sb.append('\n');
      }

      StringBuffer summary = new StringBuffer();
      for (Class<? extends CacheHandle> c : types.keySet()) {
        summary.append(c.getName());
        summary.append(": ");
        summary.append(types.get(c));
        summary.append("\n");
      }
      summary.append("\n");
      summary.append(sb);
      return summary.toString();
    }
  }

  /**
   * Returns the cache's key set.
   * 
   * @return the key set
   */
  /*
   * @Deprecated public static Set<CacheHandle> keys() { synchronized(cache) {
   * return cache.keySet(); } }
   */

  /**
   * Returns the cache's entry set.
   * 
   * @return the entry set
   */
  /*
   * @Deprecated public static Set<Map.Entry<CacheHandle, CacheEntry>> entries()
   * { synchronized(cache) { return cache.entrySet(); } }
   */

  /**
   * Recursively dumps cache entries to a string buffer. <br>
   * 
   * Note: this methods assumes that calling thread holds the big cache-lock
   * 
   * @param sb
   *          the buffer to dump the entries to
   * @param hnd
   *          the cache handle to dump
   * @param depth
   *          the current depth in the hierarchy
   */
  private static void dumpHierarchy(StringBuffer sb, CacheHandle hnd, int depth) {
    if (hnd == null)
      return;
    CacheEntry e = cache.get(hnd);
    if (e == null)
      return;
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < depth - 1; i++)
      b.append("|  ");
    if (depth > 0)
      b.append("+--");
    b.append(Integer.toHexString(hnd.getClass().hashCode() ^ hnd.hashCode()).toUpperCase());
    b.append(": ");
    b.append(hnd.getShortName());
    sb.append(b);
    int len = 80 - b.length();
    while (--len > 0)
      sb.append(' ');
    sb.append(formatTime(e.expires - System.currentTimeMillis()));
    sb.append(", ");
    sb.append(formatTime(e.recheck));
    sb.append('\n');
    if (e.children == null)
      return;
    for (Iterator<CacheHandle> i = e.children.iterator(); i.hasNext();)
      dumpHierarchy(sb, i.next(), depth + 1);
  }

  /**
   * Method formatTime
   * 
   * @param time
   * @return
   */
  public static final String formatTime(long time) {
    boolean sig = time < 0;
    time = Math.abs(time) / 1000;
    long s = time % 60;
    time /= 60;
    long m = time % 60;
    long h = time / 60;
    return MessageFormat.format((sig ? '-' : ' ') + "{0,number,000}:{1,number,00}:{2,number,00}", new Object[] {
        new Long(h),
        new Long(m),
        new Long(s) });
  }

  /**
   * Represents a single entry in the cache.
   * 
   * @version $Revision: 1.25.2.3 $ $Date: 2006/10/26 00:25:37 $
   * @author Daniel Steiner
   */
  public static class CacheEntry implements Comparable<CacheHandle> {
    protected CacheEntry(CacheHandle key, long expires, long recheck,
        long modified, byte buf[], CacheHandle parent,
        List<CacheHandle> children, CachedResponseMetaInfo meta) {
      this.key = key;
      this.expires = expires;
      this.recheck = recheck;
      this.buf = buf;
      this.modified = modified;
      if (parent != null)
        this.parents.add(parent);
      this.children = children;
      this.meta = meta;
    }

    protected CacheHandle key;
    protected List<CacheHandle> parents = new ArrayList<CacheHandle>(3);
    protected List<CacheHandle> children;
    protected long expires;
    protected long recheck;
    protected byte buf[];
    protected long modified;
    protected CacheEntry prev, next;
    protected CachedResponseMetaInfo meta;

    protected CacheHandle getKey() {
      return key;
    }

    protected long getModified() {
      return modified;
    }

    protected void unlink() {
      next.prev = prev;
      prev.next = next;
    }

    protected void link() {
      prev = lru;
      next = lru.next;
      next.prev = this;
      lru.next = this;
    }

    public int compareTo(CacheHandle c) {
      return key.toString().compareTo(c.toString());
    }

  }

  /**
   * Holds the statistics for a specific cached element type.
   * 
   * @version $Revision: 1.25.2.3 $ $Date: 2006/10/26 00:25:37 $
   * @author Daniel Steiner
   */
  public static class TypeStats {
    protected int hit;
    protected int miss;
    protected String name;

    protected TypeStats(String name) {
      this.name = name;
    }

    public int getHit() {
      return hit;
    }

    public int getMiss() {
      return miss;
    }

    public int getTotal() {
      return hit + miss;
    }

    public String getName() {
      return name;
    }

  }

}
