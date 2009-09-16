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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Properties;

import javax.xml.xpath.XPath;

/**
 * This class holds the configuration data for a servlet.
 * 
 * @author Tobias Wunden
 */
public class ServletConfiguration {

  /** The servlet name */
  private String name;

  /** The servlet implementation */
  private String classname;

  /** The init params */
  private Properties params;

  /**
   * Creates a new servlet configuration.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   *           if the configuration failed
   */
  public ServletConfiguration(XPath path, Node config)
      throws ConfigurationException {
    init(path, config);
  }

  /**
   * Returns the servlet name;
   * 
   * @return the servlet name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the classname.
   * 
   * @return the class
   */
  public String getClassname() {
    return classname;
  }

  /**
   * Returns the init parameters.
   * 
   * @return the parameters
   */
  public Properties getInitParameters() {
    return params;
  }

  /**
   * Configures the servlet.
   * 
   * @param config
   *          the config node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void init(XPath path, Node config) {
    name = XPathHelper.valueOf(path, config, "servlet-name");
    if (name == null || name.length() == 0)
      throw new ConfigurationException("servlet-name is invalid");
    classname = XPathHelper.valueOf(path, config, "servlet-class");
    if (classname == null || classname.length() == 0)
      throw new ConfigurationException(name + ": servlet-class is invalid");
    params = new Properties();
    NodeList nodes = XPathHelper.selectList(path, config, "init-param");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node initParam = nodes.item(i);
      String paramName = XPathHelper.valueOf(path, initParam, "param-name");
      if (paramName == null || paramName.length() == 0)
        throw new ConfigurationException(name + ": param-name is invalid");
      String paramValue = XPathHelper.valueOf(path, initParam, "param-value");
      if (paramValue == null || paramValue.length() == 0)
        throw new ConfigurationException(name + ": param-value for '" + paramName + "' is invalid");
      params.setProperty(paramName, paramValue);
    }
  }

}