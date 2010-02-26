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

package ch.o2it.weblounge.common.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for the {@link CronJobTrigger}.
 */
public class CronJobTriggerTest {

  /** Trigger firing several times per month */
  protected CronJobTrigger dayOfMonthTrigger = null;

  /** Trigger firing several times per week */
  protected CronJobTrigger dayOfWeekTrigger = null;

  /** Trigger firing several times per hour */
  protected CronJobTrigger hourTrigger = null;

  /** Trigger firing several times per minute */
  protected CronJobTrigger minuteTrigger = null;

  /** Days that the trigger will fire */
  protected int[] daysOfMonth = new int[] { 2, 12, 26 };

  /** Days that the trigger will fire */
  protected int[] daysOfWeek = new int[] { 1, 5 };

  /** Cron expression for the day of month trigger */
  protected String dayOfMonthExpression = "";

  /** Cron expression for the day of week trigger */
  protected String dayOfWeekExpression = "";

  /** Hours that the trigger will fire */
  protected int[] hours = new int[] { 4, 23 };

  /** Cron expression for the hours trigger */
  protected String hoursExpression = "";

  /** Minutes that the trigger will fire */
  protected int[] minutes = new int[] { 1, 27, 34 };

  /** Cron expression for the minutes trigger */
  protected String minutesExpression = "";

  /** Today */
  protected Date now = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    dayOfMonthTrigger = new CronJobTrigger(dayOfMonthExpression);
    dayOfWeekTrigger = new CronJobTrigger(dayOfWeekExpression);
    hourTrigger = new CronJobTrigger(hoursExpression);
    minuteTrigger = new CronJobTrigger(minutesExpression);
    now = new Date();
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#CronJobTrigger()}
   * .
   */
  @Test
  public void testCronJobTrigger() {
    CronJobTrigger emptyTrigger = new CronJobTrigger();
    assertTrue(emptyTrigger.getNextExecutionAfter(now) == null);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getNextExecutionAfter(java.util.Date)}
   * .
   */
  @Test
  @Ignore
  public void testGetNextExecutionAfter() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#triggered(java.util.Date)}
   * .
   */
  @Test
  @Ignore
  public void testTriggered() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getMinutes()}
   * .
   */
  @Test
  @Ignore
  public void testGetMinutes() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setMinutes(short[])}
   * .
   */
  @Test
  @Ignore
  public void testSetMinutesShortArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setMinutes(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testSetMinutesString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getHours()}.
   */
  @Test
  @Ignore
  public void testGetHours() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setHours(short[])}
   * .
   */
  @Test
  @Ignore
  public void testSetHoursShortArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setHours(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testSetHoursString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getMonths()}.
   */
  @Test
  @Ignore
  public void testGetMonths() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setMonths(short[])}
   * .
   */
  @Test
  @Ignore
  public void testSetMonthsShortArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setMonths(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testSetMonthsString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getDaysOfMonth()}
   * .
   */
  @Test
  @Ignore
  public void testGetDaysOfMonth() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setDaysOfMonth(short[])}
   * .
   */
  @Test
  @Ignore
  public void testSetDaysOfMonthShortArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setDaysOfMonth(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testSetDaysOfMonthString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#getDaysOfWeek()}
   * .
   */
  @Test
  @Ignore
  public void testGetDaysOfWeek() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setDaysOfWeek(short[])}
   * .
   */
  @Test
  @Ignore
  public void testSetDaysOfWeekShortArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.scheduler.CronJobTrigger#setDaysOfWeek(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testSetDaysOfWeekString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for asterisk expressions.
   */
  @Test
  public void testAsteriskOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* * * * *");
    assertEquals(1, trigger.getMinutes().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getMinutes()[0]);
    assertEquals(1, trigger.getHours().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getHours()[0]);
    assertEquals(1, trigger.getDaysOfWeek().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getDaysOfWeek()[0]);
    assertEquals(1, trigger.getDaysOfMonth().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getDaysOfMonth()[0]);
    assertEquals(1, trigger.getMonths().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getMonths()[0]);
  }

  /**
   * Test method for range expressions like asterisk/3 in the hour field, which
   * results in these hours: <code>0, 3, 6, 9, 12, 15, 18, 21</code>.
   */
  @Test
  public void testModuloOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* */3 * * *");
    assertEquals(8, trigger.getHours().length);
    assertEquals(0, trigger.getHours()[0]);
    assertEquals(3, trigger.getHours()[1]);
    assertEquals(6, trigger.getHours()[2]);
    assertEquals(9, trigger.getHours()[3]);
    assertEquals(12, trigger.getHours()[4]);
    assertEquals(15, trigger.getHours()[5]);
    assertEquals(18, trigger.getHours()[6]);
    assertEquals(21, trigger.getHours()[7]);
  }

  /**
   * Test method for modulo overlap expressions like asterisk/61 in the minutes
   * field, which results in once every hour.
   */
  @Test
  public void testModuloOperatorOverlap() {
    CronJobTrigger trigger = new CronJobTrigger("*/61 * * * *");
    assertEquals(1, trigger.getMinutes().length);
    assertEquals(0, trigger.getMinutes()[0]);
    assertEquals(1, trigger.getHours().length);
    assertEquals(CronJobTrigger.ALWAYS, trigger.getHours()[0]);
  }

  /**
   * Test method for range expressions like 1,3 (1,3).
   */
  @Test
  public void testCommaOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* 1,3 * * *");
    assertEquals(2, trigger.getHours().length);
    assertEquals(1, trigger.getHours()[0]);
    assertEquals(3, trigger.getHours()[1]);
  }

  /**
   * Test method for range expressions like 1-6 (1,2,3,4,5,6).
   */
  @Test
  public void testRangeOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* 1-3 * * *");
    assertEquals(3, trigger.getHours().length);
    assertEquals(1, trigger.getHours()[0]);
    assertEquals(2, trigger.getHours()[1]);
    assertEquals(3, trigger.getHours()[2]);
  }

  /**
   * Test method for <code>@yearly</code>, which is equivalent to
   * <code>0 0 1 1 *</code>.
   */
  @Test
  public void testYearly() {
    CronJobTrigger yearlyTrigger = new CronJobTrigger("@yearly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 1 *");
    assertTrue(compare(yearlyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@annually</code>, which is equivalent to
   * <code>0 0 1 1 *</code> and of course <code>@yearly</code>.
   */
  @Test
  public void testAnnually() {
    CronJobTrigger AnnuallyTrigger = new CronJobTrigger("@annually");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 1 *");
    assertTrue(compare(AnnuallyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@monthly</code>, which is equivalent to
   * <code>0 0 1 * *</code>.
   */
  @Test
  public void testMonthly() {
    CronJobTrigger monthlyTrigger = new CronJobTrigger("@monthly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 * *");
    assertTrue(compare(monthlyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@weekly</code>, which is equivalent to
   * <code>0 0 * * 0</code>.
   */
  @Test
  public void testWeekly() {
    CronJobTrigger weeklyTrigger = new CronJobTrigger("@weekly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * 0");
    assertTrue(compare(weeklyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@daily</code>, which is equivalent to
   * <code>0 0 * * *</code>.
   */
  @Test
  public void testDaily() {
    CronJobTrigger dailyTrigger = new CronJobTrigger("@daily");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * *");
    assertTrue(compare(dailyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@midnight</code>, which is equivalent to
   * <code>0 0 * * *</code> and of course to <code>@daily</code>.
   */
  @Test
  public void testMidnight() {
    CronJobTrigger midnightTrigger = new CronJobTrigger("@midnight");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * *");
    assertTrue(compare(midnightTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@hourly</code>, which is equivalent to
   * <code>0 * * * *</code>.
   */
  @Test
  public void testHourly() {
    CronJobTrigger dailyTrigger = new CronJobTrigger("@hourly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 * * * *");
    assertTrue(compare(dailyTrigger, equivalentTrigger));
  }

  /**
   * Compares to cron triggers.
   * 
   * @param a
   *          trigger a
   * @param b
   *          trigger b
   */
  private boolean compare(CronJobTrigger a, CronJobTrigger b) {
    // minutes
    assertEquals(a.getMinutes().length, b.getMinutes().length);
    for (short i=0; i < a.getMinutes().length; i++) {
      assertEquals(a.getMinutes()[i], b.getMinutes()[i]);
    }
    // hours
    assertEquals(a.getHours().length, b.getHours().length);
    for (short i=0; i < a.getHours().length; i++) {
      assertEquals(a.getHours()[i], b.getHours()[i]);
    }
    // daysOfWeek
    assertEquals(a.getDaysOfWeek().length, b.getDaysOfWeek().length);
    for (short i=0; i < a.getDaysOfWeek().length; i++) {
      assertEquals(a.getDaysOfWeek()[i], b.getDaysOfWeek()[i]);
    }
    // month
    assertEquals(a.getMonths().length, b.getMonths().length);
    for (short i=0; i < a.getMonths().length; i++) {
      assertEquals(a.getMonths()[i], b.getMonths()[i]);
    }
    // daysOfMonth
    assertEquals(a.getDaysOfMonth().length, b.getDaysOfMonth().length);
    for (short i=0; i < a.getDaysOfMonth().length; i++) {
      assertEquals(a.getDaysOfMonth()[i], b.getDaysOfMonth()[i]);
    }
    return true;
  }

}
