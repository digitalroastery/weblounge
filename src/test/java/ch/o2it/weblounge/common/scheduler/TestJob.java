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

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Dictionary;

/**
 * Test implementation for a job, used by the scheduler test cases.
 * <p>
 * The behavior of this implementation is as follows: On every execution, it
 * will update the value of {@link #CTX_EXECUTIONS} in the context, while making
 * sure it fit the expected value. In case of a mismatch, it will throw a
 * {@link JobException}.
 * <p>
 * A <code>JobException</code> will also be thrown on the third execution of the
 * job in order to allow for proper testing of exception handling. In addition,
 * on the tenth execution of the job, a {@link ArithmeticException} will be
 * thrown as an example of a <code>RuntimeException</code> as opposed to the
 * checked <code>JobException</code>.
 */
@Ignore
public class TestJob implements JobWorker {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(TestJob.class);
  
  /** String constant identifying the number of executions */
  public static final String CTX_EXECUTIONS = "executions";

  /** Execution where a <code>JobException</code> will be thrown */
  public static final int JOB_EXCEPTION_COUNT = 3;

  /** Execution where a <code>ArithmeticException</code> will be thrown */
  public static final int ARITHMETIC_EXCEPTION_COUNT = 10;

  /** The number of test job instances */
  private static int instances = 0;

  /** Current instance */
  private int id = -1;

  /** The last context */
  private Dictionary<String, Serializable> lastContext = null;

  public TestJob() {
    id = ++instances;
  }

  /**
   * Returns the last context that was passed in at job execution.
   * 
   * @return the last context
   */
  public Dictionary<String, Serializable> getLastContext() {
    return lastContext;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {

    int executions = 0;
    Integer storedInCtx = (Integer) ctx.get(CTX_EXECUTIONS);
    if (storedInCtx != null)
      executions = storedInCtx.intValue();

    // Increase execution count
    executions++;

    logger.debug("Test job " + id + " execution " + executions);

    // Update the context
    lastContext = ctx;
    ctx.put(CTX_EXECUTIONS, Integer.valueOf(executions));

    if (executions == JOB_EXCEPTION_COUNT)
      throw new JobException(this, "The third execution always triggers this exception");
    if (executions == ARITHMETIC_EXCEPTION_COUNT)
      throw new ArithmeticException("The tenth execution always triggers this exception");
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Testjob " + id;
  }

}
