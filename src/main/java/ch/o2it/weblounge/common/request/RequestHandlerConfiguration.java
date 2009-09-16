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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.Customizable;

import java.util.Iterator;

/**
 * TODO Comment RequestHandlerConfiguration
 */
public interface RequestHandlerConfiguration extends Customizable {

  /**
   * Returns the handler identifier, e. g. <tt>xmldatabase</tt>.
   * 
   * @return the handler identifier
   */
  String getIdentifier();

  /**
   * Returns the handler name, e. g. <tt>eXist XML Database</tt>.
   * 
   * @return the handler name
   */
  String getName();

  /**
   * Returns the handler description, e. g. <tt>Embedded XML Database Storage
	 * </tt>.
   * 
   * @return the handler description
   */
  String getDescription();

  /**
   * Returns an iteration of all available java property names.
   * 
   * @return the available java property names
   * @see #hasProperty(java.lang.String)
   * @see #getProperty(java.lang.String)
   * @see #getProperty(java.lang.String, java.lang.String)
   */
  Iterator<String> properties();

  /**
   * Returns <code>true</code> if the the java property with name
   * <code>name</code> has been configured.
   * 
   * @param name
   *          the property name
   * @return <code>true</code> if an option with that name exists
   * @see #properties()
   * @see #getProperty(java.lang.String)
   * @see #getProperty(java.lang.String, java.lang.String)
   */
  boolean hasProperty(String name);

  /**
   * Returns the java property value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * 
   * @param name
   *          the property name
   * @return the property value
   * @see #properties()
   * @see #hasProperty(java.lang.String)
   * @see #getProperty(java.lang.String, java.lang.String)
   */
  String getProperty(String name);

  /**
   * Returns the value for the java property <code>name</code> if it has been
   * configured, <code>defaultValue</code> otherwise.
   * 
   * @param name
   *          the proeperty name
   * @param defaultValue
   *          the default value
   * @return the property value
   * @see #properties()
   * @see #hasProperty(java.lang.String)
   * @see #getProperty(java.lang.String)
   */
  String getProperty(String name, String defaultValue);

  /**
   * Returns the property values for property <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * 
   * @param name
   *          the property name
   * @return the property values
   * @see #properties()
   * @see #hasProperty(java.lang.String)
   * @see #getProperty(java.lang.String)
   */
  String[] getProperties(String name);

  /**
   * Returns the name of the class that implements the handler.
   * 
   * @return the implementing handler class name
   */
  String getClassName();

}