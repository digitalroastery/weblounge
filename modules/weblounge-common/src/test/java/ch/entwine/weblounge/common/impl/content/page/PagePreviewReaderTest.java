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

package ch.entwine.weblounge.common.impl.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Locale;

/**
 * Test case for the {@link PagePreviewReaderTest} class.
 */
public class PagePreviewReaderTest {

  /** The preview to read */
  protected String previewFile = "/pagepreview.xml";

  /** The page uri */
  protected ResourceURIImpl pageURI = null;

  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** The Italian language */
  protected Language italian = new LanguageImpl(new Locale("it"));

  /** The site */
  protected Site site = null;

  /** The stage composer */
  protected ComposerImpl stage = null;

  /** Name of the composer */
  protected String composerName = "preview";

  /** Pagelet module */
  protected String titleModule = "text";

  /** Pagelet identifier */
  protected String titlePagelet = "title";

  /**
   * Setup for the test cases.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    stage = new ComposerImpl(composerName);
    stage.addPagelet(new PageletImpl(titleModule, titlePagelet));
  }

  /**
   * Preliminary setup work.
   */
  protected void setupPrerequisites() {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getDefaultLanguage()).andReturn(german);
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("admin"));
    EasyMock.expect(site.getDefaultLanguage()).andReturn(german);
    EasyMock.replay(site);
    pageURI = new PageURIImpl(site, "/test", Resource.LIVE);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PagePreviewReader#read(ch.entwine.weblounge.common.content.PageURI, java.io.InputStream)}
   * .
   */
  @Test
  public void testRead() {
    InputStream is = getClass().getResourceAsStream(previewFile);
    Composer preview = null;
    try {
      PagePreviewReader reader = new PagePreviewReader();
      preview = reader.read(is, pageURI);
    } catch (Throwable t) {
      t.printStackTrace();
      fail("Error parsing page preview");
    }

    // Test preview
    assertNotNull(preview);
    assertEquals(composerName, preview.getIdentifier());
    assertEquals(1, preview.getPagelets().length);
    Pagelet previewTitle = preview.getPagelets()[0];

    // Test title module
    assertEquals(titleModule, previewTitle.getModule());
    assertEquals(titlePagelet, previewTitle.getIdentifier());

    // TODO: Test pagelet contents

  }

}
