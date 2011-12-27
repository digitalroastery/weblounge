/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.image;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.site.ModuleImpl;
import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test case for class {@link ImageStyleUtils}.
 */
public class ImageStyleUtilsTest {

  /** The site */
  protected Site site = new SiteImpl();

  /** The test module */
  protected Module module = new ModuleImpl();

  /** The image style */
  protected String style1Id = "style1";

  /** First image style */
  protected ImageStyle style1 = new ImageStyleImpl(style1Id, 10, 10);

  /** Second image style */
  protected ImageStyle style2 = new ImageStyleImpl("style2", 10, 10);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site.addModule(module);
    style2.setComposeable(false);
    module.addImageStyle(style1);
    module.addImageStyle(style2);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#findStyle(java.lang.String, ch.entwine.weblounge.common.site.Site)}
   * .
   */
  @Test
  public void testFindStyle() {
    assertEquals(style1, ImageStyleUtils.findStyle(style1Id, site));
    Assert.assertNull(ImageStyleUtils.findStyle("xyz2", site));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#findStyles(java.lang.String, boolean, ch.entwine.weblounge.common.site.Site)}
   * .
   */
  @Test
  public void testFindStyles() {
    assertEquals(1, ImageStyleUtils.findStyles(style1Id, false, site).length);
    assertEquals(2, ImageStyleUtils.findStyles("style.*", false, site).length);
    assertEquals(1, ImageStyleUtils.findStyles("style.*", true, site).length);
    assertEquals(0, ImageStyleUtils.findStyles("xyz.*", false, site).length);
  }

}
