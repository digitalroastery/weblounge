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

package ch.o2it.weblounge.common.impl.util.cron;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.xpath.XPath;

/**
 * This class holds information about when a job is being executed. The
 * configuration style follows the cron convention, which looks like the
 * following:
 * 
 * <pre>
 * 
 *        The time and date fields are:
 *              field          allowed values
 *              -----          --------------
 *              minute         0-59
 *              hour           0-23
 *              day of month   1-31
 *              month          1-12 (or names, see below)
 *              day of week    0-7 (0 or 7 is Sun, or use names)
 *       A field may be an asterisk (*), which always stands for ``first-last''.
 *       
 *       Ranges of numbers are allowed.  Ranges are two numbers separated with a
 *       hyphen.   The  specified  range is inclusive.  For example, 8-11 for an
 *       ``hours'' entry specifies execution at hours 8, 9, 10 and 11.
 *       Lists are allowed.  A list is a set of numbers (or ranges) separated by
 *       commas.  Examples: ``1,2,5,9'', ``0-4,8-12''.
 *       Step  values can be used in conjunction with ranges.  Following a range
 *       with ``/&lt;number&gt;'' specifies skips of the number's  value  through  the
 *       range.  For example, ``0-23/2'' can be used in the hours field to spec-
 *       ify command execution every other hour (the alternative in the V7 stan-
 *       dard  is ``0,2,4,6,8,10,12,14,16,18,20,22'').  Steps are also permitted
 *       after an asterisk, so if you want to say ``every two hours'', just  use
 *       ``(asterisk)/2''.
 *       Names  can  also  be used for the ``month'' and ``day of week'' fields.
 *       Use the first three letters  of  the  particular  day  or  month  (case
 *       doesn't matter).  Ranges or lists of names are not allowed.
 *       Note:  The  day of a command's execution can be specified by two fields
 *       -- day of month, and day of week.  If both fields are  restricted  (ie,
 *       aren't  *),  the command will be run when either field matches the cur-
 *       rent time.  For example,
 *       ``30 4 1,15 * 5'' would cause a command to be run at 4:30 am on the 1st
 *       and 15th of each month, plus every Friday.
 *       
 *       Instead  of  the  first  five  fields, one of eight special strings may
 *       appear:
 *             string            meaning
 *             ------            -------
 *             &#064;restart           Run once, at startup.
 *             &#064;yearly           Run once a year, &quot;0 0 1 1 *&quot;.
 *             &#064;annually         (same as @yearly)
 *             &#064;monthly          Run once a month, &quot;0 0 1 * *&quot;.
 *             &#064;weekly           Run once a week, &quot;0 0 * * 0&quot;.
 *             &#064;daily            Run once a day, &quot;0 0 * * *&quot;.
 *             &#064;midnight         (same as @daily)
 *             &#064;hourly           Run once an hour, &quot;0 * * * *&quot;.
 * 
 * </pre>
 * 
 * @author Tobias Wunden
 */
public abstract class CronJob extends PeriodicJob {

  /** The month names */
  private static final String[] MONTHS = new String[] { "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

  /** The day names */
  private static final String[] DAYS = new String[] { "sun", "mon", "tue", "wed", "thu", "fri", "sat", "sun" };

  /** Constant for "always" */
  public static final short ALWAYS = -1;

  /** The job identifier */
  protected String identifier;

  /** The minutes on which to execute the job */
  protected short[] minutes;

  /** The hours on which to execute the job */
  protected short[] hours;

  /** The month on which to execute the job */
  protected short[] months;

  /** The days of month on which to execute the job */
  protected short[] daysOfMonth;

  /** The days of week on which to execute the job */
  protected short[] daysOfWeek;

  /** True if this job should be run once */
  protected boolean once = false;

  /** Job options */
  protected Map options = new HashMap();

  /** True if the job may be suspended */
  protected boolean suspendable = true;

  /** True if the job has been successfully configured */
  private boolean configured = true;

  /** The cached execution time */
  private long nextExecution = Long.MIN_VALUE;

  /**
   * Creates a new and unconfigured cron job.
   */
  protected CronJob() {
    init();
  }

  /**
   * Creates a new cron job. The next execution is calculated from
   * <code>entry</code> which specifies a unix style crontab entry.
   * 
   * @param entry
   *          the crontab entry
   */
  public CronJob(String entry) throws ConfigurationException {
    try {
      init();
      parse(entry);
    } catch (Exception e) {
      configured = false;
      throw new ConfigurationException("Cron schedule " + entry + " is malformed!", e);
    }
  }

  /**
   * Creates a new cron job from the given configuration node. A node is
   * expected to look like this:
   * 
   * <pre>
   *     &lt;job&gt;
   *         &lt;name&gt;Job title&lt;/name&gt;
   *         &lt;description&gt;Job title&lt;/description&gt;
   *         &lt;class&gt;ch.o2it.weblounge.module.test.SampleCronJob&lt;/class&gt;
   *         &lt;option&gt;
   *             &lt;name&gt;opt&lt;/name&gt;
   *             &lt;value&gt;optvalue&lt;/value&gt;
   *         &lt;/option&gt;
   *         &lt;minutes&gt;0&lt;/minutes&gt;
   *         &lt;hours&gt;0&lt;/hours&gt;
   *         &lt;days&gt;*&lt;/days&gt;
   *         &lt;months&gt;*&lt;/months&gt;
   *         &lt;weekdays&gt;1&lt;/weekdays&gt;
   *     &lt;/job&gt;
   * </pre>
   * <p>
   * Fields that are left out are assumed to equal <code>*</code>.
   * <p>
   * Alternatively, the <code>minutes</code>, <code>hours</code>,
   * <code>days</code>, <code>months</code> and <code>weekdays</code> may be
   * replaced by a <code>&lt;schedule&gt;</code> tag, containing a crontab style
   * configuration string:
   * 
   * <pre>
   *     &lt;schedule&gt;0 0 * * 1&lt;/schedule&gt;
   * </pre>
   * 
   * @param config
   *          the crontab entry
   * @param path
   *          the XPath object used to parse the configuration
   */
  public CronJob(XPath path, Node config) throws ConfigurationException {
    try {
      init(path, config);
    } catch (ConfigurationException e) {
      configured = false;
      throw e;
    }
  }

  /**
   * Returns the job identifier.
   * 
   * @see ch.o2it.weblounge.common.impl.util.cron.AbstractJob#getIdentifier()
   */
  public String getIdentifier() {
    return (identifier != null) ? identifier : super.getIdentifier();
  }

  /**
   * This implementation returns <code>true</code> which makes the job
   * suspendable.
   * 
   * @see ch.o2it.weblounge.common.impl.util.cron.PeriodicJob#isSuspendable()
   */
  public boolean isSuspendable() {
    return true;
  }

  /**
   * Sets the sunspendable flag of this job.
   * 
   * @param suspendable
   *          <code>true</code> to make the job suspendable
   */
  public void setSuspendable(boolean suspendable) {
    this.suspendable = suspendable;
  }

  /**
   * Returns the minutes on which to execute the job.
   * 
   * @return the minutes
   */
  public short[] getMinutes() {
    return minutes;
  }

  /**
   * Sets the minutes on which to execute the job.
   * 
   * @param minutes
   *          the minutes
   */
  public void setMinutes(short[] minutes) {
    if (minutes == null)
      minutes = new short[] {};
    this.minutes = minutes;
    this.configured = true;
  }

  /**
   * Sets the minutes on which to execute the job.
   * 
   * @param minutes
   *          the minutes
   */
  public void setMinutes(String minutes) {
    this.minutes = parseMinutes(minutes);
    this.configured = true;
  }

  /**
   * Returns the hours on which to execute the job.
   * 
   * @return the hours
   */
  public short[] getHours() {
    return hours;
  }

  /**
   * Sets the hours on which to execute the job.
   * 
   * @param hours
   *          the hours
   */
  public void setHours(short[] hours) {
    if (hours == null)
      hours = new short[] {};
    this.hours = hours;
    this.configured = true;
  }

  /**
   * Sets the hours on which to execute the job.
   * 
   * @param hours
   *          the hours
   */
  public void setHours(String hours) {
    this.hours = parseHours(hours);
    this.configured = true;
  }

  /**
   * Returns the months on which to execute the job.
   * 
   * @return the months
   */
  public short[] getMonths() {
    return months;
  }

  /**
   * Sets the months on which to execute the job.
   * 
   * @param months
   *          the months
   */
  public void setMonths(short[] months) {
    if (months == null)
      months = new short[] {};
    this.months = months;
    this.configured = true;
  }

  /**
   * Sets the months on which to execute the job.
   * 
   * @param months
   *          the months
   */
  public void setMonths(String months) {
    this.months = parseMonths(months);
    this.configured = true;
  }

  /**
   * Returns the days of month on which to execute the job.
   * 
   * @return the days of month
   */
  public short[] getDaysOfMonth() {
    return daysOfMonth;
  }

  /**
   * Sets the days of month on which to execute the job.
   * 
   * @param daysOfMonth
   *          the days of month
   */
  public void setDaysOfMonth(short[] daysOfMonth) {
    if (daysOfMonth == null)
      daysOfMonth = new short[] {};
    this.daysOfMonth = daysOfMonth;
    this.configured = true;
  }

  /**
   * Sets the days of month on which to execute the job.
   * 
   * @param daysOfMonth
   *          the days of month
   */
  public void setDaysOfMonth(String daysOfMonth) {
    this.daysOfMonth = parseDaysOfMonth(daysOfMonth);
    this.configured = true;
  }

  /**
   * Returns the days of week on which to execute the job.
   * 
   * @return the days of week
   */
  public short[] getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Sets the days of week on which to execute the job.
   * 
   * @param daysOfWeek
   *          the days of week
   */
  public void setDaysOfWeek(short[] daysOfWeek) {
    if (daysOfWeek == null)
      daysOfWeek = new short[] {};
    this.daysOfWeek = daysOfWeek;
    this.configured = true;
  }

  /**
   * Sets the days of week on which to execute the job.
   * 
   * @param daysOfWeek
   *          the days of week
   */
  public void setDaysOfWeek(String daysOfWeek) {
    this.daysOfWeek = parseDaysOfWeek(daysOfWeek);
    this.configured = true;
  }

  /**
   * Returns the next execution date in milliseconds or
   * <code>{@link AbstractJob.NOW}</code> to stop job execution.
   * 
   * @return the next execution date
   */
  public final long getNextExecution() {
    if (!configured || nextExecution == AbstractJob.NEVER)
      return AbstractJob.NEVER;
    else if (once || nextExecution == AbstractJob.NOW)
      return AbstractJob.NOW;
    else if (nextExecution > System.currentTimeMillis())
      return nextExecution;

    Calendar c = Calendar.getInstance();

    // Move to next full minute
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.SECOND, 60 - c.get(Calendar.SECOND));

    // minutes
    while (!matches(c, Calendar.MINUTE, minutes)) {
      c.add(Calendar.MINUTE, 1);
    }

    // hours
    while (!matches(c, Calendar.HOUR_OF_DAY, hours)) {
      c.add(Calendar.HOUR_OF_DAY, 1);
    }

    // days
    while (!matches(c, Calendar.DAY_OF_MONTH, daysOfMonth, daysOfWeek)) {
      c.add(Calendar.DAY_OF_MONTH, 1);
    }

    // months
    while (!matches(c, Calendar.MONTH, months)) {
      c.add(Calendar.MONTH, 1);
    }

    // Cache the new value
    nextExecution = c.getTimeInMillis();
    return nextExecution;
  }

  /**
   * Returns <code>true</code> if <code>field</code> specifies a field in the
   * calendar that matches an entry of the reference.
   * 
   * @param c
   *          the calendar
   * @param field
   *          the calendar field
   * @param reference
   *          the reference values
   * @return <code>true</code> if the calendar matches
   */
  private static boolean matches(Calendar c, int field, short[] reference) {
    if (reference.length == 0)
      return true;
    if (reference.length == 1 && reference[0] == ALWAYS)
      return true;
    int offset = (field == Calendar.MONTH) ? 1 : 0;
    for (int i = 0; i < reference.length; i++)
      if (reference[i] == c.get(field) + offset)
        return true;
    return false;
  }

  /**
   * Returns <code>true</code> if <code>field</code> specifies a field in the
   * calendar that matches an entry of the reference.
   * 
   * @param c
   *          the calendar
   * @param field
   *          the calendar field
   * @param reference
   *          the reference values
   * @return <code>true</code> if the calendar matches
   */
  private static boolean matches(Calendar c, int field, short[] days,
      short[] weekdays) {
    if (days.length == 0 || (days.length == 1 && days[0] == ALWAYS))
      return matches(c, field, weekdays);
    else if (weekdays.length == 0 || (weekdays.length == 1 && weekdays[0] == ALWAYS))
      return matches(c, field, days);
    else
      return matches(c, field, days) || matches(c, field, weekdays);
  }

  /**
   * Parses the given configuration string and tries to extract the execution
   * information for minute, hour, day of month, month and day of week.
   * 
   * @param str
   *          the configuration string
   * @throw IllegalArgumentException if <code>str</code> is either empty or
   *        malformed
   */
  protected void parse(String str) {
    if (str == null) {
      throw new IllegalArgumentException("Empty job execution instruction found!");
    }
    String[] parts = str.split(" ");
    if (parts.length == 1) {
      parseSpecial(str);
    } else if (parts.length == 5) {
      try {
        setMinutes(parseMinutes(parts[0].trim().toLowerCase()));
        setHours(parseHours(parts[1].trim().toLowerCase()));
        setDaysOfMonth(parseDaysOfMonth(parts[2].trim().toLowerCase()));
        setMonths(parseMonths(parts[3].trim().toLowerCase()));
        setDaysOfWeek(parseDaysOfWeek(parts[4].trim().toLowerCase()));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Schedule " + str + " is malformed:" + e.getMessage());
      }
    } else {
      throw new IllegalArgumentException("Schedule " + str + " is malformed!");
    }
  }

  /**
   * Parses the minutes field. The field may only contain integer numbers
   * between 0 and 59.
   * 
   * @param str
   *          the field
   * @return the minutes
   */
  protected short[] parseMinutes(String str) {
    try {
      return enumerate(str, (short) 0, (short) 59);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error parsing minutes: " + e.getMessage());
    }
  }

  /**
   * Parses the hours field. The field may only contain integer numbers between
   * 0 and 23.
   * 
   * @param str
   *          the field
   * @return the hours
   */
  protected short[] parseHours(String str) {
    try {
      return enumerate(str, (short) 0, (short) 23);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error parsing hours: " + e.getMessage());
    }
  }

  /**
   * Parses the months field. The field may contain integer numbers between 1
   * and 12 or the first three letters of the english month names (jan, feb
   * etc.).
   * 
   * @param str
   *          the field
   * @return the months
   */
  protected short[] parseMonths(String str) {
    try {
      return enumerate(str, (short) 1, (short) 12);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error parsing months: " + e.getMessage());
    }
  }

  /**
   * Parses the day of month field. The field may contain only integer numbers
   * between 1 and 31.
   * 
   * @param str
   *          the field
   * @return the days
   */
  protected short[] parseDaysOfMonth(String str) {
    try {
      return enumerate(str, (short) 1, (short) 31);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error parsing days of month: " + e.getMessage());
    }
  }

  /**
   * Parses the day of month field. The field may contain integer numbers
   * between 0 and 7 or the first three letters of the english day names (mon,
   * tue etc.). <br>
   * Note that 0 and 7 both mean sunday.
   * 
   * @param str
   *          the field
   * @return the weekdays
   */
  protected short[] parseDaysOfWeek(String str) {
    try {
      return enumerate(str, (short) 0, (short) 7);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error parsing days of week: " + e.getMessage());
    }
  }

  /**
   * Parses the string for special commands like "daily", "weekly" etc. Valid
   * identifiers are:
   * 
   * @restart Run once, at startup.
   * @yearly Run once a year, "0 0 1 1 *".
   * @annually (same as @yearly)
   * @monthly Run once a month, "0 0 1 * *".
   * @weekly Run once a week, "0 0 * * 0".
   * @daily Run once a day, "0 0 * * *".
   * @midnight (same as @daily)
   * @hourly Run once an hour, "0 * * * *".
   * 
   * @param str
   * @return
   */
  protected void parseSpecial(String str) {
    if (str == null)
      return;
    str = str.trim().toLowerCase();

    // Set default values
    short minute = 0;
    short hour = 0;
    short dayOfMonth = 1;
    short month = 0;
    short dayOfWeek = ALWAYS;

    if ("@restart".equals(str)) {
      once = true;
    } else if ("@yearly".equals(str) || "@annually".equals(str)) {
      month = 1;
    } else if ("@monthly".equals(str)) {
      month = ALWAYS;
    } else if ("@weekly".equals(str)) {
      dayOfMonth = ALWAYS;
      month = ALWAYS;
      dayOfWeek = 0;
    } else if ("@daily".equals(str) || "@midnight".equals(str)) {
      dayOfMonth = ALWAYS;
      month = ALWAYS;
    } else if ("@hourly".equals(str)) {
      hour = ALWAYS;
      dayOfMonth = ALWAYS;
      month = ALWAYS;
    }

    // Set the values
    setMinutes(new short[] { minute });
    setHours(new short[] { hour });
    setDaysOfMonth(new short[] { dayOfMonth });
    setMonths(new short[] { month });
    setDaysOfWeek(new short[] { dayOfWeek });
  }

  /**
   * Takes a field part (that is, field entries split by ",") and extracts the
   * short values.
   */
  private static short[] enumerate(String in, short min, short max) {
    List<Short> result = new ArrayList<Short>();

    // a, b, c
    StringTokenizer tok = new StringTokenizer(in.trim(), ",");
    while (tok.hasMoreTokens()) {

      String str = tok.nextToken().trim();
      short stepsize = 1;
      short start = min;
      short end = max;

      // Extract the stepsize, */2 or 7-10/2
      int stepdivider = str.indexOf('/');
      if (stepdivider > 0) {
        try {
          stepsize = Short.parseShort(str.substring(stepdivider + 1));
          str = str.substring(0, str.indexOf('/'));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Illegal stepsize in '" + str + "'");
        }
      }

      // *, 7-12, 13
      int hyphen = str.indexOf('-');
      if (str.startsWith("*")) {
        // Nothing to do
      } else if (hyphen > 0) {
        try {
          start = Short.parseShort(toNumber(str.substring(0, hyphen)));
          end = Short.parseShort(toNumber(str.substring(hyphen + 1)));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid interval in '" + str + "'");
        }
      } else {
        try {
          start = end = Short.parseShort(toNumber(str));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid number: '" + str + "'");
        }
      }

      // Check for minimum / maximum
      if (start < min)
        throw new IllegalArgumentException("Value " + start + " in '" + in + "' is smaller than the minimum of " + min);
      if (end > max)
        throw new IllegalArgumentException("Value " + end + " in '" + in + "' is larger than the maximum of " + min);

      // Return the result
      for (short i = start; i <= end; i += stepsize) {
        result.add(new Short(i));
      }

    }
    return toShort(result);
  }

  /**
   * Replaces day and month names with their corresponding value.
   * 
   * @param in
   *          the incoming string, may contain names or numbers
   * @return the number
   */
  private static String toNumber(String in) {
    in = in.toLowerCase();
    for (int i = 0; i < MONTHS.length; i++)
      if (MONTHS[i].equals(in))
        return Integer.toString(i + 1);
    for (int i = 0; i < DAYS.length; i++)
      if (DAYS[i].equals(in))
        return Integer.toString(i);
    try {
      Short.parseShort(in);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("'" + in + "' is not a number!");
    }
    return in;
  }

  /**
   * Returns an array of <code>short</code> values that are contained in the
   * <code>shorts</code> list.
   * 
   * @param shorts
   *          the list of <code>short</code> values
   * @return an array of <code>short</code> values.
   */
  private static short[] toShort(List shorts) {
    short[] array = new short[shorts.size()];
    for (int i = 0; i < shorts.size(); i++)
      array[i] = ((Short) shorts.get(i)).shortValue();
    return array;
  }

  /**
   * Returns an iteration of all available option names.
   * 
   * @return the available option names
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public Iterator options() {
    return options.keySet().iterator();
  }

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
  public boolean hasOption(String name) {
    return (options.keySet().contains(name));
  }

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multivalue option (that is, if the option has been
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
  public String getOption(String name) {
    Object option = options.get(name);
    if (option instanceof ArrayList) {
      return (String) ((ArrayList) option).get(0);
    }
    return (String) option;
  }

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
  public String getOption(String name, String defaultValue) {
    String value = getOption(name);
    return (value != null) ? value : defaultValue;
  }

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
  public String[] getOptions(String name) {
    Object option = options.get(name);
    if (option instanceof ArrayList) {
      return (String[]) ((ArrayList) option).toArray(new String[] {});
    } else if (option instanceof String) {
      return new String[] { (String) option };
    }
    return null;
  }

  /**
   * Initializes this cron job. By default, the job will never be executed.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   *           if the configuration data is incomplete or invalid
   */
  public void init() {
    setMinutes(new short[] { ALWAYS });
    setHours(new short[] { ALWAYS });
    setDaysOfMonth(new short[] { ALWAYS });
    setMonths(new short[] { ALWAYS });
    setDaysOfWeek(new short[] { ALWAYS });
    configured = true;
  }

  /**
   * Initializes this cron job from an xml configuration node.
   * 
   * @param config
   *          the configuration node
   * @param path
   *          the XPath object used to parse the configuration
   * @throws ConfigurationException
   *           if the configuration data is incomplete or invalid
   */
  public void init(XPath path, Node config) throws ConfigurationException {
    identifier = XPathHelper.valueOf(path, config, "@id");
    name = XPathHelper.valueOf(path, config, "name");
    description = XPathHelper.valueOf(path, config, "description");

    // Read options
    readOptions(path, config);

    // See if crontab - style entry has been given
    if (XPathHelper.select(path, config, "schedule") != null) {
      parse(XPathHelper.valueOf(path, config, "schedule"));
      configured = true;
      return;
    }

    // See if something has been configured
    boolean foundOne = false;

    // Minutes
    if (XPathHelper.select(path, config, "minutes") != null) {
      parseMinutes(XPathHelper.valueOf(path, config, "minutes"));
      foundOne = true;
    } else
      setMinutes(new short[] { ALWAYS });

    // Hours
    if (XPathHelper.select(path, config, "hours") != null) {
      parseHours(XPathHelper.valueOf(path, config, "hours"));
      foundOne = true;
    } else
      setHours(new short[] { ALWAYS });

    // Days
    if (XPathHelper.select(path, config, "days") != null) {
      parseDaysOfMonth(XPathHelper.valueOf(path, config, "days"));
      foundOne = true;
    } else
      setDaysOfMonth(new short[] { ALWAYS });

    // Months
    if (XPathHelper.select(path, config, "months") != null) {
      parseMonths(XPathHelper.valueOf(path, config, "months"));
      foundOne = true;
    } else
      setMonths(new short[] { ALWAYS });

    // Weekdays
    if (XPathHelper.select(path, config, "weekdays") != null) {
      parseDaysOfWeek(XPathHelper.valueOf(path, config, "weekdays"));
      foundOne = true;
    } else
      setDaysOfWeek(new short[] { ALWAYS });

    // Did we find something?
    if (!foundOne)
      throw new ConfigurationException("No schedule has been defined for job '" + name + "'");

    configured = true;
  }

  /**
   * Reads the job options from the configuration.
   * 
   * @param config
   *          job configuration node
   * @param path
   *          the XPath object used to parse the configuration
   */
  private void readOptions(XPath path, Node config) {
    NodeList nodes = XPathHelper.selectList(path, config, "options/option");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String name = XPathHelper.valueOf(path, option, "name/text()");
      String value = XPathHelper.valueOf(path, option, "value/text()");
      if (options.get(name) != null) {
        Object o = options.get(name);
        if (o instanceof ArrayList) {
          ((ArrayList) o).add(value);
        } else {
          List values = new ArrayList();
          values.add(o);
          values.add(value);
          options.remove(name);
          options.put(name, values);
        }
      } else {
        options.put(name, value);
      }
    }
  }

}