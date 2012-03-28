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

package ch.entwine.weblounge.common.impl.scheduler;

import ch.entwine.weblounge.common.scheduler.JobTrigger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Job trigger that is created from a cron-style configuration.
 * 
 * <pre>
 *   .---------------- minute (0 - 59) 
 *   |  .------------- hour (0 - 23)
 *   |  |  .---------- day of month (1 - 31)
 *   |  |  |  .------- month (1 - 12) OR jan,feb,mar,apr ... 
 *   |  |  |  |  .---- day of week (0 - 6) (Sunday=0 or 7) OR sun,mon,tue,wed,thu,fri,sat 
 *   |  |  |  |  |
 *   *  *  *  *  *
 * </pre>
 * <p>
 * This trigger also supports the following special values as the scheduling
 * expression:
 * <ul>
 * <li>@restart - fires once after scheduling</li>
 * <li>@hourly - fires on the first minute of the hour</li>
 * <li>@daily - fires once per day, at midnight</li>
 * <li>@midnight - same as @daily</li>
 * <li>@weekly - fires once a week, at midnight on Sundays</li>
 * <li>@monthly - fires once at midnight on the first of every month</li>
 * <li>@yearly - fires once per year, at midnight on the first of January</li>
 * <li>@annually - same as @yearly</li>
 * </ul>
 */
public final class CronJobTrigger implements JobTrigger {

  /** The month names */
  private static final String[] MONTHS = new String[] {
      "jan",
      "feb",
      "mar",
      "apr",
      "may",
      "jun",
      "jul",
      "aug",
      "sep",
      "oct",
      "nov",
      "dec" };

  /** The day names */
  private static final String[] DAYS = new String[] {
      "sun",
      "mon",
      "tue",
      "wed",
      "thu",
      "fri",
      "sat",
      "sun" };

  /** All hours of a day */
  private static final int[] ALL_HOURS = new int[] {
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      18,
      19,
      20,
      21,
      22,
      23 };

  /** All days of a month */
  private static final int[] ALL_DAYS_OF_MONTH = new int[] {
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      26,
      27,
      28,
      29,
      30,
      31 };

  /** All months of a year */
  private static final int[] ALL_MONTHS = new int[] {
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12 };

  /** All days of a week */
  private static final int[] ALL_DAYS_OF_WEEK = new int[] { 0, 1, 2, 3, 4, 5, 6 };

  /** The minutes on which to execute the job */
  private int[] minutes;

  /** The hours on which to execute the job */
  private int[] hours;

  /** The month on which to execute the job */
  private int[] months;

  /** The days of month on which to execute the job */
  private int[] daysOfMonth;

  /** The days of week on which to execute the job */
  private int[] daysOfWeek;

  /** True if this job should be run once */
  private boolean once = false;

  /** The cached execution time */
  private long nextExecution = -1;

  /** The last execution */
  private long lastExecution = -1;

  /**
   * Creates a new trigger, that will - without further configuration - never
   * fire.
   */
  public CronJobTrigger() {
    init();
  }

  /**
   * Creates a new cron job. The next execution is calculated from
   * <code>entry</code> which specifies a crontab entry.
   * 
   * @param entry
   *          the crontab entry
   * @throws IllegalArgumentException
   *           if <code>entry</code> is not a proper crontab entry
   */
  public CronJobTrigger(String entry) throws IllegalArgumentException {
    try {
      init();
      parse(entry);
    } catch (Throwable t) {
      throw new IllegalArgumentException("Cron schedule " + entry + " is malformed: " + t.getMessage(), t);
    }
  }

  /**
   * Resets this trigger's memory, which is equal to setting its last execution
   * date to <code>-1</code>.
   */
  public void reset() {
    lastExecution = -1;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (date == null)
      return null;
    if (nextExecution > date.getTime())
      return new Date(nextExecution);
    else if (once) {
      if (lastExecution > -1)
        return null;
      lastExecution = date.getTime();
      return date;
    }

    Calendar c = Calendar.getInstance();
    c.setFirstDayOfWeek(Calendar.SUNDAY);
    c.setTime(date);

    // Move to next full minute
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.SECOND, 60 - c.get(Calendar.SECOND));

    // We are looking for the next possibility *after* date
    if (c.getTime().equals(date))
      c.add(Calendar.MINUTE, 1);

    // minutes
    while (!matches(c, Calendar.MINUTE, minutes)) {
      c.add(Calendar.MINUTE, 1);
    }

    // hours
    while (!matches(c, Calendar.HOUR_OF_DAY, hours)) {
      c.add(Calendar.HOUR_OF_DAY, 1);
    }

    // days and weekdays
    while (!matches(c, daysOfMonth, daysOfWeek)) {
      c.add(Calendar.DAY_OF_MONTH, 1);
    }

    // months
    while (!matches(c, Calendar.MONTH, months)) {
      c.add(Calendar.MONTH, 1);
      c.set(Calendar.DAY_OF_MONTH, 1);
    }

    // re-adjust days and weekdays after month has changed
    while (!matches(c, daysOfMonth, daysOfWeek)) {
      c.add(Calendar.DAY_OF_MONTH, 1);
    }

    // Cache the new value
    return new Date(c.getTimeInMillis());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobTrigger#triggered(java.util.Date)
   */
  public void triggered(Date date) {
    // We don't care too much...
  }

  /**
   * Initializes this cron job. By default, the job will never be executed.
   */
  private void init() {
    setMinutes(new int[] {});
    setHours(new int[] {});
    setDaysOfMonth(new int[] {});
    setMonths(new int[] {});
    setDaysOfWeek(new int[] {});
  }

  /**
   * Returns the minutes on which to execute the job.
   * 
   * @return the minutes
   */
  public int[] getMinutes() {
    return minutes;
  }

  /**
   * Sets the minutes on which to execute the job.
   * 
   * @param minutes
   *          the minutes
   */
  public void setMinutes(int[] minutes) {
    if (minutes == null)
      minutes = new int[] {};
    this.minutes = minutes;
  }

  /**
   * Sets the minutes on which to execute the job.
   * 
   * @param minutes
   *          the minutes
   */
  public void setMinutes(String minutes) {
    this.minutes = parseMinutes(minutes);
  }

  /**
   * Returns the hours on which to execute the job.
   * 
   * @return the hours
   */
  public int[] getHours() {
    return hours;
  }

  /**
   * Sets the hours on which to execute the job.
   * 
   * @param hours
   *          the hours
   */
  public void setHours(int[] hours) {
    if (hours == null)
      hours = new int[] {};
    this.hours = hours;
  }

  /**
   * Sets the hours on which to execute the job.
   * 
   * @param hours
   *          the hours
   */
  public void setHours(String hours) {
    this.hours = parseHours(hours);
  }

  /**
   * Returns the months on which to execute the job.
   * 
   * @return the months
   */
  public int[] getMonths() {
    return months;
  }

  /**
   * Sets the months on which to execute the job.
   * 
   * @param months
   *          the months
   */
  public void setMonths(int[] months) {
    if (months == null)
      months = new int[] {};
    this.months = months;
  }

  /**
   * Sets the months on which to execute the job.
   * 
   * @param months
   *          the months
   */
  public void setMonths(String months) {
    this.months = parseMonths(months);
  }

  /**
   * Returns the days of month on which to execute the job.
   * 
   * @return the days of month
   */
  public int[] getDaysOfMonth() {
    return daysOfMonth;
  }

  /**
   * Sets the days of month on which to execute the job.
   * 
   * @param daysOfMonth
   *          the days of month
   */
  public void setDaysOfMonth(int[] daysOfMonth) {
    if (daysOfMonth == null)
      daysOfMonth = new int[] {};
    this.daysOfMonth = daysOfMonth;
  }

  /**
   * Sets the days of month on which to execute the job.
   * 
   * @param daysOfMonth
   *          the days of month
   */
  public void setDaysOfMonth(String daysOfMonth) {
    this.daysOfMonth = parseDaysOfMonth(daysOfMonth);
  }

  /**
   * Returns the days of week on which to execute the job.
   * 
   * @return the days of week
   */
  public int[] getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Sets the days of week on which to execute the job.
   * 
   * @param daysOfWeek
   *          the days of week
   */
  public void setDaysOfWeek(int[] daysOfWeek) {
    if (daysOfWeek == null)
      daysOfWeek = new int[] {};
    this.daysOfWeek = daysOfWeek;
  }

  /**
   * Sets the days of week on which to execute the job.
   * 
   * @param daysOfWeek
   *          the days of week
   */
  public void setDaysOfWeek(String daysOfWeek) {
    this.daysOfWeek = parseDaysOfWeek(daysOfWeek);
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
  private static boolean matches(Calendar c, int field, int[] reference) {
    if (reference.length == 0)
      return true;
    int offset = 0;
    switch (field) {
      case Calendar.MONTH:
        offset = 1;
        break;
      case Calendar.DAY_OF_WEEK:
        offset = -1;
        break;
      default:
        offset = 0;
    }
    for (int value : reference)
      if (value == c.get(field) + offset)
        return true;
    return false;
  }

  /**
   * Returns <code>true</code> if <code>field</code> specifies a field in the
   * calendar that matches an entry of the reference.
   * 
   * @param c
   *          the calendar
   * @param reference
   *          the reference values
   * @return <code>true</code> if the calendar matches
   */
  private static boolean matches(Calendar c, int[] days, int[] weekdays) {
    if (days.length == 0)
      return matches(c, Calendar.DAY_OF_WEEK, weekdays);
    else if (weekdays.length == 0)
      return matches(c, Calendar.DAY_OF_MONTH, days);
    else
      return matches(c, Calendar.DAY_OF_MONTH, days) && matches(c, Calendar.DAY_OF_WEEK, weekdays);
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
  private void parse(String str) {
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
  private int[] parseMinutes(String str) {
    try {
      return enumerate(str, 0, 59);
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
  private int[] parseHours(String str) {
    try {
      return enumerate(str, 0, 23);
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
  private int[] parseMonths(String str) {
    try {
      return enumerate(str, 1, 12);
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
  private int[] parseDaysOfMonth(String str) {
    try {
      return enumerate(str, 1, 31);
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
  private int[] parseDaysOfWeek(String str) {
    try {
      return enumerate(str.replaceAll("7", "0"), 0, 6);
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
  private void parseSpecial(String str) {
    if (str == null)
      return;
    str = str.trim().toLowerCase();

    if ("@restart".equals(str)) {
      once = true;
    } else if ("@yearly".equals(str) || "@annually".equals(str)) {
      setMinutes(new int[] { 0 });
      setHours(new int[] { 0 });
      setDaysOfMonth(new int[] { 1 });
      setMonths(new int[] { 1 });
      setDaysOfWeek(ALL_DAYS_OF_WEEK);
    } else if ("@monthly".equals(str)) {
      setMinutes(new int[] { 0 });
      setHours(new int[] { 0 });
      setDaysOfMonth(new int[] { 1 });
      setMonths(ALL_MONTHS);
      setDaysOfWeek(ALL_DAYS_OF_WEEK);
    } else if ("@weekly".equals(str)) {
      setMinutes(new int[] { 0 });
      setHours(new int[] { 0 });
      setDaysOfMonth(ALL_DAYS_OF_MONTH);
      setMonths(ALL_MONTHS);
      setDaysOfWeek(new int[] { 0 });
    } else if ("@daily".equals(str) || "@midnight".equals(str)) {
      setMinutes(new int[] { 0 });
      setHours(new int[] { 0 });
      setDaysOfMonth(ALL_DAYS_OF_MONTH);
      setMonths(ALL_MONTHS);
      setDaysOfWeek(ALL_DAYS_OF_WEEK);
    } else if ("@hourly".equals(str)) {
      setMinutes(new int[] { 0 });
      setHours(ALL_HOURS);
      setDaysOfMonth(ALL_DAYS_OF_MONTH);
      setMonths(ALL_MONTHS);
      setDaysOfWeek(ALL_DAYS_OF_WEEK);
    } else {
      throw new IllegalArgumentException("Special value " + str + " is unknown");
    }
  }

  /**
   * Takes a field part (that is, field entries split by ",") and extracts the
   * int values.
   */
  private static int[] enumerate(String in, int min, int max) {
    List<Integer> result = new ArrayList<Integer>();

    // a, b, c
    StringTokenizer tok = new StringTokenizer(in.trim(), ",");
    while (tok.hasMoreTokens()) {

      String str = tok.nextToken().trim();
      int stepsize = 1;
      int start = min;
      int end = max;

      // Extract the step size, */2 or 7-10/2
      int stepdivider = str.indexOf('/');
      if (stepdivider > 0) {
        try {
          stepsize = Integer.parseInt(str.substring(stepdivider + 1));
          if (!"*".equals(str.substring(0, str.indexOf('/'))))
            throw new IllegalArgumentException("Malformed stepsize expression '" + str + "', first argument should be *");
          for (int i = 0; i < max; i++) {
            if (i % stepsize == 0)
              result.add(i);
          }
          return toIntArray(result);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Illegal stepsize in '" + str + "'");
        }
      }

      // *, 7-12, 13
      int hyphen = str.indexOf('-');
      if (str.startsWith("*")) {
        start = min;
        end = max;
      } else if (hyphen > 0) {
        try {
          start = Integer.parseInt(toNumber(str.substring(0, hyphen)));
          end = Integer.parseInt(toNumber(str.substring(hyphen + 1)));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid interval in '" + str + "'");
        }
      } else {
        try {
          start = Integer.parseInt(toNumber(str));
          end = start;
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
      for (int i = start; i <= end; i += stepsize) {
        result.add(new Integer(i));
      }

    }
    return toIntArray(result);
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
      Integer.parseInt(in);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("'" + in + "' is not a number!");
    }
    return in;
  }

  /**
   * Returns an array of <code>int</code> values that are contained in the
   * <code>shorts</code> list.
   * 
   * @param shorts
   *          the list of <code>int</code> values
   * @return an array of <code>int</code> values.
   */
  private static int[] toIntArray(List<Integer> shorts) {
    int[] array = new int[shorts.size()];
    for (int i = 0; i < shorts.size(); i++)
      array[i] = shorts.get(i).shortValue();
    return array;
  }

  /**
   * Returns a cron-style expression of this trigger.
   * 
   * @return the expression
   */
  public Object getCronExpression() {
    StringBuffer buf = new StringBuffer();

    // minutes
    if (minutes.length < 60) {
      for (int i = 0; i < minutes.length; i++) {
        if (i > 0)
          buf.append(",");
        buf.append(minutes[i]);
      }
      buf.append(" ");
    } else {
      buf.append("* ");
    }

    // hours
    if (hours.length < 24) {
      for (int i = 0; i < hours.length; i++) {
        if (i > 0)
          buf.append(",");
        buf.append(hours[i]);
      }
      buf.append(" ");
    } else {
      buf.append("* ");
    }

    // days of month
    if (daysOfMonth.length < 31) {
      for (int i = 0; i < daysOfMonth.length; i++) {
        if (i > 0)
          buf.append(",");
        buf.append(daysOfMonth[i]);
      }
      buf.append(" ");
    } else {
      buf.append("* ");
    }

    // months
    if (months.length < 12) {
      for (int i = 0; i < months.length; i++) {
        if (i > 0)
          buf.append(",");
        buf.append(months[i]);
      }
      buf.append(" ");
    } else {
      buf.append("* ");
    }

    // days of week
    if (daysOfWeek.length < 7) {
      for (int i = 0; i < daysOfWeek.length; i++) {
        if (i > 0)
          buf.append(",");
        buf.append(daysOfWeek[i]);
      }
    } else {
      buf.append("*");
    }

    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CronJobTrigger) {
      CronJobTrigger trigger = (CronJobTrigger) obj;
      return getCronExpression().equals(trigger.getCronExpression());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer("Cron job trigger");
    buf.append(" [");
    boolean detailsAdded = false;

    // minutes
    if (minutes.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("minute=");
      for (int i = 0; i < minutes.length; i++) {
        if (i > 0)
          buf.append(",");
        if (minutes[i] == -1)
          buf.append("*");
        else
          buf.append(minutes[i]);
      }
      detailsAdded = true;
    }

    // hours
    if (hours.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("hour=");
      for (int i = 0; i < hours.length; i++) {
        if (i > 0)
          buf.append(",");
        if (hours[i] == -1)
          buf.append("*");
        else
          buf.append(hours[i]);
      }
      detailsAdded = true;
    }

    // days of month
    if (daysOfMonth.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("day-of-month=");
      for (int i = 0; i < daysOfMonth.length; i++) {
        if (i > 0)
          buf.append(",");
        if (daysOfMonth[i] == -1)
          buf.append("*");
        else
          buf.append(daysOfMonth[i]);
      }
      detailsAdded = true;
    }

    // months
    if (months.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("months=");
      for (int i = 0; i < months.length; i++) {
        if (i > 0)
          buf.append(",");
        if (months[i] == -1)
          buf.append("*");
        else
          buf.append(CronJobTrigger.MONTHS[months[i]]);
      }
      detailsAdded = true;
    }

    // days of week
    if (daysOfWeek.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("day-of-week=");
      for (int i = 0; i < daysOfWeek.length; i++) {
        if (i > 0)
          buf.append(",");
        if (daysOfWeek[i] == -1)
          buf.append("*");
        else
          buf.append(DAYS[daysOfWeek[i]]);
      }
      detailsAdded = true;
    }

    if (!detailsAdded)
      buf.append("never");
    buf.append("]");
    return buf.toString();
  }

}
