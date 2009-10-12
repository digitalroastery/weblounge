/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.util.datatype;

import ch.o2it.weblounge.common.impl.util.datatype.IdentityHashSet;

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
	Object o[] = new Object[5];
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		s = new IdentityHashSet<Object>();
		o = new Object[5];
		for (int i = 0; i < o.length; i++)
			o[i] = new String("Test1");
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
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#size()}.
	 */
	@Test
	public final void testSize() {
		assert(s.size() == 0);
		s.add(o[0]);
		assert(s.size() == 1);
		s.add(o[1]);
		assert(s.size() == 2);
		s.add(o[2]);
		assert(s.size() == 3);
		s.add(o[2]);
		assert(s.size() == 3);
		s.add(o[3]);
		assert(s.size() == 4);
		s.remove(o[2]);
		assert(s.size() == 3);
		s.remove(o[2]);
		assert(s.size() == 3);
		s.remove("Test1");
		assert(s.size() == 3);
		s.clear();
		assert(s.size() == 0);
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#isEmpty()}.
	 */
	@Test
	public final void testIsEmpty() {
		assert(s.isEmpty());
		s.add(o[0]);
		assert(!s.isEmpty());
		s.remove("Test1");
		assert(!s.isEmpty());
		s.remove(o[0]);
		assert(s.isEmpty());
		s.add(o[0]);
		s.add(o[1]);
		assert(!s.isEmpty());
		s.clear();
		assert(s.isEmpty());
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#clear()}.
	 */
	@Test
	public final void testClear() {
		assert(s.isEmpty());
		s.clear();
		assert(s.isEmpty());
		s.add(o[0]);
		assert(!s.isEmpty());
		s.clear();
		assert(s.isEmpty());
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#IdentityHashSet()}.
	 */
	@Test
	public final void testIdentityHashSet() {
		Set<String> l =  new IdentityHashSet<String>();
		assert(l != null);
		assert(l instanceof IdentityHashSet);
		assert(l.size() == 0);
		assert(l.isEmpty());
		assert(!s.remove("Test"));
		assert(!s.contains("Test"));
		assert(s.add("Test"));
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#add(java.lang.Object)}.
	 */
	@Test
	public final void testAddE() {
		assert(s.add(o[0]));
		assert(s.add(o[1]));
		assert(!s.add(o[0]));
		assert(!s.add(o[1]));
		assert(s.add(o[2]));
		s.clear();
		assert(s.add(o[0]));
		assert(s.add(o[1]));
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#contains(java.lang.Object)}.
	 */
	@Test
	public final void testContainsObject() {
		assert(!s.contains(o[0]));
		s.add(o[0]);
		assert(s.contains(o[0]));
		assert(!s.contains(o[1]));
		s.add(o[1]);
		assert(s.contains(o[1]));
		s.remove(o[0]);
		assert(!s.contains(o[0]));
		s.remove(o[1]);
		assert(!s.contains(o[1]));
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#iterator()}.
	 */
	@Test
	public final void testIterator() {
		Iterator<Object> i = s.iterator();
		assert(i != null);
		assert(!i.hasNext());
		for (int j = 0; j < o.length; j++)
			s.add(o[j]);
		i = s.iterator();
		assert(i != null);
		for (int j = 0; j < o.length; j++) {
			assert(i.hasNext());
			assert(i.next() != null);
		}
	}

	/**
	 * Test method for {@link ch.o2it.weblounge.core.util.datatype.IdentityHashSet#remove(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveObject() {
		assert(!s.remove(o[0]));
		assert(!s.remove(o[1]));
		s.add(o[0]);
		s.add(o[1]);
		assert(s.remove(o[0]));
		assert(s.remove(o[1]));
		s.add(o[0]);
		s.add(o[1]);
		s.clear();
		assert(!s.remove(o[0]));
		assert(!s.remove(o[1]));		
	}

}
