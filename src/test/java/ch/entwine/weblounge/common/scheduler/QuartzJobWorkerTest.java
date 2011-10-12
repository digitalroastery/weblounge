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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.scheduler.FireOnceJobTrigger;
import ch.entwine.weblounge.common.impl.scheduler.PeriodicJobTrigger;
import ch.entwine.weblounge.common.impl.scheduler.QuartzJob;
import ch.entwine.weblounge.common.impl.scheduler.QuartzJobTrigger;
import ch.entwine.weblounge.common.impl.scheduler.QuartzJobWorker;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Test case for {@link QuartzJobWorker}.
 */
public class QuartzJobWorkerTest {

  /** The quartz scheduler */
  protected static Scheduler scheduler = null;

  /** The job worker */
  protected Class<? extends JobWorker> jobClass = null;

  /** Quartz job */
  protected QuartzJob quartzJob = null;

  /** Name of the test job */
  protected String jobName = "testjob";

  /** Name of the scheduler group */
  protected String schedulerGroup = "testgroup";

  /** Job context */
  protected Dictionary<String, Object> jobContext = null;

  /** Job trigger that fires every second */
  protected JobTrigger trigger = null;

  /** The trigger period */
  protected long period = 100L;

  /** The quartz job wrapper */
  protected Trigger quartzTrigger = null;

  /** The trigger listener */
  protected TestTriggerListener triggerListener = null;

  /** The monitor */
  protected static final Object monitor = new Object();

  @BeforeClass
  public static void setUpClass() throws Exception {
    StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    scheduler = schedulerFactory.getScheduler();
    scheduler.start();
  }

  /**
   * Clean up after the last test case has been run.
   * 
   * @throws Exception
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
    scheduler.shutdown();
  }

  /**
   * Prepare for a new test method.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    jobClass = TestJob.class;
    jobContext = new Hashtable<String, Object>();
  }

  /**
   * Clean up after test method.
   * 
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    if (triggerListener != null)
      scheduler.removeTriggerListener(triggerListener.getName());
    if (quartzJob != null)
      unscheduleJob(quartzJob);
    assertEquals(0, scheduler.getGlobalTriggerListeners().size());
    assertEquals(0, scheduler.getJobNames(schedulerGroup).length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.QuartzJobWorker#execute(org.quartz.JobExecutionContext)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public void testExecuteOnce() throws Exception {

    // Set up and register the listener
    trigger = new FireOnceJobTrigger();
    triggerListener = new TestTriggerListener(monitor, 1);
    scheduler.addTriggerListener(triggerListener);

    // Create the job
    quartzJob = new QuartzJob(jobName, jobClass, jobContext, trigger);

    // Schedule
    scheduleJob(quartzJob);
    assertEquals(1, scheduler.getJobNames(schedulerGroup).length);
    assertEquals(1, scheduler.getTriggersOfJob(jobName, schedulerGroup).length);

    synchronized (monitor) {
      try {
        monitor.wait(1000);
      } catch (InterruptedException e) {
        fail("Trigger was interrupted while waiting for notification: " + e.getMessage());
      }
    }

    assertEquals(1, triggerListener.getFiredCount());
    assertEquals(1, triggerListener.getCompletedCount());
    assertEquals(0, triggerListener.getVetoedCount());
    assertEquals(0, triggerListener.getMisfiredCount());
    assertNotNull(jobContext.get(TestJob.CTX_EXECUTIONS));
    assertEquals(1, ((Integer)jobContext.get(TestJob.CTX_EXECUTIONS)).intValue());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.QuartzJobWorker#execute(org.quartz.JobExecutionContext)}
   * 
   * @throws Exception
   */
  @Test
  public void testExecutePeriodically() throws Exception {
    int triggerCount = 2;

    // Set up and register the listener
    trigger = new PeriodicJobTrigger(period);
    ((PeriodicJobTrigger) trigger).setRepeatCount(triggerCount);
    triggerListener = new TestTriggerListener(monitor, triggerCount);
    scheduler.addTriggerListener(triggerListener);

    // Create the job
    quartzJob = new QuartzJob(jobName, jobClass, jobContext, trigger);

    // Schedule
    scheduleJob(quartzJob);
    assertEquals(1, scheduler.getJobNames(schedulerGroup).length);
    assertEquals(1, scheduler.getTriggersOfJob(jobName, schedulerGroup).length);

    synchronized (monitor) {
      try {
        monitor.wait((triggerCount + 1) * period);
      } catch (InterruptedException e) {
        fail("Trigger was interrupted while waiting for notification: " + e.getMessage());
      }
    }

    assertEquals(triggerCount, triggerListener.getFiredCount());
    assertEquals(triggerCount, triggerListener.getCompletedCount());
    assertEquals(0, triggerListener.getVetoedCount());
    assertEquals(0, triggerListener.getMisfiredCount());
    assertNotNull(jobContext.get(TestJob.CTX_EXECUTIONS));
    assertEquals(triggerCount, ((Integer)jobContext.get(TestJob.CTX_EXECUTIONS)).intValue());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.QuartzJobWorker#execute(org.quartz.JobExecutionContext)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public void testExecuteVetoed() throws Exception {

    // Set up and register the listener
    trigger = new FireOnceJobTrigger();
    triggerListener = new TestTriggerListener(monitor, 1);
    triggerListener.setVeto(true);
    scheduler.addTriggerListener(triggerListener);

    // Create the job
    quartzJob = new QuartzJob(jobName, jobClass, jobContext, trigger);

    // Schedule
    scheduleJob(quartzJob);
    assertEquals(1, scheduler.getJobNames(schedulerGroup).length);
    assertEquals(1, scheduler.getTriggersOfJob(jobName, schedulerGroup).length);

    synchronized (monitor) {
      try {
        monitor.wait(1000);
      } catch (InterruptedException e) {
        fail("Trigger was interrupted while waiting for notification: " + e.getMessage());
      }
    }

    assertEquals(1, triggerListener.getFiredCount());
    assertEquals(0, triggerListener.getCompletedCount());
    assertEquals(1, triggerListener.getVetoedCount());
    assertEquals(0, triggerListener.getMisfiredCount());
    assertTrue(jobContext.get(TestJob.CTX_EXECUTIONS) == null);
  }

  /**
   * Schedules the job with the Quartz job scheduler.
   * 
   * @param job
   *          the job
   * @param trigger
   *          the job trigger
   */
  private Date scheduleJob(QuartzJob job)
      throws SchedulerException {
    String jobIdentifier = job.getIdentifier();
    JobTrigger trigger = job.getTrigger();
    Class<?> jobClass = job.getWorker();

    // Set up the job detail
    JobDataMap jobData = new JobDataMap();
    jobData.put(QuartzJobWorker.CLASS, jobClass);
    jobData.put(QuartzJobWorker.CONTEXT, job.getContext());
    JobDetail quartzJobDetail = new JobDetail(jobIdentifier, schedulerGroup, QuartzJobWorker.class);
    quartzJobDetail.setJobDataMap(jobData);

    // Define the trigger
    Trigger quartzTrigger = new QuartzJobTrigger(jobIdentifier, schedulerGroup, trigger);
    quartzTrigger.addTriggerListener(triggerListener.getName());

    // Schedule
    return scheduler.scheduleJob(quartzJobDetail, quartzTrigger);
  }

  /**
   * Removes the job from the Quartz job scheduler.
   * 
   * @param job
   *          the job
   */
  private void unscheduleJob(QuartzJob job) throws SchedulerException {
    String jobIdentifier = job.getIdentifier();
    scheduler.unscheduleJob(jobIdentifier, schedulerGroup);
  }

}
