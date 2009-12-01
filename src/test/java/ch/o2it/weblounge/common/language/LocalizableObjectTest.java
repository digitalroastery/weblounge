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

package ch.o2it.weblounge.common.language;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.language.Localizable.LanguageResolution;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 *  LocalizableObjectImplTest
 */
public class LocalizableObjectTest {

  /** The localizable content */
  protected LocalizableContent<String> content = null;

  /** English locale */
  protected Locale englishLocale = new Locale("en");

  /** French locale */
  protected Locale frenchLocale = new Locale("fr");

  /** Italian locale */
  protected Locale italianLocale = new Locale("it");

  /** English */
  protected Language english = new LanguageImpl(englishLocale);

  /** French */
  protected Language french = new LanguageImpl(frenchLocale);

  /** Italian */
  protected Language italian = new LanguageImpl(italianLocale);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    content = new LocalizableContent<String>();
    content.put(englishLocale.getDisplayLanguage(englishLocale), english);
    content.put(englishLocale.getDisplayLanguage(frenchLocale), french);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#LocalizableObject(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testLocalizableObjectLanguage() {
    LocalizableObject lc = new LocalizableContent<String>(french);
    assertEquals(french, lc.getDefaultLanguage());
    assertEquals(LanguageResolution.Default, lc.getLanguageResolution());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#enableLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testEnableLanguage() {
    content.enableLanguage(italian);
    assertTrue(content.supportsLanguage(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#remove(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testRemove() {
    content.remove(french);
    assertFalse(content.supportsLanguage(french));
    assertEquals(1, content.size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#switchedTo(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSwitchedTo() {
    final StringBuffer newLanguage = new StringBuffer();
    content.addLocalizationListener(new LocalizationListener() {
      public void switchedTo(Language language) {
        newLanguage.append(language.getIdentifier());
      }
    });
    content.switchTo(french);
    assertEquals(french.getIdentifier(), newLanguage.toString());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#switchTo(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSwitchToLanguage() {
    content.switchTo(french);
    assertEquals(french, content.getLanguage());
    content.switchTo(italian);
    assertEquals(content.getOriginalLanguage(), content.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#switchTo(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testSwitchToLanguageBoolean() {
    try {
      content.switchTo(italian, true);
      fail("Language switch to unresolvable language did not fail");
    } catch (IllegalStateException e) {
      // Expected, everything's fine
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(english, content.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#setLanguageResolution(ch.o2it.weblounge.common.language.Localizable.LanguageResolution)}.
   */
  @Test
  public void testSetLanguageResolution() {
    content.setDefaultLanguage(french);
    content.setLanguageResolution(LanguageResolution.Default);
    assertEquals(LanguageResolution.Default, content.getLanguageResolution());
    assertEquals(french, content.switchTo(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#getLanguageResolution()}.
   */
  @Test
  public void testGetLanguageResolution() {
    assertEquals(LanguageResolution.Original, content.getLanguageResolution());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#setDefaultLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetDefaultLanguage() {
    content.setDefaultLanguage(null);
    content.setDefaultLanguage(french);
    assertEquals(french, content.getDefaultLanguage());
    try {
      content.setLanguageResolution(LanguageResolution.Default);
      content.setDefaultLanguage(null);
      fail("Setting the default language to null should be prohited in this case");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#getDefaultLanguage()}.
   */
  @Test
  public void testGetDefaultLanguage() {
    assertTrue(content.getDefaultLanguage() == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#getOriginalLanguage()}.
   */
  @Test
  public void testGetOriginalLanguage() {
    assertEquals(english, content.getOriginalLanguage());
    LocalizableObject lc = new LocalizableContent<String>(french);
    assertTrue(lc.getOriginalLanguage() == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#setOriginalLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetOriginalLanguage() {
    content.setOriginalLanguage(french);
    assertEquals(french, content.getOriginalLanguage());
    assertEquals(english, content.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#supportsLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSupportsLanguage() {
    assertTrue(content.supportsLanguage(english));
    assertFalse(content.supportsLanguage(italian));
    LocalizableObject lc = new LocalizableContent<String>();
    lc.setOriginalLanguage(english);
    lc.setDefaultLanguage(french);
    assertFalse(lc.supportsLanguage(english));
    assertFalse(lc.supportsLanguage(french));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableObject#languages()}.
   */
  @Test
  public void testLanguages() {
    assertEquals(2, content.languages().size());
    assertTrue(content.languages().contains(english));
    assertFalse(content.languages().contains(italian));
  }

}
