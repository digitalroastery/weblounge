/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Comment PageRepositoryTest
 */
public class PageRepositoryTest {

  @ClassRule
  // CHECKSTYLE:OFF - field must be public, because it's a ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();
  // CHECKSTYLE:ON

  private static TransientRepository repository = null;

  private static PageRepository pageRepository = null;

  /** Page template */
  private static PageTemplate template = null;

  /** The mock site */
  private static Site site = null;

  private Page page1 = null;

  private Page page2 = null;

  @BeforeClass
  public static void setUpClass() throws Exception {
    File dir = temp.newFolder("repository");
    File xml = new File(PageRepositoryTest.class.getResource("/repository.xml").toURI());
    repository = new TransientRepository(xml, dir);

    // Template
    template = EasyMock.createNiceMock(PageTemplate.class);
    EasyMock.expect(template.getIdentifier()).andReturn("templateid").anyTimes();
    EasyMock.expect(template.getStage()).andReturn("non-existing").anyTimes();
    EasyMock.replay(template);

    Set<Language> languages = new HashSet<Language>();
    languages.add(LanguageUtils.getLanguage("en"));
    languages.add(LanguageUtils.getLanguage("de"));

    // Site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.expect(site.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(site.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(site.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("testsite")).anyTimes();
    EasyMock.replay(site);

    // JCR repository
    pageRepository = new PageRepositoryStub();
    pageRepository.bindRepository(repository);
    pageRepository.bindSite(site);
  }

  @AfterClass
  public static void afterClass() {
    repository.shutdown();
    repository = null;
  }

  @Before
  public void setUp() throws Exception {

    // Pages
    PageReader pageReader = new PageReader();
    InputStream is = this.getClass().getResourceAsStream("/page1.xml");
    page1 = pageReader.read(is, site);
    IOUtils.closeQuietly(is);

    is = this.getClass().getResourceAsStream("/page2.xml");
    page2 = pageReader.read(is, site);
    IOUtils.closeQuietly(is);
  }

  @Test
  public void testAddPageWithInvalidPath() throws Exception {
    ResourceURI uri = new ResourceURIImpl(Page.TYPE, site, "invalid:path");

    PageReader pageReader = new PageReader();
    InputStream is1 = this.getClass().getResourceAsStream("/page1.xml");
    Page page1 = pageReader.read(is1, site);
    IOUtils.closeQuietly(is1);

    pageRepository.addPage(uri, page1);
  }

  @Test
  public void testAddPageNull() throws Exception {
    try {
      pageRepository.addPage(null, page1);
      fail("Adding a page without an URI should throw an exception");
    } catch (IllegalArgumentException e) {
    }

    try {
      pageRepository.addPage(page1.getURI(), null);
      fail("Adding a null-value page should throw an exception");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testAddPageWithVersion() throws Exception {
    ResourceURI uri = page1.getURI();
    uri.setPath("/test-page-with-version-live");
    uri.setVersion(Resource.LIVE);
    page1.setVersion(Resource.LIVE);

    Page page = pageRepository.addPage(uri, page1);
    assertEquals(Resource.LIVE, page.getVersion());

    uri.setPath("/test-page-with-version-work");
    page1.setVersion(Resource.WORK);

    page = pageRepository.addPage(uri, page1);
    assertEquals(Resource.WORK, page.getVersion());
  }

  @Test
  public void testAddPage() throws Exception {

    // // Prepare the pages
    // PageReader pageReader = new PageReader();
    // InputStream is1 = this.getClass().getResourceAsStream("/page1.xml");
    // Page page1 = pageReader.read(is1, site);
    // IOUtils.closeQuietly(is1);
    // InputStream is2 = this.getClass().getResourceAsStream("/page2.xml");
    // Page page2 = pageReader.read(is2, site);
    // IOUtils.closeQuietly(is2);

    try {
      ResourceURI uri = page1.getURI();
      pageRepository.addPage(uri, page1);

      Page pageRead = pageRepository.getPage(uri);
      assertEquals("home", pageRead.getTemplate());
      // assertEquals("news", pageRead.getLayout());
      //
      // page1.setTemplate("my-new-template");
      // page1.setVersion(Resource.WORK);
      // pageRepository.updatePage(page1.getURI(), page1);
      //
      // page1.setVersion(Resource.LIVE);
      // pageRepository.updatePage(page1.getURI(), page1);
    } catch (ContentRepositoryException e) {
      fail("Failed to add page to the repository: " + e.getMessage());
    }

    try {
      ResourceURI uri = page1.getURI();
      uri.setVersion(100);
      pageRepository.getPage(uri);
      fail("Version 100 should not be present");
    } catch (ContentRepositoryException e) {
      // expected
    }
  }

  @Test
  public void testGetVersions() throws Exception {
    ResourceURI uri = page1.getURI();
    uri.setPath("test-get-versions");

    pageRepository.addPage(uri, page1);
    pageRepository.updatePage(uri, page1);
    pageRepository.updatePage(uri, page1);
    pageRepository.updatePage(uri, page1);

    List<String> versions = pageRepository.getVersions(uri);
    assertEquals(4, versions.size());
  }

}
