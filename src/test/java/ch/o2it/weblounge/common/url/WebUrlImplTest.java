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

import static ch.o2it.weblounge.common.url.WebUrl.Flavor.html;
import static ch.o2it.weblounge.common.url.WebUrl.Flavor.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the web url implementation.
 */
public class WebUrlImplTest {
  
  /** The default url instance */
  protected WebUrlImpl defaultUrl = null;
  
  /** The default url instance */
  protected WebUrlImpl flavoredUrl = null;

  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl flavoredLiveUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl flavoredWorkUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl flavoredVersionedUrl = null;

  /** Default url path */
  protected String defaultPath = "/test";

  /** Trimmed default url path */
  protected String defaultTrimmedPath = UrlSupport.trim(defaultPath);

  /** JSON path to live version */
  protected String flavoredLivePath = "/test/index.json";

  /** JSON path to work version */
  protected String flavoredWorkPath = "/test/work.json";

  /** JSON path to version 17 */
  protected String flavoredVersionedPath = "/test/17.json";

  /** The mock site */
  protected Site siteMock = null;

  /** The mock site */
  protected Site otherSiteMock = null;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    siteMock = EasyMock.createNiceMock(Site.class);
    // TODO: Finish mock setup
    otherSiteMock = EasyMock.createNiceMock(Site.class);
    // TODO: Finish mock setup
    
    // Env setup
    // TODO: Remove
    Env.set("system.uri", "/");
    Env.set("system.servletpath", "/");
    
    defaultUrl = new WebUrlImpl(siteMock, defaultPath);
    flavoredUrl = new WebUrlImpl(siteMock, flavoredLivePath);
    flavoredLiveUrl = new WebUrlImpl(siteMock, flavoredLivePath);
    flavoredWorkUrl = new WebUrlImpl(siteMock, flavoredWorkPath);
    flavoredVersionedUrl = new WebUrlImpl(siteMock, flavoredVersionedPath);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(defaultUrl.equals(defaultUrl));
    assertTrue(flavoredUrl.equals(flavoredUrl));
    assertFalse(defaultUrl.equals(flavoredUrl));
    assertFalse(flavoredLiveUrl.equals(flavoredWorkUrl));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(siteMock, defaultUrl.getSite());
    assertEquals(siteMock, flavoredUrl.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink()}.
   */
  @Test
  public void testGetLink() {
    assertEquals("/test/index.html", defaultUrl.getLink());
    assertTrue(flavoredLiveUrl.getLink().endsWith(flavoredLivePath));
    assertTrue(flavoredWorkUrl.getLink().endsWith(flavoredWorkPath));
    assertTrue(flavoredVersionedUrl.getLink().endsWith(flavoredVersionedPath));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(defaultTrimmedPath, defaultUrl.getPath());
    assertEquals(defaultTrimmedPath, flavoredLiveUrl.getPath());
    assertEquals(defaultTrimmedPath, flavoredWorkUrl.getPath());
    assertEquals(defaultTrimmedPath, flavoredVersionedUrl.getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(java.lang.String)}.
   */
  @Test
  public void testGetLinkString() {
    assertEquals("/test/index.pdf", defaultUrl.getLink("pdf"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(long)}.
   */
  @Test
  public void testGetLinkLong() {
    assertEquals("/test/13.html", defaultUrl.getLink(13));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(java.lang.String, long)}.
   */
  @Test
  public void testGetLinkStringLong() {
    assertEquals("/test/28.json", defaultUrl.getLink("json", 28));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getFlavor()}.
   */
  @Test
  public void testGetFlavor() {
    assertEquals(html.toString(), defaultUrl.getFlavor());
    assertEquals(json.toString(), flavoredUrl.getFlavor());
    assertEquals(json.toString(), new WebUrlImpl(siteMock, "/test/json").getFlavor());
    assertEquals(json.toString(), new WebUrlImpl(siteMock, "/test/JSON").getFlavor());
    assertEquals(html.toString(), new WebUrlImpl(siteMock, "/test/json/").getFlavor());
  }

}
