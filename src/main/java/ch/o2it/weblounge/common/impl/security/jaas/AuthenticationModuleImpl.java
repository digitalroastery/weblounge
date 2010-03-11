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

package ch.o2it.weblounge.common.impl.security.jaas;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.AuthenticationModule;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

import javax.security.auth.spi.LoginModule;
import javax.xml.xpath.XPath;

/**
 * This class is used to wrap information on JAAS login modules read from the
 * site configuration file.
 */
public final class AuthenticationModuleImpl implements AuthenticationModule {

  /** The login module's class name */
  protected Class<? extends LoginModule> prototype = null;

  /** The module's relevance */
  protected Relevance relevance = null;

  /** Module configuration */
  protected OptionsHelper configuration = null;
  
  /**
   * Creates a new authentication module and throws various exceptions while
   * trying to read in the module configuration.
   */
  public AuthenticationModuleImpl(XPath path, Node config)
      throws ConfigurationException {
    init(path, config);
  }

  /**
   * Creates an authentication module definition from scratch.
   * 
   * @param classname
   *          the implementing class name
   * @param relevance
   *          the relevance
   */
  public AuthenticationModuleImpl(String classname, String relevance) {
    setClass(classname);
    setRelevance(relevance);
    configuration = new OptionsHelper();
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticationModule#getModuleClass()
   */
  public Class<? extends LoginModule> getModuleClass() {
    return prototype;
  }

  /**
   * Sets the class name of the implementing class.
   * 
   * @param className
   *          the class name
   */
  @SuppressWarnings("unchecked")
  private void setClass(String className) {
    if (className == null) {
      throw new ConfigurationException("Login module does not specify implementation!");
    }
    try {
      Class<LoginModule> c = (Class<LoginModule>)Class.forName(className);
      Object o = c.newInstance();
      if (!(o instanceof LoginModule)) {
        String msg = "Class " + className + " does not implement the interface javax.security.auth.spi.LoginModule!";
        throw new ConfigurationException(msg);
      }
      this.prototype = c;
    } catch (ClassCastException e) {
      String msg = "Configured class " + className + " is not of type LoginModule";
      throw new ConfigurationException(msg, e);
    } catch (InstantiationException e) {
      String msg = "Unable to instantiate login module " + className;
      throw new ConfigurationException(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Access violation while instantiating login module " + className;
      throw new ConfigurationException(msg, e);
    } catch (NoClassDefFoundError e) {
      String msg = "Class '" + e.getMessage() + "' which is required by login module class '" + className + "' was not found";
      throw new ConfigurationException(msg, e);
    } catch (ClassNotFoundException e) {
      String msg = "Login module class " + className + " was not found";
      throw new ConfigurationException(msg, e);
    }
  }

  /**
   * Sets the relevance value.
   * 
   * @param relevance
   *          the relevance
   */
  private void setRelevance(String relevance) {
    if (relevance == null) {
      String msg = "Relevance value of login module " + prototype.getName() + " is null!";
      throw new ConfigurationException(msg);
    }
    relevance = relevance.toLowerCase();
    try {
      this.relevance = Relevance.valueOf(relevance);
    } catch (IllegalArgumentException e) {
      String msg = "Unknown relevance value for login module " + prototype.getName() + ": " + relevance;
      throw new ConfigurationException(msg);
    }
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticationModule#getRelevance()
   */
  public Relevance getRelevance() {
    return relevance;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.Customizable#setOption(java.lang.String, java.lang.String)
   */
  public void setOption(String name, String value) {
    configuration.setOption(name, value);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    configuration.removeOption(name);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.security.AuthenticationModule#getOptions()
   */
  public Map<String, List<String>> getOptions() {
    return configuration.getOptions();
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return configuration.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String, java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return configuration.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return configuration.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return configuration.hasOption(name);
  }

  /**
   * Reads the login module configuration.
   * 
   * @param config
   *          the configuration node
   * @param xpathProcessor
   *          the XPath object used to parse the configuration
   */
  public void init(XPath xpathProcessor, Node config) throws ConfigurationException {
    setClass(XPathHelper.valueOf(config, "@class", xpathProcessor));
    setRelevance(XPathHelper.valueOf(config, "@relevance", xpathProcessor));
    configuration = OptionsHelper.fromXml(config, xpathProcessor);
  }

}