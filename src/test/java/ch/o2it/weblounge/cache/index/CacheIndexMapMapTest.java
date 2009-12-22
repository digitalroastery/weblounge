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

package ch.o2it.weblounge.cache.index;

import ch.o2it.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.o2it.weblounge.cache.impl.index.CacheIndexMapMap;
import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.impl.request.CacheTag;
import ch.o2it.weblounge.common.request.CacheHandle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * TODO: Comment CacheIndexMapMapTest
 */
public class CacheIndexMapMapTest {

  @Test
  public final void testLookup() {
    CacheIndexMapMap idx = new CacheIndexMapMap();
    List<Tag> l = new ArrayList<Tag>();
    l.add(new CacheTag("key1", "value1"));
    l.add(new CacheTag("key2", "value1"));
    CacheHandle h1 = new TaggedCacheHandle(l, 0, 0); 
    idx.addEntry(h1); 
    l.clear();
    l.add(new CacheTag("key1", "value2"));
    CacheHandle h2 = new TaggedCacheHandle(l, 0, 0);
    h2.addTag(new CacheTag("key2", "value1"));
    idx.addEntry(h2);   
    l.clear();
    l.add(new CacheTag("key1", "value1"));
    l.add(new CacheTag("key3", "value1"));
    CacheHandle h3 = new TaggedCacheHandle(l, 0, 0);
    idx.addEntry(h3);   
    l.clear();
    l.add(new CacheTag("key4", "value1"));
    CacheHandle h4 = new TaggedCacheHandle(l, 0, 0);
    h4.addTag(new CacheTag("key3"));
    idx.addEntry(h4);

    
    l.clear();
    l.add(new CacheTag("key1", "value1"));
    Set<CacheHandle> res = new HashSet<CacheHandle>();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h1));
    assert(res.contains(h3));
  
    // tests
    l.clear();
    l.add(new CacheTag("key1", "value2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 1);
    assert(res.contains(h2));
  
    l.clear();
    l.add(new CacheTag("key1", "value3"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 0);

    l.clear();
    l.add(new CacheTag("key1", new Object()));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 0);

    l.clear();
    l.add(new CacheTag("key1", null));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 3);
    assert(res.contains(h1));
    assert(res.contains(h2));
    assert(res.contains(h3));

    l.clear();
    l.add(new CacheTag("key1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 3);
    assert(res.contains(h1));
    assert(res.contains(h2));
    assert(res.contains(h3));

    l.clear();
    l.add(new CacheTag("key2", "value1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h1));
    assert(res.contains(h2));

    l.clear();
    l.add(new CacheTag("key2", "value2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 0);

    l.clear();
    l.add(new CacheTag("key2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h1));
    assert(res.contains(h2));

    l.clear();
    l.add(new CacheTag("key1", "value1"));
    l.add(new CacheTag("key2", "value1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 1);
    assert(res.contains(h1));

    l.clear();
    l.add(new CacheTag("key1", "value1"));
    l.add(new CacheTag("key2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 1);
    assert(res.contains(h1));

    l.clear();
    l.add(new CacheTag("key1"));
    l.add(new CacheTag("key2", "value1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h1));
    assert(res.contains(h2));

    l.clear();
    l.add(new CacheTag("key1"));
    l.add(new CacheTag("key2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h1));
    assert(res.contains(h2));

    l.clear();
    l.add(new CacheTag("key2", "value2"));
    l.add(new CacheTag("key1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 0);

    l.clear();
    l.add(new CacheTag("key3", "value1"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h3));
    assert(res.contains(h4));

    l.clear();
    l.add(new CacheTag("key3", "value2"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 1);
    assert(res.contains(h4));

    l.clear();
    l.add(new CacheTag("key3", new Object()));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 1);
    assert(res.contains(h4));

    l.clear();
    l.add(new CacheTag("key3"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 2);
    assert(res.contains(h3));
    assert(res.contains(h4));

    l.clear();
    l.add(new CacheTag("key5"));
    res.clear();
    for (CacheHandle h : idx.lookup(l))
      res.add(h);
    assert(res.size() == 0);
  }

  @Test
  public void testAddEntry() {
    // TODO: implement, in the mean time, we have a passing test!   
  }

  @Test
  public void testRemoveEntry() {
    // TODO: implement, in the mean time, we have a passing test!   
  }

  @Test
  public final void perfLookup() {
    testPerformance(new PerfTest() {
      @Override void lookup(CacheIndexMapMap idx, List<Tag> l) { idx.lookup(l); }
      @Override public String toString() { return "Unoptimized lookup, optimized intersect, optimized ANY"; }
      });
  }
  
  @SuppressWarnings("boxing")
  private final void testPerformance(PerfTest pt) {
    System.out.println("Testing performance: " + pt);
    CacheIndexMapMap idx = new CacheIndexMapMap();
    Random r = new Random();
    
    final int NR_HDL = pt.getHdlCount();
    final int TAGS_PER_HDL = pt.getTagsPerHdl();
    final int MAX_RND = pt.getMaxRnd();
    final int PERF_LOOPS = pt.getPerfLoops();
    System.out.format("Params: NR_HDL=%d, TAGS_PER_HDL=%d, MAX_RND=%d, PERF_LOOPS=%d%n", NR_HDL, TAGS_PER_HDL, MAX_RND, PERF_LOOPS);
    
    long t = System.currentTimeMillis();
    for (int i = 1; i < NR_HDL; i++) {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("hdl", i));
      CacheHandle h = new TaggedCacheHandle(l, 0, 0);
      for (int j = 0; j < TAGS_PER_HDL; j++) {
        h.addTag(new CacheTag("key" + r.nextInt(MAX_RND), j == 0 ? CacheTag.ANY : "value" + r.nextInt(MAX_RND)));
      }
      idx.addEntry(h);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("insert: " + t);

    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", "value1"));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 1: " + t);
    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", "value1"));
      l.add(new CacheTag("key2", "value2"));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 2: " + t);
    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", "value1"));
      l.add(new CacheTag("key2", "value2"));
      l.add(new CacheTag("key3", "value3"));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 3: " + t);
    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", "value1"));
      l.add(new CacheTag("key2", null));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 4: " + t);
    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", null));
      l.add(new CacheTag("key2", "value2"));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 5: " + t);
    t = System.currentTimeMillis();
    for (int i = 0; i < PERF_LOOPS; i++)
    {
      List<Tag> l = new ArrayList<Tag>(10);
      l.add(new CacheTag("key1", null));
      l.add(new CacheTag("key2", null));
      pt.lookup(idx, l);
    }
    t = System.currentTimeMillis() - t;
    System.out.println("lookup 6: " + t + '\n');
  }

  static abstract class PerfTest {
    int getHdlCount() { return 10000; }
    int getMaxRnd()  { return 50; }
    int getTagsPerHdl() { return 10; }
    int getPerfLoops() { return 1000; }
    abstract void lookup(CacheIndexMapMap idx, List<Tag> l);
  }

}