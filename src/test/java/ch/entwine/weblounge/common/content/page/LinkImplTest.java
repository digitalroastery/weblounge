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

import ch.entwine.weblounge.common.impl.content.page.LinkImpl;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link LinkImplTest}.
 */
public class LinkImplTest {
  
  /** The link to test */
  protected LinkImpl link = null;
  
  /** Link url */
  protected String href = "style.css";

  /** Link url */
  protected String absoluteHref = "http://localhost/module/test/style.css";

  /** Link mime type */
  protected String type = "text/css";

  /** The character set */
  protected String charset = "utf-8";
  
  /** Media */
  protected String media = "all";
  
  /** Relation */
  protected String relation = "stylesheet";
  
  /** Reverse relation */
  protected String reverseRelation = "contents";
  
  /** Mock module */
  protected Module module = null;
  
  /** Mock url */
  protected WebUrl moduleUrl = null;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();
    link = new LinkImpl(href, type, media, relation, reverseRelation, charset);
  }

  /**
   * Prepares mock objects.
   */
  protected void setUpPreliminaries() {
    moduleUrl = EasyMock.createNiceMock(WebUrl.class);
    EasyMock.expect(moduleUrl.getLink()).andReturn("http://localhost/module/test");
    EasyMock.replay(moduleUrl);
    module = EasyMock.createNiceMock(Module.class);
    EasyMock.expect(module.getUrl()).andReturn(moduleUrl);
    EasyMock.replay(module);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#setModule(ch.entwine.weblounge.common.site.Module)}.
   */
  @Test
  @Ignore
  public void testSetModule() {
    assertEquals(href, link.getHref());
    link.setModule(module);
    assertEquals(absoluteHref, link.getHref());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getHref()}.
   */
  @Test
  public void testGetHref() {
    assertEquals(href, link.getHref());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getCharset()}.
   */
  @Test
  public void testGetCharset() {
    assertEquals(charset, link.getCharset());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getMedia()}.
   */
  @Test
  public void testGetMedia() {
    assertEquals(media, link.getMedia());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getRelation()}.
   */
  @Test
  public void testGetRelationship() {
    assertEquals(relation, link.getRelation());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getReverseRelation()}.
   */
  @Test
  public void testGetReverseRelationship() {
    assertEquals(reverseRelation, link.getReverseRelation());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.LinkImpl#getType()}.
   */
  @Test
  public void testGetType() {
    assertEquals(type, link.getType());
  }

}
