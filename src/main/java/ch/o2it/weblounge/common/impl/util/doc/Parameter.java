/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.doc;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Represents a parameter for an endpoint.
 */
public class Parameter {

  /** Parameter type */
  public static enum Type {
    Text, String, Boolean, File, Enum
  };

  /** The parameter name */
  private String name = null;

  /** Default value for the parameter */
  private String defaultValue = null;

  /** Parameter type */
  private Type type = null;

  /** Parameter description */
  private String description = null;

  /** Possible values for this parameter */
  private List<String> choices = null;

  /** Parameter attributes */
  private Map<String, String> attributes = new HashMap<String, String>();

  /** Flag to indicate whether this parameter is required */
  private boolean required = false;

  /** Flag to indicate whether this parameter can be part of the url */
  private boolean path = false;

  /**
   * Creates a parameter for this endpoint.
   * 
   * @param name
   *          the parameter name
   * @param type
   *          the parameter type
   * @param description
   *          the description to display with this parameter
   */
  public Parameter(String name, Type type, String description) {
    this(name, type, description, null, null);
  }

  /**
   * Creates a parameter for this endpoint.
   * 
   * @param name
   *          the parameter name
   * @param type
   *          the parameter type
   * @param description
   *          the description to display with this parameter
   * @param defaultValue
   *          an optional default value which is used if this parameter is
   *          missing
   */
  public Parameter(String name, Type type, String description,
      String defaultValue) {
    this(name, type, description, defaultValue, null);
  }

  /**
   * Creates a parameter for this endpoint.
   * 
   * @param name
   *          the parameter name
   * @param type
   *          the parameter type
   * @param description
   *          the description to display with this parameter
   * @param defaultValue
   *          an optional default value which is used if this parameter is
   *          missing
   * @param choices
   *          a list of valid choices for this parameter
   */
  public Parameter(String name, Type type, String description,
      String defaultValue, String[] choices) {

    if (!EndpointDocumentation.isValid(name))
      throw new IllegalArgumentException("The parameter name must be an alphanumeric text");
    if (type == null)
      throw new IllegalArgumentException("The parameter type must not be null");

    this.name = name;
    this.type = type;
    this.description = description;
    this.defaultValue = defaultValue;
    setChoices(choices);
  }

  /**
   * Adds a possible parameter value.
   * 
   * @param choice
   *          the choice to add to the list of choices
   */
  public void addChoice(String choice) {
    if (choices == null) {
      choices = new Vector<String>();
    }
    choices.add(choice);
  }

  /**
   * Sets the possible parameter values.
   * 
   * @param choices
   *          the parameter values
   */
  public void setChoices(String[] choices) {
    if (choices == null) {
      this.choices = null;
    } else {
      this.choices = new Vector<String>(choices.length);
      for (int i = 0; i < choices.length; i++) {
        addChoice(choices[i]);
      }
    }
  }

  /**
   * Attributes are used for adjusting rendering of form elements related to
   * this parameter
   * 
   * @param key
   *          the attribute key (e.g. size)
   * @param value
   *          the attribute value (e.g. 80)
   */
  public void setAttribute(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("key must be set");
    }
    if (value == null) {
      this.attributes.remove(key);
    } else {
      this.attributes.put(key, value);
    }
  }

  /**
   * Returns the rendering attribute <code>key</code> for this parameter or
   * <code>null</code> if this parameter doesn't exist.
   * 
   * @param key
   *          the attribute name
   * @return the attribute value
   */
  public String getAttribute(String key) {
    if (key == null) {
      return null;
    }
    return this.attributes.get(key);
  }

  /**
   * Returns the parameter name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the parameter's default value or <code>null</code> if no default
   * value has been specified.
   * 
   * @return the default value
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Returns an <code>HTML</code> escaped version of the default value.
   * 
   * @return the escaped default value
   */
  public String getDefaultValueHtml() {
    return StringEscapeUtils.escapeHtml(defaultValue);
  }

  /**
   * Returns the parameter type.
   * 
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the parameter description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the possible parameter values.
   * 
   * @return the parameter values
   */
  public List<String> getChoices() {
    return choices;
  }

  /**
   * Returns the parameter attributes.
   * 
   * @return the attributes
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }

  /**
   * Sets the parameter to either mandatory or optional.
   * 
   * @param required
   *          <code>true</code> to make this parameter required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Returns <code>true</code> if the parameter is required.
   * 
   * @return <code>true</code> if the parameter is required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Sets the parameter to be part of the url rather than a regular request
   * parameter.
   * 
   * @param isPathParameter
   *          <code>true</code> to make this parameter a path parameter
   */
  public void setIsPathParameter(boolean isPathParameter) {
    this.path = isPathParameter;
  }

  /**
   * Returns <code>true</code> if the parameter is expected to be part of the
   * url rather than a regular request parameter.
   * 
   * @return <code>true</code> if this is a path parameter
   */
  public boolean isPath() {
    return path;
  }

  @Override
  public String toString() {
    return name;
  }

}
