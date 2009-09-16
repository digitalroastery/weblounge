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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * This class may be used as a basis for configuration objects containing a
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
public class ConfigurationBase implements Customizable {

  /** Job options */
  protected Map<String, List<String>> options = new HashMap<String, List<String>>();

  /**
   * Returns an iteration of all available option names.
   * 
   * @return the available option names
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public Iterator<String> options() {
    return options.keySet().iterator();
  }

  /**
   * Returns <code>true</code> if the the option with name <code>name</code> has
   * been configured.
   * 
   * @param name
   *          the option name
   * @return <code>true</code> if an option with that name exists
   * @see #options()
   * @see #getOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public boolean hasOption(String name) {
    return (options.keySet().contains(name));
  }

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multivalue option (that is, if the option has been
   * configured multiple times), this method returns the first value onyl. Use
   * {@link #getOptions(java.lang.String)} to get all option values.
   * 
   * @param name
   *          the option name
   * @return the option value
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public String getOption(String name) {
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
   * @see #getOption(java.lang.String)
   */
  public String getOption(String name, String defaultValue) {
    String value = getOption(name);
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
   * @see #getOption(java.lang.String)
   */
  public String[] getOptions(String name) {
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
  public Map<String, List<String>> getOptions() {
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
  public void init(XPath path, Node config) throws ConfigurationException {
    readOptions(path, config);
  }

  /**
   * Reads the options from the configuration.
   * 
   * @param config
   *          configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  protected void readOptions(XPath path, Node config) {
    if (path == null || config == null)
      return;
    NodeList nodes = XPathHelper.selectList(path, config, "options/option");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String name = XPathHelper.valueOf(path, option, "name");
      String value = XPathHelper.valueOf(path, option, "value");
      List<String> values = options.get(name);
      if (values != null) {
        values.add(value);
      } else {
        values = new ArrayList<String>();
        values.add(value);
        options.put(name, values);
      }
    }
  }

}