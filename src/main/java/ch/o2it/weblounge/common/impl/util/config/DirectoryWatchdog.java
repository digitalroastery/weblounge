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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This watchdog wathes the contents of a given directory for changes. Concrete
 * subclasses must implement {@link #fileChanged(File)} to be notified of
 * changed files.
 */
public abstract class DirectoryWatchdog extends Watchdog {

  /** handle to the observed directory */
  protected File directory;

  /** The name of the file to observe for changes */
  private String path_;

  /** the path - timestamp mapping */
  private Map<String, Long> modified_;

  /** true if a warning has already been issued */
  private boolean warned_;

  /** True if the watchdog should also watch subdirectories and their content */
  private boolean recurse_;

  /** Current content, used to detect removed files */
  private List<String> content_;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = DirectoryWatchdog.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a watchdog that watches the contents of the given directory with
   * the default delay. Note that only files with registered extensions are
   * watched.
   * 
   * @param directory
   *          the directory to watch
   * @param name
   *          name for the watchdog
   * @param recurse
   *          <code>true</code> if the watchdog should also watch subdirectories
   */
  protected DirectoryWatchdog(File directory, String name, boolean recurse) {
    super(name);
    Arguments.checkNull(directory, "directory");
    this.directory = directory;
    path_ = directory.getAbsolutePath();
    warned_ = false;
    modified_ = new HashMap<String, Long>();
    recurse_ = recurse;
    content_ = new ArrayList<String>();
  }

  /**
   * Returns the watched directory.
   * 
   * @return the watched directory
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Returns the path of the watched directory.
   * 
   * @return the watched file's name
   */
  public String getDirectoryPath() {
    if (directory != null)
      return directory.getAbsolutePath();
    return path_;
  }

  /**
   * Returns a string representation of this watchdog.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return path_;
  }

  /**
   * Returns a description for this watchdog.
   * 
   * @return the watchdog description
   * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getDescription()
   */
  public String getDescription() {
    return "DirectoryWatchdog for '" + path_ + "'";
  }

  /**
   * Abstract method which needs to be implemented by the concrete subclass. The
   * method is called everytime the a new file is detected either in the base
   * directory or withing its directory subtree.
   * 
   * @param file
   *          the file that appeared
   */
  abstract protected void fileAppeared(File file);

  /**
   * Abstract method which needs to be implemented by the concrete subclass. The
   * method is called everytime a file is removed from the base directory or its
   * directory subtree.
   * 
   * @param file
   *          file that disappeared
   */
  abstract protected void fileDisappeared(File file);

  /**
   * Abstract method which needs to be implemented by the concrete subclass. The
   * method is called everytime a file changed within the base directory or its
   * directory subtree.
   * 
   * @param file
   *          the file that changed
   */
  abstract protected void fileChanged(File file);

  /**
   * Checks the observed file for changes. If changes are detected, then the
   * {@link #fileChanged(File)} method is called.
   */
  public void check() {
    boolean directoryExists;
    try {
      directoryExists = directory.exists();
    } catch (Exception e) {
      log_.warn("Unable to check file existance of directory [" + path_ + "].");
      stopWatching();
      return;
    }

    if (directoryExists) {
      Stack<String> dirs = new Stack<String>();
      dirs.push(directory.getAbsolutePath());

      while (!dirs.empty()) {

        // get the current directory

        String currentDir = dirs.pop();
        File dir = new File(currentDir);
        File[] files = dir.listFiles();
        if (files == null) {
          String msg = dir + " is either not a directory or cannot be read!";
          log_.warn(msg);
          continue;
        }

        // process directory contents

        for (int i = 0; i < files.length; i++) {

          File file = files[i];
          String filename = file.getAbsolutePath();

          // if file is directory, push it onto the stack
          // for later processing of it's contents

          if (file.isDirectory() && recurse_) {
            dirs.push(filename);
          }

          // file is a regular file. See if we already had a look
          // at it and if so, compare the modification dates

          else {
            Long modified = modified_.get(filename);
            if (modified == null) {
              log_.debug("New file detected [file=" + filename + "]");
              content_.add(filename);
              fileAppeared(file);
            } else if (file.lastModified() > modified.longValue()) {
              log_.debug("Modified file detected [file=" + filename + "]");
              fileChanged(file);
            }
            modified_.put(filename, new Long(file.lastModified()));
          }
        }
      }

      // Check for removed files and call the appropriate
      // method for each of them.

      content_.removeAll(modified_.keySet());
      Iterator removed = content_.iterator();
      while (removed.hasNext()) {
        String file = (String) removed.next();
        modified_.remove(file);
        fileDisappeared(new File(file));
      }

      // Prepare for next check of removed files

      content_.clear();
      content_.addAll(modified_.keySet());

      warned_ = false;
    } else {
      if (!warned_ && isWarningEnabled()) {
        log_.warn("[" + path_ + "] does not exist.");
        warned_ = true;
      }
    }
  }

}