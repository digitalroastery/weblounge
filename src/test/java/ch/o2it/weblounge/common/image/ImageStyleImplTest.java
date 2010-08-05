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

package ch.o2it.weblounge.common.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.ScalingMode;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link ImageStyleImpl}.
 */
public class ImageStyleImplTest {

  /** The image style under test */
  protected ImageStyleImpl imageStyle = null;
  
  /** Image style identifier */
  protected String id = "default";
  
  /** Image style width */
  protected int width = 250; 

  /** Image style height */
  protected int height = 300; 
  
  /** Scaling mode */
  protected ScalingMode scalingMode = ScalingMode.Cover;

  /** Is the style composeable? */
  protected boolean composeable = true;

  /** The German language */
  protected Language german = LanguageSupport.getLanguage("de");

  /** The English language */
  protected Language english = LanguageSupport.getLanguage("en");
  
  /** English image style name */
  protected String englishName = "Default";

  /** German image style name */
  protected String germanName = "Standard";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    imageStyle = new ImageStyleImpl(id, width, height, scalingMode, composeable);
    imageStyle.setName(germanName, german);
    imageStyle.setName(englishName, english);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(id, imageStyle.getIdentifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#getScalingMode()}.
   */
  @Test
  public void testGetScalingMode() {
    assertEquals(scalingMode, imageStyle.getScalingMode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#getHeight()}.
   */
  @Test
  public void testGetHeight() {
    assertEquals(height, imageStyle.getHeight());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#getWidth()}.
   */
  @Test
  public void testGetWidth() {
    assertEquals(width, imageStyle.getWidth());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#getName(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetNameLanguage() {
    assertEquals(germanName, imageStyle.getName(german));
    assertEquals(englishName, imageStyle.getName(english));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#isComposeable()}.
   */
  @Test
  public void testIsComposeable() {
    assertEquals(composeable, imageStyle.isComposeable());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(imageStyle.equals(new ImageStyleImpl(id, 1, 1)));
    assertFalse(imageStyle.equals(new ImageStyleImpl("test", width, height, scalingMode, composeable)));
  }

}
