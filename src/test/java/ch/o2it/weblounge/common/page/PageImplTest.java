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

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.impl.language.English;
import ch.o2it.weblounge.common.impl.page.PageImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.user.SiteAdminImpl;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case to test {@link PageImpl}.
 */
public class PageImplTest {

  /** The page that was read in */
  protected PageImpl page = null;
  
  /** The page uri */
  protected PageURIImpl pageURI = null;
  
  /** The site */
  protected Site mockSite = null;
  
  /** The page type */
  protected String pageType = "default";

  /** The page template */
  protected String template = "default";

  /** The page layout */
  protected String layout = "default";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
  }
  
  /**
   * Preliminary setup work.
   */
  protected void setupPrerequisites() {
    mockSite = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(mockSite.getDefaultLanguage()).andReturn(English.getInstance());    
    EasyMock.expect(mockSite.getAdministrator()).andReturn(new SiteAdminImpl("admin", "test"));    
    EasyMock.expect(mockSite.getDefaultLanguage()).andReturn(English.getInstance());    
    EasyMock.replay(mockSite);
    pageURI = new PageURIImpl(mockSite, "/test", Page.LIVE);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(pageURI.hashCode(), page.hashCode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(mockSite, page.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getVersion()}.
   */
  @Test
  public void testGetVersion() {
    assertEquals(pageURI.getVersion(), page.getVersion());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getType()}.
   */
  @Test
  public void testGetType() {
    assertEquals(pageType, page.getType());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setType(java.lang.String)}.
   */
  @Test
  public void testSetType() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addKeyword(java.lang.String)}.
   */
  @Test
  public void testAddKeyword() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getURI()}.
   */
  @Test
  public void testGetURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishingContext()}.
   */
  @Test
  public void testGetPublishingContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublisher()}.
   */
  @Test
  public void testGetPublisher() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishFrom()}.
   */
  @Test
  public void testGetPublishFrom() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishTo()}.
   */
  @Test
  public void testGetPublishTo() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#inSitemap()}.
   */
  @Test
  public void testInSitemap() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadline(java.lang.String, java.lang.String, ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testGetHeadlineStringStringAuthenticatedUser() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadline(java.lang.String, java.lang.String, ch.o2it.weblounge.common.user.AuthenticatedUser, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testGetHeadlineStringStringAuthenticatedUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadlines()}.
   */
  @Test
  public void testGetHeadlines() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setTitle(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetTitle() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetTitleLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetTitleLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getLayout()}.
   */
  @Test
  public void testGetLayout() {
    assertEquals(layout, page.getLayout());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setLayout(java.lang.String)}.
   */
  @Test
  public void testSetLayout() {
    page.setLayout("test");
    assertEquals("test", page.getLayout());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(template, page.getTemplate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setTemplate(java.lang.String)}.
   */
  @Test
  public void testSetTemplate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getKeywords()}.
   */
  @Test
  public void testGetKeywords() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setKeywords(java.lang.String[])}.
   */
  @Test
  public void testSetKeywords() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getSecurityContext()}.
   */
  @Test
  public void testGetSecurityContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#permissions()}.
   */
  @Test
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getOwner()}.
   */
  @Test
  public void testGetOwner() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testAddSecurityListener() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testRemoveSecurityListener() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#toString()}.
   */
  @Test
  public void testToString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isPublished()}.
   */
  @Test
  public void testIsPublished() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isPublished(java.util.Date)}.
   */
  @Test
  public void testIsPublishedDate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreationContext()}.
   */
  @Test
  public void testGetCreationContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreationDate()}.
   */
  @Test
  public void testGetCreationDate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreator()}.
   */
  @Test
  public void testGetCreator() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isCreatedAfter(java.util.Date)}.
   */
  @Test
  public void testIsCreatedAfter() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModificationContext()}.
   */
  @Test
  public void testGetModificationContext() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setModifiedSince(java.util.Date)}.
   */
  @Test
  public void testSetModifiedSince() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifiedSince()}.
   */
  @Test
  public void testGetModifiedSince() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setModifiedBy(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testSetModifiedBy() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifiedBy()}.
   */
  @Test
  public void testGetModifiedBy() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModificationDate()}.
   */
  @Test
  public void testGetModificationDate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifier()}.
   */
  @Test
  public void testGetModifier() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAfter(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAfter() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedBefore(java.util.Date)}.
   */
  @Test
  public void testIsModifiedBefore() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getLastModificationDate()}.
   */
  @Test
  public void testGetLastModificationDate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getLastModifier()}.
   */
  @Test
  public void testGetLastModifier() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAll()}.
   */
  @Test
  public void testIsModifiedAtAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAllAfter(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAtAllAfter() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAllBefore(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAtAllBefore() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModified()}.
   */
  @Test
  public void testIsModified() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getEditor()}.
   */
  @Test
  public void testGetEditor() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#lock(ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testLock() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#unlock(ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testUnlock() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isLocked()}.
   */
  @Test
  public void testIsLocked() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isLocked(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testIsLockedUser() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#appendPagelet(ch.o2it.weblounge.common.page.Pagelet, java.lang.String)}.
   */
  @Test
  public void testAppendPagelet() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addPagelet(ch.o2it.weblounge.common.page.Pagelet, java.lang.String, int)}.
   */
  @Test
  public void testAddPagelet() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#movePageletUp(java.lang.String, int)}.
   */
  @Test
  public void testMovePageletUp() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#movePageletDown(java.lang.String, int)}.
   */
  @Test
  public void testMovePageletDown() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPagelets(java.lang.String)}.
   */
  @Test
  public void testGetPageletsString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPagelets(java.lang.String, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetPageletsStringStringString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removePagelet(java.lang.String, int)}.
   */
  @Test
  public void testRemovePagelet() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addPageContentListener(ch.o2it.weblounge.common.page.PageContentListener)}.
   */
  @Test
  public void testAddPageContentListener() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removePageContentListener(ch.o2it.weblounge.common.page.PageContentListener)}.
   */
  @Test
  public void testRemovePageContentListener() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getDocument(long)}.
   */
  @Test
  public void testGetDocument() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getVersion(long)}.
   */
  @Test
  public void testGetVersionLong() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getVersion(java.lang.String)}.
   */
  @Test
  public void testGetVersionString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    fail("Not yet implemented"); // TODO
  }

}
