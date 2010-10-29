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

package ch.o2it.weblounge.contentrepository.solr;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.impl.user.WebloungeUserImpl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Test case for {@link SolrUtilsTest}.
 */
public class SolrUtilsTest {

  /** The date format */
  protected final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils#clean(java.lang.String)}.
   */
  @Test
  public void testClean() {
    String test = "+-!(){}[]^\"~*?:&&||&|";
    String expected = "\\+\\-\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\&\\&\\|\\|&|";
    assertEquals(expected, SolrUtils.clean(test));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils#serializeDate(java.util.Date)}.
   */
  @Test
  public void testSerializeDate() {
    Calendar d = Calendar.getInstance();
    d.set(Calendar.DAY_OF_MONTH, 2);
    d.set(Calendar.HOUR, 5);
    d.set(Calendar.HOUR_OF_DAY, 5);
    d.set(Calendar.MINUTE, 59);
    d.set(Calendar.SECOND, 13);
    d.set(Calendar.MILLISECOND, 0);
    Date date = d.getTime();
    String serializedDate = df.format(date) + "T05:59:13Z";
    assertEquals(serializedDate, SolrUtils.serializeDate(date));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils#serializeDateRange(Date, Date)}.
   */
  @Test
  public void testSerializeDateRange() {
    Calendar d = Calendar.getInstance();
    d.set(Calendar.MILLISECOND, 0);
    d.set(Calendar.SECOND, 0);
    d.set(Calendar.MINUTE, 0);
    d.set(Calendar.HOUR_OF_DAY, 0);
    Date startDate = d.getTime();
    d.add(Calendar.DAY_OF_MONTH, 2);
    d.set(Calendar.HOUR_OF_DAY, 5);
    d.set(Calendar.MINUTE, 59);
    Date endDate = d.getTime();
    String serializedStartDate = df.format(startDate) + "T00:00:00Z";
    String serializedEndDate = df.format(endDate) + "T05:59:00Z";
    String day = "[" + serializedStartDate + " TO " + serializedEndDate + "]";
    assertEquals(day, SolrUtils.serializeDateRange(startDate, endDate));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils#selectDay(java.util.Date)}.
   */
  @Test
  public void testSelectDay() {
    Date date = new Date();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    String dayStart = df.format(date) + "T00:00:00Z";
    String dayEnd = df.format(date) + "T23:59:59Z";
    String day = "[" + dayStart + " TO " + dayEnd + "]";
    assertEquals(day, SolrUtils.selectDay(date));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils#serializeUser(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testSerializeUser() {
    User user = new WebloungeUserImpl("heiri");
    assertEquals("heiri", SolrUtils.serializeUser(user));
  }

}
