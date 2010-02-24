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

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Test case for class {@link TestJob}. This test ensures that the test job is
 * actually exhibiting expected test behavior and is running well, at least
 * outside of the scheduler context.
 */
public class TestJobTest {

  /** The test job */
  protected TestJob job = null;

  /** Name for the test job */
  protected String jobName = "testjob";

  /** Job context */
  protected Dictionary<String, Serializable> jobContext = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    job = new TestJob();
    jobContext = new Hashtable<String, Serializable>();
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.scheduler.TestJob#getLastContext()}.
   */
  @Test
  public void testGetLastContext() {
    for (int i = 1; i < 3; i++) {
      try {
        job.execute(jobName, jobContext);
        assertNotNull(job.getLastContext());
        assertNotNull(job.getLastContext().get(TestJob.CTX_EXECUTIONS));
        assertEquals(i, job.getLastContext().get(TestJob.CTX_EXECUTIONS));
      } catch (JobException e) {
        fail(e.getMessage());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.scheduler.TestJob#execute(java.lang.String, java.util.Dictionary)}
   * .
   */
  @Test
  public void testExecute() {
    for (int i = 1; i < 15; i++) {
      try {
        job.execute(jobName, jobContext);
        switch (i) {
          case TestJob.JOB_EXCEPTION_COUNT:
          case TestJob.ARITHMETIC_EXCEPTION_COUNT:
            fail("The job should have failed during execution " + i);
            break;
          default:
            break;
        }
      } catch (JobException e) {
        if (i != TestJob.JOB_EXCEPTION_COUNT)
          fail("Job throw JobException during run " + i + " instead of " + TestJob.JOB_EXCEPTION_COUNT);
      } catch (Throwable t) {
        if (i != TestJob.ARITHMETIC_EXCEPTION_COUNT)
          fail("Job throw JobException during run " + i + " instead of " + TestJob.ARITHMETIC_EXCEPTION_COUNT);
      }
    }
  }

}
