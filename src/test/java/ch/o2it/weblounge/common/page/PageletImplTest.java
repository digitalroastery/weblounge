/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.page.PageletImpl;
import ch.o2it.weblounge.common.impl.page.PageletLocationImpl;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case for the implementation at {@link PageletImpl}.
 */
public class PageletImplTest {

  /** Page identifier */
  protected PageURI uri = null;
  
  /** Associated site */
  protected Site site = null;
  
  /** Path to the page */
  protected String path = "/test";
  
  /** Enclosing composer */
  protected String composer = "main";
  
  /** Pagelet position */
  protected int position = 1;
  
  /** The pagelet location */
  protected PageletLocationImpl location = null;
  
  /** Module identifier */
  protected String module = "module-id";
  
  /** Pagelet identifier */
  protected String id = "pagelet-id";
  
  /** The pagelet instance under test */
  protected PageletImpl pagelet = null;
  
  protected User john = new UserImpl("john", "testland", "John Doe");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    uri = new PageURIImpl(site, path);
    location = new PageletLocationImpl(uri, composer, position);
    pagelet = new PageletImpl(location, module, id);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(location.hashCode(), pagelet.hashCode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getModule()}.
   */
  @Test
  public void testGetModule() {
    assertEquals(module, pagelet.getModule());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(id, pagelet.getIdentifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getProperty(java.lang.String)}.
   */
  @Test
  public void testGetProperty() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isMultiValueProperty(java.lang.String)}.
   */
  @Test
  public void testIsMultiValueProperty() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueProperty(java.lang.String)}.
   */
  @Test
  public void testGetMultiValueProperty() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#setProperty(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testSetProperty() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#setOwner(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testSetOwner() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getOwner()}.
   */
  @Test
  public void testGetOwner() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getPublishingContext()}.
   */
  @Test
  public void testGetPublishingContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getPublisher()}.
   */
  @Test
  public void testGetPublisher() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getPublishFrom()}.
   */
  @Test
  public void testGetPublishFrom() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getPublishTo()}.
   */
  @Test
  public void testGetPublishTo() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isPublished()}.
   */
  @Test
  public void testIsPublished() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isPublished(java.util.Date)}.
   */
  @Test
  public void testIsPublishedDate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getRenderer()}.
   */
  @Test
  public void testGetRenderer() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getSecurityContext()}.
   */
  @Test
  public void testGetSecurityContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#permissions()}.
   */
  @Test
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testAddSecurityListener() {
    final List<String> result = new ArrayList<String>();
    pagelet.addSecurityListener(new SecurityListener() {
      public void ownerChanged(Securable source, User newOwner, User oldOwner) {
        result.add("Owner changed");
      }
      public void permissionChanged(Securable source, Permission p) {
        result.add("Permission changed");
      }
    });
    pagelet.setOwner(john);
    pagelet.getSecurityContext().allow(SystemPermission.READ, SystemRole.EDITOR);
    assertEquals(2, result.size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testRemoveSecurityListener() {
    final List<String> result = new ArrayList<String>();
    SecurityListener listener = new SecurityListener() {
      public void ownerChanged(Securable source, User newOwner, User oldOwner) {
        result.add("Owner changed");
      }
      public void permissionChanged(Securable source, Permission p) {
        result.add("Permission changed");
      }
    };
    pagelet.addSecurityListener(listener);
    pagelet.removeSecurityListener(listener);
    pagelet.setOwner(john);
    pagelet.getSecurityContext().allow(SystemPermission.READ, SystemRole.EDITOR);
    assertEquals(0, result.size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getLocation()}.
   */
  @Test
  public void testGetLocation() {
    assertEquals(location, pagelet.getLocation());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#toString()}.
   */
  @Test
  public void testToString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isMultiValueContent(java.lang.String)}.
   */
  @Test
  public void testIsMultiValueContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetContentStringLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetContentStringLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String)}.
   */
  @Test
  public void testGetContentString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetMultiValueContentStringLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetMultiValueContentStringLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String)}.
   */
  @Test
  public void testGetMultiValueContentString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#setContent(java.lang.String, java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageletImpl#compareTo(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testCompareTo() {
    fail("Not yet implemented"); // TODO
  }

}
