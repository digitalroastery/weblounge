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

package ch.o2it.weblounge.common.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class used to handle parameters from configuration files.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class ConfigurationUtils {

  /**
   * Returns the single option values as a <code>String[]</code> array. The
   * values are expected to be separated by either coma, semicolon or space
   * characters.
   * 
   * @param optionValue
   *          the option value
   * @return the values
   */
  public static String[] getMultiOptionValues(String optionValue) {
    if (optionValue == null) {
      return new String[] {};
    }
    List values = new ArrayList();
    StringTokenizer tok = new StringTokenizer(optionValue, " ,;");
    while (tok.hasMoreTokens()) {
      values.add(tok.nextToken());
    }
    return (String[]) values.toArray(new String[values.size()]);
  }

}