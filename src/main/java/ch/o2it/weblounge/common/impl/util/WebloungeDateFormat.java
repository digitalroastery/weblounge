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

package ch.o2it.weblounge.common.impl.util;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Formats and parses Weblounge compatible dates. This formatter is thread safe.
 */
public final class WebloungeDateFormat {

  /** the date formatter */
  private static final ThreadLocal<SoftReference<DateFormat>> ldf = new ThreadLocal<SoftReference<DateFormat>>();

  /** the date format */
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  /**
   * Formats a date using a weblounge compatible date format.
   * 
   * @param date
   *          the date to format
   * @return the formatted date string
   */
  public String format(Date date) {
    return getDateFormat().format(date) + "Z";
  }

  /**
   * Formats a date using a weblounge compatible date format.
   * 
   * @param date
   *          the date to format
   * @return the formatted date string
   */
  public static String formatStatic(Date date) {
    return getDateFormat().format(date) + "Z";
  }

  /**
   * Formats a time using a weblounge compatible date format.
   * 
   * @param milliseconds
   *          the time to format
   * @return the formatted date string
   */
  public static String formatStatic(long milliseconds) {
    return getDateFormat().format(new Date(milliseconds)) + "Z";
  }

  /**
   * Parses a string containing a Weblounge compatible date.
   * 
   * @param source
   *          the string to parse
   * @return the date parsed from the string
   * @throws ParseException
   *           if the date cannot be parsed
   */
  public Date parse(String source) throws ParseException {
    return getDateFormat().parse(source);
  }

  /**
   * Parses a string containing a Weblounge compatible date.
   * 
   * @param source
   *          the string to parse
   * @return the date parsed from the string
   * @throws ParseException
   *           if the date cannot be parsed
   */
  public static Date parseStatic(String source) throws ParseException {
    return getDateFormat().parse(source);
  }

  /**
   * Gets the <code>DateFormat</code> for the current thread.
   * 
   * @return the date formatter for the current thread
   */
  private static DateFormat getDateFormat() {
    SoftReference<DateFormat> sr = ldf.get();
    DateFormat df;
    if (sr == null || (df = sr.get()) == null) {
      df = new SimpleDateFormat(DATE_FORMAT, Locale.US);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      ldf.set(new SoftReference<DateFormat>(df));
    }
    return df;
  }

}