/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.config;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.cron.AbstractJob;
import ch.o2it.weblounge.common.impl.util.cron.Daemon;
import ch.o2it.weblounge.common.impl.util.cron.SimplePeriodicJob;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

/**
 * This class is used to configure a system using an xml configuration file. To
 * use it, simply overwrite the method {@link #configure(Document)}.
 */
public abstract class XMLConfigurator extends ConfigurationBase {

  /** the configuration file */
  private File file_;

  /** the configuration object */
  protected Document configuration;

  /** the watcher name */
  private String name_;

  /** periodic job executing the watchdog */
  private AbstractJob xmlDog_;

  private XPath xpath_ = null;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = XMLConfigurator.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Instantiates an xml configurator from a configuration file.
   * 
   * @param file
   *          the configuration file
   * @param name
   *          the configuration name
   */
  public XMLConfigurator(File file, String name) {
    file_ = file;
    name_ = name;
  }

  protected XPath getXPath() {
    if (xpath_ == null)
      xpath_ = XMLUtilities.getXPath();
    return xpath_;
  }

  /**
   * Returns the url to the configuration file.
   * 
   * @return the file url
   */
  public File getFile() {
    return file_;
  }

  /**
   * Initiates the configuration.
   */
  public void configure() {
    loadConfiguration();
    configure(configuration);
  }

  /**
   * Like {@link #configureAndWatch(long)} except that the default delay as
   * defined by {@link FileWatchdog#DEFAULT_DELAY} is used.
   * 
   * @throws ConfigurationException
   *           if an exception occured when loading or parsing the configuration
   *           file
   */
  public void configureAndWatch() throws ConfigurationException {
    configureAndWatch(Watchdog.DEFAULT_DELAY);
  }

  /**
   * Read the configuration file <code>configFilename</code> if it exists.
   * Moreover, a thread will be created that will periodically check if
   * <code>configFilename</code> has been created or modified. The period is
   * determined by the <code>delay</code> argument. If a change or file creation
   * is detected, then <code>configFilename</code> is read and configuration is
   * triggered.
   * 
   * @param delay
   *          The delay in milliseconds to wait between each check.
   * @throws ConfigurationException
   *           if an exception occured when loading or parsing the configuration
   *           file
   */
  public void configureAndWatch(long delay) throws ConfigurationException {
    log_.debug("New xml configurator started [file=" + file_ + "; type=PeriodicJob]");
    loadConfiguration();
    xmlDog_ = new XMLWatchdog(file_, name_, this);
    ((SimplePeriodicJob) xmlDog_).setPeriod_(delay);
    Daemon.getInstance().addJob(xmlDog_);
  }

  /**
   * Stops watching the configuration file on a regular basis.
   */
  public void stopWatching() {
    if (xmlDog_ != null)
      Daemon.getInstance().removeJob(xmlDog_);
  }

  /**
   * Read configuration options from <code>properties</code>. See
   * {@link #configure(Document)} for the expected format and overwrite this
   * method to actually read the properties and configure the system.
   * 
   * @param config
   *          the xml configuration
   */
  protected abstract void configure(Document config)
      throws ConfigurationException;

  /**
   * Loads the configuration file. This operation is called everytime the
   * properties file has been modified.
   */
  protected void loadConfiguration() throws ConfigurationException {
    try {
      DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder();
      docBuilder.getDOMImplementation();
      configuration = docBuilder.parse(new FileInputStream(file_));
    } catch (SAXException e) {
      configuration = null;
      throw new ConfigurationException("Parser error in document '" + file_ + "'!", e);
    } catch (IOException e) {
      configuration = null;
      throw new ConfigurationException("Could not read configuration file '" + file_ + "'!", e);
    } catch (Exception e) {
      configuration = null;
      throw new ConfigurationException("Exception while reading configuration file '" + file_ + "'!", e);
    }
  }

  /**
   * Sets the configurator's name.
   * 
   * @param name
   *          the name
   */
  public void setName(String name) {
    name_ = name;
  }

  /**
   * Returns the configurator's name.
   * 
   * @return the name
   */
  public String getName() {
    return name_;
  }

  /*
   * ------------------------------------------------------------- I N N E R C L
   * A S S PropertyWatchdog
   * -------------------------------------------------------------
   */

  class XMLWatchdog extends FileWatchdog {

    /** the xml configurator, used as a callback */
    private XMLConfigurator configurator_;

    // Logging

    /** the class name, used for the loggin facility */
    private final String className = XMLWatchdog.class.getName();

    /** Logging facility */
    private final Logger log_ = LoggerFactory.getLogger(className);

    /**
     * Instantiates a new watch dog, watching the given xml file for
     * modifications.
     * 
     * @param file
     *          the properties file name
     * @param name
     *          the name of this watchdog
     * @param configurator
     *          the xml configurator
     */
    XMLWatchdog(File file, String name, XMLConfigurator configurator)
        throws ConfigurationException {
      super(file, name);
      configurator_ = configurator;
      if (configurator_.configuration != null) {
        configurator_.configure(configuration);
      }
    }

    /**
     * Call {@link XMLConfigurator#configure(Document)} with the
     * <code>filename</code> to reconfigure log4j.
     */
    public void fileChanged(File file) {
      log_.debug("Properties file " + file + " changed");
      if (configurator_.configuration != null) {
        try {
          configurator_.configure(configuration);
        } catch (ConfigurationException e) {
          String msg = "Error executing xml configurator '" + getName() + "': ";
          Throwable reason = e.getCause();
          msg = msg + ((reason != null) ? reason.getMessage() : e.getMessage());
          log_.warn(msg);
        }
      }
    }

    /**
     * Returns the configurator's name.
     * 
     * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getName()
     */
    public String getName() {
      return configurator_.getName();
    }

    /**
     * Returns a description for this job.
     * 
     * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getDescription()
     */
    public String getDescription() {
      return "XMLWatchdog for '" + getFile() + "'";
    }
  }

}