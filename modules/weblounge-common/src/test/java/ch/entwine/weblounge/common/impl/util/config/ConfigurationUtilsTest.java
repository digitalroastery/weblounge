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

package ch.entwine.weblounge.common.impl.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.Times;

import org.junit.Test;

/**
 * Test case for {@link ConfigurationUtils}.
 */
public class ConfigurationUtilsTest {

  /** Options test */
  protected String options = "a b, c ; d";

  /** 25 minutes in milliseconds */
  protected long twentyFiveMinutes = 25 * Times.MS_PER_MIN;

  /** 2 hours in milliseconds */
  protected long twoHours = 2 * Times.MS_PER_HOUR;

  /** 1 week in milliseconds */
  protected long oneWeek = 1 * Times.MS_PER_WEEK;

  /** everything in milliseconds */
  protected long durationInMillis = twentyFiveMinutes + twoHours + oneWeek;

  /** The encoded duration */
  protected String duration = "1w2H25M";

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#getMultiOptionValues(java.lang.String)}
   * .
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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#isTrue(java.lang.String)}
   * .
   */
  @Test
  public void testIsTrue() {
    assertTrue(ConfigurationUtils.isTrue("true"));
    assertTrue(ConfigurationUtils.isTrue("TRUe"));
    assertTrue(ConfigurationUtils.isTrue("on"));
    assertTrue(ConfigurationUtils.isTrue("yes"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#isFalse(java.lang.String)}
   * .
   */
  @Test
  public void testIsFalse() {
    assertTrue(ConfigurationUtils.isFalse("false"));
    assertTrue(ConfigurationUtils.isFalse("FALsE"));
    assertTrue(ConfigurationUtils.isFalse("off"));
    assertTrue(ConfigurationUtils.isFalse("no"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#toDuration(long)}
   * .
   */
  @Test
  public void testToDuration() {
    assertEquals(duration, ConfigurationUtils.toDuration(durationInMillis));
    assertEquals("1y", ConfigurationUtils.toDuration(Times.MS_PER_YEAR));
    assertEquals("2y", ConfigurationUtils.toDuration(2 * Times.MS_PER_YEAR));
    assertEquals("1m", ConfigurationUtils.toDuration(Times.MS_PER_MONTH));
    assertEquals("2m", ConfigurationUtils.toDuration(2 * Times.MS_PER_MONTH));
    assertEquals("1w", ConfigurationUtils.toDuration(Times.MS_PER_WEEK));
    assertEquals("2w", ConfigurationUtils.toDuration(2 * Times.MS_PER_WEEK));
    assertEquals("1d", ConfigurationUtils.toDuration(Times.MS_PER_DAY));
    assertEquals("2d", ConfigurationUtils.toDuration(2 * Times.MS_PER_DAY));
    assertEquals("1H", ConfigurationUtils.toDuration(Times.MS_PER_HOUR));
    assertEquals("2H", ConfigurationUtils.toDuration(2 * Times.MS_PER_HOUR));
    assertEquals("1M", ConfigurationUtils.toDuration(Times.MS_PER_MIN));
    assertEquals("2M", ConfigurationUtils.toDuration(2 * Times.MS_PER_MIN));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#parseDuration(java.lang.String)}
   * .
   */
  @Test
  public void testParseDuration() {
    long m = ConfigurationUtils.parseDuration(duration);
    assertEquals(durationInMillis, m);
    assertEquals(Times.MS_PER_YEAR, ConfigurationUtils.parseDuration("1y"));
    assertEquals(2 * Times.MS_PER_YEAR, ConfigurationUtils.parseDuration("2y"));
    assertEquals(Times.MS_PER_MONTH, ConfigurationUtils.parseDuration("1m"));
    assertEquals(2 * Times.MS_PER_MONTH, ConfigurationUtils.parseDuration("2m"));
    assertEquals(Times.MS_PER_WEEK, ConfigurationUtils.parseDuration("1w"));
    assertEquals(2 * Times.MS_PER_WEEK, ConfigurationUtils.parseDuration("2w"));
    assertEquals(Times.MS_PER_DAY, ConfigurationUtils.parseDuration("1d"));
    assertEquals(2 * Times.MS_PER_DAY, ConfigurationUtils.parseDuration("2d"));
    assertEquals(Times.MS_PER_HOUR, ConfigurationUtils.parseDuration("1H"));
    assertEquals(2 * Times.MS_PER_HOUR, ConfigurationUtils.parseDuration("2H"));
    assertEquals(Times.MS_PER_MIN, ConfigurationUtils.parseDuration("1M"));
    assertEquals(2 * Times.MS_PER_MIN, ConfigurationUtils.parseDuration("2M"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils#processTemplate(java.lang.String)}
   * .
   */
  @Test
  public void testProcessTemplateString() {
    System.setProperty("test", "testvalue");
    assertEquals("testvalue", ConfigurationUtils.processTemplate("${test}"));
    assertEquals("embedded testvalue", ConfigurationUtils.processTemplate("embedded ${test}"));
  }

}
