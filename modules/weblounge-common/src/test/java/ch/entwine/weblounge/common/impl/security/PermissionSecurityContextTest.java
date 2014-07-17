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


import ch.entwine.weblounge.common.impl.content.page.PageSecurityContext;
import ch.entwine.weblounge.common.impl.util.xml.XMLUtils;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SystemAction;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

/**
 * Test case for {@link SecurityContextImpl}.
 */

public class PermissionSecurityContextTest extends TestCase {

  /** The xml context definition */
  private Node config;

  /** the XPath object used to parse the configuration */
  private XPath path;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(PermissionSecurityContextTest.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  protected void setUp() throws Exception {
    super.setUp();
    config = createSecurityContext();
    path = XMLUtils.getXPath();
  }

  /**
   * Test for void allow(Action, Authority)
   */
  @Test
  public final void testPermitPermissionAuthority() {
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.allow(publish, editor);

    // Test (publish, translator) - expected: success
    if (!context.check(publish, editor)) {
      fail("Check for Action " + publish + " and role " + editor + " failed while it shouldn't");
    }
  }

  /**
   * Test for void allow(Action, Authority[])
   */
  @Test
  public final void testPermitPermissionAuthorityArray() {
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;

    // Initialize the weblounge admin
    // WebloungeAdminImpl.init("admin", "weblounge".getBytes(),
    // "admin@weblounge.org");

    // Create the security context
    SecurityContextImpl context = new PageSecurityContext();
    context.init(path, config);

    // Deny all
    context.allow(publish, editor);

    // Test (publish, editor) - expected: success
    if (!context.check(publish, editor)) {
      fail("Check for Action " + publish + " and role " + editor + " failed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Action, Authority)
   */
  @Test
  public final void testDenyPermissionAuthority() {
    Action write = SystemAction.WRITE;
    Role editor = SystemRole.EDITOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.deny(write, editor);

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Action, Authority[])
   */
  @Test
  public final void testDenyPermissionAuthorityArray() {
    Action write = SystemAction.WRITE;
    Role editor = SystemRole.EDITOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.deny(write, editor);

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll()
   */
  @Test
  public final void testDenyAll() {
    Action write = SystemAction.WRITE;
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.denyAll();

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (context.check(publish, publisher)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll(Action)
   */
  @Test
  public final void testDenyAllPermission() {
    Action write = SystemAction.WRITE;
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.denyAll(write);

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (!context.check(publish, publisher)) {
      fail("Check for Action " + write + " and role " + editor + " failed while it shouldn't");
    }
  }

  /**
   * Test for boolean check(Action, Authority)
   */
  @Test
  public final void testCheckPermissionAuthority() {
    Action write = SystemAction.WRITE;
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test (write, editor) - expected: success
    if (!context.check(write, editor)) {
      fail("Check for Action " + write + " and role " + editor + " failed while it shouldn't");
    }

    // Test (publish, editor) - expected: failure
    if (context.check(publish, editor)) {
      fail("Check for Action " + write + " and role " + editor + " passed while it shouldn't");
    }

  }

  /**
   * Test for boolean getAllowed(Action)
   */
  @Test
  public final void testGetAllowed() {
    Action write = SystemAction.WRITE;
    Action manage = SystemAction.MANAGE;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

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

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test write Action - expected: 0
    Authority[] authorities = context.getDenied(write);
    int expected = 0;
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
   * Test checkOne(Action, Authority[])
   */
  @Test
  public final void testCheckOneOf() {
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role guest = SystemRole.GUEST;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test one of (editor, publisher) - expected: success
    Authority[] authorities = new Authority[] { editor, publisher };
    if (!context.checkOne(publish, authorities)) {
      fail(publisher + " was expected to pass but failed");
    }

    // Test one of (translator, editor) - expected: failure
    authorities = new Authority[] { guest, editor };
    if (context.checkOne(publish, authorities)) {
      fail("Neither " + guest + " nor " + editor + " were expected to pass");
    }
  }

  /**
   * Test checkAll(Action, Authority[])
   */
  @Test
  public final void testCheckAllOf() {
    Action write = SystemAction.WRITE;
    Action publish = SystemAction.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test one of (editor, publisher) - expected: success
    Authority[] authorities = new Authority[] { editor, publisher };
    if (!context.checkAll(write, authorities)) {
      fail("Both " + editor + " and " + publisher + " were expected to pass but failed");
    }

    // Test one of (editor, publisher) - expected: failure
    if (context.checkAll(publish, authorities)) {
      fail(editor + " was expected to fail");
    }
  }

  /**
   * Test for actions()
   */
  @Test
  public final void testPermissions() {
    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    int expected = 2;
    Action[] actions = context.actions();
    if (actions.length != expected) {
      fail("Found " + actions.length + " actions while " + expected + " were expected");
    }
  }

  /*
   * Class under test for Authority getAuthorization(Action)
   */
  @Test
  public final void testGetAuthorizationPermission() {
    // TODO Implement getAuthorization().
  }

  /*
   * Class under test for Authority[] getAuthorization(Action[])
   */
  @Test
  public final void testGetAuthorizationPermissionArray() {
    // TODO Implement getAuthorization().
  }

  /**
   * Creates a simple security context definition.
   * 
   * @return the security context definition
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private Node createSecurityContext() throws ParserConfigurationException,
      SAXException, IOException {
    Node root;
    StringBuffer xml = new StringBuffer();
    xml.append("<security>");
    xml.append("<owner>tobias.wunden</owner>");
    xml.append("<permission id=\"weblounge:publish\" type=\"role\">weblounge:publisher</permission>");
    xml.append("<permission id=\"weblounge:write\" type=\"" + Role.class.getName() + "\">weblounge:editor</permission>");
    xml.append("<permission id=\"weblounge:write\" type=\"role\">weblounge:editor,weblounge:translator</permission>");
    xml.append("<permission id=\"weblounge:write\" type=\"user\">tobias.wunden</permission>");
    xml.append("</security>");

    // Create xml builder
    DocumentBuilder builder = XMLUtils.getDocumentBuilder();

    // Read document and create xml node
    root = builder.parse(new ByteArrayInputStream(xml.toString().getBytes()));
    return root;
  }

}