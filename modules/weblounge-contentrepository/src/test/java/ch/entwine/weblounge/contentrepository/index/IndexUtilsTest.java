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

package ch.entwine.weblounge.contentrepository.index;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.search.impl.IndexUtils;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Test case for {@link IndexUtils}.
 */
public class IndexUtilsTest {

  /** The date format */
  protected final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Test method for {@link ch.entwine.weblounge.search.impl.IndexUtils#clean(java.lang.String)}.
   */
  @Test
  public void testClean() {
    String test = "+-!(){}[]^\"~*?:&&||&|";
    String expected = "\\+\\-\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\&\\&\\|\\|&|";
    assertEquals(expected, IndexUtils.clean(test));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.search.impl.IndexUtils#serializeDate(java.util.Date)}.
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
    assertEquals(serializedDate, IndexUtils.serializeDate(date));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.search.impl.IndexUtils#serializeDateRange(Date, Date)}.
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
    assertEquals(day, IndexUtils.serializeDateRange(startDate, endDate));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.search.impl.IndexUtils#serializeUserId(ch.entwine.weblounge.common.security.User)}.
   */
  @Test
  public void testSerializeUser() {
    User user = new WebloungeUserImpl("heiri");
    assertEquals("heiri", IndexUtils.serializeUserId(user));
  }
  
  /** Test method for {@link IndexUtils#serializeAuthority(ch.entwine.weblounge.common.security.Authority)} */
  @Test
  public void testSerializeAuthority() {
    assertNull(IndexUtils.serializeAuthority(null));
    assertEquals("weblounge:tom", IndexUtils.serializeAuthority(new WebloungeUserImpl("tom")));
    assertEquals("bigsite:vip", IndexUtils.serializeAuthority(new RoleImpl("bigsite", "vip")));
  }

}
