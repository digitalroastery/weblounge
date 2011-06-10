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

package ch.entwine.weblounge.common.security;


import ch.entwine.weblounge.common.impl.content.page.PageSecurityContext;
import ch.entwine.weblounge.common.impl.security.SecurityContextImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.util.xml.XMLUtilities;

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
 * Testcase for {@link SecurityContextImpl}.
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
    path = XMLUtilities.getXPath();
  }

  /**
   * Constructor for PermissionSecurityContextTest.
   * 
   * @param arg0
   */
  public PermissionSecurityContextTest(String arg0) {
    super(arg0);
  }

  /**
   * Test for void allow(Permission, Authority)
   */
  @Test
  public final void testPermitPermissionAuthority() {
    Permission publish = SystemPermission.PUBLISH;
    Role translator = SystemRole.TRANSLATOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.allow(publish, translator);

    // Test (publish, translator) - expected: success
    if (!context.check(publish, translator)) {
      fail("Check for permission " + publish + " and role " + translator + " failed while it shouldn't");
    }
  }

  /**
   * Test for void allow(Permission, Authority[])
   */
  @Test
  public final void testPermitPermissionAuthorityArray() {
    Permission publish = SystemPermission.PUBLISH;
    Role translator = SystemRole.TRANSLATOR;
    Role editor = SystemRole.TRANSLATOR;

    // Initialize the weblounge admin
    // WebloungeAdminImpl.init("admin", "weblounge".getBytes(),
    // "admin@weblounge.org");

    // Create the security context
    SecurityContextImpl context = new PageSecurityContext();
    context.init(path, config);

    // Deny all
    context.allow(publish, translator);
    context.allow(publish, translator);

    // Test (publish, translator) - expected: success
    if (!context.check(publish, translator)) {
      fail("Check for permission " + publish + " and role " + translator + " failed while it shouldn't");
    }

    // Test (publish, editor) - expected: success
    if (!context.check(publish, editor)) {
      fail("Check for permission " + publish + " and role " + editor + " failed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Permission, Authority)
   */
  @Test
  public final void testDenyPermissionAuthority() {
    Permission write = SystemPermission.WRITE;
    Role translator = SystemRole.TRANSLATOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.deny(write, translator);

    // Test (write, editor) - expected: failure
    if (context.check(write, translator)) {
      fail("Check for permission " + write + " and role " + translator + " passed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Permission, Authority[])
   */
  @Test
  public final void testDenyPermissionAuthorityArray() {
    Permission write = SystemPermission.WRITE;
    Role translator = SystemRole.TRANSLATOR;
    Role editor = SystemRole.TRANSLATOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.deny(write, translator);
    context.deny(write, editor);

    // Test (write, translator) - expected: failure
    if (context.check(write, translator)) {
      fail("Check for permission " + write + " and role " + translator + " passed while it shouldn't");
    }

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for permission " + write + " and role " + editor + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll()
   */
  @Test
  public final void testDenyAll() {
    Permission write = SystemPermission.WRITE;
    Permission publish = SystemPermission.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.denyAll();

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for permission " + write + " and role " + editor + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (context.check(publish, publisher)) {
      fail("Check for permission " + write + " and role " + editor + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll(Permission)
   */
  @Test
  public final void testDenyAllPermission() {
    Permission write = SystemPermission.WRITE;
    Permission publish = SystemPermission.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role publisher = SystemRole.PUBLISHER;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Deny all
    context.denyAll(write);

    // Test (write, editor) - expected: failure
    if (context.check(write, editor)) {
      fail("Check for permission " + write + " and role " + editor + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (!context.check(publish, publisher)) {
      fail("Check for permission " + write + " and role " + editor + " failed while it shouldn't");
    }
  }

  /**
   * Test for boolean check(Permission, Authority)
   */
  @Test
  public final void testCheckPermissionAuthority() {
    Permission write = SystemPermission.WRITE;
    Permission publish = SystemPermission.PUBLISH;
    Role editor = SystemRole.EDITOR;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test (write, editor) - expected: success
    if (!context.check(write, editor)) {
      fail("Check for permission " + write + " and role " + editor + " failed while it shouldn't");
    }

    // Test (publish, editor) - expected: failure
    if (context.check(publish, editor)) {
      fail("Check for permission " + write + " and role " + editor + " passed while it shouldn't");
    }

  }

  /**
   * Test for boolean getAllowed(Permission)
   */
  @Test
  public final void testGetAllowed() {
    Permission write = SystemPermission.WRITE;
    Permission manage = SystemPermission.MANAGE;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test write permission - expected: 3
    Authority[] authorities = context.getAllowed(write);
    int expected = 3;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

    // Test undefined manage permission - expected: 0
    authorities = context.getAllowed(manage);
    expected = 0;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

    // Test null permission - expected: 0
    authorities = context.getAllowed(null);
    expected = 0;
    if (authorities.length != expected) {
      fail("Allowed authorities should be " + expected + " but found " + authorities.length);
    }

  }

  /**
   * Test for boolean getDenied(Permission)
   */
  @Test
  public final void testGetDenied() {
    Permission write = SystemPermission.WRITE;

    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    // Test write permission - expected: 0
    Authority[] authorities = context.getDenied(write);
    int expected = 0;
    if (authorities.length != expected) {
      fail("Denied authorities should be " + expected + " but found " + authorities.length);
    }

    // Test null permission - expected: 0
    authorities = context.getDenied(null);
    expected = 0;
    if (authorities.length != expected) {
      fail("Denied authorities should be " + expected + " but found " + authorities.length);
    }

  }

  /**
   * Test checkOne(Permission, Authority[])
   */
  @Test
  public final void testCheckOneOf() {
    Permission publish = SystemPermission.PUBLISH;
    Role editor = SystemRole.EDITOR;
    Role translator = SystemRole.TRANSLATOR;
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
    authorities = new Authority[] { translator, editor };
    if (context.checkOne(publish, authorities)) {
      fail("Neither " + translator + " nor " + editor + " were expected to pass");
    }
  }

  /**
   * Test checkAll(Permission, Authority[])
   */
  @Test
  public final void testCheckAllOf() {
    Permission write = SystemPermission.WRITE;
    Permission publish = SystemPermission.PUBLISH;
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
   * Test for permissions()
   */
  @Test
  public final void testPermissions() {
    // Create the security context
    SecurityContextImpl context = new SecurityContextImpl();
    context.init(path, config);

    int expected = 2;
    Permission[] p = context.permissions();
    if (p.length != expected) {
      fail("Found " + p.length + " permissions while " + expected + " were expected");
    }
  }

  /*
   * Class under test for Authority getAuthorization(Permission)
   */
  @Test
  public final void testGetAuthorizationPermission() {
    // TODO Implement getAuthorization().
  }

  /*
   * Class under test for Authority[] getAuthorization(Permission[])
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
    xml.append("<permission id=\"system:publish\" type=\"role\">system:publisher</permission>");
    xml.append("<permission id=\"system:write\" type=\"" + Role.class.getName() + "\">system:editor</permission>");
    xml.append("<permission id=\"system:write\" type=\"role\">system:editor,system:translator</permission>");
    xml.append("<permission id=\"system:write\" type=\"user\">tobias.wunden</permission>");
    xml.append("</security>");

    // Create xml builder
    DocumentBuilder builder = XMLUtilities.getDocumentBuilder();

    // Read document and create xml node
    root = builder.parse(new ByteArrayInputStream(xml.toString().getBytes()));
    return root;
  }

}