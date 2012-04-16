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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;
import ch.entwine.weblounge.common.site.Environment;

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

  /** Key for environment depending settings */
  protected String environmentKey = "envkey";

  /** Option value during production */
  protected String developmentValue = "developmentValue";

  /** Option value during production */
  protected String productionValue = "productionValue";

  /** Option value during production */
  protected String otherProductionValue = "otherProductionValue";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    options.setOption(optionKey, optionValue);
    options.setOption(multiOptionKey, optionValue);
    options.setOption(multiOptionKey, otherOptionValue);
    options.setOption(environmentKey, developmentValue, Environment.Development);
    options.setOption(environmentKey, productionValue, Environment.Production);
    options.setOption(environmentKey, otherProductionValue, Environment.Production);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#removeOption(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveOption() {
    options.removeOption(optionKey);
    assertEquals(2, options.getOptions().size());
    assertFalse(options.hasOption(optionKey));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionNames()}
   * .
   */
  @Test
  public void testGetOptionNames() {
    assertEquals(2, options.getOptionNames().length);

    // There is no option specifically for staging
    options.setEnvironment(Environment.Staging);
    assertEquals(2, options.getOptionNames().length);

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Production);
    assertEquals(3, options.getOptionNames().length);

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Development);
    assertEquals(3, options.getOptionNames().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#hasOption(java.lang.String)}
   * .
   */
  @Test
  public void testHasOption() {
    assertTrue(options.hasOption(optionKey));
    assertTrue(options.hasOption(multiOptionKey));
    assertFalse(options.hasOption("test"));

    // There is no option specifically for staging
    options.setEnvironment(Environment.Staging);
    assertTrue(options.hasOption(optionKey));
    assertTrue(options.hasOption(multiOptionKey));
    assertFalse(options.hasOption(environmentKey));

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Production);
    assertTrue(options.hasOption(optionKey));
    assertTrue(options.hasOption(multiOptionKey));
    assertTrue(options.hasOption(environmentKey));

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Development);
    assertTrue(options.hasOption(optionKey));
    assertTrue(options.hasOption(multiOptionKey));
    assertTrue(options.hasOption(environmentKey));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValue(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, options.getOptionValue(optionKey));
    assertEquals(otherOptionValue, options.getOptionValue(multiOptionKey));
    assertTrue(options.getOptionValue("test") == null);

    // There is no option specifically for staging
    options.setEnvironment(Environment.Staging);
    assertNull(options.getOptionValue(environmentKey));

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Production);
    assertNotNull(options.getOptionValue(environmentKey));

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Development);
    assertNotNull(options.getOptionValue(environmentKey));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValue(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, options.getOptionValue(optionKey, "abc"));
    assertEquals(otherOptionValue, options.getOptionValue(multiOptionKey, "abc"));
    assertEquals("test", options.getOptionValue("abc", "test"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptionValues(java.lang.String)}
   * .
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

    // There is no option specifically for staging
    options.setEnvironment(Environment.Staging);
    assertEquals(0, options.getOptionValues(environmentKey).length);

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Production);
    assertEquals(2, options.getOptionValues(environmentKey).length);

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Development);
    assertEquals(1, options.getOptionValues(environmentKey).length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.options.OptionsHelper#getOptions()}
   * .
   */
  @Test
  public void testGetOptions() {
    assertEquals(3, options.getOptions().size());

    // There is no option specifically for staging
    options.setEnvironment(Environment.Staging);
    assertEquals(3, options.getOptions().size());

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Production);
    assertEquals(3, options.getOptions().size());

    // There is 1 option specifically for production
    options.setEnvironment(Environment.Development);
    assertEquals(3, options.getOptions().size());

  }

}
