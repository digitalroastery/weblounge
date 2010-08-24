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

package ch.o2it.weblounge.common.content.file;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.file.FileContentImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
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
 * Test case to test {@link FileResourceImpl}.
 */
public class FileImplTest {

  /** The file that was read in */
  protected FileResource file = null;
  
  /** The file uri */
  protected ResourceURIImpl fileURI = null;
  
  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The English language */
  protected Language english = new LanguageImpl(new Locale("en"));

  /** The French language */
  protected Language french = new LanguageImpl(new Locale("fr"));

  /** The Italian language */
  protected Language italian = new LanguageImpl(new Locale("it"));

  /** The site */
  protected Site site = null;
  
  /** The file type */
  protected String fileType = "File";

  /** Indexed */
  protected boolean isIndexed = true;

  /** Anchor file */
  protected boolean isPromoted = true;
  
  /** German file title */
  protected String germanTitle = "Seitentitel"; 

  /** French file title */
  protected String frenchTitle = "Il titre de la page"; 

  /** German file description */
  protected String germanDescription = "Beschreibung"; 

  /** French file description */
  protected String frenchDescription = "Déscription";

  /** German file coverage */
  protected String germanCoverage = "Zürich"; 

  /** French file coverage */
  protected String frenchCoverage = "Zurich";

  /** Content creation date */
  protected Date creationDate = new Date(1234037141000L);
  
  /** Content Modification date */
  protected Date modificationDate = new Date(1237414000000L);

  /** Publishing start date */
  protected Date publishingStartDate = new Date(1144259901000L);

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
  
  /** The English file name */
  protected String englishFilename = "image.jpg";

  /** The English file name */
  protected String englishMimetype = "image/jpeg";
  
  /** The English file size */
  protected long englishFilesize = 745569L;

  /** The German file name */
  protected String germanFilename = "image.png";

  /** The German file name */
  protected String germanMimetype = "image/png";

  /** The German file size */
  protected long germanFilesize = 520894L;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    file = new FileResourceImpl(fileURI);
    file.setIndexed(isIndexed);
    file.setPromoted(isPromoted);
    file.setCoverage(germanCoverage, german);
    file.setCoverage(frenchCoverage, french);
    ((FileResourceImpl)file).setCreated(hans, creationDate);
    file.setDescription(germanDescription, german);
    file.setDescription(frenchDescription, french);
    file.setLocked(amelie);
    file.setModified(amelie, modificationDate);
    file.setOwner(hans);
    file.setPublished(hans, publishingStartDate, publishingEndDate);
    file.setRights(germanRights, german);
    file.setTitle(germanTitle, german);
    file.setTitle(frenchTitle, french);
    file.setType(fileType);
    for (String subject : subjects)
      file.addSubject(subject);
    
    FileContentImpl germanContent = new FileContentImpl(germanFilename, german, germanMimetype, germanFilesize);
    germanContent.setCreated(creationDate, amelie);
    file.addContent(germanContent);

    FileContentImpl englishContent = new FileContentImpl(englishFilename, english, englishMimetype, englishFilesize);
    englishContent.setCreated(modificationDate, amelie);
    file.addContent(englishContent);
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
    fileURI = new FileResourceURIImpl(site, "/service/test", "4bb19980-8f98-4873-a813-71b6dfab22as", Resource.LIVE);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(fileURI.hashCode(), file.hashCode());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, file.getURI().getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getVersion()}.
   */
  @Test
  public void testGetVersion() {
    assertEquals(fileURI.getVersion(), file.getURI().getVersion());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getType()}.
   */
  @Test
  public void testGetType() {
    assertEquals(fileType, file.getType());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#removeSubject(java.lang.String)}.
   */
  @Test
  public void testRemoveSubject() {
    file.removeSubject(subjects[0]);
    assertEquals(subjects.length - 1, file.getSubjects().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getURI()}.
   */
  @Test
  public void testGetURI() {
    assertEquals(fileURI, file.getURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getPublisher()}.
   */
  @Test
  public void testGetPublisher() {
    assertEquals(hans, file.getPublisher());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getPublishFrom()}.
   */
  @Test
  public void testGetPublishFrom() {
    assertEquals(publishingStartDate, file.getPublishFrom());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getPublishTo()}.
   */
  @Test
  public void testGetPublishTo() {
    assertEquals(publishingEndDate, file.getPublishTo());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isPromoted()}.
   */
  @Test
  public void testIsPromoted() {
    assertEquals(isPromoted, file.isPromoted());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isIndexed()}.
   */
  @Test
  public void testIsIndexed() {
    assertEquals(isIndexed, file.isIndexed());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getCoverage()}.
   */
  @Test
  public void testGetCoverage() {
    assertEquals(germanCoverage, file.getCoverage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getCoverage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetCoverageLanguage() {
    assertEquals(germanCoverage, file.getCoverage(german));
    assertEquals(frenchCoverage, file.getCoverage(french));
    assertEquals(germanCoverage, file.getCoverage(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getCoverage(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetCoverageLanguageBoolean() {
    assertEquals(germanCoverage, file.getCoverage(german, false));
    assertEquals(germanCoverage, file.getCoverage(german, true));
    assertEquals(frenchCoverage, file.getCoverage(french, false));
    assertEquals(frenchCoverage, file.getCoverage(french, true));
    assertEquals(germanCoverage, file.getCoverage(italian, false));
    assertTrue(file.getCoverage(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getDescription()}.
   */
  @Test
  public void testGetDescription() {
    assertEquals(germanDescription, file.getDescription());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getDescription(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetDescriptionLanguage() {
    assertEquals(germanDescription, file.getDescription(german));
    assertEquals(frenchDescription, file.getDescription(french));
    assertEquals(germanDescription, file.getDescription(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getDescription(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetDescriptionLanguageBoolean() {
    assertEquals(germanDescription, file.getDescription(german, false));
    assertEquals(germanDescription, file.getDescription(german, true));
    assertEquals(frenchDescription, file.getDescription(french, false));
    assertEquals(frenchDescription, file.getDescription(french, true));
    assertEquals(germanDescription, file.getDescription(italian, false));
    assertTrue(file.getDescription(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getRights()}.
   */
  @Test
  public void testGetRights() {
    assertEquals(germanRights, file.getRights());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getRights(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetRightsLanguage() {
    assertEquals(germanRights, file.getRights(german));
    assertEquals(germanRights, file.getRights(french));
    assertEquals(germanRights, file.getRights(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getRights(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetRightsLanguageBoolean() {
    assertEquals(germanRights, file.getRights(german, false));
    assertEquals(germanRights, file.getRights(german, true));
    assertEquals(germanRights, file.getRights(french, false));
    assertTrue(file.getRights(french, true) == null);
    assertEquals(germanRights, file.getRights(italian, false));
    assertTrue(file.getRights(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    assertEquals(germanTitle, file.getTitle());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getTitle(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetTitleLanguage() {
    assertEquals(germanTitle, file.getTitle(german));
    assertEquals(frenchTitle, file.getTitle(french));
    assertEquals(germanTitle, file.getTitle(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getTitle(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetTitleLanguageBoolean() {
    assertEquals(germanTitle, file.getTitle(german, false));
    assertEquals(germanTitle, file.getTitle(german, true));
    assertEquals(frenchTitle, file.getTitle(french, false));
    assertEquals(frenchTitle, file.getTitle(french, true));
    assertEquals(germanTitle, file.getTitle(italian, false));
    assertTrue(file.getTitle(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#hasSubject(String)}.
   */
  @Test
  public void testHasSubject() {
    assertTrue(file.hasSubject(subjects[0]));
    assertFalse(file.hasSubject("xxx"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getSubjects()}.
   */
  @Test
  public void testGetSubjects() {
    assertEquals(subjects.length, file.getSubjects().length);
    
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#allow(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testAllow() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#deny(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testDeny() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testCheckPermissionAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test
  @Ignore
  public void testCheckPermissionSetAuthority() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  @Ignore
  public void testCheckOne() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])}.
   */
  @Test
  @Ignore
  public void testCheckAll() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#permissions()}.
   */
  @Test
  @Ignore
  public void testPermissions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getOwner()}.
   */
  @Test
  public void testGetOwner() {
    assertEquals(hans, file.getOwner());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(file.equals(file));
    assertTrue(file.equals(new FileResourceImpl(fileURI)));
    assertFalse(file.equals(new FileResourceImpl(new FileResourceURIImpl(site, "/test/2", Resource.LIVE))));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isPublished()}.
   */
  @Test
  public void testIsPublished() {
    Date yesterday = new Date(new Date().getTime() - Times.MS_PER_DAY);
    Date tomorrow = new Date(new Date().getTime() + Times.MS_PER_DAY);
    file.setPublished(amelie, yesterday, tomorrow);
    assertTrue(file.isPublished());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isPublished(java.util.Date)}.
   */
  @Test 
  public void testIsPublishedDate() {
    Date d = new Date(publishingStartDate.getTime() + Times.MS_PER_DAY);
    assertTrue(file.isPublished(d));
    assertFalse(file.isPublished(futureDate));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getCreationDate()}.
   */
  @Test
  public void testGetCreationDate() {
    assertEquals(creationDate, file.getCreationDate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getCreator()}.
   */
  @Test
  public void testGetCreator() {
    assertEquals(hans, file.getCreator());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isCreatedAfter(java.util.Date)}.
   */
  @Test
  public void testIsCreatedAfter() {
    assertFalse(file.isCreatedAfter(futureDate));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getModificationDate()}.
   */
  @Test
  public void testGetModificationDate() {
    assertEquals(modificationDate, file.getModificationDate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getModifier()}.
   */
  @Test
  public void testGetModifier() {
    assertEquals(amelie, file.getModifier());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#getLockOwner()}.
   */
  @Test
  public void testGetLockOwner() {
    assertEquals(amelie, file.getLockOwner());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#setUnlocked()}.
   */
  @Test
  public void testSetUnlocked() {
    file.setUnlocked();
    assertFalse(file.isLocked());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#isLocked()}.
   */
  @Test
  public void testIsLocked() {
    assertTrue(file.isLocked());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.FileResourceImpl#compareTo(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testCompareTo() {
    FileResource p2 = new FileResourceImpl(new FileResourceURIImpl(site, "/test/2", Resource.LIVE));
    p2.setTitle(germanTitle, german);
    assertEquals(0, file.compareTo(p2, german));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceImpl#getContent(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetContent() {
    assertNotNull(file.getContent(german));
    assertEquals(german, file.getContent(german).getLanguage());
    assertNotNull(file.getContent(english));
    assertEquals(english, file.getContent(english).getLanguage());
    assertNull(file.getContent(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceImpl#getOriginalContent()}.
   */
  @Test
  public void testGetOriginalContent() {
    assertNotNull(file.getOriginalContent());
    assertEquals(german, file.getOriginalContent().getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceImpl#removeContent(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testRemoveContent() {
    file.removeContent(italian);
    file.removeContent(german);
    assertEquals(1, file.contents().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceImpl#contents()}.
   */
  @Test
  public void testContents() {
    assertEquals(2, file.contents().size());
  }

}