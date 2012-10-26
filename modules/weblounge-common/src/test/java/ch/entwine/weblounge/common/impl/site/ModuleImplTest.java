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

package ch.entwine.weblounge.common.impl.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.ModuleException;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Locale;

/**
 * Test cases for {@link ModuleImpl}
 */
public class ModuleImplTest {

  /** The module to test */
  protected Module module = null;

  /** The hosting site */
  protected Site site = null;

  /** Module identifier */
  protected String identifier = "testmodule";

  /** Enabled flag */
  protected boolean enabled = true;

  /** Searchable flag */
  protected boolean searchable = false;

  /** Name of the simple option */
  protected String simpleOptionName = "simple";

  /** Value of the simple option */
  protected String simpleOptionValue = "simple value";

  /** Name of the simple option */
  protected String complexOptionName = "complex";

  /** Value of the complex option */
  protected String[] complexOptionValue = new String[] { "complex", "value" };

  /** English title */
  protected String name = "Test module";

  /** The English language */
  protected static final Language ENGLISH = new LanguageImpl(new Locale("en"));

  /** The German language */
  protected static final Language GERMAN = new LanguageImpl(new Locale("de"));

  /** The Italian language */
  protected static final Language ITALIAN = new LanguageImpl(new Locale("it"));
  
  /** The test action */
  protected Action action = null;
  
  /** The action identifier */
  protected String actionIdentifier = "myaction";
  
  /** The pagelet renderer */
  protected PageletRenderer renderer = null;
  
  /** The pagelet renderer identifier */
  protected String rendererIdentifier = "renderer";
  
  /** The image style */
  protected ImageStyle imageStyle = null;
  
  protected String imageStyleIdentifier = "modulestyle";
  
  /**
   * Sets up the test bed.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();
    module = new ModuleImpl();
    module.setIdentifier(identifier);
    module.setName(name);
    module.setEnabled(enabled);
    module.setSearchable(searchable);
    module.setOption(simpleOptionName, simpleOptionValue);
    for (String o : complexOptionValue) {
      module.setOption(complexOptionName, o);
    }
    module.setSite(site);
    module.addAction(action);
    module.addRenderer(renderer);
    module.addImageStyle(imageStyle);
  }

  /**
   * Sets up preliminary items.
   * 
   * @throws Exception
   *           if setup fails
   */
  protected void setUpPreliminaries() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("test");
    EasyMock.expect(site.getHostname((Environment) EasyMock.anyObject())).andReturn(new SiteURLImpl(new URL("http://localhost"))).anyTimes();
    EasyMock.replay(site);
    
    action = new TestAction();
    action.setIdentifier(actionIdentifier);
    
    renderer = new PageletRendererImpl();
    renderer.setIdentifier(rendererIdentifier);

    imageStyle = new ImageStyleImpl(imageStyleIdentifier);
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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#setIdentifier(java.lang.String)}
   * .
   */
  @Test
  public void testSetIdentifier() {
    module.setIdentifier("1ab_2ABC3-.0");
    module.setIdentifier("1");
    module.setIdentifier("a");
    try {
      module.setIdentifier("Test id with spaces and,strange/characters");
      fail("Module accepted identifier with spaces in it");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
    try {
      module.setIdentifier(".abc");
      fail("Module accepted identifier starting with a special character");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
    try {
      module.setIdentifier("");
      fail("Module accepted an empty identifier");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, module.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getAction(java.lang.String)}
   * .
   */
  @Test
  public void testGetAction() {
    assertNotNull(module.getAction(actionIdentifier));
    assertTrue(module.getAction("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getActions()}.
   */
  @Test
  public void testGetActions() {
    assertTrue(module.getActions().length == 1);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getImageStyles()}.
   */
  @Test
  public void testGetImageStyles() {
    assertTrue(module.getImageStyles().length == 1);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getImageStyle(java.lang.String)}
   * .
   */
  @Test
  public void testGetImageStyle() {
    assertEquals(imageStyle, module.getImageStyle(imageStyleIdentifier));
    assertTrue(module.getImageStyle("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getRenderer(java.lang.String)}
   * .
   */
  @Test
  public void testGetRenderer() {
    assertEquals(renderer, module.getRenderer(rendererIdentifier));
    assertTrue(module.getRenderer("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getRenderers()}.
   */
  @Test
  public void testGetRenderers() {
    assertTrue(module.getRenderers().length == 1);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, module.getSite());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#isEnabled()}.
   */
  @Test
  public void testIsEnabled() {
    assertEquals(enabled, module.isEnabled());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#isSearchable()}.
   */
  @Test
  public void testIsSearchable() {
    assertEquals(searchable, module.isSearchable());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#search(java.lang.String)}
   * .
   */
  @Test
  public void testSearch() {
    assertTrue(module.search("test").length == 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#start()}.
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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#stop()}.
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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#removeOption(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveOption() {
    module.removeOption(complexOptionName);
    assertTrue(module.getOptions().size() == 1);
    assertTrue(module.getOptionValue(complexOptionName) == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getOptionValue(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(simpleOptionValue, module.getOptionValue(simpleOptionName));
    assertEquals(complexOptionValue[0], module.getOptionValue(complexOptionName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getOptionValue(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(simpleOptionValue, module.getOptionValue(simpleOptionName));
    assertEquals(complexOptionValue[0], module.getOptionValue(complexOptionName));
    assertEquals("default", module.getOptionValue("test", "default"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getOptionValues(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, module.getOptionValues(simpleOptionName).length);
    assertEquals(complexOptionValue.length, module.getOptionValues(complexOptionName).length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#hasOption(java.lang.String)}
   * .
   */
  @Test
  public void testHasOption() {
    assertTrue(module.hasOption(simpleOptionName));
    assertTrue(module.hasOption(complexOptionName));
    assertFalse(module.hasOption("test"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(2, module.getOptions().size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    assertTrue(module.equals(module));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ModuleImpl#toString()}.
   */
  @Test
  public void testToString() {
    assertEquals(identifier, module.toString());
  }

}
