/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.timeline;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class to support the timeline implementation.
 */
public final class TimelineUtils {

  /** the date formatter */
  private static final ThreadLocal<SoftReference<DateFormat>> ldf = new ThreadLocal<SoftReference<DateFormat>>();

  /** the date format */
  private static final String DATE_FORMAT = "yyyy,MM,dd";

  /**
   * Private constructor only to prevent initialization of the utility class.
   */
  private TimelineUtils() {
    // Nothing to do
  }

  /**
   * Formats a date using a weblounge compatible date format.
   * 
   * @param date
   *          the date to format
   * @return the formatted date string
   */
  public static String format(Date date) {
    return getDateFormat().format(date);
  }

  /**
   * Formats a time using a weblounge compatible date format.
   * 
   * @param milliseconds
   *          the time to format
   * @return the formatted date string
   */
  public static String format(long milliseconds) {
    return getDateFormat().format(new Date(milliseconds));
  }

  /**
   * Gets the <code>DateFormat</code> for the current thread.
   * 
   * @return the date formatter for the current thread
   */
  private static DateFormat getDateFormat() {
    SoftReference<DateFormat> sr = ldf.get();
    DateFormat df = (sr != null) ? sr.get() : null;
    if (sr == null || df == null) {
      df = new SimpleDateFormat(DATE_FORMAT, Locale.US);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      ldf.set(new SoftReference<DateFormat>(df));
    }
    return df;
  }

}
