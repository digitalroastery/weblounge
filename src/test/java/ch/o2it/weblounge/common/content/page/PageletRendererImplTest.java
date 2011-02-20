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

package ch.o2it.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.page.Link;
import ch.o2it.weblounge.common.content.page.PagePreviewMode;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.impl.content.page.LinkImpl;
import ch.o2it.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.language.Language;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Locale;

/**
 * Test case for {@link PageletRendererImpl}.
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
    renderer.setRecheckTime(recheckTime);
    renderer.setValidTime(validTime);
    renderer.setComposeable(composeable);
    renderer.setPreviewMode(previewMode);
    renderer.setName(name);
    renderer.addHTMLHeader(css);
  }

  /**
   * sets up everything else.
   * 
   * @throws Exception
   *           if setup fails
   */
  protected void setUpPreliminaries() throws Exception {
    rendererUrl = new URL(rendererPath);
    feedRendererUrl = new URL(feedRendererPath);
    editorUrl = new URL(editorPath);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getIdentifier()}
   * .
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, renderer.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(name, renderer.getName());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#isComposeable()}
   * .
   */
  @Test
  public void testIsComposeable() {
    assertEquals(composeable, renderer.isComposeable());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.page.PageletRendererImpl#getPreviewMode()}
   * .
   */
  @Test
  public void testGetPreviewMode() {
    assertEquals(previewMode, renderer.getPreviewMode());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getRecheckTime()}
   * .
   */
  @Test
  public void testGetRecheckTime() {
    assertEquals(recheckTime, renderer.getRecheckTime());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.GeneralComposeable#getValidTime()}
   * .
   */
  @Test
  public void testGetValidTime() {
    assertEquals(validTime, renderer.getValidTime());
  }
  
  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.page.PageletRendererImpl#getEditor()}.
   */
  @Test
  public void testGetEditor() {
    assertEquals(editorUrl, renderer.getEditor());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.page.PageletRendererImpl#render(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)}
   * .
   */
  @Test
  public void testRender() {
    assertEquals(rendererUrl, renderer.getRenderer());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.GeneralComposeabl#getHTMLHeaders()
   * .
   */
  @Test
  public void testGetIncludes() {
    assertEquals(1, renderer.getHTMLHeaders().length);
    assertEquals(css, renderer.getHTMLHeaders()[0]);
  }

}
