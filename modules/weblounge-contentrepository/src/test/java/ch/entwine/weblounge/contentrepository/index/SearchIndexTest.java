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

import static ch.entwine.weblounge.common.content.SearchQuery.Quantifier.All;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.NotImplementedException;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Order;
import ch.entwine.weblounge.common.content.SearchQuery.Quantifier;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceReader;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceReader;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.search.SearchIndex;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.search.impl.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link SearchIndexImpl}.
 */
public class SearchIndexTest {

  /** The search index */
  protected static SearchIndexImplStub idx = null;

  /** The index root directory */
  protected static File idxRoot = null;

  /** Flag to indicate read only index */
  protected static boolean isReadOnly = false;

  /** Page template */
  protected static PageTemplate template = null;

  /** The mock site */
  protected static Site testSite = null;

  /** The second mock site */
  protected static Site demoSite = null;

  /** The sample pages of the test site */
  protected Page[] testPages = null;
  
  /** The sample pages of the demo site */
  protected Page[] demoPages = null;

  /** The sample file */
  protected FileResource file = null;

  /** The sample file */
  protected ImageResource image = null;

  /** UUID of page 1 */
  protected String uuid1 = "4bb19980-8f98-4873-a813-71b6dfab22af";

  /** UUID of page 2 */
  protected String uuid2 = "4bb19980-8f98-4873-a813-71b6dfab22ag";

  /** UUID of the image resource */
  protected String imageid = "4bb19980-8f98-4873-a813-71b6dfab22as";

  /** Path of page 1 */
  protected String path1 = "/test/";

  /** Path of page 2 */
  protected String path2 = "/a/b/c";

  /** The topic */
  protected String subject = "topic";

  /** Filename */
  protected String filename = "image.jpg";

  /** Mime type */
  protected String mimetype = "image/jpeg";

  /** Element key */
  protected String elementId = "title";

  /** Element value */
  protected String elementValue = "joyeux";

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
    testSite = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(testSite.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.expect(testSite.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(testSite.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(testSite.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.expect(testSite.getAdministrator()).andReturn(new SiteAdminImpl("testsite")).anyTimes();
    EasyMock.replay(testSite);

    demoSite = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(demoSite.getIdentifier()).andReturn("demo").anyTimes();
    EasyMock.expect(demoSite.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(demoSite.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(demoSite.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.expect(demoSite.getAdministrator()).andReturn(new SiteAdminImpl("demosite")).anyTimes();
    EasyMock.replay(demoSite);

    // Resource serializer
    serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());

    // Index
    String rootPath = PathUtils.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    System.setProperty("weblounge.home", rootPath);
    idxRoot = new File(rootPath);
    ElasticSearchUtils.createIndexConfigurationAt(idxRoot);
    idx = SearchIndexImplStub.mkSearchIndexImplStub();
    idx.bindResourceSerializerService(serializer);
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
    testPages = new Page[2];
    for (int i = 0; i < testPages.length; i++) {
      InputStream is = this.getClass().getResourceAsStream("/page" + (i + 1) + ".xml");
      testPages[i] = pageReader.read(is, testSite);
      IOUtils.closeQuietly(is);
    }
    demoPages = new Page[2];
    for (int i = 0; i < demoPages.length; i++) {
      InputStream is = this.getClass().getResourceAsStream("/page" + (i + 1) + ".xml");
      demoPages[i] = pageReader.read(is, demoSite);
      IOUtils.closeQuietly(is);
    }

    // Prepare the sample file
    FileResourceReader fileReader = new FileResourceReader();
    InputStream fileIs = this.getClass().getResourceAsStream("/file.xml");
    file = fileReader.read(fileIs, testSite);
    IOUtils.closeQuietly(fileIs);

    // Prepare the sample image
    ImageResourceReader imageReader = new ImageResourceReader();
    InputStream imageIs = this.getClass().getResourceAsStream("/image.xml");
    image = imageReader.read(imageIs, testSite);
    IOUtils.closeQuietly(imageIs);
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
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() throws Exception {
    populateIndex();
    assertEquals(SearchIndex.INDEX_VERSION, idx.getIndexVersion());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithId() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withIdentifier(uuid1);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPath() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withPath(path1);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPathPrefix() throws Exception {
    populateIndex();

    // Add 10 sub pages
    for (int i = 0; i < 10; i++) {
      String id = UUID.randomUUID().toString();
      String path = PathUtils.concat(path1, id);
      ResourceURI uri = new PageURIImpl(testSite, path, id);
      Page p = new PageImpl(uri);
      p.setTemplate(template.getIdentifier());
      idx.add(p);

      String subPageId = UUID.randomUUID().toString();
      String subPath = PathUtils.concat(path, subPageId);
      uri = new PageURIImpl(testSite, subPath, subPageId);
      p = new PageImpl(uri);
      p.setTemplate(template.getIdentifier());
      idx.add(p);
    }

    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withPathPrefix(path1);
    q.withLimit(100);
    assertEquals(21, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPagelet() throws Exception {
    populateIndex();
    Pagelet[] mainPagelets = testPages[0].getPagelets("main");
    Pagelet titlePagelet = new PageletImpl(mainPagelets[0].getModule(), mainPagelets[0].getIdentifier());
    Pagelet imagePagelet = new PageletImpl(mainPagelets[1].getModule(), mainPagelets[1].getIdentifier());

    // Search for pages containing a pagelet (pages 1 and 2)
    SearchQuery q = new SearchQueryImpl(testSite).withPagelet(titlePagelet);
    assertEquals(2, idx.getByQuery(q).getDocumentCount());

    // Search for pages containing an image pagelet (only page 1)
    q = new SearchQueryImpl(testSite).withPagelet(imagePagelet);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());

    // Search for pages containing the title pagelet in the correct position
    // (only page 1)
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).inComposer("main");
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).inComposer("main").atPosition(0);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).atPosition(0);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());

    // Search for pages containing the title pagelet in the correct position
    // (only page 1)
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).inComposer("test");
    assertEquals(0, idx.getByQuery(q).getDocumentCount());
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).atPosition(2);
    assertEquals(0, idx.getByQuery(q).getDocumentCount());
    q = new SearchQueryImpl(testSite).withPagelet(titlePagelet).inComposer("main").atPosition(1);
    assertEquals(0, idx.getByQuery(q).getDocumentCount());

    // Find documents with both pagelets on one page
    q = new SearchQueryImpl(testSite).withPagelets(All, titlePagelet, imagePagelet);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());

    // Find documents with both pagelets on one page and in the same composer
    q = new SearchQueryImpl(testSite).withPagelets(All, titlePagelet, imagePagelet).inComposer("main");
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithTemplate() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withTemplate("default");
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithText() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withFulltext(true, "Technik");
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithWildcardText() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withFulltext(true, "Techn");
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithAuthor() throws Exception {
    populateIndex();
    User amelie = new UserImpl("amelie");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withAuthor(amelie);
    SearchResult result = idx.getByQuery(q);
    assertEquals(testPages.length, result.getDocumentCount());
    assertEquals(testPages.length, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCreator() throws Exception {
    populateIndex();
    User hans = new UserImpl("hans");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withCreator(hans);
    SearchResult result = idx.getByQuery(q);
    assertEquals(testPages.length, result.getDocumentCount());
    assertEquals(testPages.length, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCreationDate() throws Exception {
    populateIndex();
    Date date = WebloungeDateFormat.parseStatic("2009-01-07T20:05:41Z");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withCreationDate(date);
    SearchResult result = idx.getByQuery(q);
    assertEquals(1, result.getDocumentCount());
    assertEquals(1, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithModifier() throws Exception {
    populateIndex();
    User amelie = new UserImpl("amelie");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withModifier(amelie);
    SearchResult result = idx.getByQuery(q);
    assertEquals(testPages.length, result.getDocumentCount());
    assertEquals(testPages.length, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithModificationDate() throws Exception {
    populateIndex();
    Date date = WebloungeDateFormat.parseStatic("2009-02-18T22:06:40Z");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withModificationDate(date);
    SearchResult result = idx.getByQuery(q);
    assertEquals(1, result.getDocumentCount());
    assertEquals(1, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetSortedByPublicationDate() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).sortByPublishingDate(Order.Descending);
    SearchResult result = idx.getByQuery(q);
    assertEquals(testPages.length, result.getDocumentCount());
    assertEquals(testPages.length, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPublisher() throws Exception {
    populateIndex();
    User amelie = new UserImpl("amelie");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withPublisher(amelie);
    SearchResult result = idx.getByQuery(q);
    assertEquals(1, result.getDocumentCount());
    assertEquals(1, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPublishingDate() throws Exception {
    populateIndex();
    Date date = WebloungeDateFormat.parseStatic("2006-05-05T17:58:21Z");
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withPublishingDate(date);
    SearchResult result = idx.getByQuery(q);
    assertEquals(1, result.getDocumentCount());
    assertEquals(1, result.getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithSubjects() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    String[] subjects = new String[] { "Other topic", "Topic a" };
    for (String subject : subjects)
      q.withSubject(subject);
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithAllSubjects() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    q.withSubjects(Quantifier.All, "Topic a", "This subject");
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithContent() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    q.withElement(elementId, elementValue);
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithProperty() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    q.withProperty("resourceid", imageid);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithFilename() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite);
    q.withFilename(filename);
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#getByQuery(ch.entwine.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithMimetype() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite);
    q.withMimetype(mimetype);
    assertEquals(2, idx.getByQuery(q).getDocumentCount());
  }
  
  @Test
  public void testClearWithSite() throws Exception {
    populateIndex();
    
    idx.clear(demoSite);
    
    SearchQuery countDemoSite = new SearchQueryImpl(demoSite).withTypes(Page.TYPE);
    assertEquals(0, idx.getByQuery(countDemoSite).getDocumentCount());
    
    SearchQuery countTestSite = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    assertEquals(2, idx.getByQuery(countTestSite).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#clear()} .
   */
  @Test
  public void testClear() throws Exception {
    populateIndex();

    idx.clear();

    // Run a query and see if we get anything back
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    assertEquals(0, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#delete(ch.entwine.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testDelete() throws Exception {
    populateIndex();

    // Delete a page
    idx.delete(testPages[0].getURI());

    // Test if we can query for the added document
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    assertEquals(testPages.length - 1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#add(ch.entwine.weblounge.common.content.page.Page)}
   * .
   */
  @Test
  public void testAdd() throws Exception {
    populateIndex();
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    assertEquals(testPages.length, idx.getByQuery(q).getDocumentCount());
  }

  @Test(expected = NotImplementedException.class)
  public void testAddWithNotSupportedAclOrder() throws Exception {
    Page page = testPages[0];
    page.setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order.DenyAllow);
    idx.add(page);
  }

  @Test(expected = NotImplementedException.class)
  public void testUpdateWithNotSupportedAclOrder() throws Exception {
    Page page = testPages[0];
    page.setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order.DenyAllow);
    idx.update(page);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#update(ch.entwine.weblounge.common.content.page.Page)}
   * .
   */
  @Test
  public void testUpdate() throws Exception {
    populateIndex();
    String subject = "testsubject";
    Page page = testPages[0];
    page.addSubject(subject);

    // Post the update
    idx.update(page);

    // Check if the index actually reflects the updated data
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withSubject(subject);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#move(ch.entwine.weblounge.common.content.ResourceURI, java.lang.String)}
   * .
   */
  @Test
  public void testMove() throws Exception {
    populateIndex();
    String newPath = "/new/path/test";

    // Post the update
    idx.move(testPages[0].getURI(), newPath);

    // Make sure there is a page with the new path
    SearchQuery q = new SearchQueryImpl(testSite).withTypes(Page.TYPE).withPath(newPath);
    assertEquals(1, idx.getByQuery(q).getDocumentCount());

    // Make sure the number of pages remains the same
    q = new SearchQueryImpl(testSite).withTypes(Page.TYPE);
    assertEquals(testPages.length, idx.getByQuery(q).getDocumentCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.search.impl.SearchIndexImpl#suggest(ch.entwine.weblounge.contentrepository.impl.index.solr.Suggestions.Dictionary, String, boolean, int, boolean)}
   * .
   */
  @Test
  @Ignore
  public void testSuggest() throws Exception {
    populateIndex();

    String subject = "Topic a";
    String seed = subject.split(" ")[0];
    boolean onlyMorePopular = false;
    int count = 5;
    boolean collate = true;

    String dictionary = "subject";

    // Make sure the matching topic is
    List<String> suggestions = idx.suggest(dictionary, seed, onlyMorePopular, count, collate);
    assertEquals(1, suggestions.size());
    // assertEquals(subject, suggestions.first());

    // Prevent case sensitivity
    seed = seed.toLowerCase();
    suggestions = idx.suggest(dictionary, seed, onlyMorePopular, count, collate);
    assertEquals(1, suggestions.size());
    // assertEquals(subject, suggestions.first());

    // Prevent case sensitivity
    seed = "Another";
    suggestions = idx.suggest(dictionary, seed, onlyMorePopular, count, collate);
    assertEquals(2, suggestions.size());
    // assertEquals(subject, suggestions.first());
  }

  /**
   * Adds sample pages to the search index and returns the number of documents
   * added.
   *
   * @return the number of pages added
   */
  protected int populateIndex() throws Exception {
    int count = 0;

    // Add the pages
    for (Page page : testPages) {
      idx.add(page);
      count++;
    }
    for (Page page : demoPages) {
      idx.add(page);
      count++;
    }

    // Add the file
    idx.add(file);
    count++;

    // Add the image
    idx.add(image);
    count++;

    return count;
  }

}
