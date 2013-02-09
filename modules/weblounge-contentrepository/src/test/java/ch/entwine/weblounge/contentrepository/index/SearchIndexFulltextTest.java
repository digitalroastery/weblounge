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
import static org.junit.Assert.assertTrue;
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
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
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
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.AfterClass;
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
  protected static String pageFile = "/page.xml";

  /** The sample live page */
  protected static Page livePage = null;

  /** The sample work page */
  protected static Page workPage = null;

  /** The sample pagelet */
  protected static Pagelet pagelet = null;

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

    // Prepare the pages
    PageReader pageReader = new PageReader();
    InputStream is = null;

    // Add the live page
    try {
      is = SearchIndexFulltextTest.class.getResourceAsStream(pageFile);
      livePage = pageReader.read(is, site);
      pagelet = livePage.getPagelets()[0];
      idx.add(livePage);
    } finally {
      IOUtils.closeQuietly(is);
    }

    // Add the work page
    try {
      is = SearchIndexFulltextTest.class.getResourceAsStream(pageFile);
      workPage = pageReader.read(is, site);
      workPage.setVersion(Resource.WORK);
      idx.add(workPage);
    } finally {
      IOUtils.closeQuietly(is);
    }
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
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPath() throws Exception {
    String path = livePage.getURI().getPath();
    SearchQuery q = new SearchQueryImpl(site).withPathPrefix(path);
    assertEquals(2, idx.getByQuery(q).getItems().length);
    q.withText(path);
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

    // Check the full path
    String pathPrefix = path.substring(0, path.indexOf('/', 1));

    assertSearchResult(pathPrefix, true, 2, 1);

    // Check path elements
    for (String pathElement : StringUtils.split(path, "/")) {
      if (pathElement.length() > 2)
        assertSearchResult(pathElement, true, 2, 1);
    }

  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithTitle() throws Exception {
    assertSearchResult(livePage.getTitle(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithDescription() throws Exception {
    assertSearchResult(livePage.getDescription(), false, 2, 0);
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
    assertSearchResult(livePage.getOwner().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCreator() throws Exception {
    assertSearchResult(livePage.getCreator().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithModifier() throws Exception {
    assertSearchResult(livePage.getModifier().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPublisher() throws Exception {
    assertSearchResult(livePage.getPublisher().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithLockOwner() throws Exception {
    assertSearchResult(livePage.getLockOwner().getName(), false, 2, 0);
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
    assertSearchResult("text", true, 2, 1);
    assertSearchResult("titre", true, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletOwner() throws Exception {
    assertSearchResult(pagelet.getOwner().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletCreator() throws Exception {
    assertSearchResult(pagelet.getCreator().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletModifier() throws Exception {
    assertSearchResult(pagelet.getModifier().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletPublisher() throws Exception {
    assertSearchResult(pagelet.getPublisher().getName(), false, 2, 0);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPageletContent() throws Exception {
    assertSearchResult(pagelet.getContent("textid"), false, 2, 1);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithWildcardPageletContent() throws Exception {
    assertSearchResult(pagelet.getContent("textid").substring(0, 5), true, 2, 1);
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

  /**
   * Helper method to test existence of search text and parts thereof in the
   * fulltext and in the text index.
   * 
   * @param searchText
   *          the text to search for
   * @param fuzzy
   *          whether to do a fuzzy search only
   * @param expectedInFulltext
   *          expected number of hits in the fulltext index
   * @param expectedInText
   *          expected number of hits in the text index
   * @throws ContentRepositoryException
   *           if searching fails
   */
  private void assertSearchResult(String searchText, boolean fuzzy,
      int expectedInFulltext, int expectedInText)
      throws ContentRepositoryException {
    SearchQuery q = new SearchQueryImpl(site).withFulltext(fuzzy, searchText);
    assertEquals(expectedInFulltext, idx.getByQuery(q).getItems().length);
    q = new SearchQueryImpl(site).withText(fuzzy, searchText);
    assertEquals(expectedInText, idx.getByQuery(q).getItems().length);

    // Lowercase match
    q = new SearchQueryImpl(site).withFulltext(true, searchText.toLowerCase());
    assertEquals(2, idx.getByQuery(q).getItems().length);

    // Partial matches
    for (String part : StringUtils.split(searchText)) {
      q = new SearchQueryImpl(site).withFulltext(true, part);
      assertTrue(idx.getByQuery(q).getItems().length >= expectedInFulltext);
      q.withText(true, part);
      assertTrue(idx.getByQuery(q).getItems().length >= expectedInText);
    }
  }

}
