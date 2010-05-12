/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl;
import ch.o2it.weblounge.common.language.Language;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Locale;

/**
 * Test case for {@link I18nDictionaryImpl}.
 */
public class I18nDictionaryImplTest {
  
  /** The i18n dictionary */
  protected I18nDictionaryImpl i18n = null;

  /** Test key */
  protected String key = "tsest.language.displayname";

  /** English locale */
  protected Locale englishLocale = new Locale("en");

  /** German locale */
  protected Locale germanLocale = new Locale("de");

  /** French locale */
  protected Locale frenchLocale = new Locale("fr");

  /** Italian locale */
  protected Locale italianLocale = new Locale("it");

  /** English */
  protected Language english = new LanguageImpl(englishLocale);

  /** German */
  protected Language german = new LanguageImpl(germanLocale);

  /** French */
  protected Language french = new LanguageImpl(frenchLocale);

  /** Italian */
  protected Language italian = new LanguageImpl(italianLocale);

  /** The English value */
  protected String englishValue = germanLocale.getDisplayLanguage(englishLocale);

  /** The German value */
  protected String germanValue = germanLocale.getDisplayLanguage(germanLocale);
    
  /** The French value */
  protected String frenchValue = germanLocale.getDisplayLanguage(frenchLocale);
  
  /** Filename of the default dictionary */
  protected String defaultDictionaryFile = "/i18n.xml";

  /** Filename of the German dictionary */
  protected String frenchDictionaryFile = "/i18n_fr.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    i18n = new I18nDictionaryImpl();
    i18n.add(key, englishValue);
    i18n.add(key, germanValue, german);
    i18n.add(key, frenchValue, french);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#add(java.lang.String, java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testAdd() {
    String testKey = "test.key";
    String testValue = "Hello World!";
    assertEquals(testKey, i18n.get(testKey));
    i18n.add(testKey, testValue);
    assertNotNull(i18n.get(testKey));
    assertEquals(testValue, i18n.get(testKey));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#add(java.lang.String, java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testAddLanguage() {
    String testKey = "test.key";
    String testValue = "Hello World!";
    assertEquals(testKey, i18n.get(testKey, german));
    i18n.add(testKey, testValue, german);
    assertNotNull(i18n.get(testKey, german));
    assertEquals(testValue, i18n.get(testKey, german));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#get(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetString() {
    assertEquals(germanValue, i18n.get(key, german));
    assertEquals(frenchValue, i18n.get(key, french));
    assertEquals(key, i18n.get(key, italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#get(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetStringLanguage() {
    assertEquals(germanValue, i18n.get(key, german));
    assertEquals(frenchValue, i18n.get(key, french));
    assertEquals(key, i18n.get(key, italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#get(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetStringLanguageBoolean() {
    assertEquals(germanValue, i18n.get(key, german));
    assertEquals(frenchValue, i18n.get(key, french));
    assertEquals(key, i18n.get(key, italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#getAsHTML(java.lang.String)}.
   */
  @Test
  public void testGetAsHTMLString() {
    String testKey = "test.key";
    String testValue = "Grüezi!";
    String testValueHtml = "Gr&uuml;ezi!";
    i18n.add(testKey, testValue, german);
    assertEquals(testValueHtml, i18n.getAsHTML(testKey, german));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#getAsHTML(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetAsHTMLStringLanguage() {
    String testKey = "test.key";
    String testValue = "Grüezi!";
    String testValueHtml = "Gr&uuml;ezi!";
    i18n.add(testKey, testValue);
    assertEquals(testValueHtml, i18n.getAsHTML(testKey));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#remove(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testRemoveStringLanguage() {
    i18n.remove(key, german);
    assertEquals(key, i18n.get(key, german));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#remove(java.lang.String)}.
   */
  @Test
  public void testRemoveString() {
    i18n.remove(key);
    assertEquals(key, i18n.get(key, german));
    assertEquals(key, i18n.get(key, french));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#addDictionary(java.io.File)}.
   */
  @Test
  public void testAddDictionaryUrl() {
    String helloKey = "welcome.hello";
    String worldKey = "welcome.world";
    URL defaultDictionaryUrl = I18nDictionaryImplTest.class.getResource(defaultDictionaryFile);
    i18n.addDictionary(defaultDictionaryUrl);
    URL frenchDictionaryUrl = I18nDictionaryImplTest.class.getResource(frenchDictionaryFile);
    i18n.addDictionary(frenchDictionaryUrl);
    assertEquals("hello", i18n.get(helloKey));
    assertEquals("world", i18n.get(worldKey));
    assertEquals("bonjour", i18n.get(helloKey, french));
    assertEquals("tout le monde", i18n.get(worldKey, french));
    assertEquals(helloKey, i18n.get(helloKey, italian));
    assertEquals(worldKey, i18n.get(worldKey, italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.I18nDictionaryImpl#addDictionary(java.io.File, boolean)}.
   */
  @Test
  public void testAddDictionaryLanguage() {
    String helloKey = "welcome.hello";
    String worldKey = "welcome.world";
    URL frenchDictionaryUrl = I18nDictionaryImplTest.class.getResource(frenchDictionaryFile);
    i18n.addDictionary(frenchDictionaryUrl, italian);
    assertEquals(helloKey, i18n.get(helloKey));
    assertEquals(worldKey, i18n.get(worldKey));
    assertEquals(helloKey, i18n.get(helloKey, french));
    assertEquals(worldKey, i18n.get(worldKey, french));
    assertEquals("bonjour", i18n.get(helloKey, italian));
    assertEquals("tout le monde", i18n.get(worldKey, italian));
  }

}
