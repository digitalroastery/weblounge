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

package ch.entwine.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.content.page.LinkImpl;
import ch.entwine.weblounge.common.impl.content.page.PageTemplateImpl;
import ch.entwine.weblounge.common.impl.content.page.ScriptImpl;
import ch.entwine.weblounge.common.request.RequestFlavor;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test cases for {@link PageTemplateImpl}.
 */
public class PageTemplateImplTest extends GeneralComposeableTest {
  
  /** The page template under test */
  protected PageTemplate template = null;
  
  /** The stage composer */
  protected String stage = "boxes";
  
  /** Default page layout */
  protected String layout = "unrestricted";
  
  /** Cascading style sheet include */
  protected Link css = new LinkImpl("http://localhost/css.css");

  /** Java script include */
  protected Script script = new ScriptImpl("http://localhost/javascript.js");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    template = new PageTemplateImpl(identifier, rendererURL);    
    composeable = (PageTemplateImpl)template;
    setUpComposeable();
    template.setStage(stage);
    template.setDefaultLayout(layout);
    template.addHTMLHeader(css);
    template.addHTMLHeader(script);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageTemplateImpl#getStage()}.
   */
  @Test
  public void testGetStage() {
    assertEquals(stage, template.getStage());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#removeFlavor(java.lang.String)}.
   */
  @Test
  public void testRemoveFlavor() {
    template.removeFlavor(RequestFlavor.XML);
    assertEquals(1, template.getFlavors().length);
    template.removeFlavor(RequestFlavor.HTML);
    assertEquals(0, template.getFlavors().length);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#getFlavors()}.
   */
  @Test
  public void testGetFlavors() {
    assertEquals(1, template.getFlavors().length);
    assertEquals(RequestFlavor.HTML, template.getFlavors()[0]);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#supportsFlavor(java.lang.String)}.
   */
  @Test
  public void testSupportsFlavor() {
    assertTrue(template.supportsFlavor(RequestFlavor.HTML));
    assertFalse(template.supportsFlavor(RequestFlavor.XML));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#getRenderer()}.
   */
  @Test
  public void testGetRenderer() {
    assertEquals(rendererURL, template.getRenderer());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageTemplateImpl#getDefaultLayout()}.
   */
  @Test
  public void testGetDefaultLayout() {
    assertEquals(layout, template.getDefaultLayout());
  }
  
  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.page.GeneralComposeabl#getHTMLHeaders()
   * .
   */
  @Test
  public void testGetIncludes() {
    assertEquals(2, template.getHTMLHeaders().length);
    List<HTMLHeadElement> includes = Arrays.asList(template.getHTMLHeaders());
    assertTrue(includes.contains(css));
    assertTrue(includes.contains(script));
  }

}
