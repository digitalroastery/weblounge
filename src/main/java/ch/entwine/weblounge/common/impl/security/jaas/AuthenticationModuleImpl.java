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

package ch.entwine.weblounge.common.impl.security.jaas;

import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.security.AuthenticationModule;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class is used to wrap information on JAAS login modules read from the
 * site configuration file.
 */
public final class AuthenticationModuleImpl implements AuthenticationModule {

  /** The login module's class name */
  protected String moduleClass = null;

  /** The module's relevance */
  protected Relevance relevance = null;

  /** Module configuration */
  protected OptionsHelper options = null;

  /**
   * Creates an authentication module definition from scratch.
   * 
   * @param moduleClass
   *          the implementation class name
   * @param relevance
   *          the relevance
   */
  public AuthenticationModuleImpl(String moduleClass, Relevance relevance) {
    if (moduleClass == null)
      throw new IllegalArgumentException("Implementation class cannot be null");
    if (relevance == null)
      throw new IllegalArgumentException("Relevance cannot be null");
    this.moduleClass = moduleClass;
    this.relevance = relevance;
    options = new OptionsHelper();
  }

  /**
   * @see ch.entwine.weblounge.common.security.AuthenticationModule#getModuleClass()
   */
  public String getModuleClass() {
    return moduleClass;
  }

  /**
   * @see ch.entwine.weblounge.common.security.AuthenticationModule#getRelevance()
   */
  public Relevance getRelevance() {
    return relevance;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    options.setOption(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.AuthenticationModule#getOptions()
   */
  public Map<String, List<String>> getOptions() {
    return options.getOptions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return moduleClass.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AuthenticationModule) {
      AuthenticationModule m = (AuthenticationModule)obj;
      if (!moduleClass.equals(m.getModuleClass()))
        return false;
      if (!relevance.equals(m.getRelevance()))
        return false;
      return true;
    }
    return false;
  }

  /**
   * Initializes this authentication module from an XML node that was generated
   * using {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the authentication module node
   * @throws IllegalStateException
   *           if the authentication module configuration cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static AuthenticationModule fromXml(Node config)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Initializes this authentication module from an XML node that was generated
   * using {@link #toXml()}.
   * 
   * @param config
   *          the authentication module node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the authentication module configuration cannot be parsed
   * @see #toXml()
   */
  public static AuthenticationModule fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    // class
    String moduleClassName = XPathHelper.valueOf(config, "ns:class", xpathProcessor);
    if (moduleClassName == null)
      throw new IllegalStateException("Login module must have an implementation class");

    // relevance
    String relevanceValue = XPathHelper.valueOf(config, "ns:relevance", xpathProcessor);
    if (relevanceValue == null)
      throw new IllegalStateException("Login module must have a relevance");
    Relevance relevance = null;
    try {
      relevance = Relevance.valueOf(relevanceValue.toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Unknown relevance value for login module: " + relevance);
    }
    
    AuthenticationModuleImpl module = new AuthenticationModuleImpl(moduleClassName, relevance);

    // options
    Node optionsNode = XPathHelper.select(config, "ns:options", xpathProcessor);
    module.options = OptionsHelper.fromXml(optionsNode, xpathProcessor);
    
    return module;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AuthenticationModule#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<loginmodule>");

    // class
    b.append("<class>").append(moduleClass).append("</class>");

    // relevance
    b.append("<relevance>").append(relevance.toString()).append("</relevance>");

    // Options
    b.append(options.toXml());

    b.append("</loginmodule>");

    return b.toString();
  }

}