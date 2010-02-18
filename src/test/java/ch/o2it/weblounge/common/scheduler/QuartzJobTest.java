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

import ch.o2it.weblounge.common.impl.scheduler.FireOnceJobTrigger;
import ch.o2it.weblounge.common.impl.scheduler.QuartzJob;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Test case for {@link QuartzJob}.
 */
public class QuartzJobTest {

  /** The job worker */
  protected Class<? extends Job> jobClass = null;

  /** Quartz job */
  protected QuartzJob quartzJob = null;
  
  /** Name of the test job */
  protected String jobName = "testjob";

  /** Job context */
  protected Dictionary<String, Serializable> jobContext = null;

  /** Job trigger that fires every second */
  protected JobTrigger trigger = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    jobClass = TestJob.class;
    jobContext = new Hashtable<String, Serializable>();
    trigger = new FireOnceJobTrigger();
    quartzJob = new QuartzJob(jobName, jobClass, jobContext, trigger);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzJob#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(jobName, quartzJob.getName());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzJob#getJob()}.
   */
  @Test
  public void testGetJob() {
    assertEquals(jobClass, quartzJob.getJob());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzJob#getContext()}.
   */
  @Test
  public void testGetContext() {
    assertEquals(jobContext, quartzJob.getContext());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.scheduler.QuartzJob#getTrigger()}.
   */
  @Test
  public void testGetTrigger() {
    assertEquals(trigger, quartzJob.getTrigger());
  }

}
