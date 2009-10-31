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

package ch.o2it.weblounge.common.impl.util;

import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * This class provides necessary environmental settings for the weblounge
 * system, including various application paths and other system settings.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class Env {

  /** Properties containing the environmental settings */
  protected static Properties properties = new Properties();

  /** Servlet context */
  public static ServletContext servletContext;

  /** Servlet configuration */
  public static ServletConfig servletConfig;

  /** Webapp physical base path */
  public static String basePath_;

  /** Weblounge mountpoint: /uri/servletpath */
  public static String mountpoint_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = Env.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * This constructor is private since <code>Environment</code> is a completely
   * static class and should not be instantiated.
   */
  private Env() {
  }

  /**
   * Initializes the system context. This method is called at initialization
   * time and is public as an implementation side effect.
   * 
   * @param config
   *          the servlet configuration
   */
  public static void init(ServletConfig config) {
    servletConfig = config;
    servletContext = config.getServletContext();
  }

  /**
   * Returns the requested property or <code>null</code> if the property doesn't
   * exist.
   * 
   * @param name
   *          the property name
   * @return the property value
   * @see #get(java.lang.String, java.lang.String)
   * @see #set(java.lang.String, java.lang.String)
   */
  public static String get(String name) {
    return properties.getProperty(name);
  }

  /**
   * Returns the requested property or <code>defaultValue</code> if the property
   * doesn't exist.
   * 
   * @param name
   *          the property name
   * @param defaultValue
   *          the property default value
   * @return the property value
   * @see #get(java.lang.String)
   * @see #set(java.lang.String, java.lang.String)
   */
  public static String get(String name, String defaultValue) {
    Arguments.checkNull(name, "name");
    String value = properties.getProperty(name);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Sets the property value and returns, if available, the old value of this
   * property. If no old value is available, <code>null</code> is returned.
   * 
   * @param name
   *          property name
   * @param value
   *          new property value
   * @return the old property value or <code>null</code>
   * @see #get(java.lang.String)
   * @see #get(java.lang.String, java.lang.String)
   */
  public static String set(String name, String value) {
    if (value == null) {
      log_.warn("Tried to store 'null' in environment property '" + name + "'!");
      return null;
    }
    String oldValue = (String) properties.get("property");
    properties.setProperty(name, value);
    return oldValue;
  }

  /**
   * Returns the real path on the server for a given virtual path.
   * 
   * @param path
   *          the virtual (webapp-relative) path
   * @return the real (physical) path on the server
   */
  public static String getRealPath(String path) {
    if (basePath_ == null) {
      File url = new File(servletContext.getRealPath("/"));
      basePath_ = url.toString();
    }
    return PathSupport.concat(basePath_, path);
  }

  /**
   * Returns the path that was used to install the whole webapplication.
   * 
   * @return the path to the webapplication
   */
  public static String getURI() {
    return properties.getProperty("system.uri");
  }

  /**
   * Returns the path to the webapp's main servlet relative to the install path.
   * 
   * @return the relative path to the main servlet
   */
  public static String getServletPath() {
    return properties.getProperty("system.servletpath");
  }

  /**
   * Returns the prefix to the weblounge main servlet, which is a concatenation
   * of uri and servlet path.
   * 
   * @return the relative path to the main servlet
   */
  public static String getMountpoint() {
    if (mountpoint_ == null)
      mountpoint_ = UrlSupport.concat(getURI(), getServletPath());
    return mountpoint_;
  }

  /**
   * Returns a request dispatcher, suitable for this web application.
   * 
   * @return a request dispatcher
   */
  public static RequestDispatcher getRequestDispatcher(String url) {
    return servletContext.getRequestDispatcher(url);
  }

  /**
   * Dumps the current environment to <code>System.out</code>.
   */
  public static void dump() {
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      log_.debug(key + " : " + properties.getProperty(key));
    }
  }

  /**
   * Returns the MIME type of a file.
   * 
   * @param file
   *          the name of the file
   * @return the MIME type of the file
   */
  public static String getMimeType(String file) {
    String mime = servletContext.getMimeType(file);
    return (mime == null) ? "unknown" : mime;
  }

}