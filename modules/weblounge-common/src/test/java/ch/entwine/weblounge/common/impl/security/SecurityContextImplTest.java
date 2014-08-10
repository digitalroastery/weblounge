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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Securable.Order;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test case for {@link SecurityContextImpl}.
 */

public class SecurityContextImplTest extends TestCase {

  /** The context definition */
  protected SecurityContextImpl context;

  private User owner = new UserImpl("john.doe", "testland");

  /**
   * {@inheritDoc}
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  protected void setUp() throws Exception {
    super.setUp();
    context = new SecurityContextImpl(owner);
    context.setAllowDenyOrder(Order.AllowDeny);
    context.addAccessRule(new AllowAccessRule(SystemRole.EDITOR, SystemAction.WRITE));
    context.addAccessRule(new AllowAccessRule(new RoleImpl("weblounge:translator"), SystemAction.WRITE));
    context.addAccessRule(new AllowAccessRule(owner, SystemAction.WRITE));
    context.addAccessRule(new AllowAccessRule(owner, SystemAction.PUBLISH));
    context.addAccessRule(new DenyAllAccessRule(SystemAction.WRITE));
    context.addAccessRule(new DenyAllAccessRule(SystemAction.PUBLISH));
  }

  /**
   * Test for boolean getAllowed(Action)
   */
  @Test
  public final void testGetAllowed() {
    Action write = SystemAction.WRITE;
    Action manage = SystemAction.MANAGE;

    // Test write Action - expected: 3
    Authority[] authorities = context.getAllowed(write);
    int expected = 3;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

    // Test undefined manage Action - expected: 0
    authorities = context.getAllowed(manage);
    expected = 0;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

    // Test null Action - expected: 0
    authorities = context.getAllowed(null);
    expected = 0;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

  }

  /**
   * Test for boolean getDenied(Action)
   */
  @Test
  public final void testGetDenied() {
    Action write = SystemAction.WRITE;

    // Test write Action - expected: 0
    Authority[] authorities = context.getDenied(write);
    int expected = 1;
    if (authorities.length != expected) {
      fail("Denied authorities should be " + expected + " but found " + authorities.length);
    }

    // Test null Action - expected: 0
    authorities = context.getDenied(null);
    expected = 0;
    if (authorities.length != expected) {
      fail("Denied authorities should be " + expected + " but found " + authorities.length);
    }

  }

  /**
   * Test for actions()
   */
  @Test
  public final void testActions() {
    int expected = 4;
    Action[] actions = context.getActions();
    if (actions.length != expected) {
      fail("Found " + actions.length + " actions while " + expected + " were expected");
    }
  }

}