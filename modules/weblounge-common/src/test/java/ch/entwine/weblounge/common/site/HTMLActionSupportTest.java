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

package ch.entwine.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageTemplateImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.entwine.weblounge.common.impl.site.HTMLActionSupport;
import ch.entwine.weblounge.common.impl.site.SiteURLImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case for {@link ch.entwine.weblounge.common.impl.site.ActionSupport}.
 */
public class HTMLActionSupportTest extends ActionSupportTest {

  /** The action to test */
  protected HTMLActionSupport htmlAction = null;

  /** URI of the testpage */
  protected String pageURIPath = "/testpage/";

  /** The page uri */
  protected ResourceURI pageURI = null;

  /** The page */
  protected Page page = null;

  /** Identifier of the template */
  protected String template = "testtemplate";

  /** The page template */
  protected PageTemplate pageTemplate = null;

  /** A renderer */
  protected PageletRenderer renderer = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    htmlAction = (HTMLActionSupport) action;
    htmlAction.setPageURI(pageURI);
    htmlAction.setDefaultTemplate(site.getTemplate(template));
    htmlAction.setPage(page);
    htmlAction.setModule(module);
  }

  /**
   * Sets up mock objects.
   * 
   * @throws Exception
   *           if setting up the preliminaries fails
   */
  protected void setUpPreliminaries() throws Exception {
    super.setUpPreliminaries();

    pageTemplate = new PageTemplateImpl(template, new URL("file:///template.jsp"));

    // site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getTemplate(template)).andReturn(pageTemplate);
    EasyMock.expect(site.getHostname((Environment) EasyMock.anyObject())).andReturn(new SiteURLImpl(new URL(siteUrl)));
    EasyMock.replay(site);

    // Renderer
    renderer = new PageletRendererImpl("renderer");

    // module
    module.addRenderer(renderer);

    pageURI = new PageURIImpl(site, pageURIPath);
    page = new PageImpl(pageURI);
    actionUrl = new WebUrlImpl(site, UrlUtils.concat(siteUrl, mountpoint));
  }

  /**
   * Test method for {@link
   * ch.entwine.weblounge.common.impl.site.HTMLActionSupport(String)}.
   */
  @Test
  public void testConstructWithRenderer() {
    HTMLActionSupport myAction = new HTMLActionSupport(renderer.getIdentifier());
    myAction.setModule(module);
    assertNotNull(myAction.getModule());
    assertNotNull(myAction.getStageRenderer());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.HTMLActionSupport#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertEquals(page, htmlAction.getPage());
    assertEquals(page.getURI(), pageURI);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.HTMLActionSupport#getPageURI()}
   * .
   */
  @Test
  public void testGetPageURI() {
    assertEquals(pageURI, htmlAction.getPageURI());
    htmlAction.setPageURI(new PageURIImpl(site, "/testpage"));
    assertEquals(new PageURIImpl(site, "/testpage/"), htmlAction.getPageURI());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.HTMLActionSupport#getDefaultTemplate()}
   * .
   */
  @Test
  public void testGetDefaultTemplate() {
    assertEquals(pageTemplate, htmlAction.getDefaultTemplate());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.HTMLActionSupport#passivate()}
   * .
   */
  @Test
  public void testPassivate() {
    super.testPassivate();

    assertEquals(pageURI, htmlAction.getPageURI());
    assertNotNull(htmlAction.getDefaultTemplate());
    assertNull(htmlAction.getTemplate());

    // These should have gone away
    assertNull(htmlAction.getPage());
  }

}
