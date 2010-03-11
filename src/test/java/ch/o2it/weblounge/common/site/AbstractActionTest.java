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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.page.LinkImpl;
import ch.o2it.weblounge.common.impl.page.PageImpl;
import ch.o2it.weblounge.common.impl.page.PageTemplateImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.site.AbstractAction;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.page.Link;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case for {@link AbstractAction}.
 */
public class AbstractActionTest {
  
  /** The action to test */
  protected AbstractAction action = new TestAction();
  
  /** Action identifier */
  protected String identifier = "myaction";
  
  /** The action implementation */
  protected Class<? extends Action> actionClass = TestAction.class;
  
  /** Stylesheet */
  protected Link link = new LinkImpl("http://localhost/css/stylesheet.css", "text/css");

  /** Stylesheet */
  protected Link otherLink = new LinkImpl("http://localhost/css/stylesheet2.css", "text/css");

  /** Javascript */
  protected Link script = new LinkImpl("http://localhost/scripts/script.js", "javascript");
  
  /** The mountpoint */
  protected String mountpoint = "/test/";

  /** Recheck time */
  protected long recheckTime = 0;

  /** Valid time */
  protected long validTime = 60000;
  
  /** URI of the testpage */
  protected String pageURIPath = "/testpage/";

  /** The full mountpoint */
  protected WebUrl actionUrl = null;
  
  /** The page uri */
  protected PageURI pageURI = null;
  
  /** The page */
  protected Page page = null;
  
  /** Identifier of the template */
  protected String template = "testtemplate";

  /** The page template */
  protected PageTemplate pageTemplate = null;

  /** Name of the option key */
  protected String optionKey = "key";
  
  /** Option value */
  protected String optionValue = "value";
  
  /** Main url of the site */
  protected String siteUrl = "http://localhost/";

  /** The mock site */
  protected Site site = null;
  
  /** The mock module */
  protected Module module = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();
    action.setIdentifier(identifier);
    action.addInclude(link);
    action.addFlavor(RequestFlavor.HTML);
    action.addInclude(script);
    action.setPath(mountpoint);
    action.setRecheckTime(recheckTime);
    action.setValidTime(validTime);
    action.setPageURI(pageURI);
    action.setTemplate(site.getTemplate(template));
    action.setOption(optionKey, optionValue);
    action.setSite(site);
    action.setModule(module);
    action.setPage(page);
  }
  
  /**
   * Sets up mock objects.
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
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getModule()}.
   */
  @Test
  public void testGetModule() {
    assertEquals(module, action.getModule());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, action.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertEquals(page, action.getPage());
    assertEquals(page.getURI(), pageURI);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    assertEquals(actionUrl, action.getUrl());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getPath()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(mountpoint, action.getPath());
    action.setPath("/test");
    assertEquals("/test/", action.getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getPageURI()}.
   */
  @Test
  public void testGetPageURI() {
    assertEquals(pageURI, action.getPageURI());
    action.setPageURI(new PageURIImpl(site, "/testpage"));
    assertEquals(new PageURIImpl(site, "/testpage/"), action.getPageURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(pageTemplate, action.getTemplate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getFlavors()}.
   */
  @Test
  public void testGetFlavors() {
    assertEquals(1, action.getFlavors().length);
    assertEquals(RequestFlavor.HTML, action.getFlavors()[0]);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#supportsFlavor(ch.o2it.weblounge.common.request.RequestFlavor)}.
   */
  @Test
  public void testSupportsFlavor() {
    assertTrue(action.supportsFlavor(RequestFlavor.HTML));
    assertFalse(action.supportsFlavor(RequestFlavor.JSON));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getOptionValue(java.lang.String)}.
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, action.getOptionValue(optionKey));
    assertTrue(action.getOptionValue("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getOptionValue(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, action.getOptionValue(optionKey, "abc"));
    assertEquals(optionValue, action.getOptionValue("abc", optionValue));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getOptionValues(java.lang.String)}.
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, action.getOptionValues(optionKey).length);
    assertEquals(0, action.getOptionValues("test").length);
    action.setOption(optionKey, optionValue);
    assertEquals(1, action.getOptionValues(optionKey).length);
    action.setOption(optionKey, "abc");
    assertEquals(2, action.getOptionValues(optionKey).length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(1, action.getOptions().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#hasOption(java.lang.String)}.
   */
  @Test
  public void testHasOption() {
    assertTrue(action.hasOption(optionKey));
    assertFalse(action.hasOption("test"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#removeOption(java.lang.String)}.
   */
  @Test
  public void testRemoveOption() {
    action.removeOption(optionKey);
    assertFalse(action.hasOption(optionKey));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#activate()}.
   * <p>
   * Nothing to test, since implementation is empty
   */
  @Test
  public void testActivate() {
    action.activate();
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.AbstractAction#passivate()}.
   */
  @Test
  public void testPassivate() {
    action.passivate();
    
    // These things should still be set
    assertNotNull(action.getSite());
    assertNotNull(action.getModule());
    assertEquals(identifier, action.getIdentifier());
    assertEquals(2, action.getIncludes().length);
    assertEquals(actionUrl, action.getUrl());
    assertEquals(recheckTime, action.getRecheckTime());
    assertEquals(validTime, action.getValidTime());
    assertEquals(pageURI, action.getPageURI());
    assertNotNull(template, action.getTemplate());
    assertEquals(1, action.getOptions().size());
    
    // These should have gone away
    assertNull(action.getPage());
  }

}
