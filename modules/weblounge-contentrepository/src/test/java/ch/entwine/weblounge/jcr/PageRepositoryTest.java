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

import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Comment PageRepositoryTest
 */
public class PageRepositoryTest {

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  private static File repositoryDir = null;

  private static TransientRepository repository = null;

  /** Page template */
  protected static PageTemplate template = null;

  /** The mock site */
  protected static Site site = null;

  @BeforeClass
  public static void setUpClass() throws IOException {
    repositoryDir = temp.newFolder("repository");
    repository = new TransientRepository(repositoryDir);

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
  }

  @AfterClass
  public static void afterClass() {
    repository.shutdown();
    repository = null;
  }

  @Before
  public void setUp() {

  }

  @Test
  public void testAddPage() throws Exception {
    PageRepository pageRepo = new PageRepositoryStub();
    pageRepo.setRepository(repository);

    // Prepare the pages
    PageReader pageReader = new PageReader();
    InputStream is = this.getClass().getResourceAsStream("/page1.xml");
    Page page = pageReader.read(is, site);
    IOUtils.closeQuietly(is);

    try {
      pageRepo.addPage(page.getURI(), page);
    } catch (ContentRepositoryException e) {
      fail("Failed to add page to the repository: " + e.getMessage());
    }
  }

}
