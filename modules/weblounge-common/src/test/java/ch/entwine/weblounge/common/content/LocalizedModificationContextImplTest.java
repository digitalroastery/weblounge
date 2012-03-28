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

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.content.LocalizedModificationContext;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

/**
 * Test case for the implementation of {@link LocalizedModificationContext}.
 */
public class LocalizedModificationContextImplTest {

  /** Default test instance */
  protected LocalizedModificationContext ctx = null;

  /** Test context with an initial date only */
  protected LocalizedModificationContext ctxWithModifier = null;

  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** Content creation date */
  protected Date creationDate = new Date(1000000000000L);

  /** German modification date */
  protected Date germanModifcationDate = new Date(1231358741000L);

  /** French modification date */
  protected Date frenchModifcationDate = new Date(1234994800000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** One day after the date identified by futureDate */
  protected Date dayAfterFutureDate = new Date(futureDate.getTime() + Times.MS_PER_DAY);
  
  /** One day before the date identified by futureDate */
  protected Date dayBeforeFutureDate = new Date(2000000000000L - Times.MS_PER_DAY);

  /** German modifier */
  protected User hans = new UserImpl("hans", "testland", "Hans Muster");

  /** French modifier */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    ctx = new LocalizedModificationContext();
    ctx.setModified(hans, germanModifcationDate, german);
    ctx.setModified(amelie, frenchModifcationDate, french);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getModificationDate()}.
   */
  @Test
  public void testGetModificationDate() {
    // Test behavior if no language is set
    assertEquals(frenchModifcationDate, ctx.getModificationDate());

    // Now switch to specific language version
    ctx.enableLanguage(german);
    ctx.switchTo(german);
    assertEquals(germanModifcationDate, ctx.getModificationDate());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getModificationDate(ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetModificationDateLanguage() {
    assertEquals(germanModifcationDate, ctx.getModificationDate(german));
    assertEquals(frenchModifcationDate, ctx.getModificationDate(french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getModifier()}.
   */
  @Test
  public void testGetModifier() {
    // Test behavior if no language is set
    assertEquals(amelie, ctx.getModifier());

    // Now switch to specific language version
    ctx.enableLanguage(german);
    ctx.switchTo(german);
    assertEquals(hans, ctx.getModifier());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getModifier(ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetModifierLanguage() {
    assertEquals(hans, ctx.getModifier(german));
    assertEquals(amelie, ctx.getModifier(french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#isModifiedAfter(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAfter() {
    assertTrue(ctx.isModifiedAfter(creationDate));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#isModifiedAfter(java.util.Date, ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testIsModifiedAfterLanguage() {
    assertTrue(ctx.isModifiedAfter(creationDate, german));
    assertTrue(ctx.isModifiedAfter(creationDate, french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#isModifiedBefore(java.util.Date)}.
   */
  @Test
  public void testIsModifiedBefore() {
    assertFalse(ctx.isModifiedBefore(creationDate));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#isModifiedBefore(java.util.Date, ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testIsModifiedBeforeLanguage() {
    assertTrue(ctx.isModifiedBefore(futureDate, german));
    assertTrue(ctx.isModifiedBefore(futureDate, french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#setModified(ch.entwine.weblounge.common.security.User, java.util.Date, ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetModified() {
    ctx.setModified(hans, futureDate, german);
    assertTrue(ctx.isModifiedBefore(dayAfterFutureDate));
    assertTrue(ctx.isModifiedBefore(dayAfterFutureDate, german));
    assertTrue(ctx.isModifiedAfter(dayBeforeFutureDate));
    assertTrue(ctx.isModifiedAfter(dayBeforeFutureDate, german));
    assertTrue(ctx.isModifiedBefore(dayAfterFutureDate, french));
    assertFalse(ctx.isModifiedAfter(dayBeforeFutureDate, french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getLastModificationDate()}.
   */
  @Test
  public void testGetLastModificationDate() {
    assertEquals(frenchModifcationDate, ctx.getLastModificationDate());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#getLastModifier()}.
   */
  @Test
  public void testGetLastModifier() {
    assertEquals(amelie, ctx.getLastModifier());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#clone()}.
   */
  @Test
  public void testClone() {
    LocalizedModificationContext c = null;
    try {
      c = (LocalizedModificationContext)ctx.clone();
      assertEquals(hans, c.getModifier(german));
      assertEquals(germanModifcationDate, c.getModificationDate(german));
      assertEquals(amelie, c.getModifier(french));
      assertEquals(frenchModifcationDate, c.getModificationDate(french));
      assertEquals(ctx.getLastModifier(), c.getLastModifier());
      assertEquals(ctx.getLastModificationDate(), c.getLastModificationDate());
    } catch (CloneNotSupportedException e) {
      fail("Creating clone of modification context failed");
    }
  }

}
