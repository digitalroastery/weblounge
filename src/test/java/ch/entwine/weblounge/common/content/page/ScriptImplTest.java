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

import ch.entwine.weblounge.common.impl.content.page.ScriptImpl;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link ScriptImpl}.
 */
public class ScriptImplTest {
  
  /** The script to test */
  protected ScriptImpl script = null;
  
  /** Script url */
  protected String href = "script.js";

  /** Script url */
  protected String absoluteHref = "http://localhost/module/test/script.js";

  /** Script type or language */
  protected String type = "text/javascript";

  /** The character set */
  protected String charset = "utf-8";

  /** Whether to defer the execution of the script */
  protected boolean defer = true;
  
  /** Mock module */
  protected Module module = null;
  
  /** Mock url */
  protected WebUrl moduleUrl = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    script = new ScriptImpl(href, type, charset, defer);
    setUpPreliminaries();
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
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.ScriptImpl#getHref()
   */
  @Test
  public void testGetHref() {
    assertEquals(href, script.getHref());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.ScriptImpl#getType()
   */
  @Test
  public void testGetType() {
    assertEquals(type, script.getType());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.ScriptImpl#getCharset()
   */
  @Test
  public void testGetCharset() {
    assertEquals(charset, script.getCharset());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.ScriptImpl#isDeferred()
   */
  @Test
  public void testGetDefer() {
    assertEquals(defer, script.isDeferred());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.ScriptImpl#setModule(ch.entwine.weblounge.common.site.Module)}.
   */
  @Test
  @Ignore
  public void testSetModule() {
    assertEquals(href, script.getHref());
    script.configure(null, null, module);
    assertEquals(absoluteHref, script.getHref());
  }

}
