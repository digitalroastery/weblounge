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

package ch.o2it.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageletImpl;
import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.user.SiteAdminImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

/**
 * Test case to test {@link PageImpl}.
 */
public class PageImplTest {

  /** The page that was read in */
  protected Page page = null;
  
  /** The page uri */
  protected ResourceURIImpl pageURI = null;
  
  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** The Italian language */
  protected Language italian = new LanguageImpl(new Locale("it"));

  /** The site */
  protected Site site = null;
  
  /** The page type */
  protected String pageType = "Text";

  /** The page template */
  protected String template = "home";

  /** The page layout */
  protected String layout = "news";
  
  /** Indexed */
  protected boolean isIndexed = true;

  /** Anchor page */
  protected boolean isPromoted = true;
  
  /** German page title */
  protected String germanTitle = "Seitentitel"; 

  /** French page title */
  protected String frenchTitle = "Il titre de la page"; 

  /** German page description */
  protected String germanDescription = "Beschreibung"; 

  /** French page description */
  protected String frenchDescription = "Déscription";

  /** German page coverage */
  protected String germanCoverage = "Zürich"; 

  /** French page coverage */
  protected String frenchCoverage = "Zurich";

  /** Content creation date */
  protected Date creationDate = new Date(1231358741000L);
  
  /** French modification date */
  protected Date frenchModificationDate = new Date(1234994800000L);

  /** Publishing start date */
  protected Date publishingStartDate = new Date(1146851901000L);

  /** Publishing end date */
  protected Date publishingEndDate = new Date(1262307600000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** One day after the date identified by futureDate */
  protected Date dayAfterFutureDate = new Date(futureDate.getTime() + Times.MS_PER_DAY);

  /** One day before the date identified by futureDate */
  protected Date dayBeforeFutureDate = new Date(2000000000000L - Times.MS_PER_DAY);

  /** Creator */
  protected User hans = new UserImpl("hans", "testland", "Hans Muster");
  
  /** French editor */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /** Rights declaration */
  protected String germanRights = "Copyright 2009 by T. Wunden";
  
  /** The subjects */
  protected String[] subjects = new String[] { "This subject", "Other subject"};
  
  /** Name of the composer */
  protected String composer = "main";
  
  /** Pagelet module */
  protected String module = "text";
  
  /** Pagelet identifier */
  protected String pagelet = "title";
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    page = new PageImpl(pageURI);
    page.setIndexed(isIndexed);
    page.setPromoted(isPromoted);
    page.setCoverage(germanCoverage, german);
    page.setCoverage(frenchCoverage, french);
    ((PageImpl)page).setCreated(hans, creationDate);
    page.setDescription(germanDescription, german);
    page.setDescription(frenchDescription, french);
    page.setLayout(layout);
    page.setLocked(amelie);
    page.setModified(amelie, frenchModificationDate);
    page.setOwner(hans);
    page.setPublished(hans, publishingStartDate, publishingEndDate);
    page.setRights(germanRights, german);
    page.setTemplate(template);
    page.setTitle(germanTitle, german);
    page.setTitle(frenchTitle, french);
    page.setType(pageType);
    for (String subject : subjects)
      page.addSubject(subject);
    page.addPagelet(new PageletImpl(module, pagelet), composer);
  }
  
  /**
   * Preliminary setup work.
   */
  protected void setupPrerequisites() {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getDefaultLanguage()).andReturn(german);    
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("admin"));    
    EasyMock.expect(site.getDefaultLanguage()).andReturn(german);    
    EasyMock.replay(site);
    pageURI = new PageURIImpl(site, "/service/test", "4bb19980-8f98-4873-a813-71b6dfab22as", Resource.LIVE);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(pageURI.hashCode(), page.hashCode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getType()}.
   */
  @Test
  public void testGetType() {
    assertEquals(pageType, page.getType());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#removeSubject(java.lang.String)}.
   */
  @Test
  public void testRemoveSubject() {
    page.removeSubject(subjects[0]);
    assertEquals(subjects.length - 1, page.getSubjects().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getURI()}.
   */
  @Test
  public void testGetURI() {
    assertEquals(pageURI, page.getURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPreview()}.
   */
  @Test
  public void testGetPreview() {
    // FIXME Implement test case
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPublisher()}.
   */
  @Test
  public void testGetPublisher() {
    assertEquals(hans, page.getPublisher());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPublishFrom()}.
   */
  @Test
  public void testGetPublishFrom() {
    assertEquals(publishingStartDate, page.getPublishFrom());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPublishTo()}.
   */
  @Test
  public void testGetPublishTo() {
    assertEquals(publishingEndDate, page.getPublishTo());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isPromoted()}.
   */
  @Test
  public void testIsPromoted() {
    assertEquals(isPromoted, page.isPromoted());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isIndexed()}.
   */
  @Test
  public void testIsIndexed() {
    assertEquals(isIndexed, page.isIndexed());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getCoverage()}.
   */
  @Test
  public void testGetCoverage() {
    assertEquals(germanCoverage, page.getCoverage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getCoverage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetCoverageLanguage() {
    assertEquals(germanCoverage, page.getCoverage(german));
    assertEquals(frenchCoverage, page.getCoverage(french));
    assertEquals(germanCoverage, page.getCoverage(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getCoverage(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetCoverageLanguageBoolean() {
    assertEquals(germanCoverage, page.getCoverage(german, false));
    assertEquals(germanCoverage, page.getCoverage(german, true));
    assertEquals(frenchCoverage, page.getCoverage(french, false));
    assertEquals(frenchCoverage, page.getCoverage(french, true));
    assertEquals(germanCoverage, page.getCoverage(italian, false));
    assertTrue(page.getCoverage(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getDescription()}.
   */
  @Test
  public void testGetDescription() {
    assertEquals(germanDescription, page.getDescription());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getDescription(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetDescriptionLanguage() {
    assertEquals(germanDescription, page.getDescription(german));
    assertEquals(frenchDescription, page.getDescription(french));
    assertEquals(germanDescription, page.getDescription(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getDescription(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetDescriptionLanguageBoolean() {
    assertEquals(germanDescription, page.getDescription(german, false));
    assertEquals(germanDescription, page.getDescription(german, true));
    assertEquals(frenchDescription, page.getDescription(french, false));
    assertEquals(frenchDescription, page.getDescription(french, true));
    assertEquals(germanDescription, page.getDescription(italian, false));
    assertTrue(page.getDescription(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getRights()}.
   */
  @Test
  public void testGetRights() {
    assertEquals(germanRights, page.getRights());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getRights(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetRightsLanguage() {
    assertEquals(germanRights, page.getRights(german));
    assertEquals(germanRights, page.getRights(french));
    assertEquals(germanRights, page.getRights(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getRights(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetRightsLanguageBoolean() {
    assertEquals(germanRights, page.getRights(german, false));
    assertEquals(germanRights, page.getRights(german, true));
    assertEquals(germanRights, page.getRights(french, false));
    assertTrue(page.getRights(french, true) == null);
    assertEquals(germanRights, page.getRights(italian, false));
    assertTrue(page.getRights(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    assertEquals(germanTitle, page.getTitle());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetTitleLanguage() {
    assertEquals(germanTitle, page.getTitle(german));
    assertEquals(frenchTitle, page.getTitle(french));
    assertEquals(germanTitle, page.getTitle(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getTitle(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetTitleLanguageBoolean() {
    assertEquals(germanTitle, page.getTitle(german, false));
    assertEquals(germanTitle, page.getTitle(german, true));
    assertEquals(frenchTitle, page.getTitle(french, false));
    assertEquals(frenchTitle, page.getTitle(french, true));
    assertEquals(germanTitle, page.getTitle(italian, false));
    assertTrue(page.getTitle(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getLayout()}.
   */
  @Test
  public void testGetLayout() {
    assertEquals(layout, page.getLayout());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getTemplate()}.
   */
  @Test
  public void testGetTemplate() {
    assertEquals(template, page.getTemplate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#hasSubject(String)}.
   */
  @Test
  public void testHasSubject() {
    assertTrue(page.hasSubject(subjects[0]));
    assertFalse(page.hasSubject("xxx"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getSubjects()}.
   */
  @Test
  public void testGetSubjects() {
    assertEquals(subjects.length, page.getSubjects().length);
    
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#allow(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testAllow() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#deny(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testDeny() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  @Ignore
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  @Ignore
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#permissions()}.
   */
  @Test
  @Ignore
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getOwner()}.
   */
  @Test
  public void testGetOwner() {
    assertEquals(hans, page.getOwner());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(page.equals(page));
    assertTrue(page.equals(new PageImpl(pageURI)));
    assertFalse(page.equals(new PageImpl(new PageURIImpl(site, "/test/2", Resource.LIVE))));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isPublished()}.
   */
  @Test
  public void testIsPublished() {
    Date yesterday = new Date(new Date().getTime() - Times.MS_PER_DAY);
    Date tomorrow = new Date(new Date().getTime() + Times.MS_PER_DAY);
    page.setPublished(amelie, yesterday, tomorrow);
    assertTrue(page.isPublished());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isPublished(java.util.Date)}.
   */
  @Test
  public void testIsPublishedDate() {
    Date d = new Date(publishingStartDate.getTime() + Times.MS_PER_DAY);
    assertTrue(page.isPublished(d));
    assertFalse(page.isPublished(futureDate));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getCreationDate()}.
   */
  @Test
  public void testGetCreationDate() {
    assertEquals(creationDate, page.getCreationDate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getCreator()}.
   */
  @Test
  public void testGetCreator() {
    assertEquals(hans, page.getCreator());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isCreatedAfter(java.util.Date)}.
   */
  @Test
  public void testIsCreatedAfter() {
    assertFalse(page.isCreatedAfter(futureDate));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getModificationDate()}.
   */
  @Test
  public void testGetModificationDate() {
    assertEquals(frenchModificationDate, page.getModificationDate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getModifier()}.
   */
  @Test
  public void testGetModifier() {
    assertEquals(amelie, page.getModifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getLockOwner()}.
   */
  @Test
  public void testGetLockOwner() {
    assertEquals(amelie, page.getLockOwner());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#setUnlocked()}.
   */
  @Test
  public void testSetUnlocked() {
    page.setUnlocked();
    assertFalse(page.isLocked());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#isLocked()}.
   */
  @Test
  public void testIsLocked() {
    assertTrue(page.isLocked());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet, java.lang.String)}.
   */
  @Test
  public void testAddPageletPageletString() {
    Pagelet p = new PageletImpl(module, pagelet);
    assertTrue(p.getURI() == null);
    page.addPagelet(p, composer);
    assertTrue(p.getURI() != null);
    assertEquals(2, page.getPagelets(composer).length);
    page.addPagelet(p, "xxx");
    assertEquals(1, page.getPagelets("xxx").length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet, java.lang.String, int)}.
   */
  @Test
  public void testAddPageletPageletStringInt() {
    Pagelet p = new PageletImpl(module, pagelet);
    page.addPagelet(p, composer, 0);
    page.addPagelet(p, composer, 1);
    assertEquals(3, page.getPagelets(composer).length);
    page.addPagelet(p, "xxx", 0);
    assertEquals(1, page.getPagelets("xxx").length);
    try {
      page.addPagelet(p, composer, 5);
      fail("Should not be able to add pagelet at impossible position");
    } catch (IndexOutOfBoundsException e) {
      // This is expected
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPagelets(java.lang.String)}.
   */
  @Test
  public void testGetPageletsString() {
    assertEquals(0, page.getPagelets("xxx").length);
    assertEquals(1, page.getPagelets(composer).length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#getPagelets(java.lang.String, java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetPageletsStringStringString() {
    assertEquals(1, page.getPagelets(composer, "text", "title").length);
    assertEquals(0, page.getPagelets("xxx", "text", "title").length);
    assertEquals(0, page.getPagelets(composer, "text", "xxx").length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#removePagelet(java.lang.String, int)}.
   */
  @Test
  public void testRemovePagelet() {
    try {
      page.removePagelet("xxx", 0);
      fail("Should not be able to remove from non-existing composer");
    } catch (IndexOutOfBoundsException e) {
      // This is expected
    }
    try {
      page.removePagelet(composer, 1);
      fail("Should not be able to remove non-existing pagelet");
    } catch (IndexOutOfBoundsException e) {
      // This is expected
    }
    page.removePagelet(composer, 0);
    assertEquals(0, page.getPagelets(composer).length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#compareTo(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testCompareTo() {
    Page p2 = new PageImpl(new PageURIImpl(site, "/test/2", Resource.LIVE));
    p2.setTitle(germanTitle, german);
    assertEquals(0, page.compareTo(p2, german));
  }

}