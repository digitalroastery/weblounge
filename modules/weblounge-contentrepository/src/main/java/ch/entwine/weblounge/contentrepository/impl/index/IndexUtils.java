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

package ch.entwine.weblounge.contentrepository.impl.index;

import ch.entwine.weblounge.common.security.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for the solr database.
 */
public final class IndexUtils {

  /** The solr supported date format. **/
  protected static DateFormat dateFormat = new SimpleDateFormat(IndexSchema.SOLR_DATE_FORMAT);

  /** The solr supported date format for days **/
  protected static DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

  /** The regular filter expression for single characters */
  private static final String charCleanerRegex = "([\\+\\-\\!\\(\\)\\{\\}\\[\\]\\\\^\"\\~\\*\\?\\:])";

  /**
   * Utility classes should not be initialized.
   */
  private IndexUtils() {
  }

  /**
   * Clean up the user query input string to avoid invalid input parameters.
   * 
   * @param q
   *          The input String.
   * @return The cleaned string.
   */
  public static String clean(String q) {
    q = q.replaceAll(charCleanerRegex, "\\\\$1");
    q = q.replaceAll("\\&\\&", "\\\\&\\\\&");
    q = q.replaceAll("\\|\\|", "\\\\|\\\\|");
    return q;
  }

  /**
   * Returns a serialized version of the date or <code>null</code> if
   * <code>null</code> was passed in for the date.
   * 
   * @param date
   *          the date
   * @return the serialized date
   */
  public static String serializeDate(Date date) {
    if (date == null)
      return null;
    return dateFormat.format(date);
  }

  /**
   * Returns an expression to search for any date that lies in between
   * <code>startDate</date> and <code>endDate</date>.
   * 
   * @param startDate
   *          the start date
   * @param endDate
   *          the end date
   * @return the serialized search expression
   */
  public static String serializeDateRange(Date startDate, Date endDate) {
    if (startDate == null)
      throw new IllegalArgumentException("Start date cannot be null");
    if (endDate == null)
      throw new IllegalArgumentException("End date cannot be null");
    StringBuffer buf = new StringBuffer("[");
    buf.append(dateFormat.format(startDate));
    buf.append(" TO ");
    buf.append(dateFormat.format(endDate));
    buf.append("]");
    return buf.toString();
  }

  /**
   * Returns the date with all time related fields set to the start of the day.
   * 
   * @param date
   *          the date
   * @return the date with its time component set to the beginning of the day
   */
  public static Date beginningOfDay(Date date) {
    if (date == null)
      return null;
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }

  /**
   * Returns the date with all time related fields set to the end of the day.
   * 
   * @param date
   *          the date
   * @return the date with its time component set to the beginning of the day
   */
  public static Date endOfDay(Date date) {
    if (date == null)
      return null;
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(Calendar.HOUR_OF_DAY, 23);
    c.set(Calendar.MINUTE, 59);
    c.set(Calendar.SECOND, 59);
    c.set(Calendar.MILLISECOND, 99);
    return c.getTime();
  }

  /**
   * Serializes the user to a string or to <code>null</code> if
   * <code>null</code> was passed to this method.
   * 
   * @param user
   *          the user
   * @return the serialized user
   */
  public static String serializeUserId(User user) {
    if (user == null)
      return null;
    return user.getLogin();
  }

  /**
   * Serializes the user name to a string or to <code>null</code> if
   * <code>null</code> was passed to this method.
   * 
   * @param user
   *          the user
   * @return the serialized user name
   */
  public static String serializeUserName(User user) {
    if (user == null)
      return null;
    return user.getName();
  }

}
