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

package ch.entwine.weblounge.contentrepository.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.contentrepository.impl.index.SearchIndex;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link SearchIndex}.
 */
public class SearchIndexRecencyPriorityTest {

  /** The search index */
  protected static SearchIndex idx = null;

  /** The index root directory */
  protected static File idxRoot = null;

  /** Flag to indicate read only index */
  protected static boolean isReadOnly = false;

  /** Page template */
  protected static PageTemplate template = null;

  /** The mock site */
  protected static Site site = null;

  /** The sample page */
  protected String pageFile = "/page.xml";

  /** The sample live page */
  protected Page livePage = null;

  /** The live page identifier */
  protected String livePageId = "00000000-0000-0000-0000-00000000000A";

  /** The other live page identifier */
  protected String otherLivePageId = "00000000-0000-0000-0000-00000000000B";

  /** The other sample live page */
  protected Page otherLivePage = null;

  /** The sample pagelet */
  protected Pagelet pagelet = null;

  /** Sample content */
  protected String content = null;

  /** German */
  protected Language german = LanguageUtils.getLanguage(Locale.GERMAN);

  /** The resource serializer */
  private static ResourceSerializerServiceImpl serializer = null;

  /**
   * Sets up the solr search index. Since solr sometimes has a hard time
   * shutting down cleanly, it's done only once for all the tests.
   * 
   * @throws Exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
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
    EasyMock.replay(site);

    // Resource serializer
    serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());

    String rootPath = PathUtils.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    idxRoot = new File(rootPath);
    System.setProperty("weblounge.home", rootPath);
    ElasticSearchUtils.createIndexConfigurationAt(idxRoot);
    idx = new SearchIndex(site, serializer, isReadOnly);
  }

  /**
   * Does the cleanup after the test suite.
   */
  @AfterClass
  public static void tearDownClass() {
    try {
      idx.close();
      FileUtils.deleteQuietly(idxRoot);
    } catch (IOException e) {
      fail("Error closing search index: " + e.getMessage());
    }
  }

  /**
   * Creates the test setup.
   * 
   * @throws java.lang.Exception
   *           if setup of the index fails
   */
  @Before
  public void setUp() throws Exception {
    // Prepare the pages
    PageReader pageReader = new PageReader();
    InputStream is = null;
    Calendar date = Calendar.getInstance();
    Date today = date.getTime();
    date.add(Calendar.YEAR, -1);
    Date lastYear = date.getTime();

    // Add the live page
    try {
      is = this.getClass().getResourceAsStream(pageFile);
      livePage = pageReader.read(is, site);
      livePage.setIdentifier(livePageId);
      livePage.setCreated(livePage.getCreator(), lastYear);
      livePage.setModified(livePage.getModifier(), lastYear);
      pagelet = livePage.getPagelets()[0];

      // Duplicate the content to gain higher ranking

      String elementId = "textid";
      content = pagelet.getContent(elementId, german);
      pagelet.setContent(elementId, content + " " + content, german);
      idx.add(livePage);
    } finally {
      IOUtils.closeQuietly(is);
    }

    // Add the second live page
    try {
      is = this.getClass().getResourceAsStream(pageFile);
      otherLivePage = pageReader.read(is, site);
      otherLivePage.setIdentifier(otherLivePageId);
      otherLivePage.setCreated(otherLivePage.getCreator(), today);
      otherLivePage.setModified(otherLivePage.getModifier(), today);
      idx.add(otherLivePage);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Does the cleanup after each test.
   */
  @After
  public void tearDown() throws Exception {
    idx.clear();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithRecencyPriority() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withText(true, content);

    // Search without recency boosting
    SearchResultItem[] items = idx.getByQuery(q).getItems();
    assertEquals(2, items.length);
    assertEquals(livePageId, items[0].getId());
    assertEquals(otherLivePageId, items[1].getId());

    // Search with recency boosting
    q.withRececyPriority();
    items = idx.getByQuery(q).getItems();
    assertEquals(2, items.length);
    assertEquals(otherLivePageId, items[0].getId());
    assertEquals(livePageId, items[1].getId());
  }

}
