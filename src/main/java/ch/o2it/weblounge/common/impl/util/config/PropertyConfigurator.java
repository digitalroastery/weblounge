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

import ch.o2it.weblounge.common.impl.util.cron.Daemon;
import ch.o2it.weblounge.common.impl.util.cron.PeriodicJob;
import ch.o2it.weblounge.common.impl.util.cron.SimplePeriodicJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is used to configure a system using a common properties file. To
 * use it, simply overwrite the method {@link #configure()}.
 */
public abstract class PropertyConfigurator {

  /** the properties file */
  private File file_;

  /** the properties object */
  protected Properties properties;

  /** the key prefix */
  private String keyPrefix_;

  /** the watcher name */
  private String name_;

  /** periodic job watching the properties file */
  private PeriodicJob propertyDog_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = PropertyConfigurator.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Instantiates a property configurator from a properties file.
   * 
   * @param file
   *          the properties file
   * @param name
   *          the configurator's name
   */
  public PropertyConfigurator(File file, String name) {
    this(file, name, null);
  }

  /**
   * Instantiates a property configurator from a properties file.
   * 
   * @param file
   *          the properties file
   * @param prefix
   *          the key prefix
   */
  public PropertyConfigurator(File file, String name, String prefix) {
    file_ = file;
    name_ = name;
    keyPrefix_ = prefix;
    loadProperties();
  }

  /**
   * Like {@link #configureAndWatch(long)} except that the default delay as
   * defined by {@link FileWatchdog#DEFAULT_DELAY} is used.
   */
  public void configureAndWatch() {
    configureAndWatch(Watchdog.DEFAULT_DELAY);
  }

  /**
   * Read the configuration file <code>configFilename</code> if it exists.
   * Moreover, a thread will be created that will periodically check if
   * <code>configFilename</code> has been created or modified. The period is
   * determined by the <code>delay</code> argument. If a change or file creation
   * is detected, then <code>configFilename</code> is read to configure log4j.
   * 
   * @param delay
   *          The delay in milliseconds to wait between each check.
   */
  public void configureAndWatch(long delay) {
    log_.debug("New property configurator started [file=" + file_ + "; type=PeriodicJob]");
    propertyDog_ = new PropertyWatchdog(file_, name_, this);
    ((SimplePeriodicJob) propertyDog_).setPeriod_(delay);
    Daemon.getInstance().addJob(propertyDog_);
  }

  /**
   * Stops watching the configuration file on a regular basis.
   */
  public void stopWatching() {
    Daemon.getInstance().removeJob(propertyDog_);
  }

  /**
   * Read configuration options from <code>properties</code>. See
   * {@link #configure()} for the expected format and overwrite this method to
   * actually read the properties and configure the system.
   */
  protected abstract void configure();

  /**
   * Loads the properties file. This operation is called everytime the
   * properties file has been modified.
   */
  protected void loadProperties() {
    properties = new Properties();
    try {
      FileInputStream istream = new FileInputStream(file_);
      properties.load(istream);
      istream.close();
    } catch (IOException e) {
      log_.error("Could not read configuration file [" + file_ + "].", e);
      log_.error("Ignoring configuration file [" + file_ + "].");
      return;
    }
  }

  /**
   * Returns the suffix, completed with the prefix passed in the constructor. If
   * the prefix is <code>null</code>, then the suffix is returned unchanged.
   * 
   * @param suffix
   *          the key suffix, e. g. <tt>name</code>
   * @return the key, e. g. <tt>database.name</tt>
   */
  protected String getKey(String suffix) {
    if (keyPrefix_ == null) {
      return suffix;
    }
    return keyPrefix_ + "." + suffix;
  }

  /*
   * ------------------------------------------------------------- I N N E R C L
   * A S S PropertyWatchdog
   * -------------------------------------------------------------
   */

  class PropertyWatchdog extends FileWatchdog {

    /** the property configurator, used as a callback */
    private PropertyConfigurator configurator_;

    // Logging

    /** the class name, used for the loggin facility */
    private final String className = PropertyWatchdog.class.getName();

    /** Logging facility */
    private final Logger log_ = LoggerFactory.getLogger(className);

    /**
     * Instantiates a new watch dog, watching the given properties file for
     * modifications.
     * 
     * @param file
     *          the properties file
     * @param name
     *          the watchdog name
     * @param configurator
     *          the property configurator
     */
    PropertyWatchdog(File file, String name, PropertyConfigurator configurator) {
      super(file, name);
      configurator_ = configurator;
      configurator_.loadProperties();
      configurator_.configure();
    }

    /**
     * Call {@link PropertyConfigurator#configure()} with the
     * <code>filename</code> to reconfigure log4j.
     */
    public void fileChanged(File file) {
      log_.debug("Properties file " + file + " changed");
      configurator_.loadProperties();
      configurator_.configure();
    }

    /**
     * Returns a description for this job.
     * 
     * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getDescription()
     */
    public String getDescription() {
      return "PropertyWatchdog for '" + getFile() + "'";
    }
  }

}