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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the web url implementation.
 */
public class WebUrlImplTest {
  
  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl liveUrl = null;

  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl flavoredLiveUrl = null;

  /** The localized live url */
  protected WebUrlImpl localizedLiveUrl = null;

  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl flavoredLocalizedLiveUrl = null;

  /** The localized segmented live url */
  protected WebUrlImpl localizedSegmentedLiveUrl = null;

  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl flavoredSegmentedLiveUrl = null;

  /** Url instance pointing to JSON output of the document's live version */
  protected WebUrlImpl flavoredLocalizedSegmentedLiveUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl workUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl flavoredWorkUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl localizedWorkUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl flavoredLocalizedWorkUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl localizedSegmentedWorkUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl flavoredSegmentedWorkUrl = null;

  /** Url instance pointing to JSON output of the document's work version */
  protected WebUrlImpl flavoredLocalizedSegmentedWorkUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl versionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl flavoredVersionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl localizedVersionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl flavoredLocalizedVersionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl localizedSegmentedVersionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl flavoredSegmentedVersionedUrl = null;

  /** Url instance pointing to JSON output of the document's version 17 */
  protected WebUrlImpl flavoredLocalizedSegmentedVersionedUrl = null;

  /** Default url path */
  protected String livePath = "/test/";

  /** Segmented live path */
  protected String segmentedLivePath = "/test/";

  /** Localized live path */
  protected String localizedLivePath = "/test/index_de.html";

  /** JSON path to live version */
  protected String flavoredLivePath = "/test/index.json";

  /** JSON path to live version */
  protected String flavoredLocalizedLivePath = "/test/index_de.json";

  /** JSON path to live version */
  protected String flavoredSegmentedLivePath = "/test/json/";

  /** Localized live path */
  protected String localizedSegmentedLivePath = "/test/de/";

  /** JSON path to live version */
  protected String flavoredLocalizedSegmentedLivePath = "/test/de/json";

  /** JSON path to work version */
  protected String workPath = "/test/work.html";

  /** JSON path to work version */
  protected String flavoredWorkPath = "/test/work.json";

  /** JSON path to work version */
  protected String localizedWorkPath = "/test/work_de.html";

  /** JSON path to work version */
  protected String flavoredLocalizedWorkPath = "/test/work_de.json";

  /** JSON path to work version */
  protected String flavoredSegmentedWorkPath = "/test/work/json/";

  /** JSON path to work version */
  protected String localizedSegmentedWorkPath = "/test/work/de/";

  /** JSON path to work version */
  protected String flavoredLocalizedSegmentedWorkPath = "/test/work/de/json/";

  /** JSON path to version 17 */
  protected String versionedPath = "/test/17.html";

  /** JSON path to version 17 */
  protected String flavoredVersionedPath = "/test/17.json";

  /** JSON path to version 17 */
  protected String localizedVersionedPath = "/test/17_de.html";

  /** JSON path to version 17 */
  protected String flavoredLocalizedVersionedPath = "/test/17_de.json";

  /** JSON path to version 17 */
  protected String flavoredSegmentedVersionedPath = "/test/17/json/";

  /** JSON path to version 17 */
  protected String localizedSegmentedVersionedPath = "/test/17/de/";

  /** JSON path to version 17 */
  protected String flavoredLocalizedSegmentedVersionedPath = "/test/17/de/json/";

  /** The mock site */
  protected Site siteMock = null;

  /** The mock site */
  protected Site otherSiteMock = null;

  /** The English language */
  protected Language english = LanguageSupport.getLanguage("en");

  /** The German language */
  protected Language german = LanguageSupport.getLanguage("de");
  
  /** The French language */
  protected Language french = LanguageSupport.getLanguage("fr");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    siteMock = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getLanguage("de")).andReturn(german);
    EasyMock.expect(siteMock.getDefaultLanguage()).andReturn(english);
    EasyMock.replay(siteMock);
    otherSiteMock = EasyMock.createNiceMock(Site.class);
    
    // Env setup
    // TODO: Remove
    Env.set("system.uri", "/");
    Env.set("system.servletpath", "/");
    
    liveUrl = new WebUrlImpl(siteMock, livePath);
    localizedLiveUrl = new WebUrlImpl(siteMock, localizedLivePath);
    flavoredLiveUrl = new WebUrlImpl(siteMock, flavoredLivePath);
    flavoredLocalizedLiveUrl = new WebUrlImpl(siteMock, flavoredLocalizedLivePath);
    localizedSegmentedLiveUrl = new WebUrlImpl(siteMock, localizedSegmentedLivePath);
    flavoredSegmentedLiveUrl = new WebUrlImpl(siteMock, flavoredSegmentedLivePath);
    flavoredLocalizedSegmentedLiveUrl = new WebUrlImpl(siteMock, flavoredLocalizedSegmentedLivePath);
    workUrl = new WebUrlImpl(siteMock, workPath);
    flavoredWorkUrl = new WebUrlImpl(siteMock, flavoredWorkPath);
    localizedWorkUrl = new WebUrlImpl(siteMock, localizedWorkPath);
    flavoredLocalizedWorkUrl = new WebUrlImpl(siteMock, flavoredLocalizedWorkPath);
    flavoredSegmentedWorkUrl = new WebUrlImpl(siteMock, flavoredSegmentedWorkPath);
    localizedSegmentedWorkUrl = new WebUrlImpl(siteMock, localizedSegmentedWorkPath);
    flavoredLocalizedSegmentedWorkUrl = new WebUrlImpl(siteMock, flavoredLocalizedSegmentedWorkPath);
    versionedUrl = new WebUrlImpl(siteMock, versionedPath);
    flavoredVersionedUrl = new WebUrlImpl(siteMock, flavoredVersionedPath);
    localizedVersionedUrl = new WebUrlImpl(siteMock, localizedVersionedPath);
    flavoredLocalizedVersionedUrl = new WebUrlImpl(siteMock, flavoredLocalizedVersionedPath);
    flavoredSegmentedVersionedUrl = new WebUrlImpl(siteMock, flavoredSegmentedVersionedPath);
    localizedSegmentedVersionedUrl = new WebUrlImpl(siteMock, localizedSegmentedVersionedPath);
    flavoredLocalizedSegmentedVersionedUrl = new WebUrlImpl(siteMock, flavoredLocalizedSegmentedVersionedPath);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(liveUrl.equals(liveUrl));
    assertTrue(flavoredLiveUrl.equals(flavoredLiveUrl));
    assertTrue(flavoredLocalizedLiveUrl.equals(flavoredLocalizedLiveUrl));
    assertFalse(liveUrl.equals(flavoredLiveUrl));
    assertFalse(liveUrl.equals(flavoredLocalizedLiveUrl));
    assertFalse(flavoredLiveUrl.equals(flavoredWorkUrl));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(siteMock, liveUrl.getSite());
    assertEquals(siteMock, flavoredLiveUrl.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(livePath, liveUrl.getPath());
    assertEquals(livePath, flavoredLiveUrl.getPath());
    assertEquals(livePath, localizedLiveUrl.getPath());
    assertEquals(livePath, flavoredLocalizedLiveUrl.getPath());
    assertEquals(livePath, flavoredSegmentedLiveUrl.getPath());
    assertEquals(livePath, localizedSegmentedLiveUrl.getPath());
    assertEquals(livePath, flavoredLocalizedSegmentedLiveUrl.getPath());
    assertEquals(livePath, workUrl.getPath());
    assertEquals(livePath, flavoredWorkUrl.getPath());
    assertEquals(livePath, localizedWorkUrl.getPath());
    assertEquals(livePath, flavoredLocalizedWorkUrl.getPath());
    assertEquals(livePath, flavoredSegmentedWorkUrl.getPath());
    assertEquals(livePath, localizedSegmentedWorkUrl.getPath());
    assertEquals(livePath, flavoredLocalizedSegmentedWorkUrl.getPath());
    assertEquals(livePath, versionedUrl.getPath());
    assertEquals(livePath, flavoredVersionedUrl.getPath());
    assertEquals(livePath, localizedVersionedUrl.getPath());
    assertEquals(livePath, flavoredLocalizedVersionedUrl.getPath());
    assertEquals(livePath, flavoredSegmentedVersionedUrl.getPath());
    assertEquals(livePath, localizedSegmentedVersionedUrl.getPath());
    assertEquals(livePath, flavoredLocalizedSegmentedVersionedUrl.getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink()}.
   */
  @Test
  public void testGetLink() {
    assertEquals(livePath, liveUrl.getLink());
    assertTrue(flavoredLiveUrl.getLink().endsWith(flavoredLivePath));
    assertTrue(localizedLiveUrl.getLink().endsWith(localizedLivePath));
    assertTrue(flavoredLocalizedLiveUrl.getLink().endsWith(flavoredLocalizedLivePath));
    assertTrue(flavoredSegmentedLiveUrl.getLink().endsWith(flavoredLivePath));
    assertTrue(localizedSegmentedLiveUrl.getLink().endsWith(localizedLivePath));
    assertTrue(flavoredLocalizedSegmentedLiveUrl.getLink().endsWith(flavoredLocalizedLivePath));
    assertEquals(workPath, workUrl.getLink());
    assertTrue(flavoredWorkUrl.getLink().endsWith(flavoredWorkPath));
    assertTrue(localizedWorkUrl.getLink().endsWith(localizedWorkPath));
    assertTrue(flavoredLocalizedWorkUrl.getLink().endsWith(flavoredLocalizedWorkPath));
    assertTrue(flavoredSegmentedWorkUrl.getLink().endsWith(flavoredWorkPath));
    assertTrue(localizedSegmentedWorkUrl.getLink().endsWith(localizedWorkPath));
    assertTrue(flavoredLocalizedSegmentedWorkUrl.getLink().endsWith(flavoredLocalizedWorkPath));
    assertEquals(versionedPath, versionedUrl.getLink());
    assertTrue(flavoredVersionedUrl.getLink().endsWith(flavoredVersionedPath));
    assertTrue(localizedVersionedUrl.getLink().endsWith(localizedVersionedPath));
    assertTrue(flavoredLocalizedVersionedUrl.getLink().endsWith(flavoredLocalizedVersionedPath));
    assertTrue(flavoredSegmentedVersionedUrl.getLink().endsWith(flavoredVersionedPath));
    assertTrue(localizedSegmentedVersionedUrl.getLink().endsWith(localizedVersionedPath));
    assertTrue(flavoredLocalizedSegmentedVersionedUrl.getLink().endsWith(flavoredLocalizedVersionedPath));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetLinkLanguage() {
    String l = "_fr";
    assertEquals(localizedLivePath.replaceAll("_de", l), liveUrl.getLink(french));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l), flavoredLiveUrl.getLink(french));
    assertEquals(localizedLivePath.replaceAll("_de", l), localizedLiveUrl.getLink(french));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l), flavoredSegmentedLiveUrl.getLink(french));
    assertEquals(localizedLivePath.replaceAll("_de", l), localizedSegmentedLiveUrl.getLink(french));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l), flavoredLocalizedLiveUrl.getLink(french));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l), flavoredLocalizedSegmentedLiveUrl.getLink(french));
    assertEquals(localizedWorkPath.replaceAll("_de", l), workUrl.getLink(french));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l), flavoredWorkUrl.getLink(french));
    assertEquals(localizedWorkPath.replaceAll("_de", l), localizedWorkUrl.getLink(french));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l), flavoredSegmentedWorkUrl.getLink(french));
    assertEquals(localizedWorkPath.replaceAll("_de", l), localizedSegmentedWorkUrl.getLink(french));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l), flavoredLocalizedWorkUrl.getLink(french));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l), flavoredLocalizedSegmentedWorkUrl.getLink(french));
    assertEquals(localizedVersionedPath.replaceAll("_de", l), versionedUrl.getLink(french));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l), flavoredVersionedUrl.getLink(french));
    assertEquals(localizedVersionedPath.replaceAll("_de", l), localizedVersionedUrl.getLink(french));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l), flavoredSegmentedVersionedUrl.getLink(french));
    assertEquals(localizedVersionedPath.replaceAll("_de", l), localizedSegmentedVersionedUrl.getLink(french));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l), flavoredLocalizedVersionedUrl.getLink(french));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l), flavoredLocalizedSegmentedVersionedUrl.getLink(french));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(java.lang.String)}.
   */
  @Test
  public void testGetLinkString() {
    String flavor = "pdf";
    assertEquals(flavoredLivePath.replaceAll("json", flavor), liveUrl.getLink(flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("json", flavor), localizedLiveUrl.getLink(flavor));
    assertEquals(flavoredLivePath.replaceAll("json", flavor), flavoredLiveUrl.getLink(flavor));
    assertEquals(flavoredLivePath.replaceAll("json", flavor), flavoredSegmentedLiveUrl.getLink(flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("json", flavor), localizedSegmentedLiveUrl.getLink(flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("json", flavor), flavoredLocalizedLiveUrl.getLink(flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("json", flavor), flavoredLocalizedSegmentedLiveUrl.getLink(flavor));
    assertEquals(flavoredWorkPath.replaceAll("json", flavor), workUrl.getLink(flavor));
    assertEquals(flavoredWorkPath.replaceAll("json", flavor), flavoredWorkUrl.getLink(flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("json", flavor), localizedWorkUrl.getLink(flavor));
    assertEquals(flavoredWorkPath.replaceAll("json", flavor), flavoredSegmentedWorkUrl.getLink(flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("json", flavor), localizedSegmentedWorkUrl.getLink(flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("json", flavor), flavoredLocalizedWorkUrl.getLink(flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("json", flavor), flavoredLocalizedSegmentedWorkUrl.getLink(flavor));
    assertEquals(flavoredVersionedPath.replaceAll("json", flavor), versionedUrl.getLink(flavor));
    assertEquals(flavoredVersionedPath.replaceAll("json", flavor), flavoredVersionedUrl.getLink(flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("json", flavor), localizedVersionedUrl.getLink(flavor));
    assertEquals(flavoredVersionedPath.replaceAll("json", flavor), flavoredSegmentedVersionedUrl.getLink(flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("json", flavor), localizedSegmentedVersionedUrl.getLink(flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("json", flavor), flavoredLocalizedVersionedUrl.getLink(flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("json", flavor), flavoredLocalizedSegmentedVersionedUrl.getLink(flavor));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(long)}.
   */
  @Test
  public void testGetLinkLong() {
    long version = 13;
    String v = Long.toString(version);
    assertEquals(workPath.replaceAll("work", v), liveUrl.getLink(version));
    assertEquals(flavoredLivePath.replaceAll("index", v), flavoredLiveUrl.getLink(version));
    assertEquals(localizedLivePath.replaceAll("index", v), localizedLiveUrl.getLink(version));
    assertEquals(flavoredLivePath.replaceAll("index", v), flavoredSegmentedLiveUrl.getLink(version));
    assertEquals(localizedLivePath.replaceAll("index", v), localizedSegmentedLiveUrl.getLink(version));
    assertEquals(flavoredLocalizedLivePath.replaceAll("index", v), flavoredLocalizedLiveUrl.getLink(version));
    assertEquals(flavoredLocalizedLivePath.replaceAll("index", v), flavoredLocalizedSegmentedLiveUrl.getLink(version));
    assertEquals(workPath.replaceAll("work", v), workUrl.getLink(version));
    assertEquals(flavoredWorkPath.replaceAll("work", v), flavoredWorkUrl.getLink(version));
    assertEquals(localizedWorkPath.replaceAll("work", v), localizedWorkUrl.getLink(version));
    assertEquals(flavoredWorkPath.replaceAll("work", v), flavoredSegmentedWorkUrl.getLink(version));
    assertEquals(localizedWorkPath.replaceAll("work", v), localizedSegmentedWorkUrl.getLink(version));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("work", v), flavoredLocalizedWorkUrl.getLink(version));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("work", v), flavoredLocalizedSegmentedWorkUrl.getLink(version));
    assertEquals(versionedPath.replaceAll("17", v), versionedUrl.getLink(version));
    assertEquals(flavoredVersionedPath.replaceAll("17", v), flavoredVersionedUrl.getLink(version));
    assertEquals(localizedVersionedPath.replaceAll("17", v), localizedVersionedUrl.getLink(version));
    assertEquals(flavoredVersionedPath.replaceAll("17", v), flavoredSegmentedVersionedUrl.getLink(version));
    assertEquals(localizedVersionedPath.replaceAll("17", v), localizedSegmentedVersionedUrl.getLink(version));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("17", v), flavoredLocalizedVersionedUrl.getLink(version));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("17", v), flavoredLocalizedSegmentedVersionedUrl.getLink(version));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getLink(long, ch.o2it.weblounge.common.language.Language, java.lang.String)}.
   */
  @Test
  public void testGetLinkStringLanguageLong() {
    String flavor = "pdf";
    long version = 13;
    String v = Long.toString(version);
    String l = "_fr";
    assertEquals(localizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("html", flavor), liveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), flavoredLiveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), localizedLiveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), flavoredSegmentedLiveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), localizedSegmentedLiveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), flavoredLocalizedLiveUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedLivePath.replaceAll("_de", l).replaceAll("index", v).replaceAll("json", flavor), flavoredLocalizedSegmentedLiveUrl.getLink(version, french, flavor));
    assertEquals(localizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("html", flavor), workUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), flavoredWorkUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), localizedWorkUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), flavoredSegmentedWorkUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), localizedSegmentedWorkUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), flavoredLocalizedWorkUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedWorkPath.replaceAll("_de", l).replaceAll("work", v).replaceAll("json", flavor), flavoredLocalizedSegmentedWorkUrl.getLink(version, french, flavor));
    assertEquals(localizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("html", flavor), versionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), flavoredVersionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), localizedVersionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), flavoredSegmentedVersionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), localizedSegmentedVersionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), flavoredLocalizedVersionedUrl.getLink(version, french, flavor));
    assertEquals(flavoredLocalizedVersionedPath.replaceAll("_de", l).replaceAll("17", v).replaceAll("json", flavor), flavoredLocalizedSegmentedVersionedUrl.getLink(version, french, flavor));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.url.WebUrlImpl#getFlavor()}.
   */
  @Test
  public void testGetFlavor() {
    assertTrue(liveUrl.getFlavor() == null);
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(flavoredSegmentedLiveUrl.getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/json").getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/work/json").getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/de/json").getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/work/de/json").getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/JSON").getFlavor()));
    assertEquals(RequestFlavor.JSON, RequestFlavor.parseString(new WebUrlImpl(siteMock, "/test/json/").getFlavor()));
  }

}
