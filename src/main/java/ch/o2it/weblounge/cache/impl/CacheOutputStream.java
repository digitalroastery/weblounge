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
import ch.o2it.weblounge.common.impl.util.datatype.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;

/**
 * @version $Revision: 1.5 $ $Date: 2005/11/29 00:04:26 $
 * @author Daniel Steiner
 */
class CacheOutputStream extends ServletOutputStream {

  /** the modification date of the new cached elements */
  private long modified = System.currentTimeMillis();

  /** indicates whether to write the output to the cache */
  private boolean invalid = false;

  /** holds the actual element hierarchy */
  private Stack<ActiveElement> hierarchy = new Stack<ActiveElement>();

  /** the default buffer size for the response */
  private static final int BUFFER_SIZE = 20 * 1024;

  /** the logging facility provided by log4j */
  private static final Logger log = LoggerFactory.getLogger(CacheOutputStream.class);

  /** the output buffer */
  private byte buf[] = new byte[BUFFER_SIZE];

  /** the write position in the output buffer */
  private int pos = 0;

  /**
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public synchronized void write(int b) {
    int newpos = pos + 1;
    if (newpos > buf.length)
      extendBuffer(newpos);
    buf[pos] = (byte) b;
    pos = newpos;
  }

  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public synchronized void write(byte[] b, int off, int len) {
    /*
     * NOTE: don't do any checks on the arguments since System.arraycopy() will
     * check them anyways.
     */
    int newpos = pos + len;
    if (newpos > buf.length)
      extendBuffer(newpos);
    System.arraycopy(b, off, buf, pos, len);
    pos = newpos;
  }

  /**
   * Extend the buffer to at least <code>size</code> bytes.
   * 
   * @param size
   *          the minimum length of the new buffer
   */
  private void extendBuffer(int size) {
    int goal = buf.length << 1;
    byte newbuf[] = new byte[goal > size ? goal : size];
    System.arraycopy(buf, 0, newbuf, 0, pos);
    buf = newbuf;
  }

  /**
   * Signals the begin of a new cache entry.
   * 
   * @param hnd
   *          the handle that identifies the cache entry
   * @param hit
   *          true, if already in the cache
   */
  synchronized void newEntry(CacheHandle hnd, boolean hit) {
    if (hnd == null)
      return;

    /* create a new active object */
    ActiveElement a = new ActiveElement(hnd, hit, pos);

    /* update the element hierarchy */
    ActiveElement parent = hierarchy.top();
    if (parent != null) {
      a.parent = parent.hnd;
      parent.children.add(hnd);
    }

    /* update the expiration time for all enclosing objects */
    for (ActiveElement e : hierarchy) {
      if (e == null)
        continue;
      if (hnd.getExpires() < e.hnd.getExpires())
        e.hnd.setExpires(hnd.getExpires());
      if (hnd.getRecheck() < e.hnd.getRecheck())
        e.hnd.setRecheck(hnd.getRecheck());
    }

    /* add the new active element to the hierarchy */
    hierarchy.push(a);
  }

  /**
   * Signals the end of a cache entry.
   * 
   * @param hnd
   *          the handle that identifies the cache entry
   */
  synchronized void endEntry(CacheHandle hnd) {
    if (hnd == null)
      return;

    /*
     * remove all entries up to the given handle from the active entries and
     * copy them to the cache
     */
    int count = 0;
    while (!hierarchy.empty()) {
      count++;
      ActiveElement e = hierarchy.pop();
      if (e.hnd != hnd) {
        log.error("Cache inconsistency: removed entry " + e.hnd);
        invalid = true;
        copyToCache(e, null);
      } else {
        copyToCache(e, null);
        break;
      }
    }

    /* do some consistency checks */
    switch (count) {
    case 0:
      log.error("Cache inconsistency: no active elements in endEntry() for " + hnd);
      invalid = true;
      break;
    case 1:
      break;
    default:
      log.error("Cache inconsistency: sequencing error in endEntry() for " + hnd + ", removed " + count + " entries" + (hierarchy.empty() ? ", no more entries left" : ""));
      break;
    }
  }

  /**
   * Returns the raw output buffer.
   * 
   * @return the output buffer
   */

  byte[] getBuffer() {
    return buf;
  }

  /**
   * Inserts an active cache entry into the cache.
   * 
   * @param e
   *          the element to be inserted into the cache.
   * @param meta
   *          the response meta information
   */
  private void copyToCache(ActiveElement e, CachedResponseMetaInfo meta) {
    assert e != null : "active element must not be null!";
    if (invalid)
      return;

    if (e.hit) {
      /* modify the existing cache element */
      CacheManager.modify(e.hnd, e.parent);
      log.debug("Modified element of type " + e.hnd.getClass().getName());
    } else {

      /* copy the text in a new cache buffer */
      int length = pos - e.startPos;
      byte buf[] = new byte[length];
      System.arraycopy(this.buf, e.startPos, buf, 0, length);

      /* insert the new cache buffer into the cache */
      log.debug("Trying to insert " + e.hnd.getClass().getName() + " into cache");
      CacheManager.insert(e.hnd, buf, modified, e.parent, e.children, hierarchy.empty() ? meta : null);
    }
  }

  /**
   * Signals the end of the output. Writes all uncached elements to the cache.
   * 
   * @param meta
   *          the response meta information
   */
  @SuppressWarnings("fallthrough")
  synchronized void endOutput(CachedResponseMetaInfo meta) {
    switch (hierarchy.size()) {
    case 0:
      log.error("Cache inconsistency: no active elements in endOutput()");
      return;
    default:
      log.error("Cache inconsistency: too many active elements in endOutput()");
      invalid = true;
    case 1:
      while (!hierarchy.empty())
        copyToCache(hierarchy.pop(), meta);
    }
  }

  /**
   * Tells the cache writer to stop adding output to the cache.
   */
  synchronized void invalidateOutput() {
    invalid = true;
  }

  /**
   * Represents an element that is currently created.
   */
  private static class ActiveElement {

    protected CacheHandle hnd, parent;
    protected List<CacheHandle> children = new ArrayList<CacheHandle>(5);
    protected boolean hit;
    protected int startPos;

    protected ActiveElement(CacheHandle hnd, boolean hit, int startPos) {
      this.hnd = hnd;
      this.hit = hit;
      this.startPos = startPos;
    }

  }

}