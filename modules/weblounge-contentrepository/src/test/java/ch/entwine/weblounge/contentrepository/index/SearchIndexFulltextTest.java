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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.SearchQuery;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link SearchIndex}.
 */
public class SearchIndexFulltextTest {

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

  /** The sample work page */
  protected Page workPage = null;

  /** The sample pagelet */
  protected Pagelet pagelet = null;

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

    // Add the live page
    try {
      is = this.getClass().getResourceAsStream(pageFile);
      livePage = pageReader.read(is, site);
      pagelet = livePage.getPagelets()[0];
      idx.add(livePage);
    } finally {
      IOUtils.closeQuietly(is);
    }

    // Add the work page
    try {
      is = this.getClass().getResourceAsStream(pageFile);
      workPage = pageReader.read(is, site);
      workPage.setVersion(Resource.WORK);
      idx.add(workPage);
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
  public void testGetWithPath() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getURI().getPath());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getURI().getPath());
    assertEquals(1, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPathPrefix() throws Exception {
    String path = livePage.getURI().getPath();
    String pathPrefix = path.substring(0, path.indexOf('/', 1));
    SearchQuery q = new SearchQueryImpl(site).withFulltext(true, pathPrefix);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(true, pathPrefix);
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithTitle() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getTitle());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getTitle());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithDescription() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getDescription());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getDescription());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithSubject() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getSubjects()[0]);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getSubjects()[0]);
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCoverage() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getCoverage());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getCoverage());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithOwner() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getOwner().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getOwner().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCreator() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getCreator().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getCreator().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithModifier() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getModifier().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getModifier().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPublisher() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getPublisher().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getPublisher().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithLockOwner() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(livePage.getLockOwner().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(livePage.getLockOwner().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithSubjects() throws Exception {
    String subject = "subject";
    SearchQuery q = new SearchQueryImpl(site).withFulltext(subject);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(subject);
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithContent() throws Exception {
    String text = "text";
    SearchQuery q = new SearchQueryImpl(site).withFulltext(text);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(text);
    assertEquals(1, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletOwner() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getOwner().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getOwner().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletCreator() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getCreator().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getCreator().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletModifier() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getModifier().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getModifier().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletPublisher() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getPublisher().getName());
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getPublisher().getName());
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletContent() throws Exception {
    String pageletContentId = "textid";
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getContent(pageletContentId));
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getContent(pageletContentId));
    assertEquals(1, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithWildcardPageletContent() throws Exception {
    String textPrefix = pagelet.getContent("textid").substring(0, 2);
    SearchQuery q = new SearchQueryImpl(site).withFulltext(true, textPrefix);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(true, pagelet.getContent("textid").substring(0, 2));
    assertEquals(1, idx.getByQuery(q).getItems().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithProperty() throws Exception {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(pagelet.getProperty("propertyid"));
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(pagelet.getProperty("propertyid"));
    assertEquals(0, idx.getByQuery(q).getItems().length);
  }

}
