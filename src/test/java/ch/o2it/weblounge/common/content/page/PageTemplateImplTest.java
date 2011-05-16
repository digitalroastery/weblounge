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

package ch.o2it.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.content.page.PageTemplateImpl;
import ch.o2it.weblounge.common.request.RequestFlavor;

import org.junit.Before;
import org.junit.Test;

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
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageTemplateImpl#getStage()}.
   */
  @Test
  public void testGetStage() {
    assertEquals(stage, template.getStage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.AbstractRenderer#removeFlavor(java.lang.String)}.
   */
  @Test
  public void testRemoveFlavor() {
    template.removeFlavor(RequestFlavor.XML);
    assertEquals(1, template.getFlavors().length);
    template.removeFlavor(RequestFlavor.HTML);
    assertEquals(0, template.getFlavors().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.AbstractRenderer#getFlavors()}.
   */
  @Test
  public void testGetFlavors() {
    assertEquals(1, template.getFlavors().length);
    assertEquals(RequestFlavor.HTML, template.getFlavors()[0]);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.AbstractRenderer#supportsFlavor(java.lang.String)}.
   */
  @Test
  public void testSupportsFlavor() {
    assertTrue(template.supportsFlavor(RequestFlavor.HTML));
    assertFalse(template.supportsFlavor(RequestFlavor.XML));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.AbstractRenderer#getRenderer()}.
   */
  @Test
  public void testGetRenderer() {
    assertEquals(rendererURL, template.getRenderer());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageTemplateImpl#getDefaultLayout()}.
   */
  @Test
  public void testGetDefaultLayout() {
    assertEquals(layout, template.getDefaultLayout());
  }
  
}
