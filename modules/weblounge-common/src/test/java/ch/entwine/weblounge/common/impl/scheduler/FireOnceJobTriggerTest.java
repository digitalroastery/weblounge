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

package ch.entwine.weblounge.common.impl.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for {@link FireOnceJobTrigger).
 */
public class FireOnceJobTriggerTest {

  /** The job trigger to test */
  protected FireOnceJobTrigger trigger = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    trigger = new FireOnceJobTrigger();
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.scheduler.FireOnceJobTrigger#getNextExecutionAfter(java.util.Date)}.
   */
  @Test
  public void testGetNextExecutionAfter() {
    Date now = new Date();
    Date then = new Date(now.getTime() + 60000L);
    assertEquals(now, trigger.getNextExecutionAfter(now));
    assertTrue(trigger.getNextExecutionAfter(then) == null);
    trigger.triggered(now);
    assertTrue(trigger.getNextExecutionAfter(now) == null);
    assertTrue(trigger.getNextExecutionAfter(then) == null);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.scheduler.FireOnceJobTrigger#triggered(java.util.Date)}.
   */
  @Test
  public void testTriggeredDate() {
    Date now = new Date();
    Date then = new Date(now.getTime() + 60000L);
    try {
      trigger.triggered(now); // Should pass
      trigger.triggered(then); // Should fail
      fail("Trigger should have thrown an exception");
    } catch (IllegalStateException e) {
      // expected
    }
  }
  
}
