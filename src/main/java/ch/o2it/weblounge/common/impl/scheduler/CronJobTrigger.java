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

package ch.o2it.weblounge.common.impl.scheduler;

import ch.o2it.weblounge.common.scheduler.JobTrigger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Job trigger that is created from a cron-style configuration.
 * <pre>
 *   .---------------- minute (0 - 59) 
 *   |  .------------- hour (0 - 23)
 *   |  |  .---------- day of month (1 - 31)
 *   |  |  |  .------- month (1 - 12) OR jan,feb,mar,apr ... 
 *   |  |  |  |  .---- day of week (0 - 6) (Sunday=0 or 7) OR sun,mon,tue,wed,thu,fri,sat 
 *   |  |  |  |  |
 *   *  *  *  *  *
 * </pre>
 */
public final class CronJobTrigger implements JobTrigger {

  /** The month names */
  private static final String[] MONTHS = new String[] {
      "jan",
      "feb",
      "mar",
      "apr",
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

  /** Constant for "always" */
  public static final short ALWAYS = -1;

  /** The minutes on which to execute the job */
  private short[] minutes;

  /** The hours on which to execute the job */
  private short[] hours;

  /** The month on which to execute the job */
  private short[] months;

  /** The days of month on which to execute the job */
  private short[] daysOfMonth;

  /** The days of week on which to execute the job */
  private short[] daysOfWeek;

  /** True if this job should be run once */
  private boolean once = false;

  /** The cached execution time */
  private long nextExecution = -1;

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
    } catch (Exception e) {
      throw new IllegalArgumentException("Cron schedule " + entry + " is malformed!", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#getNextExecutionAfter(Date)
   */
  public Date getNextExecutionAfter(Date date) {
    if (nextExecution > date.getTime())
      return new Date(nextExecution);
    else if (once) {
      once = false;
      return date;
    }

    Calendar c = Calendar.getInstance();
    c.setTime(date);
    boolean configured = false;

    // Move to next full minute
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.SECOND, 60 - c.get(Calendar.SECOND));

    // minutes
    while (!matches(c, Calendar.MINUTE, minutes)) {
      c.add(Calendar.MINUTE, 1);
      configured = true;
    }

    // hours
    while (!matches(c, Calendar.HOUR_OF_DAY, hours)) {
      c.add(Calendar.HOUR_OF_DAY, 1);
      configured = true;
    }

    // days
    while (!matches(c, Calendar.DAY_OF_MONTH, daysOfMonth, daysOfWeek)) {
      c.add(Calendar.DAY_OF_MONTH, 1);
      configured = true;
    }

    // months
    while (!matches(c, Calendar.MONTH, months)) {
      c.add(Calendar.MONTH, 1);
      configured = true;
    }

    if (!configured)
      return null;
    
    // Cache the new value
    return new Date(c.getTimeInMillis());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.JobTrigger#triggered(java.util.Date)
   */
  public void triggered(Date date) {
    // We don't care too much...
  }
  
  /**
   * Initializes this cron job. By default, the job will never be executed.
   */
  private void init() {
    setMinutes(new short[] { });
    setHours(new short[] { });
    setDaysOfMonth(new short[] { });
    setMonths(new short[] { });
    setDaysOfWeek(new short[] { });
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
  private short[] parseMinutes(String str) {
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
  private short[] parseHours(String str) {
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
  private short[] parseMonths(String str) {
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
  private short[] parseDaysOfMonth(String str) {
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
  private short[] parseDaysOfWeek(String str) {
    try {
      return enumerate(str.replaceAll("7", "0"), (short) 0, (short) 6);
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

      // Extract the step size, */2 or 7-10/2
      int stepdivider = str.indexOf('/');
      if (stepdivider > 0) {
        try {
          stepsize = Short.parseShort(str.substring(stepdivider + 1));
          if (!"*".equals(str.substring(0, str.indexOf('/'))))
            throw new IllegalArgumentException("Malformed stepsize expression '" + str + "', first argument should be *");
          for (short i=0; i < max; i++) {
            if (i % stepsize == 0)
              result.add(i);
          }
          return toShort(result);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Illegal stepsize in '" + str + "'");
        }
      }

      // *, 7-12, 13
      int hyphen = str.indexOf('-');
      if (str.startsWith("*")) {
        return new short[] { ALWAYS };
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
  private static short[] toShort(List<Short> shorts) {
    short[] array = new short[shorts.size()];
    for (int i = 0; i < shorts.size(); i++)
      array[i] = shorts.get(i).shortValue();
    return array;
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
    
    // months
    if (months.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("months=");
      for (int i=0; i<months.length; i++) {
        if (i>0)
          buf.append(",");
        if (months[i] == -1)
          buf.append("*");
        else
          buf.append(CronJobTrigger.MONTHS[months[i]]);
      }
      detailsAdded = true;
    }

    // days of month
    if (daysOfMonth.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("day-of-month=");
      for (int i=0; i<daysOfMonth.length; i++) {
        if (i>0)
          buf.append(",");
        if (daysOfWeek[i] == -1)
          buf.append("*");
        else
          buf.append(daysOfMonth[i]);
      }
      detailsAdded = true;
    }

    // days of week
    if (daysOfWeek.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("day-of-week=");
      for (int i=0; i<daysOfWeek.length; i++) {
        if (i>0)
          buf.append(",");
        if (daysOfWeek[i] == -1)
          buf.append("*");
        else
          buf.append(DAYS[daysOfWeek[i]]);
      }
      detailsAdded = true;
    }

    // hours
    if (hours.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("hour=");
      for (int i=0; i<hours.length; i++) {
        if (i>0)
          buf.append(",");
        if (hours[i] == -1)
          buf.append("*");
        else
          buf.append(hours[i]);
      }
      detailsAdded = true;
    }
    
    // minutes
    if (minutes.length > 0) {
      if (detailsAdded)
        buf.append(";");
      buf.append("minute=");
      for (int i=0; i<minutes.length; i++) {
        if (i>0)
          buf.append(",");
        if (minutes[i] == -1)
          buf.append("*");
        else
          buf.append(minutes[i]);
      }
      detailsAdded = true;
    }

    if (!detailsAdded)
      buf.append("never");
    buf.append("]");
    return buf.toString();
  }

}
