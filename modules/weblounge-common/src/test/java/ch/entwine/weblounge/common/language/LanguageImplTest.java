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

package ch.entwine.weblounge.common.language;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.language.LanguageImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Test cases for {@link LanguageImpl}.
 */
public class LanguageImplTest {

  /** English locale */
  private Locale englishLocale = new Locale("en");
  
  /** English */
  private Language english = new LanguageImpl(englishLocale);

  /** French */
  private Language french = new LanguageImpl(new Locale("fr"));

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(englishLocale.hashCode(), english.hashCode());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#getLocale()}.
   */
  @Test
  public void testGetLocale() {
    assertEquals(new Locale("en"), english.getLocale());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#getDescription()}.
   */
  @Test
  public void testGetDescription() {
    assertEquals(englishLocale.getDisplayLanguage(), english.getDescription());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#getDescription(ch.entwine.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetDescriptionLanguage() {
    assertEquals(englishLocale.getDisplayLanguage(french.getLocale()), english.getDescription(french));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals("en", english.getIdentifier());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(english.equals(english));
    assertTrue(english.equals(new LanguageImpl(new Locale("en"))));
    assertFalse(french.equals(english));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.language.LanguageImpl#toString()}.
   */
  @Test
  public void testToString() {
    assertEquals(englishLocale.getDisplayLanguage(), english.toString());
  }

}
