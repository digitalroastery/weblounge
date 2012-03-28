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

package ch.entwine.weblounge.common.content;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.CreationContext;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Test cases for {@link CreationContext}.
 */
public class CreationContextImplTest {

  /** Test context */
  protected CreationContext ctx = null;

  /** Creation date */
  protected Date creationDate = new Date(1257501172000L);

  /** Creator */
  protected User creator = new UserImpl("john", "testland", "John Doe");

  /**
   * Test setup.
   * 
   * @throws Exception
   *           if setup fails
   */
  @Before
  public void setUp() throws Exception {
    ctx = new CreationContext();
    ctx.setCreationDate(creationDate);
    ctx.setCreator(creator);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#getCreationDate()}
   * .
   */
  @Test
  public void testGetCreationDate() {
    assertTrue(creationDate.equals(ctx.getCreationDate()));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#getCreator()}
   * .
   */
  @Test
  public void testGetCreator() {
    assertTrue(creator.equals(ctx.getCreator()));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#setCreationDate(java.util.Date)}
   * .
   */
  @Test
  public void testSetCreationDate() {
    Calendar calendar = Calendar.getInstance();
    ctx.setCreationDate(calendar.getTime());
    calendar.set(Calendar.MILLISECOND, 0);
    assertTrue(calendar.getTime().equals(ctx.getCreationDate()));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#setCreator(ch.entwine.weblounge.common.security.User)}
   * .
   */
  @Test
  public void testSetCreator() {
    User u = new UserImpl("james", "testland");
    ctx.setCreator(u);
    assertTrue(u.equals(ctx.getCreator()));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#isCreatedAfter(java.util.Date)}
   * .
   */
  @Test
  public void testIsCreatedAfter() {
    Date d = new Date(0);
    assertTrue(ctx.isCreatedAfter(d));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.CreationContext#clone()}.
   */
  @Test
  public void testClone() {
    CreationContext c = null;
    try {
      c = (CreationContext) ctx.clone();
      assertTrue(creator.equals(c.getCreator()));
      assertTrue(creationDate.equals(c.getCreationDate()));
    } catch (CloneNotSupportedException e) {
      fail("Creating clone of creation context failed");
    }
  }

}
