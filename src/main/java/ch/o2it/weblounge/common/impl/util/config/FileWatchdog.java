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

import ch.o2it.weblounge.common.impl.util.Arguments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Check every now and then that a certain file has not changed. If it has, then
 * call the {@link #fileChanged(File)} method.
 */
public abstract class FileWatchdog extends Watchdog {

  /** handle to the observed file */
  private File file_;

  /** date of last modification */
  private long lastModification_ = 0;

  /** true if a warning has already been issued */
  private boolean warned_ = false;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = FileWatchdog.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a watchdog that watches the given file with the default frequency.
   * 
   * @param file
   *          the file to watch
   */
  protected FileWatchdog(File file, String name) {
    super(name);
    Arguments.checkNull(file, "file");
    file_ = file;
    try {
      if (file_.exists())
        lastModification_ = file_.lastModified();
    } catch (SecurityException e) {
      log_.warn("Was not allowed to check file existance, file:[" + file_ + "]");
    }
    warned_ = false;
  }

  /**
   * Returns the watched file.
   * 
   * @return the watched file
   */
  public File getFile() {
    return file_;
  }

  /**
   * Returns a string representation of this watchdog.
   * 
   * @return the filename
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return file_.getAbsolutePath();
  }

  /**
   * Returns a description for this job.
   * 
   * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getDescription()
   */
  public String getDescription() {
    return "FileWatchdog for '" + file_ + "'";
  }

  /**
   * Checks the observed file for changes. If changes are detected, then the
   * {@link #fileChanged(File)} method is called.
   */
  public void check() {
    boolean fileExists;
    long modified;

    // Check if file is available. If so, determine modificatin time

    try {
      fileExists = file_.exists();
      modified = file_.lastModified();
    } catch (SecurityException e) {
      log_.warn("Was not allowed to read check file existance, file:[" + file_ + "]");
      stopWatching();
      return;
    }

    if (fileExists) {
      if (modified > lastModification_) {
        lastModification_ = modified;
        fileChanged(file_);
        warned_ = false;
      }
    } else {
      if (!warned_ && isWarningEnabled()) {
        log_.warn("[" + file_ + "] has disappeared");
        warned_ = true;
      }
    }
  }

}