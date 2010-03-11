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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.page.LinkImpl;
import ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl;
import ch.o2it.weblounge.common.page.Link;
import ch.o2it.weblounge.common.request.RequestFlavor;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link ActionConfigurationImpl}.
 */
public class ActionConfigurationImplTest {
  
  /** The action configuration that is being tested */
  protected ActionConfigurationImpl config = null;
  
  /** Action identifier */
  protected String identifier = "myaction";
  
  /** The action implementation */
  protected Class<? extends Action> actionClass = TestAction.class;
  
  /** Stylesheet */
  protected Link link = new LinkImpl("http://localhost/css/stylesheet.css", "text/css");

  /** Stylesheet */
  protected Link otherLink = new LinkImpl("http://localhost/css/stylesheet2.css", "text/css");

  /** Javascript */
  protected Link script = new LinkImpl("http://localhost/scripts/script.js", "javascript");
  
  /** The mountpoint */
  protected String mountpoint = "/test/";

  /** Recheck time */
  protected long recheckTime = 0;

  /** Valid time */
  protected long validTime = 60000;
  
  /** URI of the testpage */
  protected String pageURI = "/testpage/";

  /** Identifier of the template */
  protected String template = "testtemplate";
  
  /** Name of the option key */
  protected String optionKey = "key";
  
  /** Option value */
  protected String optionValue = "value";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    config = new ActionConfigurationImpl();
    config.setIdentifier(identifier);
    config.setActionClass(actionClass);
    config.addInclude(link);
    config.addFlavor(RequestFlavor.HTML);
    config.addInclude(script);
    config.setMountpoint(mountpoint);
    config.setRecheckTime(recheckTime);
    config.setValidTime(validTime);
    config.setPageURI(pageURI);
    config.setTemplate(template);
    config.setOption(optionKey, optionValue);
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getActionClass()}.
   */
  @Test
  public void testGetActionClass() {
    assertEquals(actionClass, config.getActionClass());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, config.getIdentifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getIncludes()}.
   */
  @Test
  public void testGetIncludes() {
    assertEquals(2, config.getIncludes().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#addInclude(ch.o2it.weblounge.common.site.Include)}.
   */
  @Test
  public void testAddInclude() {
    config.addInclude(link);
    assertEquals(2, config.getIncludes().size());
    config.addInclude(otherLink);
    assertEquals(3, config.getIncludes().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getMountpoint()}.
   */
  @Test
  public void testGetMountpoint() {
    assertEquals(mountpoint, config.getMountpoint());
    config.setMountpoint("/test");
    assertEquals("/test/", config.getMountpoint());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getRecheckTime()}.
   */
  @Test
  public void testGetRecheckTime() {
    assertEquals(recheckTime, config.getRecheckTime());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getPageURI()}.
   */
  @Test
  public void testGetPageURI() {
    assertEquals(pageURI, config.getPageURI());
    config.setPageURI("/testpage");
    assertEquals("/testpage/", config.getPageURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(template, config.getTemplate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getValidTime()}.
   */
  @Test
  public void testGetValidTime() {
    assertEquals(validTime, config.getValidTime());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getFlavors()}.
   */
  @Test
  public void testGetFlavors() {
    assertEquals(1, config.getFlavors().size());
    assertEquals(RequestFlavor.HTML, config.getFlavors().iterator().next());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#addFlavor(java.lang.String)}.
   */
  @Test
  public void testAddFlavor() {
    config.addFlavor(RequestFlavor.HTML);
    assertEquals(1, config.getFlavors().size());
    config.addFlavor(RequestFlavor.XML);
    assertEquals(2, config.getFlavors().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getOptionValue(java.lang.String)}.
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, config.getOptionValue(optionKey));
    assertTrue(config.getOptionValue("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getOptionValue(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, config.getOptionValue(optionKey, "abc"));
    assertEquals(optionValue, config.getOptionValue("abc", optionValue));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getOptionValues(java.lang.String)}.
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, config.getOptionValues(optionKey).length);
    assertEquals(0, config.getOptionValues("test").length);
    config.setOption(optionKey, optionValue);
    assertEquals(1, config.getOptionValues(optionKey).length);
    config.setOption(optionKey, "abc");
    assertEquals(2, config.getOptionValues(optionKey).length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(1, config.getOptions().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#hasOption(java.lang.String)}.
   */
  @Test
  public void testHasOption() {
    assertTrue(config.hasOption(optionKey));
    assertFalse(config.hasOption("test"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionConfigurationImpl#removeOption(java.lang.String)}.
   */
  @Test
  public void testRemoveOption() {
    config.removeOption(optionKey);
    assertFalse(config.hasOption(optionKey));
  }

}
