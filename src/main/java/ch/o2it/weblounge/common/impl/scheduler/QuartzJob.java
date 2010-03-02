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

import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobTrigger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Base implementation for jobs.
 */
public final class QuartzJob {

  /** The job identifier */
  protected String identifier = null;

  /** The job name */
  protected String name = null;

  /** The actual job implementation */
  protected Class<? extends Job> worker = null;

  /** Job trigger */
  protected JobTrigger trigger = null;

  /** Job context map */
  protected Dictionary<String, Serializable> ctx = null;

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
  public QuartzJob(String identifier, Class<? extends Job> worker,
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
  public QuartzJob(String identifier, Class<? extends Job> worker,
      Dictionary<String, Serializable> context, JobTrigger trigger) {
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
      ctx = new Hashtable<String, Serializable>();
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
   * Returns the job identifier.
   * 
   * @return the job identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the job name.
   * 
   * @param name
   *          the job name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the job name.
   * 
   * @return the job name
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
  public static QuartzJob fromXml(Node context) throws IllegalStateException {
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
  public static QuartzJob fromXml(Node config, XPath xPathProcessor)
      throws IllegalStateException {

    Node contextRoot = XPathHelper.select(config, "//job", xPathProcessor);
    if (contextRoot == null)
      return null;

    CronJobTrigger jobTrigger = null;
    Dictionary<String, Serializable> ctx = new Hashtable<String, Serializable>();

    // Main attributes
    String identifier = XPathHelper.valueOf(config, "@id", xPathProcessor);
    String name = XPathHelper.valueOf(config, "name", xPathProcessor);

    // Implementation class
    String className = XPathHelper.valueOf(config, "class", xPathProcessor);
    Class<Job> clazz;
    try {
      clazz = (Class<Job>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException();
    }

    // Read execution schedule
    String schedule = XPathHelper.valueOf(config, "schedule", xPathProcessor);
    if (schedule == null)
      throw new IllegalStateException("No schedule has been defined for job '" + identifier + "'");
    jobTrigger = new CronJobTrigger(schedule);

    // Read options
    NodeList nodes = XPathHelper.selectList(config, "options/option", xPathProcessor);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String optionName = XPathHelper.valueOf(option, "name", xPathProcessor);
      String value = XPathHelper.valueOf(option, "value", xPathProcessor);
      ctx.put(optionName, value);
    }

    // Did we find something?

    QuartzJob job = new QuartzJob(identifier, clazz, ctx, jobTrigger);
    job.setName(name);
    return job;
  }

  /**
   * Returns an <code>XML</code> representation of the job, which will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;job id=&quot;test&quot;&gt;
   *     &lt;name&gt;Job title&lt;/name&gt;
   *     &lt;description&gt;Job title&lt;/description&gt;
   *     &lt;class&gt;ch.o2it.weblounge.module.test.SampleCronJob&lt;/class&gt;
   *     &lt;schedule&gt;0 0 * * 1&lt;/schedule&gt;
   *     &lt;option&gt;
   *         &lt;name&gt;opt&lt;/name&gt;
   *         &lt;value&gt;optvalue&lt;/value&gt;
   *     &lt;/option&gt;
   * &lt;/job&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>QuartzJob</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<job id=\"");
    b.append(identifier);
    b.append("\">");

    // Name
    if (name != null) {
      b.append("<name>");
      b.append(name);
      b.append("</name>");
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
        b.append("<value>");
        b.append(ctx.get(key));
        b.append("</value>");
        b.append("</option>");
      }
      b.append("</options>");
    }

    b.append("</job>");
    return b.toString();
  }

}
