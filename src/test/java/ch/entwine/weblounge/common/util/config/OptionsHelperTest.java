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

package ch.entwine.weblounge.common.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link OptionsHelper}.
 */
public class OptionsHelperTest {

  /** Options support */
  protected OptionsHelper options = new OptionsHelper();

  /** Name of the option key */
  protected String optionKey = "key";

  /** Name of the option key */
  protected String multiOptionKey = "multikey";

  /** Option value */
  protected String optionValue = "value";

  /** Other option value */
  protected String otherOptionValue = "othervalue";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    options.setOption(optionKey, optionValue);
    options.setOption(multiOptionKey, optionValue);
    options.setOption(multiOptionKey, otherOptionValue);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#removeOption(java.lang.String)}.
   */
  @Test
  public void testRemoveOption() {
    options.removeOption(optionKey);
    assertEquals(1, options.getOptions().size());
    assertFalse(options.hasOption(optionKey));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#hasOption(java.lang.String)}.
   */
  @Test
  public void testHasOption() {
    assertTrue(options.hasOption(optionKey));
    assertTrue(options.hasOption(multiOptionKey));
    assertFalse(options.hasOption("test"));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValue(java.lang.String)}.
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, options.getOptionValue(optionKey));
    assertEquals(otherOptionValue, options.getOptionValue(multiOptionKey));
    assertTrue(options.getOptionValue("test") == null);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValue(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, options.getOptionValue(optionKey, "abc"));
    assertEquals(otherOptionValue, options.getOptionValue(multiOptionKey, "abc"));
    assertEquals("test", options.getOptionValue("abc", "test"));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValues(java.lang.String)}.
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, options.getOptionValues(optionKey).length);
    assertEquals(2, options.getOptionValues(multiOptionKey).length);
    assertEquals(0, options.getOptionValues("test").length);
    options.setOption(optionKey, optionValue);
    assertEquals(1, options.getOptionValues(optionKey).length);
    options.setOption(optionKey, "abc");
    assertEquals(2, options.getOptionValues(optionKey).length);
    options.setOption(optionKey, "def");
    assertEquals(3, options.getOptionValues(optionKey).length);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(2, options.getOptions().size());
  }

}
