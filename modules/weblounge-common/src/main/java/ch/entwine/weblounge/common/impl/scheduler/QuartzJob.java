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

import ch.entwine.weblounge.common.Customizable;
import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.scheduler.Job;
import ch.entwine.weblounge.common.scheduler.JobTrigger;
import ch.entwine.weblounge.common.scheduler.JobWorker;
import ch.entwine.weblounge.common.site.Environment;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Base implementation for jobs.
 */
public final class QuartzJob implements Job, Customizable {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(QuartzJob.class);

  /** The job identifier */
  protected String identifier = null;

  /** The job name */
  protected String name = null;

  /** The actual job implementation */
  protected Class<? extends JobWorker> worker = null;

  /** Job trigger */
  protected JobTrigger trigger = null;

  /** The options */
  protected OptionsHelper options = null;

  /** Job context map */
  protected Dictionary<String, Object> ctx = null;

  /** The environment */
  protected Environment environment = Environment.Production;

  /**
   * Creates a new job.
   * 
   * @param identifier
   *          job identifier
   * @param worker
   *          the job implementation
   * @param trigger
   *          the job trigger
   */
  public QuartzJob(String identifier, Class<? extends JobWorker> worker,
      JobTrigger trigger) {
    this(identifier, worker, null, trigger);
  }

  /**
   * Creates a new job with an initial job context. That context will be passed
   * every time the {@link #execute(Dictionary)} method is triggered.
   * 
   * @param identifier
   *          job identifier
   * @param worker
   *          the job implementation
   * @param context
   *          the job context
   * @param trigger
   *          the job trigger
   */
  public QuartzJob(String identifier, Class<? extends JobWorker> worker,
      Dictionary<String, Object> context, JobTrigger trigger) {
    if (identifier == null)
      throw new IllegalArgumentException("Job identifier must not be null");
    if (worker == null)
      throw new IllegalArgumentException("Worker must not be null");
    if (trigger == null)
      throw new IllegalArgumentException("Trigger must not be null");
    this.identifier = identifier;
    this.worker = worker;
    this.trigger = trigger;
    this.ctx = context;
    if (this.ctx == null)
      ctx = new Hashtable<String, Object>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#setEnvironment(ch.entwine.weblounge.common.site.Environment)
   */
  public void setEnvironment(Environment environment) {
    options.setEnvironment(environment);
    ctx = new Hashtable<String, Object>();
    for (String name : options.getOptionNames()) {
      String[] values = options.getOptionValues(name);
      if (values.length == 1)
        ctx.put(name, values[0]);
      else
        ctx.put(name, values);
    }
  }

  /**
   * Sets the job identifier.
   * 
   * @param identifier
   *          the job identifier
   * @throws IllegalArgumentException
   *           if <code>identifier</code> is <code>null</code>
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Job identifier must not be null");
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#setWorker(java.lang.Class)
   */
  public void setWorker(Class<JobWorker> job) {
    if (worker == null)
      throw new IllegalArgumentException("Job implementation must not be null");
    this.worker = job;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#getWorker()
   */
  public Class<? extends JobWorker> getWorker() {
    return worker;
  }

  /**
   * Returns the job context.
   * 
   * @return the context
   */
  public Dictionary<String, Object> getContext() {
    return ctx;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#setTrigger(ch.entwine.weblounge.common.scheduler.JobTrigger)
   */
  public void setTrigger(JobTrigger trigger) {
    if (trigger == null)
      throw new IllegalArgumentException("Job trigger must not be null");
    this.trigger = trigger;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#getTrigger()
   */
  public JobTrigger getTrigger() {
    return trigger;
  }

  /**
   * Resets the job, which means that there will be no more evidence that the
   * job has been run already. This is especially useful in the case where a
   * site is stopped and restarted for maintenance.
   */
  public void reset() {
    trigger.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String key, String value) {
    options.setOption(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.site.Environment)
   */
  public void setOption(String name, String value, Environment environment) {
    options.setOption(name, value, environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptions()
   */
  public Map<String, Map<Environment, List<String>>> getOptions() {
    return options.getOptions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    return options.getOptionNames();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * Returns the string representation of this job, which is equal to the value
   * returned by <code>getName()</code>.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(identifier);
    buf.append(" [schedule=");
    buf.append(trigger);
    buf.append("; class=");
    buf.append(worker.getClass().getName());
    buf.append("]");
    return buf.toString();
  }

  /**
   * Initializes this job from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the job node
   * @throws IllegalStateException
   *           if the job cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static Job fromXml(Node context) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this job from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the job node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the job cannot be parsed
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static Job fromXml(Node config, XPath xPathProcessor)
      throws IllegalStateException {

    CronJobTrigger jobTrigger = null;
    Dictionary<String, Object> ctx = new Hashtable<String, Object>();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // Main attributes
    String identifier = XPathHelper.valueOf(config, "@id", xPathProcessor);

    // Implementation class
    String className = XPathHelper.valueOf(config, "m:class", xPathProcessor);
    Class<? extends JobWorker> c;
    try {
      c = (Class<? extends JobWorker>) classLoader.loadClass(className);
      c.newInstance(); // Create an instance just to make sure we catch any errors right here
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Implementation " + className + " for job '" + identifier + "' not found", e);
    } catch (InstantiationException e) {
      throw new IllegalStateException("Error instantiating impelementation " + className + " for job '" + identifier + "'", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Access violation instantiating implementation " + className + " for job '" + identifier + "'", e);
    } catch (Throwable t) {
      throw new IllegalStateException("Error loading implementation " + className + " for job '" + identifier + "'", t);
    }

    // Read execution schedule
    String schedule = XPathHelper.valueOf(config, "m:schedule", xPathProcessor);
    if (schedule == null)
      throw new IllegalStateException("No schedule has been defined for job '" + identifier + "'");
    jobTrigger = new CronJobTrigger(schedule);

    // Read options
    Node nodes = XPathHelper.select(config, "m:options", xPathProcessor);
    OptionsHelper options = OptionsHelper.fromXml(nodes, xPathProcessor);
    for (Map.Entry<String, Map<Environment, List<String>>> entry : options.getOptions().entrySet()) {
      String key = entry.getKey();
      Map<Environment, List<String>> environments = entry.getValue();
      for (Environment environment : environments.keySet()) {
        List<String> values = environments.get(environment);
        if (values.size() == 1)
          ctx.put(key, values.get(0));
        else
          ctx.put(key, values.toArray(new String[values.size()]));
      }
    }

    // Did we find something?

    QuartzJob job = new QuartzJob(identifier, c, ctx, jobTrigger);
    job.options = options;

    // name
    String name = XPathHelper.valueOf(config, "m:name", xPathProcessor);
    job.setName(name);

    return job;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.Job#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<job id=\"");
    b.append(identifier);
    b.append("\">");

    // Names
    if (StringUtils.isNotBlank(name)) {
      b.append("<name><![CDATA[");
      b.append(name);
      b.append("]]></name>");
    }

    // Class
    b.append("<class>");
    b.append(worker.getName());
    b.append("</class>");

    // Schedule
    if (!(trigger instanceof CronJobTrigger))
      throw new IllegalStateException("Cannot serialize job trigger of type " + trigger.getClass().getName());
    b.append("<schedule>");
    b.append(((CronJobTrigger) trigger).getCronExpression());
    b.append("</schedule>");

    // Options
    Enumeration<String> e = ctx.keys();
    if (e.hasMoreElements()) {
      b.append("<options>");
      while (e.hasMoreElements()) {
        String key = e.nextElement();
        b.append("<option>");
        b.append("<name>");
        b.append(key);
        b.append("</name>");
        Object value = ctx.get(key);
        if (value instanceof String[]) {
          String[] values = (String[]) ctx.get(key);
          for (String v : values) {
            b.append("<value><![CDATA[");
            b.append(v);
            b.append("]]></value>");
          }
        } else {
          b.append("<value><![CDATA[");
          b.append(value);
          b.append("]]></value>");
        }
        b.append("</option>");
      }
      b.append("</options>");
    }

    b.append("</job>");
    return b.toString();
  }

}
