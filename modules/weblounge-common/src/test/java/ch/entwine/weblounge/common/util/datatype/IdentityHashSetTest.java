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

package ch.entwine.weblounge.common.util.datatype;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.util.datatype.IdentityHashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

/**
 * IdentityHashSetTest
 */
public class IdentityHashSetTest {

  /** the map under test */
  private Set<Object> s;

  /** some helpers */
  private Object[] o = new Object[5];

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    s = new IdentityHashSet<Object>();
    o = new Object[5];
    for (int i = 0; i < o.length; i++) {
      // Need to do it this way, since we really want different objects
      o[i] = new StringBuffer("Test1").toString();
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    s = null;
    o = null;
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#size()}.
   */
  @Test
  public final void testSize() {
    assertTrue(s.size() == 0);
    s.add(o[0]);
    assertTrue(s.size() == 1);
    s.add(o[1]);
    assertTrue(s.size() == 2);
    s.add(o[2]);
    assertTrue(s.size() == 3);
    s.add(o[2]);
    assertTrue(s.size() == 3);
    s.add(o[3]);
    assertTrue(s.size() == 4);
    s.remove(o[2]);
    assertTrue(s.size() == 3);
    s.remove(o[2]);
    assertTrue(s.size() == 3);
    s.remove("Test1");
    assertTrue(s.size() == 3);
    s.clear();
    assertTrue(s.size() == 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#isEmpty()}.
   */
  @Test
  public final void testIsEmpty() {
    assertTrue(s.isEmpty());
    s.add(o[0]);
    assertTrue(!s.isEmpty());
    s.remove("Test1");
    assertTrue(!s.isEmpty());
    s.remove(o[0]);
    assertTrue(s.isEmpty());
    s.add(o[0]);
    s.add(o[1]);
    assertTrue(!s.isEmpty());
    s.clear();
    assertTrue(s.isEmpty());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#clear()}.
   */
  @Test
  public final void testClear() {
    assertTrue(s.isEmpty());
    s.clear();
    assertTrue(s.isEmpty());
    s.add(o[0]);
    assertTrue(!s.isEmpty());
    s.clear();
    assertTrue(s.isEmpty());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#IdentityHashSet()}
   * .
   */
  @Test
  public final void testIdentityHashSet() {
    Set<String> l = new IdentityHashSet<String>();
    assertNotNull(l);
    assertTrue(l.size() == 0);
    assertTrue(l.isEmpty());
    assertTrue(!s.remove("Test"));
    assertTrue(!s.contains("Test"));
    assertTrue(s.add("Test"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#add(java.lang.Object)}
   * .
   */
  @Test
  public final void testAddE() {
    assertTrue(s.add(o[0]));
    assertTrue(s.add(o[1]));
    assertTrue(!s.add(o[0]));
    assertTrue(!s.add(o[1]));
    assertTrue(s.add(o[2]));
    s.clear();
    assertTrue(s.add(o[0]));
    assertTrue(s.add(o[1]));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#startsWith(java.lang.Object)}
   * .
   */
  @Test
  public final void testContainsObject() {
    assertTrue(!s.contains(o[0]));
    s.add(o[0]);
    assertTrue(s.contains(o[0]));
    assertTrue(!s.contains(o[1]));
    s.add(o[1]);
    assertTrue(s.contains(o[1]));
    s.remove(o[0]);
    assertTrue(!s.contains(o[0]));
    s.remove(o[1]);
    assertTrue(!s.contains(o[1]));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#iterator()}.
   */
  @Test
  public final void testIterator() {
    Iterator<Object> i = s.iterator();
    assertTrue(i != null && !i.hasNext());
    for (int j = 0; j < o.length; j++)
      s.add(o[j]);
    i = s.iterator();
    assertTrue(i != null);
    for (int j = 0; j < o.length; j++) {
      assertTrue(i != null && i.hasNext() && i.next() != null);
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.core.util.datatype.IdentityHashSet#disableLanguage(java.lang.Object)}
   * .
   */
  @Test
  public final void testRemoveObject() {
    assertTrue(!s.remove(o[0]));
    assertTrue(!s.remove(o[1]));
    s.add(o[0]);
    s.add(o[1]);
    assertTrue(s.remove(o[0]));
    assertTrue(s.remove(o[1]));
    s.add(o[0]);
    s.add(o[1]);
    s.clear();
    assertTrue(!s.remove(o[0]));
    assertTrue(!s.remove(o[1]));
  }

}
