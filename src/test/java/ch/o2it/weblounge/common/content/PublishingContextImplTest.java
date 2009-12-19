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

package ch.o2it.weblounge.common.content;

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.content.PublishingContext;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.user.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test cases for {@link PublishingContext}.
 */
public class PublishingContextImplTest {

  /** The test context */
  protected PublishingContext ctx = null;
  
  /** Publisher */
  protected User publisher = new UserImpl("john", "testland", "John Doe");

  /** Publication start date */
  protected Date startDate = new Date(1257497572000L);

  /** Publication end date */
  protected Date endDate = new Date(1289087999000L);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    ctx = new PublishingContext();
    ctx.setPublisher(publisher);
    ctx.setPublishFrom(startDate);
    ctx.setPublishTo(endDate);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#getPublisher()}
   * .
   */
  @Test
  public void testGetPublisher() {
    assertEquals(publisher, ctx.getPublisher());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#getPublishFrom()}
   * .
   */
  @Test
  public void testGetPublishFrom() {
    assertEquals(startDate, ctx.getPublishFrom());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#getPublishTo()}
   * .
   */
  @Test
  public void testGetPublishTo() {
    assertEquals(endDate, ctx.getPublishTo());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#isPublished()}
   * .
   */
  @Test
  public void testIsPublished() {
    Date yesterday = new Date(new Date().getTime() - 86400000L);
    Date tomorrow = new Date(new Date().getTime() + 86400000L);
    ctx.setPublishFrom(yesterday);
    ctx.setPublishTo(tomorrow);
    assertTrue(ctx.isPublished());
    ctx.setPublishTo(null);
    assertTrue(ctx.isPublished());
    ctx.setPublishFrom(null);
    assertTrue(ctx.isPublished());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#isPublished(java.util.Date)}
   * .
   */
  @Test
  public void testIsPublishedDate() {
    assertTrue(ctx.isPublished(new Date(startDate.getTime() + 1000)));
    assertTrue(ctx.isPublished(new Date(endDate.getTime() - 1000)));
    assertFalse(ctx.isPublished(new Date(startDate.getTime() - 86400000L)));
    assertFalse(ctx.isPublished(new Date(endDate.getTime() + 86400000L)));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#clone()}
   * .
   */
  @Test
  public void testClone() {
    PublishingContext c = null;
    try {
      c = (PublishingContext)ctx.clone();
      assertEquals(ctx.getPublisher(), c.getPublisher());
      assertEquals(ctx.getPublishFrom(), c.getPublishFrom());
      assertEquals(ctx.getPublishTo(), c.getPublishTo());
    } catch (CloneNotSupportedException e) {
      fail("Creating clone of publishing context failed");
    }
  }
  
  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#setPublished(User, Date, Date)}
   * .
   */
  @Test
  public void testSetPublished() {
    try {
      Date yesterday = new Date(new Date().getTime() - 86400000L);
      Date tomorrow = new Date(new Date().getTime() + 86400000L);
      ctx.setPublished(publisher, tomorrow, yesterday);
      fail("Setting a start date that is after the end date should not be possible");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

}
