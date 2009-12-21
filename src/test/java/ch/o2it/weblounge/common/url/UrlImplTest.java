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

package ch.o2it.weblounge.common.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Test case for the general url implementation.
 */
public class UrlImplTest {

  /** The sample url instance */
  protected UrlImpl defaultUrl = null;

  /** The sample path instance */
  protected UrlImpl defaultPath = null;

  /** Sample url path */
  protected String defaultUrlPath = "/test";

  /** Sample url path */
  protected String defaultParentUrlPath = "/";

  /** Sample file path */
  protected String defaultFilePath = File.separator + "test";

  /** Sample file path */
  protected String defaultParentFilePath = File.separator;

  /** The url path separator */
  protected final static char urlPathSeparator = '/';

  /** The windows file path separator */
  protected final static char filePathSeparator = File.separatorChar;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    defaultUrl = new UrlImpl(defaultUrlPath, urlPathSeparator);
    defaultPath = new UrlImpl(defaultFilePath, filePathSeparator);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#UrlImpl(java.lang.String, char)}.
   */
  @Test
  public void testUrlImplStringChar() {
    try {
      new UrlImpl("test", '/');
      fail("UrlImpl accepted relative url");
    } catch (IllegalArgumentException e) { /* this is intended */ }

    try {
      new UrlImpl(null);
      fail("UrlImpl accepted null url");
    } catch (IllegalArgumentException e) { /* this is intended */ }

    try {
      new UrlImpl(null);
      fail("UrlImpl accepted null url");
    } catch (IllegalArgumentException e) { /* this is intended */ }
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#UrlImpl(java.lang.String, char)}.
   */
  @Test
  public void testUrlImplUrlString() {
    Url u = new UrlImpl(defaultUrl, "a/b");
    String extendedPath = UrlSupport.concat(defaultUrlPath, "a/b", true);
    assertEquals(u.getPath(), extendedPath);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#getPathSeparator()}.
   */
  @Test
  public void testGetSeparator() {
    assertEquals(defaultUrl.getPathSeparator(), '/');
    assertEquals(defaultPath.getPathSeparator(), File.separatorChar);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#getPath()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(UrlSupport.trim(defaultUrlPath), defaultUrl.getPath());
    assertEquals(PathSupport.trim(defaultFilePath), defaultPath.getPath());
    assertEquals("/", new UrlImpl("/").getPath());
    assertEquals("/", new UrlImpl("//").getPath());
    assertFalse("/".equals(new UrlImpl("/test").getPath()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(defaultUrl.equals(new UrlImpl(defaultUrlPath)));
    assertTrue(defaultUrl.equals(new UrlImpl(defaultFilePath)));
    assertTrue(defaultUrl.equals(new UrlImpl("/test/")));
    assertFalse(defaultUrl.equals(new UrlImpl("/test/a")));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#startsWith(java.lang.String)}.
   */
  @Test
  public void testStartsWith() {
    Url u = new UrlImpl("/ab/c/d");
    assertTrue(u.startsWith("/ab"));
    assertFalse(u.startsWith("ab"));
    assertFalse(u.startsWith("abc"));
    assertFalse(u.startsWith("/abc"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#endsWith(java.lang.String)}.
   */
  @Test
  public void testEndsWith() {
    Url u = new UrlImpl("/ab/c/d");
    assertTrue(u.endsWith("d"));
    assertTrue(u.endsWith("/d"));
    assertTrue(u.endsWith("/d/"));
    assertFalse(u.endsWith("cd"));
    assertFalse(u.endsWith("c/d/f"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#isPrefixOf(ch.o2it.weblounge.common.url.Url)}.
   */
  @Test
  public void testIsPrefixOf() {
    Url u1 = new UrlImpl(defaultUrl, "/abc");
    Url u2 = new UrlImpl("/abc");
    assertTrue(defaultUrl.isPrefixOf(u1));
    assertTrue(defaultUrl.isPrefixOf(defaultUrl));
    assertFalse(defaultUrl.isPrefixOf(u2));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#isExtensionOf(ch.o2it.weblounge.common.url.Url)}.
   */
  @Test
  public void testIsExtensionOf() {
    Url u1 = new UrlImpl(defaultUrl, "/abc");
    Url u2 = new UrlImpl("/abc");
    assertTrue(u1.isExtensionOf(defaultUrl));
    assertTrue(defaultUrl.isExtensionOf(defaultUrl));
    assertFalse(u1.isExtensionOf(u2));
    assertFalse(u2.isExtensionOf(u1));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.UrlImpl#trim(java.lang.String)}.
   */
  @Test
  public void testTrim() {
    assertEquals("/test/", new UrlImpl("/test").getPath());
    assertEquals("/test/", new UrlImpl("/test/").getPath());
  }

}
