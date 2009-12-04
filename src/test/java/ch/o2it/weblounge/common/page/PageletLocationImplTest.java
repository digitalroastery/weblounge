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

package ch.o2it.weblounge.common.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.page.PageletLocationImpl;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link PageletLocationImpl}.
 */
public class PageletLocationImplTest {
  
  /** Page identifier */
  protected PageURI uri = null;
  
  /** Associated site */
  protected Site site = null;
  
  /** Path to the page */
  protected String path = "/test";
  
  /** Enclosing composer */
  protected String composer = "main";
  
  /** Pagelet position */
  protected int position = 1;
  
  /** The pagelet location instance under test */
  protected PageletLocationImpl location = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    uri = new PageURIImpl(site, path);
    location = new PageletLocationImpl(uri, composer, position);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, location.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#getURI()}.
   */
  @Test
  public void testGetURI() {
    assertEquals(uri, location.getURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#getComposer()}.
   */
  @Test
  public void testGetComposer() {
    assertEquals(composer, location.getComposer());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#getPosition()}.
   */
  @Test
  public void testGetPosition() {
    assertEquals(position, location.getPosition());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    uri = new PageURIImpl(site, path);
    location = new PageletLocationImpl(uri, composer, position);
    PageletLocation sameLocation = new PageletLocationImpl(uri, composer, position);
    assertEquals(location, sameLocation);
    assertFalse(location.equals(new PageletLocationImpl(new PageURIImpl(site, "/test2"), composer, position)));
    assertFalse(location.equals(new PageletLocationImpl(uri, "main2", position)));
    assertFalse(location.equals(new PageletLocationImpl(uri, composer, position + 1)));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletLocationImpl#compareTo(ch.o2it.weblounge.common.page.PageletLocation)}.
   */
  @Test
  public void testCompareTo() {
    PageletLocation previous = new PageletLocationImpl(uri, composer, position - 1);
    PageletLocation next = new PageletLocationImpl(uri, composer, position + 1);
    assertEquals(1, location.compareTo(previous));
    assertEquals(0, location.compareTo(location));
    assertEquals(-1, location.compareTo(next));
  }

}
