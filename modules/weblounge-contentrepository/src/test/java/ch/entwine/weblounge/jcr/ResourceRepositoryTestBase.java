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

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Comment ResourceRepositoryTestBase
 */
public class ResourceRepositoryTestBase extends WebloungeJCRTestBase {

  /** Page template */
  protected static PageTemplate template = null;

  /** The mock site */
  protected static Site site = null;

  protected Page page1 = null;

  protected Page page2 = null;

  @BeforeClass
  public static void setupEnvironment() throws Exception {
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

}
