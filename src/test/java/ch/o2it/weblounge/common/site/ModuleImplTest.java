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

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.site.ModuleImpl;
import ch.o2it.weblounge.common.impl.site.SiteImpl;
import ch.o2it.weblounge.common.language.Language;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Test cases for {@link ModuleImpl}
 */
public class ModuleImplTest {
  
  /** The module to test */
  protected ModuleImpl module = null;
  
  /** The hosting site */
  protected Site site = null;
  
  /** Module identifier */
  protected String identifier = "test-module";
 
  /** Enabled flag */
  protected boolean enabled = true;

  /** Searchable flag */
  protected boolean searchable = true;
  
  /** Name of the simple option */
  protected String simpleOptionName = "simple";

  /** Value of the simple option */
  protected String simpleOptionValue = "simple value";

  /** Name of the simple option */
  protected String complexOptionName = "complex";

  /** Value of the complex option */
  protected String[] complexOptionValue = new String[] { "complex",  "value" };

  /** English title */
  protected String englishTitle = "Test module";

  /** German title */
  protected String germanTitle = "Test Modul";

  /** The English language */
  protected final Language English = new LanguageImpl(new Locale("en"));

  /** The German language */
  protected final Language German = new LanguageImpl(new Locale("de"));

  /** The Italian language */
  protected final Language Italian = new LanguageImpl(new Locale("it"));

  /**
   * Sets up the test bed.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    module = new ModuleImpl();
    module.setIdentifier(identifier);
    module.setTitle(englishTitle, English);
    module.setTitle(germanTitle, German);
    module.setEnabled(enabled);
    module.setSearchable(searchable);
    module.setOption(simpleOptionName, simpleOptionValue);
    for (String o : complexOptionValue) {
      module.setOption(complexOptionName, o);
    }
    module.init(site);
  }
  
  /**
   * Clears the test bed.
   * 
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    module.destroy();
  }
  
  /**
   * Sets up preliminary data structures.
   * 
   * @throws Exception
   */
  protected void setupPrerequisites() throws Exception {
    site = new SiteImpl();
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#setIdentifier(java.lang.String)}.
   */
  @Test
  public void testSetIdentifier() {
    module.setIdentifier("1ab_2ABC3-.0");
    try {
      module.setIdentifier("Test id with spaces and,strange/characters");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, module.getIdentifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getAction(java.lang.String)}.
   */
  @Test
  public void testGetAction() {
    // TODO: Improve test by adding actions first
    assertTrue(module.getAction("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getActions()}.
   */
  @Test
  public void testGetActions() {
    assertTrue(module.getActions().length == 0);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getImageStyles()}.
   */
  @Test
  public void testGetImageStyles() {
    assertTrue(module.getImageStyles().length == 0);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getImageStyle(java.lang.String)}.
   */
  @Test
  public void testGetImageStyle() {
    // TODO: Improve test by adding image styles first
    assertTrue(module.getImageStyle("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getRenderer(java.lang.String)}.
   */
  @Test
  public void testGetRenderer() {
    // TODO: Improve test by adding renderer first
    assertTrue(module.getRenderer("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getRenderers()}.
   */
  @Test
  public void testGetRenderers() {
    assertTrue(module.getRenderers().length == 0);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, module.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getTitle(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetTitle() {
    assertEquals(englishTitle, module.getTitle(English));
    assertEquals(germanTitle, module.getTitle(German));
    assertEquals(englishTitle, module.getTitle(Italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#isEnabled()}.
   */
  @Test
  public void testIsEnabled() {
    assertEquals(enabled, module.isEnabled());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#isSearchable()}.
   */
  @Test
  public void testIsSearchable() {
    assertEquals(searchable, module.isSearchable());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#search(java.lang.String)}.
   */
  @Test
  public void testSearch() {
    assertTrue(module.search("test").length == 0);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#start()}.
   */
  @Test
  public void testStart() {
    try {
      module.start();
      module.stop();
    } catch (ModuleException e) {
      fail(e.getMessage());
    }
    
    // Test disabled module
    try {
      module.setEnabled(false);
      module.start();
    } catch (ModuleException e) {
      fail(e.getMessage());
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test running module
    try {
      module.setEnabled(true);
      module.start();
      module.start();
    } catch (ModuleException e) {
      fail(e.getMessage());
    } catch (IllegalStateException e) {
      // This is expected
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#stop()}.
   */
  @Test
  public void testStop() {
    try {
      module.start();
      module.stop();
    } catch (ModuleException e) {
      fail(e.getMessage());
    }
    
    // Test stopped module
    try {
      module.stop();
    } catch (ModuleException e) {
      fail(e.getMessage());
    } catch (IllegalStateException e) {
      // This is expected
    } 
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#removeOption(java.lang.String)}.
   */
  @Test
  public void testRemoveOption() {
    module.removeOption(complexOptionName);
    assertTrue(module.getOptions().size() == 1);
    assertTrue(module.getOptionValue(complexOptionName) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getOptionValue(java.lang.String)}.
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(simpleOptionValue, module.getOptionValue(simpleOptionName));
    assertEquals(complexOptionValue[0], module.getOptionValue(complexOptionName));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getOptionValue(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(simpleOptionValue, module.getOptionValue(simpleOptionName));
    assertEquals(complexOptionValue[0], module.getOptionValue(complexOptionName));
    assertEquals("default", module.getOptionValue("test", "default"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getOptionValues(java.lang.String)}.
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, module.getOptionValues(simpleOptionName).length);
    assertEquals(complexOptionValue.length, module.getOptionValues(complexOptionName).length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#hasOption(java.lang.String)}.
   */
  @Test
  public void testHasOption() {
    assertTrue(module.hasOption(simpleOptionName));
    assertTrue(module.hasOption(complexOptionName));
    assertFalse(module.hasOption("test"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(2, module.getOptions().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(module.equals(module));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ModuleImpl#toString()}.
   */
  @Test
  public void testToString() {
    assertEquals(identifier, module.toString());
  }

}
