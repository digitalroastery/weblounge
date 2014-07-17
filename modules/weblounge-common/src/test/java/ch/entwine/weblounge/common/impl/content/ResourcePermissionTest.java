/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.content;

import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link ResourcePermission}.
 */
@Ignore
public class ResourcePermissionTest {

  /** The page to protect */
  private Page page = null;

  /** The action to take */
  private Action action = null;

  /** The page permission to test */
  private ResourcePermission permission = null;
  
  @Before
  public void setUp() {
    Site site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    page = new PageImpl(new ResourceURIImpl(Page.TYPE, site, "/"));
    action = SystemAction.READ;
    permission = new ResourcePermission(page, action);
  }
  
  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ResourcePermission#getActions()}
   * .
   */
  @Test
  public void testGetActions() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ResourcePermission#implies(java.security.Permission)}
   * .
   */
  @Test
  public void testImpliesPermission() {
    
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ResourcePermission#getPage()}
   * .
   */
  @Test
  public void testGetPage() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ResourcePermission#getUser()}
   * .
   */
  @Test
  public void testGetUser() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.ResourcePermission#getAction()}
   * .
   */
  @Test
  public void testGetAction() {
    fail("Not yet implemented");
  }

}
