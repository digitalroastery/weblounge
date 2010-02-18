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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.scheduler.QuartzTriggerListener;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link QuartzTriggerListener}.
 */
public class QuartzTriggerListenerTest {

  /** The trigger listener */
  protected QuartzTriggerListener triggerListener = null;

  /** The trigger listener for an inactive site */
  protected QuartzTriggerListener inactiveTriggerListener = null;

  /** The example site */
  protected Site site = null;

  /** The inactive example site */
  protected Site inactiveSite = null;
  
  /** The site identifier */
  protected String siteIdentifier = "testsite";

  /**
   * Prepares the test class for the next test method.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();
    triggerListener = new QuartzTriggerListener(site);
    inactiveTriggerListener = new QuartzTriggerListener(inactiveSite);
  }

  /**
   * Does preliminary test setup, in this case this mainly means setting up a
   * mock site.
   */
  protected void setUpPreliminaries() {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn(siteIdentifier).anyTimes();
    EasyMock.expect(site.isEnabled()).andReturn(true).anyTimes();
    EasyMock.expect(site.isRunning()).andReturn(true).anyTimes();
    EasyMock.replay(site);

    inactiveSite = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(inactiveSite.getIdentifier()).andReturn(siteIdentifier).anyTimes();
    EasyMock.expect(inactiveSite.isEnabled()).andReturn(false).anyTimes();
    EasyMock.expect(inactiveSite.isRunning()).andReturn(false).anyTimes();
    EasyMock.replay(inactiveSite);
}

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzTriggerListener#getName()}.
   */
  @Test
  public void testGetName() {
    assertNotNull(triggerListener.getName());
    assertTrue(triggerListener.getName().indexOf(siteIdentifier) >= 0);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzTriggerListener#vetoJobExecution(org.quartz.Trigger, org.quartz.JobExecutionContext)}.
   */
  @Test
  public void testVetoJobExecution() {
    assertFalse(triggerListener.vetoJobExecution(null, null));
    assertTrue(inactiveTriggerListener.vetoJobExecution(null, null));
  }

}
