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

package ch.entwine.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.content.page.LinkImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.site.SiteURLImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Locale;

/**
 * Test case for {@link ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl}.
 */
public class PageletRendererImplTest {

  /** The renderer to test */
  protected PageletRenderer renderer = null;

  /** The renderer identifier */
  protected String identifier = "renderer";
  
  /** The composeable state */
  protected boolean composeable = true;

  /** The renderer path */
  protected String rendererPath = "file://renderer/renderer.jsp";

  /** The renderer url */
  protected URL rendererUrl = null;

  /** Type identifier for feed renderer */
  protected String feedRendererType = "feed";

  /** The feed renderer path */
  protected String feedRendererPath = "file://renderer/renderer-feed.jsp";

  /** The feed renderer url */
  protected URL feedRendererUrl = null;

  /** The editor path */
  protected String editorPath = "file://renderer/editor.jsp";

  /** The editor url */
  protected URL editorUrl = null;

  /** The recheck time */
  protected long recheckTime = Times.MS_PER_DAY + 10 * Times.MS_PER_MIN;

  /** The valid time */
  protected long validTime = Times.MS_PER_WEEK + 2 * Times.MS_PER_DAY + Times.MS_PER_HOUR;

  /** The preview mode */
  protected PagePreviewMode previewMode = PagePreviewMode.First;
  
  /** English pagelet name */
  protected String name = "Pagelet renderer";

  /** English */
  protected Language english = new LanguageImpl(new Locale("en"));

  /** German */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** Cascading stylesheet include */
  protected Link css = new LinkImpl("http://localhost/css.css");

  /** The module */
  protected Module module = null;

  /** The site */
  protected Site site = null;

  /** The default site url */
  protected SiteURL hostname = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();
    renderer = new PageletRendererImpl();
    renderer.setIdentifier(identifier);
    renderer.setRenderer(rendererUrl);
    renderer.addRenderer(feedRendererUrl, feedRendererType);
    renderer.setEditor(editorUrl);
    renderer.setClientRevalidationTime(recheckTime);
    renderer.setCacheExpirationTime(validTime);
    renderer.setComposeable(composeable);
    renderer.setPreviewMode(previewMode);
    renderer.setName(name);
    renderer.addHTMLHeader(css);
    renderer.setModule(module);
    renderer.setEnvironment(Environment.Production);
  }

  /**
   * sets up everything else.
   * 
   * @throws Exception
   *           if setup fails
   */
  protected void setUpPreliminaries() throws Exception {
    hostname = new SiteURLImpl(new URL("http://localhost"));

    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("testsite").anyTimes();
    EasyMock.expect(site.getHostname((Environment) EasyMock.anyObject())).andReturn(hostname).anyTimes();
    EasyMock.replay(site);

    module = EasyMock.createMock(Module.class);
    EasyMock.expect(module.getIdentifier()).andReturn("testmodule").anyTimes();
    EasyMock.expect(module.getSite()).andReturn(site).anyTimes();
    EasyMock.replay(module);

    rendererUrl = new URL(rendererPath);
    feedRendererUrl = new URL(feedRendererPath);
    editorUrl = new URL(editorPath);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.GeneralComposeable#getIdentifier()}
   * .
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, renderer.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.GeneralComposeable#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(name, renderer.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.GeneralComposeable#isComposeable()}
   * .
   */
  @Test
  public void testIsComposeable() {
    assertEquals(composeable, renderer.isComposeable());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl#getPreviewMode()}
   * .
   */
  @Test
  public void testGetPreviewMode() {
    assertEquals(previewMode, renderer.getPreviewMode());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.GeneralComposeable#getClientRevalidationTime()}
   * .
   */
  @Test
  public void testGetRecheckTime() {
    assertEquals(recheckTime, renderer.getClientRevalidationTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.GeneralComposeable#getCacheExpirationTime()}
   * .
   */
  @Test
  public void testGetValidTime() {
    assertEquals(validTime, renderer.getCacheExpirationTime());
  }
  
  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl#getEditor()}.
   */
  @Test
  public void testGetEditor() {
    assertEquals(editorUrl, renderer.getEditor());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl#render(ch.entwine.weblounge.common.request.WebloungeRequest, ch.entwine.weblounge.common.request.WebloungeResponse)}
   * .
   */
  @Test
  public void testRender() {
    assertEquals(rendererUrl, renderer.getRenderer());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.page.GeneralComposeabl#getHTMLHeaders()
   * .
   */
  @Test
  public void testGetIncludes() {
    assertEquals(1, renderer.getHTMLHeaders().length);
    assertEquals(css, renderer.getHTMLHeaders()[0]);
  }

}
