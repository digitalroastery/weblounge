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

package ch.o2it.weblounge.common.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;

import org.junit.Test;

/**
 * Test case for {@link ConfigurationUtils}.
 */
public class ConfigurationUtilsTest {
  
  /** Options test */
  protected String options = "a b, c ; d";

  /** 25 minutes in milliseconds */
  protected long twentyFiveMinutes = 25*Times.MS_PER_MIN;
  
  /** 2 hours in milliseconds */
  protected long twoHours = 2*Times.MS_PER_HOUR;
  
  /** 1 week in milliseconds */
  protected long oneWeek = 1*Times.MS_PER_WEEK;
  
  /** everything in milliseconds */
  protected long durationInMillis = twentyFiveMinutes + twoHours + oneWeek;

  /** The encoded duration */
  protected String duration = "1w2H25M";

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils#getMultiOptionValues(java.lang.String)}.
   */
  @Test
  public void testGetMultiOptionValues() {
    assertEquals(4, ConfigurationUtils.getMultiOptionValues(options).length);
    assertEquals("a", ConfigurationUtils.getMultiOptionValues(options)[0]);
    assertEquals("b", ConfigurationUtils.getMultiOptionValues(options)[1]);
    assertEquals("c", ConfigurationUtils.getMultiOptionValues(options)[2]);
    assertEquals("d", ConfigurationUtils.getMultiOptionValues(options)[3]);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils#isTrue(java.lang.String)}.
   */
  @Test
  public void testIsTrue() {
    assertTrue(ConfigurationUtils.isTrue("true"));
    assertTrue(ConfigurationUtils.isTrue("TRUe"));
    assertTrue(ConfigurationUtils.isTrue("on"));
    assertTrue(ConfigurationUtils.isTrue("yes"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils#isFalse(java.lang.String)}.
   */
  @Test
  public void testIsFalse() {
    assertTrue(ConfigurationUtils.isFalse("false"));
    assertTrue(ConfigurationUtils.isFalse("FALsE"));
    assertTrue(ConfigurationUtils.isFalse("off"));
    assertTrue(ConfigurationUtils.isFalse("no"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils#toDuration(long)}.
   */
  @Test
  public void testToDuration() {
    String d = ConfigurationUtils.toDuration(durationInMillis);
    assertEquals(duration, d);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils#parseDuration(java.lang.String)}.
   */
  @Test
  public void testParseDuration() {
    long m = ConfigurationUtils.parseDuration(duration);
    assertEquals(durationInMillis, m);
  }

}
