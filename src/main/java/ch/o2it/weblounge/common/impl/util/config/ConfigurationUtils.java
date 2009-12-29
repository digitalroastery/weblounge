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

package ch.o2it.weblounge.common.impl.util.config;

import ch.o2it.weblounge.common.Times;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used to handle parameters from configuration files.
 */
public class ConfigurationUtils {

  /**
   * Returns the single option values as a <code>String[]</code> array. The
   * values are expected to be separated by either comma, semicolon or space
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
    List<String> values = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer(optionValue, " ,;");
    while (tok.hasMoreTokens()) {
      values.add(tok.nextToken());
    }
    return values.toArray(new String[values.size()]);
  }

  /**
   * Returns <code>true</code> if the lowercased and trimmed value is not
   * <code>null</code> and corresponds to any of:
   * <ul>
   * <li>true</li>
   * <li>on</li>
   * <li>yes</li>
   * </ul>
   * 
   * @param value
   *          the value to test
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isTrue(String value) {
    if (value == null)
      return false;
    value = value.trim().toLowerCase();
    return "true".equals(value) || "on".equals(value) || "yes".equals(value);
  }

  /**
   * Returns <code>true</code> if the lowercased and trimmed value is not
   * <code>null<code> and corresponds to any of:
   * <ul>
   * <li>true</li>
   * <li>on</li>
   * <li>yes</li>
   * </ul>
   * 
   * @param value
   *          the value to test
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isFalse(String value) {
    if (value == null)
      return false;
    value = value.trim().toLowerCase();
    return "false".equals(value) || "off".equals(value) || "no".equals(value);
  }

  /**
   * Returns the string representation of the given duration in milliseconds.
   * The string follows the pattern <code>ymwdHMS</code>, with the following
   * meanings:
   * <ul>
   * <li><b>y</b> - years</li>
   * <li><b>m</b> - months</li>
   * <li><b>w</b> - weeks</li>
   * <li><b>d</b> - days</li>
   * <li><b>H</b> - hours</li>
   * <li><b>M</b> - minutes</li>
   * <li><b>S</b> - seconds</li>
   * </ul>
   * Therefore, an example representing 1 week, 3 days and 25 minutes would
   * result in <code>1w3d25M</code>.
   * 
   * @param millis
   *          the duration in milliseconds
   * @return the duration as a human readable string
   */
  public static String toDuration(long millis) {
    StringBuffer result = new StringBuffer();
    long v = 0;

    // Years
    if (millis > Times.MS_PER_YEAR) {
      v = millis / Times.MS_PER_YEAR;
      millis -= v*Times.MS_PER_YEAR;
      result.append(v).append("y");
    }

    // Months
    if (millis > Times.MS_PER_MONTH) {
      v = millis / Times.MS_PER_MONTH;
      millis -= v*Times.MS_PER_MONTH;
      result.append(v).append("m");
    }

    // Weeks
    if (millis > Times.MS_PER_WEEK) {
      v = millis / Times.MS_PER_WEEK;
      millis -= v*Times.MS_PER_WEEK;
      result.append(v).append("w");
    }

    // Days
    if (millis > Times.MS_PER_DAY) {
      v = millis / Times.MS_PER_DAY;
      millis -= v*Times.MS_PER_DAY;
      result.append(v).append("d");
    }

    // Hours
    if (millis > Times.MS_PER_HOUR) {
      v = millis / Times.MS_PER_HOUR;
      millis -= v*Times.MS_PER_HOUR;
      result.append(v).append("H");
    }

    // Minutes
    if (millis > Times.MS_PER_MIN) {
      v = millis / Times.MS_PER_MIN;
      millis -= v*Times.MS_PER_MIN;
      result.append(v).append("M");
    }

    // Seconds
    if (millis > Times.MS_PER_SECOND) {
      v = millis / Times.MS_PER_SECOND;
      millis -= v*Times.MS_PER_SECOND;
      result.append(v).append("S");
    }

    return result.toString();
  }

  /**
   * Parses <code>duration</code> to determine the number of milliseconds that
   * it represents. <code>duration</code> may either be a <code>Long</code>
   * value or a duration encoded using the following characters:
   * <ul>
   * <li><b>y</b> - years</li>
   * <li><b>m</b> - months</li>
   * <li><b>w</b> - weeks</li>
   * <li><b>d</b> - days</li>
   * <li><b>H</b> - hours</li>
   * <li><b>M</b> - minutes</li>
   * <li><b>S</b> - seconds</li>
   * </ul>
   * Therefore, an example representing 1 week, 3 days and 25 minutes would
   * result in <code>1w3d25m</code>.
   * 
   * @param duration the duration either in milliseconds or encoded
   * @return the duration in milliseconds
   */
  public static long parseDuration(String duration) {
    if (duration == null)
      return 0;
    long millis = 0;
    try {
      return Long.parseLong(duration);
    } catch (NumberFormatException e) {
      Pattern p = Pattern.compile("^(\\d+y)?(\\d+m)?(\\d+w)?(\\d+d)?(\\d+H)?(\\d+M)?(\\d+S)?$");
      Matcher m = p.matcher(duration);
      if (m.matches()) {
        for (int i=1; i < m.groupCount(); i++) {
          String match = m.group(i);
          if (match == null)
            continue;
          if (match.endsWith("y"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_YEAR;
          if (match.endsWith("m"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_MONTH;
          if (match.endsWith("w"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_WEEK;
          if (match.endsWith("d"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_DAY;
          if (match.endsWith("H"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_HOUR;
          if (match.endsWith("M"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_MIN;
          if (match.endsWith("S"))
            millis += Long.parseLong(match.substring(0, match.length() - 1)) * Times.MS_PER_SECOND;
        }
      }
    }
    return millis;
  }

}