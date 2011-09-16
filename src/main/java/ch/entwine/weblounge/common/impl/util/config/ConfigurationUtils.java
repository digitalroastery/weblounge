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

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used to handle parameters from configuration files.
 */
public final class ConfigurationUtils {

  /**
   * This class is not intended to be instantiated.
   */
  private ConfigurationUtils() {
    // Nothing to be done here
  }

  /**
   * Returns <code>object</code> if it is not <code>null</code>,
   * <code>defaultObject</code> otherwise.
   * <p>
   * Note that this method will try to process templates that are contained in
   * either one of <code>object</code> or <code>defaultObject</code>.
   * 
   * @param object
   *          the object to use if not <code>null</code>
   * @param defaultObject
   *          the default object to use if <code>object</code> is
   *          <code>null</code>
   * @return <code>object</code> if it is not <code>null</code>,
   *         <code>defaultObject</code> otherwise
   */
  public static Object getValue(Object object, Object defaultObject) {
    return getValue(object, defaultObject, true);
  }

  /**
   * Returns <code>object</code> if it is not <code>null</code>,
   * <code>defaultObject</code> otherwise.
   * 
   * @param object
   *          the object to use if not <code>null</code>
   * @param defaultObject
   *          the default object to use if <code>object</code> is
   *          <code>null</code>
   * @param processTemplates
   *          <code>true</code> to process templates
   * @return <code>object</code> if it is not <code>null</code>,
   *         <code>defaultObject</code> otherwise
   */
  public static Object getValue(Object object, Object defaultObject,
      boolean processTemplates) {
    Object o = object != null ? object : defaultObject;
    if (processTemplates && o instanceof String)
      return processTemplate((String) o);
    else
      return o;
  }

  /**
   * Returns the <code>int</code> value or <code>defaultValue</code> if
   * <code>value</code> is either <code>null</code> or blank.
   * 
   * @param value
   *          the value as a string
   * @param defaultValue
   *          the default value
   * @return the value
   * @throws NumberFormatException
   *           if <code>value</code> can't be parsed into an <code>int</code>
   */
  public static int getValue(String value, int defaultValue)
      throws NumberFormatException {
    if (StringUtils.isBlank(value))
      return defaultValue;
    return Integer.parseInt(value);
  }

  /**
   * Returns the <code>long</code> value or <code>defaultValue</code> if
   * <code>value</code> is either <code>null</code> or blank.
   * 
   * @param value
   *          the value as a string
   * @param defaultValue
   *          the default value
   * @return the value
   * @throws NumberFormatException
   *           if <code>value</code> can't be parsed into a <code>long</code>
   */
  public static long getValue(String value, long defaultValue)
      throws NumberFormatException {
    if (StringUtils.isBlank(value))
      return defaultValue;
    return Long.parseLong(value);
  }

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
   * Returns <code>true</code> if the node contains an attribute named
   * <tt>default</tt> with a value that corresponds to any of:
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
  public static boolean isDefault(Node node) {
    if (node == null)
      return false;
    Node defaultAttribute = node.getAttributes().getNamedItem("default");
    if (defaultAttribute == null || defaultAttribute.getNodeValue() == null)
      return false;
    return isTrue(defaultAttribute.getNodeValue());
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
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
  public static boolean isTrue(Node node) {
    if (node == null || node.getNodeValue() == null)
      return false;
    return isTrue(node.getNodeValue());
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
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
    return isTrue(value, false);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null</code> and corresponds to any of:
   * <ul>
   * <li>true</li>
   * <li>on</li>
   * <li>yes</li>
   * </ul>
   * If <code>value</code> is either <code>null</code> or blank,
   * <code>defaultValue</code> is returned.
   * 
   * @param value
   *          the value to test
   * @param defaultValue
   *          the default value if <code>value</code> is blank
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isTrue(String value, boolean defaultValue) {
    if (StringUtils.isBlank(value))
      return defaultValue;
    value = value.trim().toLowerCase();
    return "true".equals(value) || "on".equals(value) || "yes".equals(value);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
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
    return isFalse(value, false);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null<code> and corresponds to any of:
   * <ul>
   * <li>true</li>
   * <li>on</li>
   * <li>yes</li>
   * </ul>
   * If <code>value</code> is either <code>null</code> or blank,
   * <code>defaultValue</code> is returned.
   * 
   * @param value
   *          the value to test
   * @param defaultValue
   *          the default value if <code>value</code> is blank
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isFalse(String value, boolean defaultValue) {
    if (value == null)
      return defaultValue;
    value = value.trim().toLowerCase();
    return "false".equals(value) || "off".equals(value) || "no".equals(value);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null</code> and corresponds to any of:
   * <ul>
   * <li>active</li>
   * <li>enabled</li>
   * <li>on</li>
   * </ul>
   * 
   * @param value
   *          the value to test
   * @return <code>true</code> if the value can be interpreted as
   *         <code>enabled</code>
   */
  public static boolean isEnabled(String value) {
    return isEnabled(value, false);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null</code> and corresponds to any of:
   * <ul>
   * <li>active</li>
   * <li>enabled</li>
   * <li>on</li>
   * </ul>
   * If <code>value</code> is either <code>null</code> or blank,
   * <code>defaultValue</code> is returned.
   * 
   * @param value
   *          the value to test
   * @param defaultValue
   *          the default value if <code>value</code> is blank
   * @return <code>true</code> if the value can be interpreted as
   *         <code>enabled</code>
   */
  public static boolean isEnabled(String value, boolean defaultValue) {
    if (value == null)
      return defaultValue;
    value = value.trim().toLowerCase();
    return "active".equals(value) || "enabled".equals(value) || "on".equals(value);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null<code> and corresponds to any of:
   * <ul>
   * <li>inactive</li>
   * <li>disabled</li>
   * <li>off</li>
   * </ul>
   * 
   * @param value
   *          the value to test
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isDisabled(String value) {
    return isDisabled(value, false);
  }

  /**
   * Returns <code>true</code> if the lowercase and trimmed value is not
   * <code>null<code> and corresponds to any of:
   * <ul>
   * <li>inactive</li>
   * <li>disabled</li>
   * <li>off</li>
   * </ul>
   * If <code>value</code> is either <code>null</code> or blank,
   * <code>defaultValue</code> is returned.
   * 
   * @param value
   *          the value to test
   * @param defaultValue
   *          the default value if <code>value</code> is blank
   * @return <code>true</code> if the value can be interpreted as
   *         <code>true</code>
   */
  public static boolean isDisabled(String value, boolean defaultValue) {
    if (value == null)
      return defaultValue;
    value = value.trim().toLowerCase();
    return "inactive".equals(value) || "disabled".equals(value) || "off".equals(value);
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
  public static String toHumanReadableDuration(long millis) {
    return toDuration(millis, true);
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
    return toDuration(millis, false);
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
   * @param humanReadable
   *          <code>true</code> to generate human readable output
   * @return the duration as a human readable string
   */
  private static String toDuration(long millis, boolean humanReadable) {
    StringBuffer result = new StringBuffer();
    long v = 0;

    // Years
    if (millis >= Times.MS_PER_YEAR) {
      v = millis / Times.MS_PER_YEAR;
      millis -= v * Times.MS_PER_YEAR;
      result.append(v).append(humanReadable ? " years " : "y");
    }

    // Months
    if (millis >= Times.MS_PER_MONTH) {
      v = millis / Times.MS_PER_MONTH;
      millis -= v * Times.MS_PER_MONTH;
      result.append(v).append(humanReadable ? " months " : "m");
    }

    // Weeks
    if (millis >= Times.MS_PER_WEEK) {
      v = millis / Times.MS_PER_WEEK;
      millis -= v * Times.MS_PER_WEEK;
      result.append(v).append(humanReadable ? " weeks " : "w");
    }

    // Days
    if (millis >= Times.MS_PER_DAY) {
      v = millis / Times.MS_PER_DAY;
      millis -= v * Times.MS_PER_DAY;
      result.append(v).append(humanReadable ? " days " : "d");
    }

    // Hours
    if (millis >= Times.MS_PER_HOUR) {
      v = millis / Times.MS_PER_HOUR;
      millis -= v * Times.MS_PER_HOUR;
      result.append(v).append(humanReadable ? " hours " : "H");
    }

    // Minutes
    if (millis >= Times.MS_PER_MIN) {
      v = millis / Times.MS_PER_MIN;
      millis -= v * Times.MS_PER_MIN;
      result.append(v).append(humanReadable ? " minutes " : "M");
    }

    // Seconds
    if (millis >= Times.MS_PER_SECOND) {
      v = millis / Times.MS_PER_SECOND;
      millis -= v * Times.MS_PER_SECOND;
      result.append(v).append(humanReadable ? " seconds " : "S");
    }

    // Cleanup
    if (millis > 0) {
      result.append(millis);
      result.append(v).append(humanReadable ? " milliseconds" : "");
    } else if (result.length() == 0)
      result.append("0");

    return result.toString().trim();
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
   * @param duration
   *          the duration either in milliseconds or encoded
   * @return the duration in milliseconds
   * @throws IllegalArgumentException
   *           if the duration cannot be parsed
   */
  public static long parseDuration(String duration)
      throws IllegalArgumentException {
    if (duration == null)
      return 0;
    long millis = 0;
    try {
      return Long.parseLong(duration);
    } catch (NumberFormatException e) {
      Pattern p = Pattern.compile("^([\\d]+y)?([\\d]+m)?([\\d]+w)?([\\d]+d)?([\\d]+H)?([\\d]+M)?([\\d]+S)?$");
      Matcher m = p.matcher(duration);
      if (m.matches()) {
        for (int i = 1; i <= m.groupCount(); i++) {
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
      } else {
        throw new IllegalArgumentException("Unknown duration format: " + duration);
      }
    }
    return millis;
  }

  /**
   * Processes the given text by replacing placeholders in the form of
   * <code>${key}</code> with their actual values as found in the system
   * properties, e. g.
   * <ul>
   * <li><code>${java.io.tmpdir}</code> becomes <code>/tmp</li>
   * </ul>
   * 
   * @param text
   *          the text to process
   * @return the processed text
   */
  public static String processTemplate(String text) {
    if (text.indexOf("${") >= 0 && text.indexOf("}") > 2) {
      for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
        String variable = "\\$\\{" + (String) entry.getKey() + "\\}";
        String replacement = (String) entry.getValue();
        text = text.replaceAll(variable, replacement);
      }
      for (Entry<String, String> entry : System.getenv().entrySet()) {
        String variable = "\\$\\{" + entry.getKey() + "\\}";
        String replacement = entry.getValue();
        text = text.replaceAll(variable, replacement);
      }
    }
    return text;
  }

  /**
   * Processes the given text by replacing these placeholders with their actual
   * values:
   * <ul>
   * <li><code>file://${site.root}</code> with
   * <code>http://&lt;servername&gt;/weblounge-sites/&lt;sitegt;</li>
   * </ul>
   * 
   * @param text
   *          the text to process
   * @param request
   *          the request
   * @return the processed text
   */
  public static String processTemplate(String text, WebloungeRequest request) {
    text = processTemplate(text);

    Map<String, String> replacements = new HashMap<String, String>();
    Site site = request.getSite();
    SiteURL siteURL = site.getConnector(request.getEnvironment());

    StringBuffer siteRootReplacement = new StringBuffer();
    siteRootReplacement.append(siteURL.getURL().toExternalForm());
    siteRootReplacement.append("/weblounge-sites/").append(site.getIdentifier());

    replacements.put("file://\\$\\{site.root\\}", siteRootReplacement.toString());
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      text = text.replaceAll(entry.getKey(), entry.getValue());
    }
    return text;
  }

  /**
   * Processes the given text by replacing these placeholders with their actual
   * values:
   * <ul>
   * <li><code>file://${site.root}</code> with
   * <code>http://&lt;servername&gt;/weblounge-sites/&lt;sitegt;</li>
   * <li><code>file://${module.root}</code> with
   * <code>http://&lt;servername&gt;/weblounge-sites/&lt;sitegt;/modules/&lt;module&gt;</code>
   * </li>
   * </ul>
   * 
   * @param text
   *          the text to process
   * @param module
   *          the module
   * @param environment
   *          the environment
   * @return the processed text
   */
  public static String processTemplate(String text, Module module,
      Environment environment) {
    text = processTemplate(text);

    Map<String, String> replacements = new HashMap<String, String>();
    Site site = module.getSite();

    StringBuffer siteRootReplacement = new StringBuffer();
    siteRootReplacement.append(site.getConnector(environment).toExternalForm());
    siteRootReplacement.append("/weblounge-sites/").append(site.getIdentifier());
    replacements.put("file://\\$\\{site.root\\}", siteRootReplacement.toString());

    StringBuffer moduleRootReplacement = new StringBuffer(siteRootReplacement);
    moduleRootReplacement.append("/modules/").append(module.getIdentifier());
    replacements.put("file://\\$\\{module.root\\}", moduleRootReplacement.toString());

    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      text = text.replaceAll(entry.getKey(), entry.getValue());
    }
    return text;
  }

  /**
   * Processes the given text by replacing these placeholders with their actual
   * values:
   * <ul>
   * <li><code>file://${site.root}</code> with
   * <code>http://&lt;servername&gt;/weblounge-sites/&lt;sitegt;</li>
   * </li>
   * </ul>
   * 
   * @param text
   *          the text to process
   * @param site
   *          the site
   * @param environment
   *          the environment
   * @return the processed text
   */
  public static String processTemplate(String text, Site site,
      Environment environment) {
    text = processTemplate(text);

    Map<String, String> replacements = new HashMap<String, String>();

    StringBuffer siteRootReplacement = new StringBuffer();
    siteRootReplacement.append(site.getConnector(environment).toExternalForm());
    siteRootReplacement.append("/weblounge-sites/").append(site.getIdentifier());
    replacements.put("file://\\$\\{site.root\\}", siteRootReplacement.toString());

    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      text = text.replaceAll(entry.getKey(), entry.getValue());
    }
    return text;
  }

}