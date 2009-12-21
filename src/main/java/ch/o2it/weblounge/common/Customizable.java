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

package ch.o2it.weblounge.common;

import java.util.List;
import java.util.Map;

/**
 * This interface describes objects containing a list of options. The intention
 * of it is to give configurable options inside weblounge a consistent way of
 * exposing these options.
 */
public interface Customizable {

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
  boolean hasOption(String name);

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multiple value option (that is, if the option has been
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
  String getOption(String name);

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
  String getOption(String name, String defaultValue);

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
  String[] getOptions(String name);

  /**
   * Returns the options as a <code>Map</code>.
   * 
   * @return the options
   */
  Map<String, List<String>> options();

}