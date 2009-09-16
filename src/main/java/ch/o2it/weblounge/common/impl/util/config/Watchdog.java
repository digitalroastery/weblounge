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
import ch.o2it.weblounge.common.impl.util.cron.SimplePeriodicJob;

import java.io.File;

/**
 * Check every now and then that a certain file has not changed. If it has, then
 * call the {@link #fileChanged} method.
 */
public abstract class Watchdog extends SimplePeriodicJob {

  /**
   * The default delay between every file modification check, set to 60 seconds.
   */
  public final static long DEFAULT_DELAY = 10000;

  /** true if a warning has already been issued */
  private boolean warn_ = false;

  /** Running flag */
  private boolean isWatching_;

  /**
   * Creates a watchdog that watches the given file with the default delay.
   * 
   * @param name
   *          the file to watch
   */
  protected Watchdog(String name) {
    super(name, DEFAULT_DELAY);
    warn_ = true;
    isWatching_ = false;
  }

  /**
   * Enables or disables warnings in case the observed object is not found.
   * 
   * @param enable
   *          <code>true</code> if warnings should be issued
   */
  public void setWarningsEnabled(boolean enable) {
    warn_ = enable;
  }

  /**
   * Returns <code>true</code> if warnings concerning the presence of observed
   * objects should be issued.
   * 
   * @return <code>true</code> if warnings should be issued
   */
  public boolean isWarningEnabled() {
    return warn_;
  }

  /**
   * Main method that keeps checking the file.
   * 
   * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#work()
   */
  public void work() {
    check();
  }

  /**
   * Starts the watchdog.
   */
  public void startWatching() {
    Daemon.getInstance().addJob(this);
    isWatching_ = true;
  }

  /**
   * Starts the watchdog.
   */
  public boolean isWatching() {
    return isWatching_;
  }

  /**
   * Stops the watchdog.
   */
  public void stopWatching() {
    Daemon.getInstance().removeJob(this);
    isWatching_ = false;
  }

  /**
   * Checks the observed objects for changes. If changes are detected, then the
   * {@link #fileChanged(File)} method is called. Since this watchdog is
   * intended to be used on arbitrary objects, this method has to be implemented
   * by the concrete watchdog.
   */
  public abstract void check();

  /**
   * Abstract method which needs to be implemented by the concrete subclass. The
   * method is called everytime the observed file changes.
   * 
   * @param file
   *          the file that changed
   */
  abstract protected void fileChanged(File file);

}