/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.bridge.mail;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link Account}.
 */
public class AccountTest {

  /** The account under test */
  protected Account account = null;

  /** Account host */
  protected String host = "host.com";

  /** Account login name */
  protected String login = "login";

  /** Account password */
  protected String password = "password";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    account = new Account(host, login, password);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.bridge.mail.Account#getHost()}.
   */
  @Test
  public void testGetHost() {
    assertEquals(host, account.getHost());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.bridge.mail.Account#getLogin()}.
   */
  @Test
  public void testGetLogin() {
    assertEquals(login, account.getLogin());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.bridge.mail.Account#getPassword()}
   * .
   */
  @Test
  public void testGetPassword() {
    assertEquals(password, account.getPassword());
  }

}
