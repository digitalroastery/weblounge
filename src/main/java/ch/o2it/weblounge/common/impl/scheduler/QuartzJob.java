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

package ch.o2it.weblounge.common.impl.scheduler;

import ch.o2it.weblounge.common.scheduler.JobException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.util.Dictionary;

/**
 * Wrapper class that is passed to the Quartz scheduler and that will execute
 * whatever class is passed in the <code>JobExecutionContext</code> using the
 * <code>ch.o2it.weblounge.Job</code> key.
 */
public class QuartzJob implements Job {

  /** The class key */
  public final static String CLASS = "ch.o2it.weblounge.Job";

  /** The class key */
  public final static String CONTEXT = "ch.o2it.weblounge.JobContext";

  /** The job instance */
  private ch.o2it.weblounge.common.scheduler.Job jobInstance = null;
  
  private Dictionary<String, Serializable> jobContext = null;;

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    if (jobInstance == null) {
      try {
        jobInstance = createJobInstance(ctx);
      } catch (Throwable t) {
        throw new JobExecutionException("Lookup of job implementation failed");
      }
    }
    
    // Execute the job
    try {
      jobInstance.execute(ctx.getJobDetail().getName(), jobContext);
    } catch (JobException e) {
      throw new JobExecutionException(e);
    }
  }

  /**
   * Creates the job implementation by looking for a class registered under the
   * key <code>ch.o2it.weblounge.Job</code>.
   * 
   * @param ctx
   *          the job context
   * @return the job instance
   * @throws Exception if creating an instance of the job fails
   */
  @SuppressWarnings("unchecked")
  private ch.o2it.weblounge.common.scheduler.Job createJobInstance(JobExecutionContext ctx) throws Exception {
    Class<?> c = (Class<?>) ctx.get(CLASS);
    if (c == null)
      throw new IllegalStateException("Lookup of job implementation failed");
    try {
      jobInstance = (ch.o2it.weblounge.common.scheduler.Job) c.newInstance();
      jobContext = (Dictionary<String, Serializable>)ctx.get(CONTEXT);
      return jobInstance;
    } catch (InstantiationException e) {
      throw new IllegalStateException("Error instantiating job implementation: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Access to job implementation denied: " + e.getMessage());
    }
  }

}
