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

import ch.o2it.weblounge.common.impl.page.PageImpl;
import ch.o2it.weblounge.common.impl.page.PageTemplateImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.site.AbstractActionSupport;
import ch.o2it.weblounge.common.impl.site.HTMLActionSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case for {@link AbstractActionSupport}.
 */
public class HTMLActionSupportTest extends AbstractActionTest {

  /** The action to test */
  protected HTMLActionSupport htmlAction = null;

  /** URI of the testpage */
  protected String pageURIPath = "/testpage/";

  /** The page uri */
  protected PageURI pageURI = null;

  /** The page */
  protected Page page = null;

  /** Identifier of the template */
  protected String template = "testtemplate";

  /** The page template */
  protected PageTemplate pageTemplate = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    htmlAction = (HTMLActionSupport)action;
    htmlAction.setPageURI(pageURI);
    htmlAction.setTemplate(site.getTemplate(template));
    htmlAction.setPage(page);
  }

  /**
   * Sets up mock objects.
   * 
   * @throws Exception
   *           if setting up the preliminaries fails
   */
  protected void setUpPreliminaries() throws Exception {
    pageTemplate = new PageTemplateImpl(template, new URL("file:///template.jsp"));

    // site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getTemplate(template)).andReturn(pageTemplate);
    EasyMock.expect(site.getUrl()).andReturn(new WebUrlImpl(site, siteUrl));
    EasyMock.replay(site);

    // module
    module = EasyMock.createNiceMock(Module.class);
    EasyMock.replay(module);

    pageURI = new PageURIImpl(site, pageURIPath);
    page = new PageImpl(pageURI);
    actionUrl = new WebUrlImpl(site, UrlSupport.concat(siteUrl, mountpoint));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.AbstractActionSupport#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertEquals(page, htmlAction.getPage());
    assertEquals(page.getURI(), pageURI);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.AbstractActionSupport#getPageURI()}.
   */
  @Test
  public void testGetPageURI() {
    assertEquals(pageURI, htmlAction.getPageURI());
    htmlAction.setPageURI(new PageURIImpl(site, "/testpage"));
    assertEquals(new PageURIImpl(site, "/testpage/"), htmlAction.getPageURI());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.AbstractActionSupport#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(pageTemplate, htmlAction.getTemplate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.AbstractActionSupport#passivate()}.
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
