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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.content.ModificationContextImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.user.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test cases for {@link ModificationContextImpl}.
 */
public class ModificationContextImplTest {

  /** Default test instance */
  protected ModificationContextImpl ctx = null;

  /** Test context with an initial date only */
  protected ModificationContextImpl ctxWithModifier = null;

  /** Creation date */
  protected Date modificationDate = new Date(1257497572000L);

  /** Creator */
  protected User modifier = new UserImpl("john", "testland", "John Doe");

  /**
   * Test setup.
   */
  @Before
  public void setUp() throws Exception {
    ctx = new ModificationContextImpl(modificationDate, modifier);
    setupSpecialModificationContexts();
  }

  /**
   * Sets up special modification contexts.
   * 
   * @throws Exception
   */
  public void setupSpecialModificationContexts() throws Exception {
    ctxWithModifier = new ModificationContextImpl(modifier);
  }


  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#getModificationDate()}
   * .
   */
  @Test
  public void testGetModificationDate() {
    assertEquals(modificationDate, ctx.getModificationDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#getModifier()}
   * .
   */
  @Test
  public void testGetModifier() {
    assertEquals(modifier, ctx.getModifier());
    assertEquals(modifier, ctxWithModifier.getModifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#isModified()}
   * .
   */
  @Test
  public void testIsModified() {
    Date date = new Date();
    try {
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
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#isModifiedAfter(java.util.Date)}
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
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#isModifiedBefore(java.util.Date)}
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
   * {@link ch.o2it.weblounge.common.impl.content.ModificationContextImpl#clone()}
   * .
   */
  @Test
  public void testClone() {
    ModificationContext clonedCtx = (ModificationContext) ctx.clone();
    assertEquals(modificationDate, clonedCtx.getModificationDate());
    assertEquals(modifier, clonedCtx.getModifier());
  }

}
