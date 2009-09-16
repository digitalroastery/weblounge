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

package ch.o2it.weblounge.common.impl.security.jaas;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationBase;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.AuthenticationModule;

import org.w3c.dom.Node;

import javax.security.auth.spi.LoginModule;
import javax.xml.xpath.XPath;

/**
 * This class is used to wrap information on JAAS login modules read from the
 * site configuration file.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public final class AuthenticationModuleImpl extends ConfigurationBase implements AuthenticationModule {

  /** The login module's class name */
  private String className_ = null;

  /** The module's relevance */
  private String relevance_ = null;

  /**
   * Creates a new authentication module and throws variuos exceptions while
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
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticationModule#getModuleClass()
   */
  public String getModuleClass() {
    return className_;
  }

  /**
   * Sets the class name of the implementing class.
   * 
   * @param className_
   *          the class name
   */
  private void setClass(String className) {
    if (className == null) {
      throw new ConfigurationException("Login module does not specify implementation!");
    }
    try {
      Class c = Class.forName(className);
      Object o = c.newInstance();
      if (!(o instanceof LoginModule)) {
        String msg = "Class " + className + " does not implement the interface javax.security.auth.spi.LoginModule!";
        throw new ConfigurationException(msg);
      }
      this.className_ = className;
    } catch (InstantiationException e) {
      String msg = "Unable to instantiate login module.implementation " + className;
      throw new ConfigurationException(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Access violation while instantiating login module.implementation " + className;
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
      String msg = "Relevance value of login module " + className_ + " is null!";
      throw new ConfigurationException(msg);
    }
    relevance = relevance.toLowerCase();
    if (!relevance.equals(LoginContextImpl.RELVANCE_REQUIRED) && !relevance.equals(LoginContextImpl.RELVANCE_REQUISITE) && !relevance.equals(LoginContextImpl.RELVANCE_SUFFICIENT) && !relevance.equals(LoginContextImpl.RELVANCE_OPTIONAL)) {
      String msg = "Unknown relevance value for login module " + className_ + ": " + relevance;
      throw new ConfigurationException(msg);
    }
    this.relevance_ = relevance;
  }

  /**
   * @see ch.o2it.weblounge.common.security.AuthenticationModule#getRelevance()
   */
  public String getRelevance() {
    return relevance_;
  }

  /**
   * Reads the login module configuration.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  public void init(XPath path, Node config) throws ConfigurationException {
    setClass(XPathHelper.valueOf(path, config, "@class"));
    setRelevance(XPathHelper.valueOf(path, config, "@relevance"));
    super.init(path, config);
  }

}