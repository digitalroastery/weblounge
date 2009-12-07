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

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.page.PageletImpl;
import ch.o2it.weblounge.common.impl.page.PageletLocationImpl;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.language.Language;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
  protected String module = "text";

  /** Pagelet identifier */
  protected String id = "title";

  /** Property name */
  protected String propertyName = "headline";

  /** Property value */
  protected String propertyValue = "true";

  /** Property name */
  protected String multivaluePropertyName = "tags";

  /** Property value */
  protected String[] multivaluePropertyValue = new String[] { "a", "b" };

  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** Content creation date */
  protected Date creationDate = new Date(1000000000000L);

  /** German modification date */
  protected Date germanModifcationDate = new Date(1231355141000L);

  /** French modification date */
  protected Date frenchModifcationDate = new Date(1234991200000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** One day after the date identified by futureDate */
  protected Date dayAfterFutureDate = new Date(futureDate.getTime() + Times.MS_PER_DAY);

  /** One day before the date identified by futureDate */
  protected Date dayBeforeFutureDate = new Date(2000000000000L - Times.MS_PER_DAY);

  /** Owner */
  protected User john = new UserImpl("john", "testland", "John Doe");

  /** German editor */
  protected User hans = new UserImpl("hans", "testland", "Hans Muster");

  /** German editor */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /** The pagelet instance under test */
  protected PageletImpl pagelet = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPreliminaries();
    pagelet = new PageletImpl(location, module, id);
    pagelet.setProperty(propertyName, propertyValue);
    for (String s : multivaluePropertyValue)
      pagelet.setProperty(multivaluePropertyName, s);
    pagelet.setOwner(john);
    pagelet.setCreated(hans, germanModifcationDate);
    pagelet.setModified(hans, germanModifcationDate, german);
    pagelet.setModified(amelie, frenchModifcationDate, french);
  }

  /**
   * Does some setup that is common to both the simple pagelet test and the one
   * that is reading in from XML.
   */
  public void setupPreliminaries() {
    site = EasyMock.createNiceMock(Site.class);
    uri = new PageURIImpl(site, path);
    location = new PageletLocationImpl(uri, composer, position);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(location.hashCode(), pagelet.hashCode());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getModule()}.
   */
  @Test
  public void testGetModule() {
    assertEquals(module, pagelet.getModule());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(id, pagelet.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getProperty(java.lang.String)}
   * .
   */
  @Test
  public void testGetProperty() {
    assertEquals(propertyValue, pagelet.getProperty(propertyName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isMultiValueProperty(java.lang.String)}
   * .
   */
  @Test
  public void testIsMultiValueProperty() {
    assertFalse(pagelet.isMultiValueProperty(propertyName));
    assertTrue(pagelet.isMultiValueProperty(multivaluePropertyName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueProperty(java.lang.String)}
   * .
   */
  @Test
  public void testGetMultiValueProperty() {
    assertEquals(2, pagelet.getMultiValueProperty(multivaluePropertyName).length);
    assertEquals(multivaluePropertyValue[0], pagelet.getMultiValueProperty(multivaluePropertyName)[0]);
    assertEquals(multivaluePropertyValue[1], pagelet.getMultiValueProperty(multivaluePropertyName)[1]);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getOwner()}.
   */
  @Test
  public void testGetCreator() {
    assertEquals(hans, pagelet.getCreator());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getRenderer()}.
   */
  @Test
  public void testGetRenderer() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getSecurityContext()}
   * .
   */
  @Test
  public void testGetSecurityContext() {
    assertNotNull(pagelet.getSecurityContext());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}
   * .
   */
  @Test
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}
   * .
   */
  @Test
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}
   * .
   */
  @Test
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}
   * .
   */
  @Test
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#permissions()}.
   */
  @Test
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}
   * .
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
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}
   * .
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
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getLocation()}.
   */
  @Test
  public void testGetLocation() {
    assertEquals(location, pagelet.getLocation());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#isMultiValueContent(java.lang.String)}
   * .
   */
  @Test
  public void testIsMultiValueContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)}
   * .
   */
  @Test
  public void testGetContentStringLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetContentStringLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getContent(java.lang.String)}
   * .
   */
  @Test
  public void testGetContentString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)}
   * .
   */
  @Test
  public void testGetMultiValueContentStringLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetMultiValueContentStringLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#getMultiValueContent(java.lang.String)}
   * .
   */
  @Test
  public void testGetMultiValueContentString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#setContent(java.lang.String, java.lang.String, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testSetContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#compareTo(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testCompareTo() {
    fail("Not yet implemented"); // TODO
  }

}
