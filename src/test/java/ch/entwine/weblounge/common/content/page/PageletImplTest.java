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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Permission;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.SystemPermission;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Test case for the implementation at
 * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl}.
 */
public class PageletImplTest {

  /** Page identifier */
  protected ResourceURI uri = null;

  /** Associated site */
  protected Site site = null;

  /** Path to the page */
  protected String path = "/test";

  /** Enclosing composer */
  protected String composer = "main";

  /** Pagelet position */
  protected int position = 1;

  /** The pagelet location */
  protected PageletURIImpl location = null;

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

  /** Property values */
  protected String[] multivaluePropertyValue = new String[] { "a", "b" };

  /** Name of the content */
  protected String contentName = "title";

  /** German content */
  protected String germanContent = "Ein amüsanter Titel";

  /** French content */
  protected String frenchContent = "Un titre joyeux";

  /** Lonely content name */
  protected String lonelyContentName = "credits";

  /** Lonely content name */
  protected String lonelyContent = "Friedrich Nietzsche";

  /** Content name */
  protected String multivalueContentName = "tag";

  /** Content values */
  protected String[] multivalueGermanContent = new String[] { "Neu", "Technik" };

  /** Content values */
  protected String[] multivalueFrenchContent = new String[] {
      "Nouveau",
      "Technique" };

  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** The Italian language */
  protected Language italian = new LanguageImpl(new Locale("it"));

  /** Content creation date */
  protected Date creationDate = new Date(1231358741000L);

  /** German modification date */
  protected Date germanModificationDate = new Date(1231358741000L);

  /** French modification date */
  protected Date frenchModificationDate = new Date(1234994800000L);

  /** Publishing start date */
  protected Date publishingStartDate = new Date(1231358741000L);

  /** Publishing end date */
  protected Date publishingEndDate = new Date(1234994800000L);

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

  /** French editor */
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
    pagelet.setContent(contentName, germanContent, german);
    pagelet.setContent(contentName, frenchContent, french);
    pagelet.setContent(lonelyContentName, lonelyContent, german);
    for (String s : multivalueGermanContent)
      pagelet.setContent(multivalueContentName, s, german);
    for (String s : multivalueFrenchContent)
      pagelet.setContent(multivalueContentName, s, french);
    pagelet.setProperty(propertyName, propertyValue);
    for (String s : multivaluePropertyValue)
      pagelet.addProperty(multivaluePropertyName, s);
    pagelet.setOwner(john);
    pagelet.setCreated(hans, creationDate);
    pagelet.setModified(hans, germanModificationDate, german);
    pagelet.setModified(amelie, frenchModificationDate, french);
    pagelet.setPublished(hans, publishingStartDate, publishingEndDate);
  }

  /**
   * Does some setup that is common to both the simple pagelet test and the one
   * that is reading in from XML.
   */
  public void setupPreliminaries() {
    site = EasyMock.createNiceMock(Site.class);
    uri = new PageURIImpl(site, path);
    location = new PageletURIImpl(uri, composer, position);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#hashCode()}
   * .
   */
  @Test
  public void testHashCode() {
    assertEquals(location.hashCode(), pagelet.hashCode());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getModule()}
   * .
   */
  @Test
  public void testGetModule() {
    assertEquals(module, pagelet.getModule());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getIdentifier()}
   * .
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(id, pagelet.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getPropertyNames()}
   * .
   */
  @Test
  public void testGetPropertyNames() {
    assertEquals(2, pagelet.getPropertyNames().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getProperty(java.lang.String)}
   * .
   */
  @Test
  public void testGetProperty() {
    assertEquals(propertyValue, pagelet.getProperty(propertyName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#isMultiValueProperty(java.lang.String)}
   * .
   */
  @Test
  public void testIsMultiValueProperty() {
    assertFalse(pagelet.isMultiValueProperty(propertyName));
    assertTrue(pagelet.isMultiValueProperty(multivaluePropertyName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getMultiValueProperty(java.lang.String)}
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
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getOwner()}
   * .
   */
  @Test
  public void testGetCreator() {
    assertEquals(hans, pagelet.getCreator());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getPublishFrom()}
   * .
   */
  @Test
  public void testGetPublishFrom() {
    assertEquals(publishingStartDate, pagelet.getPublishFrom());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getPublishTo()}
   * .
   */
  @Test
  public void testGetPublishTo() {
    assertEquals(publishingEndDate, pagelet.getPublishTo());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getPublisher()}
   * .
   */
  @Test
  public void testGetPublisher() {
    assertEquals(hans, pagelet.getPublisher());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#check(ch.entwine.weblounge.common.security.Permission, ch.entwine.weblounge.common.security.Authority)}
   * .
   */
  @Test
  @Ignore
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#check(ch.entwine.weblounge.common.security.PermissionSet, ch.entwine.weblounge.common.security.Authority)}
   * .
   */
  @Test
  @Ignore
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#checkOne(ch.entwine.weblounge.common.security.Permission, ch.entwine.weblounge.common.security.Authority[])}
   * .
   */
  @Test
  @Ignore
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#checkAll(ch.entwine.weblounge.common.security.Permission, ch.entwine.weblounge.common.security.Authority[])}
   * .
   */
  @Test
  @Ignore
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#permissions()}
   * .
   */
  @Test
  @Ignore
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)}
   * .
   */
  @Test
  @Ignore
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
    pagelet.allow(SystemPermission.READ, SystemRole.EDITOR);
    assertEquals(2, result.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)}
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
    pagelet.allow(SystemPermission.READ, SystemRole.EDITOR);
    assertEquals(0, result.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getURI()}.
   */
  @Test
  public void testGetLocation() {
    assertEquals(location, pagelet.getURI());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getContentNames(Language)}
   * .
   */
  @Test
  public void textGetContentNames() {
    assertEquals(3, pagelet.getContentNames(german).length);
    assertEquals(2, pagelet.getContentNames(french).length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#isMultiValueContent(java.lang.String)}
   * .
   */
  @Test
  public void testIsMultiValueContent() {
    assertFalse(pagelet.isMultiValueContent(contentName));
    assertTrue(pagelet.isMultiValueContent(multivalueContentName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getContent(java.lang.String, ch.entwine.weblounge.common.language.Language, boolean)}
   * .
   */
  @Test
  public void testGetContentStringLanguageBoolean() {
    assertEquals(germanContent, pagelet.getContent(contentName, german, true));
    assertEquals(germanContent, pagelet.getContent(contentName, german, false));
    assertEquals(frenchContent, pagelet.getContent(contentName, french, true));
    assertEquals(frenchContent, pagelet.getContent(contentName, french, false));

    // Test unsupported languages
    assertEquals(germanContent, pagelet.getContent(contentName, italian, false));
    assertTrue(pagelet.getContent(contentName, italian, true) == null);

    // Test lonely content (credits), available in German only
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName, german, true));
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName, german, false));
    assertTrue(pagelet.getContent(lonelyContentName, french, true) == null);
    assertTrue(pagelet.getContent(lonelyContentName, french, false) == null);
    assertTrue(pagelet.getContent(lonelyContentName, italian, true) == null);
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName, italian, false));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getContent(java.lang.String, ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetContentStringLanguage() {
    assertEquals(germanContent, pagelet.getContent(contentName, german));
    assertEquals(frenchContent, pagelet.getContent(contentName, french));

    // Test unsupported languages
    assertEquals(germanContent, pagelet.getContent(contentName, italian));

    // Test lonely content (credits), available in German only
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName, german));
    assertTrue(pagelet.getContent(lonelyContentName, french) == null);
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName, italian));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getContent(java.lang.String)}
   * .
   */
  @Test
  public void testGetContentString() {
    assertEquals(germanContent, pagelet.getContent(contentName));
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName));

    pagelet.switchTo(french);
    assertEquals(frenchContent, pagelet.getContent(contentName));
    assertTrue(pagelet.getContent(lonelyContentName) == null);

    // Test unsupported languages.
    // Switching to Italian will actually switch to German (original language)
    pagelet.switchTo(italian);
    assertEquals(germanContent, pagelet.getContent(contentName));
    assertEquals(lonelyContent, pagelet.getContent(lonelyContentName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getMultiValueContent(java.lang.String, ch.entwine.weblounge.common.language.Language, boolean)}
   * .
   */
  @Test
  public void testGetMultiValueContentStringLanguageBoolean() {
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, german, true).length);
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, german, false).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName, german, true)[0]);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName, german, false)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName, german, true)[1]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName, german, false)[1]);

    // French
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, french, true).length);
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, french, false).length);
    assertEquals(multivalueFrenchContent[0], pagelet.getMultiValueContent(multivalueContentName, french, true)[0]);
    assertEquals(multivalueFrenchContent[0], pagelet.getMultiValueContent(multivalueContentName, french, false)[0]);
    assertEquals(multivalueFrenchContent[1], pagelet.getMultiValueContent(multivalueContentName, french, true)[1]);
    assertEquals(multivalueFrenchContent[1], pagelet.getMultiValueContent(multivalueContentName, french, false)[1]);

    // Test unsupported languages
    assertEquals(0, pagelet.getMultiValueContent(multivalueContentName, italian, true).length);
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, italian, false).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName, italian, false)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName, italian, false)[1]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getMultiValueContent(java.lang.String, ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetMultiValueContentStringLanguage() {
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, german).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName, german)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName, german)[1]);

    // French
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, french).length);
    assertEquals(multivalueFrenchContent[0], pagelet.getMultiValueContent(multivalueContentName, french)[0]);
    assertEquals(multivalueFrenchContent[1], pagelet.getMultiValueContent(multivalueContentName, french)[1]);

    // Test unsupported languages
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, italian).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName, italian)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName, italian)[1]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#getMultiValueContent(java.lang.String)}
   * .
   */
  @Test
  public void testGetMultiValueContentString() {
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName)[1]);

    // French
    pagelet.switchTo(french);
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName).length);
    assertEquals(multivalueFrenchContent[0], pagelet.getMultiValueContent(multivalueContentName)[0]);
    assertEquals(multivalueFrenchContent[1], pagelet.getMultiValueContent(multivalueContentName)[1]);

    // Test unsupported languages
    // Switching to Italian will actually switch to German (original language)
    pagelet.switchTo(italian);
    assertEquals(2, pagelet.getMultiValueContent(multivalueContentName, italian).length);
    assertEquals(multivalueGermanContent[0], pagelet.getMultiValueContent(multivalueContentName)[0]);
    assertEquals(multivalueGermanContent[1], pagelet.getMultiValueContent(multivalueContentName)[1]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    assertTrue(pagelet.equals(new PageletImpl(module, id)));
    assertFalse(pagelet.equals(new PageletImpl(module, "x")));
    assertFalse(pagelet.equals(new PageletImpl("x", id)));
    assertTrue(pagelet.equals(new PageletImpl(location, module, id)));
    PageletURI otherLocation = new PageletURIImpl(uri, composer, position + 1);
    assertFalse(pagelet.equals(new PageletImpl(otherLocation, module, id)));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#compareTo(ch.entwine.weblounge.common.language.Localizable, ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testCompareTo() {
    PageletURI otherLocation = new PageletURIImpl(uri, composer, position + 1);
    assertEquals(0, pagelet.compareTo(new PageletImpl(module, id), german));
    assertEquals(-1, pagelet.compareTo(new PageletImpl(otherLocation, module, id), german));
  }

}
