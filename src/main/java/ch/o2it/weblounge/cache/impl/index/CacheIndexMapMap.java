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

package ch.o2it.weblounge.cache.impl.index;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.impl.request.CacheTag;
import ch.o2it.weblounge.common.impl.util.datatype.IdentityHashSet;
import ch.o2it.weblounge.common.request.CacheHandle;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The <code>CacheIndexMapMap</code> is used to speed up lookup operations in
 * the response cache.
 */
public final class CacheIndexMapMap {

  /** the actual cache index map */
  private final Map<String, Map<Object, Set<CacheHandle>>> map = new HashMap<String, Map<Object, Set<CacheHandle>>>();

  /** the index statistics map */
  private final Map<String, KeyStats> stats = new HashMap<String, KeyStats>();

  /** the index lock */
  private final ReadWriteLock rwl = new ReentrantReadWriteLock();

  /** the index read lock */
  private final Lock rl = rwl.readLock();

  /** the index write lock */
  private final Lock wl = rwl.writeLock();

  /**
   * Returns the number of indexed elements for the given key.
   * 
   * @param key
   *          the key
   * @return the number of indexed elements
   */
  public int getElementCount(String key) {
    KeyStats keyStats = stats.get(key);
    return keyStats != null ? keyStats.totalElementCount : 0;
  }

  /**
   * Returns the number of indexed elements with the value <code>any</code> for
   * the given key.
   * 
   * @param key
   *          the key
   * @return the number of indexed elements with value <code>any</code>
   */
  public int getAnyCount(String key) {
    KeyStats keyStats = stats.get(key);
    return keyStats != null ? keyStats.anyCount : 0;
  }

  /**
   * Returns the number of indexed elements with a value other than
   * <code>any</code> for the given key.
   * 
   * @param key
   *          the key
   * @return the number of indexed elements with values other than
   *         <code>any</code>
   */
  public int getValueCount(String key) {
    KeyStats keyStats = stats.get(key);
    return keyStats != null ? keyStats.valueCount : 0;
  }

  /**
   * Add an Entry to the index.
   * 
   * @param entry
   *          the entry to add to the index
   */
  public void addEntry(CacheHandle entry) {
    wl.lock();
    try {
      for (Tag tag : entry.getTags()) {
        Map<Object, Set<CacheHandle>> m = map.get(tag.getName());
        if (m == null) {
          m = new HashMap<Object, Set<CacheHandle>>();
          map.put(tag.getName(), m);
        }
        Set<CacheHandle> l = m.get(tag.getValue());
        if (l == null) {
          l = new IdentityHashSet<CacheHandle>();
          m.put(tag.getValue(), l);
        }
        l.add(entry);
      }
    } finally {
      wl.unlock();
    }
  }

  /**
   * Remove an entry from the index.
   * 
   * @param entry
   *          the entry to remove from the index
   */
  public final void removeEntry(CacheHandle entry) {
    wl.lock();
    try {
      for (Tag tag : entry.getTags()) {
        Map<Object, Set<CacheHandle>> m = map.get(tag.getName());
        if (m != null) {
          Set<CacheHandle> l = m.get(tag.getValue());
          if (l != null) {
            l.remove(entry);
            if (l.isEmpty()) {
              m.remove(tag.getValue());
              if (m.isEmpty())
                map.remove(tag.getName());
            }
          }
        }
      }
    } finally {
      wl.unlock();
    }
  }

  /**
   * Clears the whole index.
   */
  public final void clear() {
    wl.lock();
    try {
      map.clear();
      stats.clear();
    } finally {
      wl.unlock();
    }
  }

  /**
   * Lookup entries in the index.
   * 
   * @param tags
   *          a set of tags identifying the index entries
   * @return all index entries that hold the given tags
   */
  public final Iterable<CacheHandle> lookup(Iterable<Tag> tags) {
    rl.lock();
    try {
      if (tags == null)
        return new EmptyIterable<CacheHandle>();

      boolean first = true;
      Set<CacheHandle> res = null;

      // TODO: use some lookup order optimization

      for (Tag tag : tags) {
        if (first) {
          first = false;
          res = lookup(tag);
        } else {
          lookupAndIntersect(tag, res);
        }
        if (res.size() == 0)
          break;
      }
      return res == null ? new EmptyIterable<CacheHandle>() : res;
    } finally {
      rl.unlock();
    }
  }

  /**
   * Lookup all entries for the given tag.
   * 
   * @param tag
   *          a tag identifying the index entries
   * @return a set of index entries holding the given tag
   */
  private final Set<CacheHandle> lookup(Tag tag) {
    Set<CacheHandle> res = new IdentityHashSet<CacheHandle>();
    if (tag != null) {
      Map<Object, Set<CacheHandle>> m = map.get(tag.getName());
      if (m != null) {
        if (tag.getValue() == CacheTag.ANY) {
          for (Set<CacheHandle> l : m.values())
            res.addAll(l);
        } else {
          Set<CacheHandle> l = m.get(tag.getValue());
          if (l != null)
            res.addAll(l);
          l = m.get(CacheTag.ANY);
          if (l != null)
            res.addAll(l);
        }
      }
    }
    return res;
  }

  /**
   * Lookup entries for the given tag and intersect them with the given set of
   * index entries.
   * 
   * @param tag
   *          a tag identifying index entries
   * @param res
   *          the set of previously matched entries that will be intersected
   *          with the set of matching entries for <code>tag</code>
   */
  private final void lookupAndIntersect(Tag tag, Set<CacheHandle> res) {
    assert (res != null);
    if (tag != null) {
      Map<Object, Set<CacheHandle>> m = map.get(tag.getName());
      if (m != null) {
        if (tag.getValue() == CacheTag.ANY) {
          Set<CacheHandle> s = new IdentityHashSet<CacheHandle>();
          for (Set<CacheHandle> l : m.values())
            s.addAll(l);
          for (Iterator<CacheHandle> i = res.iterator(); i.hasNext();)
            if (!s.contains(i.next()))
              i.remove();
        } else {
          Set<CacheHandle> v = m.get(tag.getValue());
          Set<CacheHandle> a = m.get(CacheTag.ANY);
          if (v != null) {
            if (a != null) {
              for (Iterator<CacheHandle> i = res.iterator(); i.hasNext();) {
                CacheHandle h = i.next();
                if (!a.contains(h) && !v.contains(h))
                  i.remove();
              }
            } else {
              for (Iterator<CacheHandle> i = res.iterator(); i.hasNext();)
                if (!v.contains(i.next()))
                  i.remove();
            }
          } else if (a != null) {
            for (Iterator<CacheHandle> i = res.iterator(); i.hasNext();)
              if (!a.contains(i.next()))
                i.remove();
          } else {
            res.clear();
          }
        }
      } else {
        res.clear();
      }
    } else {
      res.clear();
    }
  }

  /**
   * Dumps the whole index to <code>System.out</code>.
   */
  final void dump() {
    rl.lock();
    try {
      SortedSet<Map.Entry<String, Map<Object, Set<CacheHandle>>>> s1 = new TreeSet<Map.Entry<String, Map<Object, Set<CacheHandle>>>>(new Comparator<Map.Entry<String, Map<Object, Set<CacheHandle>>>>() {
        public int compare(Entry<String, Map<Object, Set<CacheHandle>>> arg0,
            Entry<String, Map<Object, Set<CacheHandle>>> arg1) {
          return arg0.getKey().compareTo(arg1.getKey());
        }
      });
      s1.addAll(map.entrySet());
      for (final Map.Entry<String, Map<Object, Set<CacheHandle>>> e1 : s1) {
        System.out.println("TagKey=" + e1.getKey());
        SortedSet<Map.Entry<Object, Set<CacheHandle>>> s2 = new TreeSet<Map.Entry<Object, Set<CacheHandle>>>(new Comparator<Map.Entry<Object, Set<CacheHandle>>>() {
          public int compare(Entry<Object, Set<CacheHandle>> arg0,
              Entry<Object, Set<CacheHandle>> arg1) {
            return arg0.getKey().toString().compareTo(arg1.getKey().toString());
          }
        });
        s2.addAll(e1.getValue().entrySet());
        for (final Map.Entry<Object, Set<CacheHandle>> e2 : s2) {
          System.out.println("    TagValue=" + e2.getKey());
          for (CacheHandle h : e2.getValue()) {
            System.out.println("        Handle=" + h);
          }
        }
      }
    } finally {
      rl.unlock();
    }
  }

  private static final class KeyStats {
    int totalElementCount = 0;
    int valueCount = 0;
    int anyCount = 0;
  }

  private static final class EmptyIterable<E> implements Iterable<E> {

    /** creates a new empty iterable */
    EmptyIterable() { /* nothing to do */
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<E> iterator() {
      return new Iterator<E>() {
        public boolean hasNext() {
          return false;
        }

        public E next() {
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  /*
   * final Iterable<CacheHandle> lookup2(Iterable<Tag> tags) { if (tags == null)
   * return new EmptyIterable<CacheHandle>();
   * 
   * boolean first = true; Set<CacheHandle> res = new
   * IdentityHashSet<CacheHandle>();
   * 
   * for (Tag tag : tags) { Set<CacheHandle> x = lookup(tag); if (x.size() == 0)
   * return new EmptyIterable<CacheHandle>();
   * 
   * if (first) { first = false; res.addAll(x); } else { for
   * (Iterator<CacheHandle> i = res.iterator(); i.hasNext();) { if
   * (!x.contains(i.next())) i.remove(); } } if (res.size() == 0) return new
   * EmptyIterable<CacheHandle>(); } return res; }
   */

  /*
   * final Iterable<CacheHandle> lookup3(Iterable<Tag> tags) { if (tags == null)
   * return new EmptyIterable<CacheHandle>();
   * 
   * boolean first = true; Set<CacheHandle> res = null;
   * 
   * for (Tag tag : tags) { if (first) { first = false; res = lookup(tag); }
   * else { lookupAndIntersect2(tag, res); } if (res.size() == 0) break; }
   * return res == null ? new EmptyIterable<CacheHandle>() : res; }
   */

  /*
   * private final void lookupAndIntersect2(Tag tag, Set<CacheHandle> res) {
   * assert (res != null); if (tag != null) { Map<Object, Set<CacheHandle>> m =
   * map.get(tag.getName()); if (m != null) { if (tag.getValue() ==
   * CacheTag.ANY) { outer: for (Iterator<CacheHandle> i = res.iterator();
   * i.hasNext();) { CacheHandle h = i.next(); for (Set<CacheHandle> l :
   * m.values()) if (l.contains(h)) continue outer; i.remove(); } } else {
   * Set<CacheHandle> v = m.get(tag.getValue()); Set<CacheHandle> a =
   * m.get(CacheTag.ANY); if (v != null) { if (a != null) { for
   * (Iterator<CacheHandle> i = res.iterator(); i.hasNext();) { CacheHandle h =
   * i.next(); if (!a.contains(h) && !v.contains(h)) i.remove(); } } else { for
   * (Iterator<CacheHandle> i = res.iterator(); i.hasNext();) if
   * (!v.contains(i.next())) i.remove(); } } else if (a != null) { for
   * (Iterator<CacheHandle> i = res.iterator(); i.hasNext();) if
   * (!a.contains(i.next())) i.remove(); } else { res.clear(); } } } else {
   * res.clear(); } } else { res.clear(); } }
   */

}
