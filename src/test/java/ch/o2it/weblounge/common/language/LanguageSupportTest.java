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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Test cases for the {@link LanguageSupport} implementation.
 */
public class LanguageSupportTest {

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
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getLanguage(java.util.Locale)}
   * .
   */
  @Test
  public void testGetLanguageLocale() {
    assertEquals(italian, LanguageSupport.getLanguage(italianLocale));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getLanguage(java.lang.String)}
   * .
   */
  @Test
  public void testGetLanguageString() {
    assertEquals(italian, LanguageSupport.getLanguage("it"));
    try {
      LanguageSupport.getLanguage("xyz");
      fail("Language xyz should not be resolved");
    } catch (UnsupportedLanguageException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#addDescriptions(javax.xml.xpath.XPath, org.w3c.dom.Node, java.lang.String, ch.o2it.weblounge.common.language.Language, ch.o2it.weblounge.common.impl.language.LocalizableContent, boolean)}
   * .
   */
  @Test
  public void testAddDescriptions() {
    LocalizableContent<String> names = null;
    Node xml = null;
    
    // Read the test fragment
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      URL testContext = this.getClass().getResource("/localizable.xml");
      Document doc = docBuilder.parse(testContext.openStream());
      xml = doc.getFirstChild();
    } catch (ParserConfigurationException e) {
      fail(e.getMessage());
    } catch (SAXException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }

    // Test happy path
    names = LanguageSupport.addDescriptions(xml, "name", french, null, false);
    assertEquals(2, names.size());
    assertTrue(names.supportsLanguage(english));
    assertTrue(names.supportsLanguage(french));
    assertEquals(french, names.getDefaultLanguage());

    // Test missing default language
    names = LanguageSupport.addDescriptions(xml, "name", italian, null, false);
    assertEquals(2, names.size());
    assertNull(names.getDefaultLanguage());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getLanguageVariant(java.lang.String, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetLanguageVariant() {
    assertEquals("test_en", LanguageSupport.getLanguageVariant("test", english));
    assertEquals("test_en.jsp", LanguageSupport.getLanguageVariant("test.jsp", english));
    assertEquals("/a/test_en.jsp", LanguageSupport.getLanguageVariant("/a/test.jsp", english));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getLanguageVariantsByPriority(java.lang.String, ch.o2it.weblounge.common.language.Language, ch.o2it.weblounge.common.site.Site)}
   * .
   */
  @Test
  public void testGetLanguageVariantsByPriority() {
    String result[] = LanguageSupport.getLanguageVariantsByPriority("test.jsp", french, italian);
    assertEquals(3, result.length);
    assertEquals("test_fr.jsp", result[0]);
    assertEquals("test_it.jsp", result[1]);
    assertEquals("test.jsp", result[2]);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getLanguageVariants(java.lang.String, ch.o2it.weblounge.common.language.Language[])}
   * .
   */
  @Test
  public void testGetLanguageVariantsStringLanguageArray() {
    Language[] languages = new Language[] { english, french };
    String result[] = LanguageSupport.getLanguageVariants("test.jsp", languages);
    assertEquals(3, result.length);
    assertEquals("test_en.jsp", result[0]);
    assertEquals("test_fr.jsp", result[1]);
    assertEquals("test.jsp", result[2]);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#getBaseVersion(java.lang.String, ch.o2it.weblounge.common.language.LanguageManager)}
   * .
   */
  @Test
  public void testGetBaseVersion() {
    assertEquals("test.jsp", LanguageSupport.getBaseVersion("test_en.jsp"));
    try {
      LanguageSupport.getBaseVersion("test_xyz.jsp");
      fail("LanguageSupport was fooled by non existent language xyz");
    } catch (UnsupportedLanguageException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.language.LanguageSupport#extractLanguage(java.lang.String)}
   * .
   */
  @Test
  public void testExtractLanguage() {
    assertEquals(italian, LanguageSupport.extractLanguage("test_it.jsp"));
    assertTrue(LanguageSupport.extractLanguage("test.jsp") == null);
    try {
      assertEquals(italian, LanguageSupport.extractLanguage("test_xyz.jsp"));
      fail("LanguageSupport was fooled by non existent language xyz");
    } catch (UnsupportedLanguageException e) {
      // Expected
    }
  }

}
