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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
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

  /** Options */
  private Map<String, List<String>> options = new HashMap<String, List<String>>();

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    if (name == null)
      throw new IllegalArgumentException("Option name must not be null");
    if (value == null)
      removeOption(name);
    List<String> values = options.get(name);
    if (values == null) {
      values = new ArrayList<String>();
      options.put(name, values);
    }
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
    return (options.keySet().contains(name));
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
   * configured, or an empty list otherwise.
   * 
   * @param name
   *          the option name
   * @return the option values
   * @see #getOptions()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    List<String> optionList = options.get(name);
    if (optionList != null) {
      return optionList.toArray(new String[optionList.size()]);
    }
    return new String[] {};
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
        String value = valueNodes.item(j).getFirstChild().getNodeValue();
        customizable.setOption(name, value);
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
      List<String> values = options.get(key);
      Collections.sort(values);
      for (String value : values)
        b.append("<value><![CDATA[").append(value).append("]]></value>");
      b.append("</option>");
    }

    b.append("</options>");

    return b.toString();
  }

}