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

import ch.o2it.weblounge.common.scheduler.JobWorker;
import ch.o2it.weblounge.common.scheduler.JobException;
import ch.o2it.weblounge.common.site.Site;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Wrapper class that is passed to the Quartz scheduler and that will execute
 * whatever class is passed in the <code>JobExecutionContext</code> using the
 * <code>ch.o2it.weblounge.Job</code> key.
 */
public class QuartzJobWorker implements org.quartz.Job {
  
  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(QuartzJobWorker.class);

  /** The class key */
  public static final String CLASS = "ch.o2it.weblounge.Job";

  /** The class key */
  public static final String CONTEXT = "ch.o2it.weblounge.JobContext";

  /** The job instance */
  private JobWorker jobInstance = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @SuppressWarnings("unchecked")
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    try {
      if (jobInstance == null) {
        jobInstance = createJobInstance(ctx);
        logger.debug("Created new quartz worker " + jobInstance + " with context " + ctx);
      }

      // Prepare the local job context
      JobDataMap jobData = ctx.getJobDetail().getJobDataMap();
      Dictionary<String, Serializable> jobContext = (Dictionary<String, Serializable>)jobData.get(CONTEXT);
      if (jobContext == null) {
        logger.debug("Creating default job context");
        jobContext = new Hashtable<String, Serializable>();
        jobContext.put(Site.class.getName(), (Site)jobData.get(Site.class.getName()));
        jobData.put(CONTEXT, jobContext);
      }
      
      // Execute the worker
      jobInstance.execute(ctx.getJobDetail().getName(), jobContext);
      
    } catch (JobException e) {
      throw new JobExecutionException(e);
    } catch (Throwable t) {
      throw new JobExecutionException("Lookup of job implementation failed");
    }
  }

  /**
   * Creates the job implementation by looking for a class registered under the
   * key <code>ch.o2it.weblounge.Job</code>.
   * 
   * @param ctx
   *          the job context
   * @return the job instance
   * @throws Exception
   *           if creating an instance of the job fails
   */
  private JobWorker createJobInstance(JobExecutionContext ctx) throws Exception {
    JobDataMap jobData = ctx.getJobDetail().getJobDataMap();
    Class<?> c = (Class<?>) jobData.get(CLASS);
    if (c == null)
      throw new IllegalStateException("Lookup of job implementation failed");
    try {
      jobInstance = (JobWorker) c.newInstance();
      return jobInstance;
    } catch (InstantiationException e) {
      throw new IllegalStateException("Error instantiating job implementation: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Access to job implementation denied: " + e.getMessage());
    }
  }

}
