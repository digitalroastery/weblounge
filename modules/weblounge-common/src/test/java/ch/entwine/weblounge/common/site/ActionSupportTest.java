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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.page.Link;
import ch.entwine.weblounge.common.impl.content.page.LinkImpl;
import ch.entwine.weblounge.common.impl.site.ModuleImpl;
import ch.entwine.weblounge.common.impl.site.SiteURLImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case for {@link ch.entwine.weblounge.common.impl.site.ActionSupport}.
 */
public class ActionSupportTest {

  /** The action to test */
  protected Action action = new TestAction();

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

  /** The full mountpoint */
  protected WebUrl actionUrl = null;

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
    action.addHTMLHeader(link);
    action.addFlavor(RequestFlavor.HTML);
    action.addHTMLHeader(script);
    action.setPath(mountpoint);
    action.setClientRevalidationTime(recheckTime);
    action.setCacheExpirationTime(validTime);
    action.setOption(optionKey, optionValue);
    action.setSite(site);
    action.setModule(module);
  }

  /**
   * Sets up mock objects.
   * 
   * @throws Exception
   *           if setting up the preliminaries fails
   */
  protected void setUpPreliminaries() throws Exception {
    // site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getHostname((Environment)EasyMock.anyObject())).andReturn(new SiteURLImpl(new URL(siteUrl)));
    EasyMock.replay(site);

    // module
    module = new ModuleImpl();
    module.setIdentifier(identifier);

    actionUrl = new WebUrlImpl(site, mountpoint);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getModule()}.
   */
  @Test
  public void testGetModule() {
    assertEquals(module, action.getModule());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, action.getSite());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    assertEquals(actionUrl, action.getUrl());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getPath()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(mountpoint, action.getPath());
    action.setPath("/test");
    assertEquals("/test/", action.getPath());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getFlavors()}.
   */
  @Test
  public void testGetFlavors() {
    assertEquals(1, action.getFlavors().length);
    assertEquals(RequestFlavor.HTML, action.getFlavors()[0]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#supportsFlavor(ch.entwine.weblounge.common.request.RequestFlavor)}
   * .
   */
  @Test
  public void testSupportsFlavor() {
    assertTrue(action.supportsFlavor(RequestFlavor.HTML));
    assertFalse(action.supportsFlavor(RequestFlavor.JSON));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getOptionValue(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, action.getOptionValue(optionKey));
    assertTrue(action.getOptionValue("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getOptionValue(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, action.getOptionValue(optionKey, "abc"));
    assertEquals(optionValue, action.getOptionValue("abc", optionValue));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getOptionValues(java.lang.String)}
   * .
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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(1, action.getOptions().size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#hasOption(java.lang.String)}
   * .
   */
  @Test
  public void testHasOption() {
    assertTrue(action.hasOption(optionKey));
    assertFalse(action.hasOption("test"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#removeOption(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveOption() {
    action.removeOption(optionKey);
    assertFalse(action.hasOption(optionKey));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#activate()}.
   * <p>
   * Nothing to test, since implementation is empty
   */
  @Test
  public void testActivate() {
    action.activate();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.ActionSupport#passivate()}.
   */
  @Test
  public void testPassivate() {
    action.passivate();

    // These things should still be set
    assertNotNull(action.getSite());
    assertNotNull(action.getModule());
    assertEquals(identifier, action.getIdentifier());
    assertEquals(actionUrl, action.getUrl());
    assertEquals(recheckTime, action.getRecheckTime());
    assertEquals(validTime, action.getValidTime());
    assertEquals(2, action.getHTMLHeaders().length);
    assertEquals(1, action.getOptions().size());
  }

}
