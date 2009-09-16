/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

/**
 * This interface provides logging functionality for site related objects.
 */
public interface SiteLogger {

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>TRACE</code>.
   * 
   * @param msg
   *          the log message
   */
  void trace(String msg);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>TRACE</code>. The log will contain the message as well as the stack
   * trace provided by <code>t</code>.
   * 
   * @param msg
   *          the log message
   * @param t
   *          the throwable causing the log entry
   */
  void trace(String msg, Throwable t);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>DEBUG</code>.
   * 
   * @param msg
   *          the log message
   */
  void debug(String msg);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>DEBUG</code>. The log will contain the message as well as the stack
   * trace provided by <code>t</code>.
   * 
   * @param msg
   *          the log message
   * @param t
   *          the throwable causing the log entry
   */
  void debug(String msg, Throwable t);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>INFO</code>.
   * 
   * @param msg
   *          the log message
   */
  void info(String msg);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>INFO</code>. The log will contain the message as well as the stack
   * trace provided by <code>t</code>.
   * 
   * @param msg
   *          the log message
   * @param t
   *          the throwable causing the log entry
   */
  void info(String msg, Throwable t);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>WARN</code>.
   * 
   * @param msg
   *          the log message
   */
  void warn(String msg);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>WARN</code>. The log will contain the message as well as the stack
   * trace provided by <code>t</code>.
   * 
   * @param msg
   *          the log message
   * @param t
   *          the throwable causing the log entry
   */
  void warn(String msg, Throwable t);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>ERROR</code>.
   * 
   * @param msg
   *          the log message
   */
  void error(String msg);

  /**
   * Logs the given message to the site's logging facility using the log level
   * <code>ERROR</code>. The log will contain the message as well as the stack
   * trace provided by <code>t</code>.
   * 
   * @param msg
   *          the log message
   * @param t
   *          the throwable causing the log entry
   */
  void error(String msg, Throwable t);

}