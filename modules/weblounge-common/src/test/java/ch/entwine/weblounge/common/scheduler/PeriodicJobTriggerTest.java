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

package ch.entwine.weblounge.common.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for {@link PeriodicJobTrigger}.
 */
public class PeriodicJobTriggerTest {

  /** The job trigger to test */
  protected PeriodicJobTrigger trigger = null;

  /** The current date */
  protected Date now = new Date();

  /** The start date */
  protected Date startDate = null;

  /** The end date */
  protected Date endDate = null;

  /** The trigger frequency (one second) */
  protected long period = 100L;

  /** Number of times that the trigger is expected to be fired */
  protected long expectedTriggerCount = -1;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    expectedTriggerCount = 5;
    startDate = new Date(now.getTime() + 10L * period);
    endDate = new Date(startDate.getTime() + expectedTriggerCount * period);
    trigger = new PeriodicJobTrigger(period, startDate, endDate, expectedTriggerCount);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#getNextExecutionAfter(java.util.Date)}
   * .
   */
  @Test
  public void testGetNextExecutionAfter() {
    assertEquals(startDate.getTime(), trigger.getNextExecutionAfter(now).getTime());
    assertEquals(startDate.getTime() + period, trigger.getNextExecutionAfter(startDate).getTime());
    assertTrue(trigger.getNextExecutionAfter(endDate) == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#triggered(java.util.Date)}
   * .
   */
  @Test
  public void testTriggeredDate() {
    Date now = startDate;
    int i = 0;
    try {
      for (i = 0; i < expectedTriggerCount + 1; i++) {
        trigger.triggered(now);
        now = new Date(now.getTime() + period);
      }
      fail("Trigger should have complained");
    } catch (IllegalStateException e) {
      assertEquals(expectedTriggerCount, i);
      assertTrue(trigger.getNextExecutionAfter(now) == null);
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#getPeriod()}
   * .
   */
  @Test
  public void testGetPeriod() {
    assertEquals(expectedTriggerCount, trigger.getRepeatCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#setPeriod(long)}
   * .
   */
  @Test
  public void testSetPeriod() {
    long newPeriod = 2 * period;
    long regularTriggerTime = startDate.getTime() + period;
    long adjustedTriggerTime = startDate.getTime() + newPeriod;
    assertEquals(regularTriggerTime, trigger.getNextExecutionAfter(startDate).getTime());
    trigger.setPeriod(newPeriod);
    assertEquals(newPeriod, trigger.getPeriod());
    assertEquals(adjustedTriggerTime, trigger.getNextExecutionAfter(startDate).getTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#setRepeatCount(long)}
   * .
   */
  @Test
  public void testSetRepeatCount() {
    trigger.setRepeatCount(expectedTriggerCount);
    Date now = startDate;
    for (int i = 0; i < expectedTriggerCount; i++) {
      trigger.triggered(now);
      now = new Date(now.getTime() + period);
    }
    assertTrue(trigger.getNextExecutionAfter(startDate) == null);
    try {
      trigger.triggered(now);
      fail("Trigger should have complained");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger#getRepeatCount()}
   * .
   */
  @Test
  public void testGetRepeatCount() {
    trigger.setRepeatCount(expectedTriggerCount);
    assertEquals(expectedTriggerCount, trigger.getRepeatCount());
  }

}
