/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageTemplateImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.o2it.weblounge.common.impl.site.ActionSupport;
import ch.o2it.weblounge.common.impl.site.HTMLActionSupport;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case for {@link ActionSupport}.
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
    htmlAction.setTemplate(site.getTemplate(template));
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
    EasyMock.expect(site.getURL()).andReturn(new URL(siteUrl));
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
   * ch.o2it.weblounge.common.impl.site.HTMLActionSupport(String)}.
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
   * {@link ch.o2it.weblounge.common.impl.site.ActionSupport#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertEquals(page, htmlAction.getPage());
    assertEquals(page.getURI(), pageURI);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.ActionSupport#getPageURI()}.
   */
  @Test
  public void testGetPageURI() {
    assertEquals(pageURI, htmlAction.getPageURI());
    htmlAction.setPageURI(new PageURIImpl(site, "/testpage"));
    assertEquals(new PageURIImpl(site, "/testpage/"), htmlAction.getPageURI());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.ActionSupport#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(pageTemplate, htmlAction.getTemplate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.ActionSupport#passivate()}.
   */
  @Test
  public void testPassivate() {
    super.testPassivate();

    assertEquals(pageURI, htmlAction.getPageURI());
    assertNotNull(template, htmlAction.getTemplate());

    // These should have gone away
    assertNull(htmlAction.getPage());
  }

}
