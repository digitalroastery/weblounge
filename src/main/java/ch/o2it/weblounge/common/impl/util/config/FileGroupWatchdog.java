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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Check every now and then that a group of files has not changed. If it has,
 * the {@link #fileChanged(File)} method is called.
 */
public abstract class FileGroupWatchdog extends Watchdog {

  /** handle to the list of observed files */
  private List<File> files_;

  /** list of warnings */
  private List<Boolean> warnings_;

  /** date of last modification */
  private Map<File, Long> lastModifications_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = FileGroupWatchdog.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a watchdog with no files to watch. Use {@link #addFile(String)} to
   * add files to the group of watched files.
   */
  protected FileGroupWatchdog(String name) {
    this(new String[] {}, name);
  }

  /**
   * Creates a watchdog that watches the given file with the default delay.
   * 
   * @param filenames
   *          the files to watch
   * @param name
   *          the group name
   */
  protected FileGroupWatchdog(String filenames[], String name) {
    super(name);
    files_ = new ArrayList<File>();
    lastModifications_ = new HashMap<File, Long>();
    warnings_ = new ArrayList<Boolean>();
    for (int i = 0; i < filenames.length; i++) {
      addFile(filenames[i]);
    }
  }

  /**
   * Adds a new file to the list of observed files.
   * 
   * @param file
   *          the file to be watched
   */
  public void addFile(String file) {
    File f = new File(file);
    files_.add(f);
    warnings_.add(new Boolean(false));
    try {
      if (f.exists())
        lastModifications_.put(f, new Long(f.lastModified()));
    } catch (SecurityException e) {
      log_.warn("Was not allowed to read file existance [file=" + file + "]");
    }
  }

  /**
   * Returns the number of files that are currently being watched.
   * 
   * @return the number of watched files
   */
  public int size() {
    return files_.size();
  }

  /**
   * Returns a string representation of this watchdog.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }

  /**
   * Checks the observed file for changes. If changes are detected, then the
   * {@link #fileChanged(File)} method is called.
   */
  public void check() {
    boolean fileExists;
    long modified;

    // Check if file is available. If so, determine modificatin time

    log_.debug("Watchdog " + toString() + " is checking " + files_.size() + " files");
    Iterator i = files_.iterator();
    int j = 0;
    while (i.hasNext()) {
      File file = (File) (i.next());
      try {
        fileExists = file.exists();
        modified = file.lastModified();
      } catch (SecurityException e) {
        log_.warn("Was not allowed to read file existance [file=" + file + "]");
        return;
      }

      boolean warned = warnings_.get(j).booleanValue();
      Long lastModification = lastModifications_.get(file);
      if (fileExists) {
        if (lastModification != null) {
          if (modified > lastModification.longValue()) {
            lastModifications_.put(file, new Long(modified));
            warnings_.set(j, new Boolean(false));
            fileChanged(file);
          }
        } else {
          lastModifications_.put(file, new Long(modified));
        }
      } else {
        if (!warned && isWarningEnabled()) {
          log_.warn("[" + file + "] does not exist.");
          warnings_.set(j, new Boolean(true));
        }
      }
      j++;
    }
  }

}