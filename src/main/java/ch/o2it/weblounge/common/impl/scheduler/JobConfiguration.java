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

import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobTrigger;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Base implementation for jobs.
 */
public class JobConfiguration {

  /** The job name */
  protected String name = null;

  /** The actual job implementation */
  protected Class<? extends Job> worker = null;

  /** Job trigger */
  protected JobTrigger trigger = null;

  /** Job context map */
  protected final Dictionary<String, Serializable> ctx = new Hashtable<String, Serializable>();

  /**
   * Creates a new job.
   * 
   * @param name
   *          job name
   * @param worker
   *          the job implementation
   * @param trigger
   *          the job trigger
   */
  public JobConfiguration(String name, Class<? extends Job> worker, JobTrigger trigger) {
    this(name, worker, null, trigger);
  }

  /**
   * Creates a new job with an initial job context. That context will be passed
   * every time the {@link #execute(Dictionary)} method is triggered.
   * 
   * @param name
   *          job name
   * @param worker
   *          the job implementation
   * @param context
   *          the job context
   * @param trigger
   *          the job trigger
   */
  public JobConfiguration(String name, Class<? extends Job> worker,
      Dictionary<String, Serializable> context, JobTrigger trigger) {
    if (name == null)
      throw new IllegalArgumentException("Job name must not be null");
    if (worker == null)
      throw new IllegalArgumentException("Worker must not be null");
    if (trigger == null)
      throw new IllegalArgumentException("Trigger must not be null");
    if (trigger.getNextExecution() < 0)
      throw new IllegalArgumentException("Trigger does not provide execution date or time");
    if (trigger.getNextExecution() > 0 && trigger.getNextExecution() < System.currentTimeMillis())
      throw new IllegalArgumentException("Next trigger execution is in the past");
    this.name = name;
    this.worker = worker;
    this.trigger = trigger;
    if (context != null) {
      Enumeration<String> keys = context.keys();
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        ctx.put(key, context.get(key));
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.Job#setName(java.lang.String)
   */
  public void setName(String name) {
    if (name == null)
      throw new IllegalArgumentException("Job name must not be null");
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.Job#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the job implementation.
   * 
   * @param job
   *          the job
   */
  public void setJob(Class<Job> job) {
    if (worker == null)
      throw new IllegalArgumentException("Job implementation must not be null");
    this.worker = job;
  }

  /**
   * Returns the job implementation.
   * 
   * @return the job
   */
  public Class<? extends Job> getJob() {
    return worker;
  }
  
  /**
   * Returns the job context.
   * 
   * @return the context
   */
  public Dictionary<String, Serializable> getContext() {
    return ctx;
  }

  /**
   * Adds a new trigger to the list of triggers.
   * 
   * @param trigger
   *          the trigger to add
   */
  public void setTrigger(JobTrigger trigger) {
    if (trigger == null)
      throw new IllegalArgumentException("Job trigger must not be null");
    this.trigger = trigger;
  }

  /**
   * Returns the job trigger.
   * 
   * @return the trigger
   */
  public JobTrigger getTrigger() {
    return trigger;
  }

  /**
   * Returns the string representation of this job, which is equal to the value
   * returned by <code>getName()</code>.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (name != null)
      return name;
    return super.toString();
  }

  /**
   * Initializes this cron job from an xml configuration node. Creates a new
   * cron job from the given configuration node. A node is expected to look like
   * this:
   * 
   * <pre>
   *     &lt;job&gt;
   *         &lt;name&gt;Job title&lt;/name&gt;
   *         &lt;description&gt;Job title&lt;/description&gt;
   *         &lt;class&gt;ch.o2it.weblounge.module.test.SampleCronJob&lt;/class&gt;
   *         &lt;option&gt;
   *             &lt;name&gt;opt&lt;/name&gt;
   *             &lt;value&gt;optvalue&lt;/value&gt;
   *         &lt;/option&gt;
   *         &lt;minutes&gt;0&lt;/minutes&gt;
   *         &lt;hours&gt;0&lt;/hours&gt;
   *         &lt;days&gt;*&lt;/days&gt;
   *         &lt;months&gt;*&lt;/months&gt;
   *         &lt;weekdays&gt;1&lt;/weekdays&gt;
   *     &lt;/job&gt;
   * </pre>
   * <p>
   * Fields that are left out are assumed to equal <code>*</code>.
   * <p>
   * Alternatively, the <code>minutes</code>, <code>hours</code>,
   * <code>days</code>, <code>months</code> and <code>weekdays</code> may be
   * replaced by a <code>&lt;schedule&gt;</code> tag, containing a crontab style
   * configuration string:
   * 
   * <pre>
   *     &lt;schedule&gt;0 0 * * 1&lt;/schedule&gt;
   * </pre>
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   *           if the configuration data is incomplete or invalid
   */
  // public void init(XPath path, Node config) throws ConfigurationException {
  // identifier = XPathHelper.valueOf(path, config, "@id");
  // name = XPathHelper.valueOf(path, config, "name");
  // description = XPathHelper.valueOf(path, config, "description");
  //
  // // Read options
  // readOptions(path, config);
  //
  // // See if crontab - style entry has been given
  // if (XPathHelper.select(path, config, "schedule") != null) {
  // parse(XPathHelper.valueOf(path, config, "schedule"));
  // configured = true;
  // return;
  // }
  //
  // // See if something has been configured
  // boolean foundOne = false;
  //
  // // Minutes
  // if (XPathHelper.select(path, config, "minutes") != null) {
  // parseMinutes(XPathHelper.valueOf(path, config, "minutes"));
  // foundOne = true;
  // } else
  // setMinutes(new short[] { ALLWAYS });
  //
  // // Hours
  // if (XPathHelper.select(path, config, "hours") != null) {
  // parseHours(XPathHelper.valueOf(path, config, "hours"));
  // foundOne = true;
  // } else
  // setHours(new short[] { ALLWAYS });
  //
  // // Days
  // if (XPathHelper.select(path, config, "days") != null) {
  // parseDaysOfMonth(XPathHelper.valueOf(path, config, "days"));
  // foundOne = true;
  // } else
  // setDaysOfMonth(new short[] { ALLWAYS });
  //
  // // Months
  // if (XPathHelper.select(path, config, "months") != null) {
  // parseMonths(XPathHelper.valueOf(path, config, "months"));
  // foundOne = true;
  // } else
  // setMonths(new short[] { ALLWAYS });
  //
  // // Weekdays
  // if (XPathHelper.select(path, config, "weekdays") != null) {
  // parseDaysOfWeek(XPathHelper.valueOf(path, config, "weekdays"));
  // foundOne = true;
  // } else
  // setDaysOfWeek(new short[] { ALLWAYS });
  //
  // // Did we find something?
  // if (!foundOne)
  // throw new ConfigurationException("No schedule has been defined for job '" +
  // name + "'");
  //
  // configured = true;
  // }

  /**
   * Reads the job options from the configuration.
   * 
   * @param config
   *          job configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  // private void readOptions(XPath path, Node config) {
  // NodeList nodes = XPathHelper.selectList(path, config, "options/option");
  // for (int i = 0; i < nodes.getLength(); i++) {
  // Node option = nodes.item(i);
  // String name = XPathHelper.valueOf(path, option, "name/text()");
  // String value = XPathHelper.valueOf(path, option, "value/text()");
  // if (options.get(name) != null) {
  // Object o = options.get(name);
  // if (o instanceof ArrayList) {
  // ((ArrayList) o).add(value);
  // } else {
  // List values = new ArrayList();
  // values.add(o);
  // values.add(value);
  // options.remove(name);
  // options.put(name, values);
  // }
  // } else {
  // options.put(name, value);
  // }
  // }
  // }

}
