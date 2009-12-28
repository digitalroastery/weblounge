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

package ch.o2it.weblounge.common.impl.util.config;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * This class may be used as a utility to hold configuration data of the
 * following kind:
 * 
 * <pre>
 * 	&lt;options&gt;
 *    &lt;option&gt;
 *      &lt;name&gt;name&lt;/name&gt;
 *      &lt;value&gt;value&lt;/value&gt;
 *    &lt;/option&gt;
 *  &lt;/options&gt;
 * </pre>
 */
public class Options implements Customizable {

  /** Options */
  protected Map<String, List<String>> options = new HashMap<String, List<String>>();

  /**
   * Returns <code>true</code> if the the option with name <code>name</code> has
   * been configured.
   * 
   * @param name
   *          the option name
   * @return <code>true</code> if an option with that name exists
   * @see #options()
   * @see #getOptionValue(java.lang.String)
   * @see #getOptionValue(java.lang.String, java.lang.String)
   */
  public boolean hasOption(String name) {
    return (options.keySet().contains(name));
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    return options.keySet().toArray(new String[options.size()]);
  }
  
  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multi value option (that is, if the option has been
   * configured multiple times), this method returns the first value only. Use
   * {@link #getOptionValues(java.lang.String)} to get all option values.
   * 
   * @param name
   *          the option name
   * @return the option value
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String, java.lang.String)
   */
  public String getOptionValue(String name) {
    List<String> optionList = options.get(name);
    if (optionList != null && optionList.size() > 0) {
      return optionList.get(0);
    }
    return null;
  }

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>defaultValue</code> otherwise.
   * 
   * @param name
   *          the option name
   * @param defaultValue
   *          the default value
   * @return the option value
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    String value = getOptionValue(name);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Returns the option values for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * 
   * @param name
   *          the option name
   * @return the option values
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    List<String> optionList = options.get(name);
    if (optionList != null) {
      return optionList.toArray(new String[] {});
    }
    return null;
  }

  /**
   * Returns the options as a <code>Map</code>.
   * 
   * @return the options
   */
  public Map<String, List<String>> options() {
    return options;
  }

  /**
   * Initializes this object from an xml configuration node.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   *           if the configuration data is incomplete or invalid
   */
  public static Options load(XPath path, Node config)
      throws ConfigurationException {
    Options configurationBase = new Options();

    // No options available?
    if (path == null || config == null)
      return null;

    // Read the options
    NodeList nodes = XPathHelper.selectList(config, "options/option", path);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String name = XPathHelper.valueOf(option, "name", path);
      String value = XPathHelper.valueOf(option, "value", path);
      List<String> values = configurationBase.options.get(name);
      if (values != null) {
        values.add(value);
      } else {
        values = new ArrayList<String>();
        values.add(value);
        configurationBase.options.put(name, values);
      }
    }
    return configurationBase;
  }

}