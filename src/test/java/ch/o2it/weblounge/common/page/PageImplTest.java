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
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addKeyword(java.lang.String)}.
   */
  @Test
  public void testAddKeyword() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getURI()}.
   */
  @Test
  public void testGetURI() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishingContext()}.
   */
  @Test
  public void testGetPublishingContext() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublisher()}.
   */
  @Test
  public void testGetPublisher() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishFrom()}.
   */
  @Test
  public void testGetPublishFrom() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPublishTo()}.
   */
  @Test
  public void testGetPublishTo() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#inSitemap()}.
   */
  @Test
  public void testInSitemap() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadline(java.lang.String, java.lang.String, ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testGetHeadlineStringStringAuthenticatedUser() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadline(java.lang.String, java.lang.String, ch.o2it.weblounge.common.user.AuthenticatedUser, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testGetHeadlineStringStringAuthenticatedUserPermission() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getHeadlines()}.
   */
  @Test
  public void testGetHeadlines() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setTitle(java.lang.String, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetTitle() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetTitleLanguage() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetTitleLanguageBoolean() {
    System.err.println("Not yet implemented"); // TODO
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
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getKeywords()}.
   */
  @Test
  public void testGetKeywords() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setKeywords(java.lang.String[])}.
   */
  @Test
  public void testSetKeywords() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getSecurityContext()}.
   */
  @Test
  public void testGetSecurityContext() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionAuthority() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  public void testCheckPermissionSetAuthority() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckOne() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  public void testCheckAll() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#permissions()}.
   */
  @Test
  public void testPermissions() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getOwner()}.
   */
  @Test
  public void testGetOwner() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testAddSecurityListener() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)}.
   */
  @Test
  public void testRemoveSecurityListener() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#toString()}.
   */
  @Test
  public void testToString() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isPublished()}.
   */
  @Test
  public void testIsPublished() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isPublished(java.util.Date)}.
   */
  @Test
  public void testIsPublishedDate() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreationContext()}.
   */
  @Test
  public void testGetCreationContext() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreationDate()}.
   */
  @Test
  public void testGetCreationDate() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getCreator()}.
   */
  @Test
  public void testGetCreator() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isCreatedAfter(java.util.Date)}.
   */
  @Test
  public void testIsCreatedAfter() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModificationContext()}.
   */
  @Test
  public void testGetModificationContext() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setModifiedSince(java.util.Date)}.
   */
  @Test
  public void testSetModifiedSince() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifiedSince()}.
   */
  @Test
  public void testGetModifiedSince() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#setModifiedBy(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testSetModifiedBy() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifiedBy()}.
   */
  @Test
  public void testGetModifiedBy() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModificationDate()}.
   */
  @Test
  public void testGetModificationDate() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getModifier()}.
   */
  @Test
  public void testGetModifier() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAfter(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAfter() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedBefore(java.util.Date)}.
   */
  @Test
  public void testIsModifiedBefore() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getLastModificationDate()}.
   */
  @Test
  public void testGetLastModificationDate() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getLastModifier()}.
   */
  @Test
  public void testGetLastModifier() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAll()}.
   */
  @Test
  public void testIsModifiedAtAll() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAllAfter(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAtAllAfter() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModifiedAtAllBefore(java.util.Date)}.
   */
  @Test
  public void testIsModifiedAtAllBefore() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isModified()}.
   */
  @Test
  public void testIsModified() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getEditor()}.
   */
  @Test
  public void testGetEditor() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#lock(ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testLock() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#unlock(ch.o2it.weblounge.common.user.AuthenticatedUser)}.
   */
  @Test
  public void testUnlock() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isLocked()}.
   */
  @Test
  public void testIsLocked() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#isLocked(ch.o2it.weblounge.common.user.User)}.
   */
  @Test
  public void testIsLockedUser() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#appendPagelet(ch.o2it.weblounge.common.page.Pagelet, java.lang.String)}.
   */
  @Test
  public void testAppendPagelet() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addPagelet(ch.o2it.weblounge.common.page.Pagelet, java.lang.String, int)}.
   */
  @Test
  public void testAddPagelet() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#movePageletUp(java.lang.String, int)}.
   */
  @Test
  public void testMovePageletUp() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#movePageletDown(java.lang.String, int)}.
   */
  @Test
  public void testMovePageletDown() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPagelets(java.lang.String)}.
   */
  @Test
  public void testGetPageletsString() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getPagelets(java.lang.String, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetPageletsStringStringString() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removePagelet(java.lang.String, int)}.
   */
  @Test
  public void testRemovePagelet() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#addPageContentListener(ch.o2it.weblounge.common.page.PageContentListener)}.
   */
  @Test
  public void testAddPageContentListener() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#removePageContentListener(ch.o2it.weblounge.common.page.PageContentListener)}.
   */
  @Test
  public void testRemovePageContentListener() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getDocument(long)}.
   */
  @Test
  public void testGetDocument() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getVersion(long)}.
   */
  @Test
  public void testGetVersionLong() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#getVersion(java.lang.String)}.
   */
  @Test
  public void testGetVersionString() {
    System.err.println("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    System.err.println("Not yet implemented"); // TODO
  }

}
