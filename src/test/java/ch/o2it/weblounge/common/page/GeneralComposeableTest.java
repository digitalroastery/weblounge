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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Link;
import ch.o2it.weblounge.common.content.Script;
import ch.o2it.weblounge.common.impl.content.GeneralComposeable;
import ch.o2it.weblounge.common.impl.content.LinkImpl;
import ch.o2it.weblounge.common.impl.content.PageTemplateImpl;
import ch.o2it.weblounge.common.impl.content.ScriptImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test case for {@link GeneralComposeable}.
 */
public class GeneralComposeableTest {

  /** The composeable under test */
  protected GeneralComposeable composeable = null;

  /** Identifier */
  protected String identifier = "default";

  /** Composeable flag */
  protected boolean isComposeable = true;

  /** Renderer */
  protected static URL rendererURL = null;

  /** Number of milliseconds for recheck time */
  protected long recheckTime = 2 * Times.MS_PER_DAY;

  /** Number of milliseconds for valid time */
  protected long validTime = 3 * Times.MS_PER_WEEK;

  /** German language */
  protected Language german = LanguageSupport.getLanguage("de");

  /** English language */
  protected Language english = LanguageSupport.getLanguage("en");

  /** French language */
  protected Language french = LanguageSupport.getLanguage("fr");

  /** German name */
  protected String germanName = "Standard Vorlage";

  /** English name */
  protected String englishName = "Default template";
  
  /** Cascading stylesheet include */
  protected Link css = new LinkImpl("http://localhost/css.css");

  /** Javascript include */
  protected Script javascript = new ScriptImpl("http://localhost/javascript.js");

  @BeforeClass
  public static void setUpClass() throws Exception {
    rendererURL = new URL("file://template/default.jsp");
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    composeable = new PageTemplateImpl(identifier, rendererURL);
    setUpComposeable();
  }

  /**
   * Composeable setup has been moved here so that this test can easily be
   * reused by subclasses.
   * 
   * @throws Exception
   *           if setup fails
   */
  protected void setUpComposeable() throws Exception {
    composeable.setDefaultLanguage(german);
    composeable.setName(germanName, german);
    composeable.setName(englishName, english);
    composeable.setRecheckTime(recheckTime);
    composeable.setValidTime(validTime);
    composeable.setComposeable(isComposeable);
    composeable.addInclude(css);
    composeable.addInclude(javascript);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getIdentifier()}
   * .
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, composeable.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(germanName, composeable.getName());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getName(ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetNameLanguage() {
    assertEquals(germanName, composeable.getName(german));
    assertEquals(englishName, composeable.getName(english));
    assertEquals(germanName, composeable.getName(french));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getName(ch.o2it.weblounge.common.language.Language, boolean)}
   * .
   */
  @Test
  public void testGetNameLanguageBoolean() {
    assertEquals(germanName, composeable.getName(german, true));
    assertEquals(englishName, composeable.getName(english, true));
    assertTrue(composeable.getName(french, true) == null);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#isComposeable()}
   * .
   */
  @Test
  public void testIsComposeable() {
    assertEquals(isComposeable, composeable.isComposeable());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getRecheckTime()}
   * .
   */
  @Test
  public void testGetRecheckTime() {
    assertEquals(recheckTime, composeable.getRecheckTime());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getValidTime()}
   * .
   */
  @Test
  public void testGetValidTime() {
    assertEquals(validTime, composeable.getValidTime());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.GeneralComposeabl#getIncludes()
   * .
   */
  @Test
  public void testGetIncludes() {
    assertEquals(2, composeable.getIncludes().length);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    try {
      assertTrue(composeable.equals(new PageTemplateImpl(identifier, new URL("http://localhost"))));
      assertTrue(composeable.equals(new PageTemplateImpl(identifier, new URL("http://localhost:8080"))));
      assertFalse(composeable.equals(new PageTemplateImpl("xyz", new URL("http://localhost"))));
    } catch (MalformedURLException e) {
      fail();
    }
  }

}
