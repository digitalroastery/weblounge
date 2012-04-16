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

package ch.entwine.weblounge.common;

import ch.entwine.weblounge.common.site.Environment;

import java.util.List;
import java.util.Map;

/**
 * This interface describes objects containing a list of options. The intention
 * of it is to give configurable options inside weblounge a consistent way of
 * exposing these options.
 */
public interface Customizable {

  /**
   * Sets option <code>name</code> to <code>value</code>. When the value is set
   * to <code>null</code>, the option is removed.
   * 
   * @param name
   *          the option name
   * @param value
   *          the option value
   */
  void setOption(String name, String value);

  /**
   * Sets option <code>name</code> to <code>value</code>. When the value is set
   * to <code>null</code>, the option is removed.
   * 
   * @param name
   *          the option name
   * @param value
   *          the option value
   * @param environment
   *          the environment that this value is valid for
   */
  void setOption(String name, String value, Environment environment);

  /**
   * Removes the option with name <code>name</code>.
   * 
   * @param name
   *          the option name
   */
  void removeOption(String name);

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
  boolean hasOption(String name);

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multiple value option (that is, if the option has been
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
  String getOptionValue(String name);

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
  String getOptionValue(String name, String defaultValue);

  /**
   * Returns the option values for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * 
   * @param name
   *          the option name
   * @return the option values
   * @see #getOptions()
   * @see #hasOption(java.lang.String)
   * @see #getOptionValue(java.lang.String)
   */
  String[] getOptionValues(String name);

  /**
   * Returns the option names. Note that this method will only return names for
   * those options that have a value set for the current environment.
   * 
   * @return the option names
   */
  String[] getOptionNames();

  /**
   * Returns the options as a <code>Map</code>.
   * 
   * @return the options
   */
  Map<String, Map<Environment, List<String>>> getOptions();

}