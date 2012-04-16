/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.util.config;

import ch.entwine.weblounge.common.Customizable;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.site.Environment;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
public final class OptionsHelper implements Customizable {

  /** The current environment */
  protected Environment environment = Environment.Any;

  /** Options */
  private Map<String, Map<Environment, List<String>>> options = new HashMap<String, Map<Environment, List<String>>>();

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    setOption(name, value, Environment.Any);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.site.Environment)
   */
  public void setOption(String name, String value, Environment environment) {
    if (name == null)
      throw new IllegalArgumentException("Option name must not be null");
    if (value == null)
      removeOption(name);

    // Does this key exist?
    Map<Environment, List<String>> environments = options.get(name);
    if (environments == null) {
      environments = new HashMap<Environment, List<String>>();
      options.put(name, environments);
    }

    // Is there a list of values for the current environment
    List<String> values = environments.get(environment);
    if (values == null) {
      values = new ArrayList<String>();
      environments.put(environment, values);
    }

    // Add the value
    if (!values.contains(value)) {
      values.add(value);
      Collections.sort(values);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    if (name == null)
      throw new IllegalArgumentException("Option name must not be null");
    options.remove(name);
  }

  /**
   * Returns <code>true</code> if the the option with name <code>name</code> has
   * been configured.
   * 
   * @param name
   *          the option name
   * @return <code>true</code> if an option with that name exists
   * @see #getOptions()
   * @see #getOptionValue(java.lang.String)
   * @see #getOptionValue(java.lang.String, java.lang.String)
   */
  public boolean hasOption(String name) {
    Map<Environment, List<String>> values = options.get(name);
    if (values == null)
      return false;
    Collection<Environment> environments = values.keySet();
    return environments.contains(environment) || environments.contains(Environment.Any);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    List<String> keys = new ArrayList<String>();
    for (Map.Entry<String, Map<Environment, List<String>>> entry : options.entrySet()) {
      String key = entry.getKey();
      Map<Environment, List<String>> environments = entry.getValue();
      if (environments.get(environment) != null) {
        keys.add(key);
      } else if (environments.keySet().size() == 1 && environments.containsKey(Environment.Any)) {
        keys.add(key);
      }
    }
    return keys.toArray(new String[keys.size()]);
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
   * @see #getOptions()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String, java.lang.String)
   */
  public String getOptionValue(String name) {

    // Does this key exist?
    Map<Environment, List<String>> environments = options.get(name);
    if (environments == null) {
      environments = new HashMap<Environment, List<String>>();
      options.put(name, environments);
    }

    // Is there a value for the current environment?
    List<String> optionList = environments.get(environment);
    if (optionList != null && optionList.size() > 0) {
      return optionList.get(0);
    }

    // Is there a default value?
    optionList = environments.get(Environment.Any);
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
   * @see #getOptions()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    String value = getOptionValue(name);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Returns the option values for option <code>name</code> if it has been
   * configured, or an empty array otherwise.
   * 
   * @param name
   *          the option name
   * @return the option values
   * @see #getOptions()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  public String[] getOptionValues(String name) {

    // Does this key exist?
    Map<Environment, List<String>> environments = options.get(name);
    if (environments == null) {
      environments = new HashMap<Environment, List<String>>();
      options.put(name, environments);
    }

    // Is there a value for the current environment?
    List<String> optionList = environments.get(environment);
    if (optionList != null && optionList.size() > 0) {
      return optionList.toArray(new String[optionList.size()]);
    }

    // Is there a default value?
    optionList = environments.get(Environment.Any);
    if (optionList != null && optionList.size() > 0) {
      return optionList.toArray(new String[optionList.size()]);
    }

    return new String[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptions()
   */
  public Map<String, Map<Environment, List<String>>> getOptions() {
    return options;
  }

  /**
   * Initializes the options from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the options node
   * @throws IllegalStateException
   *           if the options cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static OptionsHelper fromXml(Node config) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Switches the current set of options to the given environment.
   * 
   * @param environment
   *          the environment
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Initializes the options from an XML node that was generated using
   * {@link #toXml()}. This method expects a <code>&lt;properties&gt;</code>
   * node as the input to <code>config</code>.
   * 
   * @param config
   *          the options node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the options cannot be parsed
   * @see #toXml()
   */
  public static OptionsHelper fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    OptionsHelper options = new OptionsHelper();
    fromXml(config, options, xpathProcessor);
    return options;
  }

  /**
   * Initializes the options from an XML node that was generated using
   * {@link #toXml()}. This method expects a <code>&lt;properties&gt;</code>
   * node as the input to <code>config</code>.
   * 
   * @param config
   *          the options node
   * @param customizable
   *          the customizable object
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the options cannot be parsed
   * @see #toXml()
   */
  public static void fromXml(Node config, Customizable customizable,
      XPath xpathProcessor) throws IllegalStateException {

    if (config == null)
      return;

    // Read the options
    NodeList nodes = XPathHelper.selectList(config, "ns:option", xpathProcessor);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String name = XPathHelper.valueOf(option, "ns:name", xpathProcessor);
      NodeList valueNodes = XPathHelper.selectList(option, "ns:value", xpathProcessor);

      for (int j = 0; j < valueNodes.getLength(); j++) {

        String env = XPathHelper.valueOf(valueNodes.item(j), "@environment");
        Environment environment = Environment.Any;
        if (StringUtils.isNotBlank(env)) {
          try {
            environment = Environment.valueOf(StringUtils.capitalize(env));
          } catch (Throwable t) {
            throw new IllegalStateException("Environment '" + env + "' is unknown");
          }
        }

        String value = valueNodes.item(j).getFirstChild().getNodeValue();
        customizable.setOption(name, value, environment);
      }

    }
  }

  /**
   * Returns an <code>XML</code> representation of the options, that will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;options&gt;
   *   &lt;option&gt;
   *     &lt;name&gt;key&lt;/name&gt;
   *     &lt;value&gt;value&lt;/value&gt;
   *   &lt;/option&gt;
   *   &lt;option&gt;
   *     &lt;name&gt;multikey&lt;/name&gt;
   *     &lt;value&gt;value&lt;/value&gt;
   *     &lt;value&gt;othervalue&lt;/value&gt;
   *   &lt;/option&gt;
   * &lt;/options&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>ActionConfiguration</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the options
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    if (options.size() == 0)
      return b.toString();

    b.append("<options>");
    List<String> keys = new ArrayList<String>();
    keys.addAll(options.keySet());
    Collections.sort(keys);
    for (String key : keys) {
      b.append("<option>");
      b.append("<name>").append(key).append("</name>");

      Map<Environment, List<String>> valuesByEnvironment = options.get(key);
      List<Environment> environments = new ArrayList<Environment>();
      environments.addAll(valuesByEnvironment.keySet());
      Collections.sort(environments, new Comparator<Environment>() {
        public int compare(Environment envA, Environment envB) {
          return envA.toString().compareTo(envB.toString());
        }
      });

      for (Environment environment : environments) {
        List<String> values = valuesByEnvironment.get(environment);
        Collections.sort(values);
        for (String value : values) {
          b.append("<value");
          if (!environment.equals(Environment.Any)) {
            b.append(" environment=\"").append(environment.toString().toLowerCase()).append("\"");
          }
          b.append(">");
          b.append("<![CDATA[").append(value).append("]]></value>");
        }
      }

      b.append("</option>");
    }

    b.append("</options>");

    return b.toString();
  }

}