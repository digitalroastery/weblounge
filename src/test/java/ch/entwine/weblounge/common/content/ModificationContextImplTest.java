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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.ModificationContext;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test cases for {@link ModificationContext}.
 */
public class ModificationContextImplTest {

  /** Default test instance */
  protected ModificationContext ctx = null;

  /** Test context with an initial date only */
  protected ModificationContext ctxWithModifier = null;

  /** Creation date */
  protected Date modificationDate = new Date(1257501172000L);

  /** Creator */
  protected User modifier = new UserImpl("john", "testland", "John Doe");

  /**
   * Test setup.
   */
  @Before
  public void setUp() throws Exception {
    ctx = new ModificationContext(modificationDate, modifier);
    setupSpecialModificationContexts();
  }

  /**
   * Sets up special modification contexts.
   * 
   * @throws Exception
   */
  public void setupSpecialModificationContexts() throws Exception {
    ctxWithModifier = new ModificationContext(modifier);
  }


  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#getModificationDate()}
   * .
   */
  @Test
  public void testGetModificationDate() {
    assertEquals(modificationDate, ctx.getModificationDate());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#getModifier()}
   * .
   */
  @Test
  public void testGetModifier() {
    assertEquals(modifier, ctx.getModifier());
    assertEquals(modifier, ctxWithModifier.getModifier());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#isModified()}
   * .
   */
  @Test
  public void testIsModified() {
    Date date = new Date();
    try {
      // TODO: Redo this test using dayBefore and dayAfter
      assertTrue(date.before(modificationDate) || ctx.isModified());
      // Sleep, since date.before() needs a significant difference
      Thread.sleep(100);
      assertTrue(date.before(modificationDate) || ctxWithModifier.isModified());
    } catch (InterruptedException e) {
      // Should not happen, we are not doing anything dangerous
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#isModifiedAfter(java.util.Date)}
   * .
   */
  @Test
  public void testIsModifiedAfter() {
    Date date = new Date(new Date().getTime() + 3600L);
    assertFalse(ctx.isModifiedAfter(date));
    assertFalse(ctxWithModifier.isModifiedAfter(date));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#isModifiedBefore(java.util.Date)}
   * .
   */
  @Test
  public void testIsModifiedBefore() {
    Date date = new Date(modificationDate.getTime() + 3600L);
    assertTrue(ctx.isModifiedBefore(date));
    assertFalse(ctxWithModifier.isModifiedBefore(date));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ModificationContext#clone()}
   * .
   */
  @Test
  public void testClone() {
    ModificationContext clonedCtx;
    try {
      clonedCtx = (ModificationContext) ctx.clone();
      assertEquals(modificationDate, clonedCtx.getModificationDate());
      assertEquals(modifier, clonedCtx.getModifier());
    } catch (CloneNotSupportedException e) {
      fail("Creaing clone of modification context failed");
    }
  }

}
