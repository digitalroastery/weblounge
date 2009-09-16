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

import javax.xml.xpath.XPath;

/**
 * This class contains the mapping information for a servlet.
 * 
 * @author Tobias Wunden
 */
public class ServletMapping {

  /** The servlet - url mapping */
  private URLMapping mapping;

  /** The servlet name */
  private String servletName;

  /**
   * Creates a new servlet mapping.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   */
  public ServletMapping(XPath path, Node config) throws ConfigurationException {
    init(path, config);
  }

  /**
   * Returns the servlet name;
   * 
   * @return the servlet name
   */
  public String getName() {
    return servletName;
  }

  /**
   * Returns the url mapping for the servlet.
   * 
   * @return the mapping
   */
  public URLMapping getMapping() {
    return mapping;
  }

  /**
   * Configures the servlet mapping.
   * 
   * @param servletMapping
   *          the mapping
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void init(XPath path, Node servletMapping)
      throws ConfigurationException {
    servletName = XPathHelper.valueOf(path, servletMapping, "servlet-name");
    if (servletName == null || servletName.length() == 0)
      throw new ConfigurationException("servlet-name in mapping definition is invalid");
    String urlPattern = XPathHelper.valueOf(path, servletMapping, "url-pattern");
    if (urlPattern == null || urlPattern.length() == 0)
      throw new ConfigurationException(servletName + ": url-pattern in mapping definition is invalid");
    mapping = new URLMapping(urlPattern);
  }

}