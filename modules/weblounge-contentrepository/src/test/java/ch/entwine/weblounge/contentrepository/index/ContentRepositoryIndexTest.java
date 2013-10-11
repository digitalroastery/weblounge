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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;
import ch.entwine.weblounge.search.impl.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test case for the {@link ContentRepositoryIndex}.
 */
public class ContentRepositoryIndexTest {

  /** The content repository index */
  protected static ContentRepositoryIndex idx = null;

  /** The index' root directory */
  protected static File idxRoot = null;

  /** The search index */
  protected static SearchIndexImplStub searchIdx = null;

  /** The structural index' root directory */
  protected File structuralIndexRootDirectory = null;

  /** The sample page */
  protected Page page = null;

  /** The other sample page */
  protected Page otherPage = null;

  /** File resource */
  protected FileResource file = null;

  /** The site */
  protected static Site site = null;

  /** Page template */
  protected static PageTemplate template = null;

  /** English */
  protected Language english = LanguageUtils.getLanguage("en");

  /** German */
  protected Language german = LanguageUtils.getLanguage("de");

  /** Italian */
  protected Language french = LanguageUtils.getLanguage("fr");

  /** The resource serializer */
  private static ResourceSerializerServiceImpl serializer = null;

  /**
   * Sets up static test data.
   * 
   * @throws IOException
   */
  @BeforeClass
  public static void setUpClass() throws IOException {
    // Resource serializer
    serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());

    // Template
    template = EasyMock.createNiceMock(PageTemplate.class);
    EasyMock.expect(template.getIdentifier()).andReturn("templateid").anyTimes();
    EasyMock.expect(template.getStage()).andReturn("main").anyTimes();
    EasyMock.replay(template);

    // Site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(site.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("testsite")).anyTimes();
    EasyMock.replay(site);

    // Search index
    searchIdx = new SearchIndexImplStub();
    searchIdx.bindResourceSerializerService(serializer);

    idxRoot = new File(new File(System.getProperty("java.io.tmpdir")), "index");
    FileUtils.deleteDirectory(idxRoot);

    ElasticSearchUtils.createIndexConfigurationAt(idxRoot);
    System.setProperty("weblounge.home", idxRoot.getAbsolutePath());
    TestUtils.startTesting();
    idx = new ContentRepositoryIndex(site, searchIdx);
  }

  /**
   * Cleanup work after the last test.
   * 
   * @throws IOException
   */
  @AfterClass
  public static void tearDownAfterClass() throws IOException {
    idx.close();
    FileUtils.deleteDirectory(idxRoot);
  }

  /**
   * Sets up data structures for each test case.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    page = new PageImpl(new PageURIImpl(site, "/weblounge"));
    page.setTemplate("home");

    otherPage = new PageImpl(new PageURIImpl(site, "/weblounge/other"));
    otherPage.setTemplate("home");

    file = new FileResourceImpl(new FileResourceURIImpl(site));
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    idx.clear();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#add(ch.entwine.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testAdd() {
    try {
      idx.add(page);
      assertEquals(1, idx.getResourceCount());
      idx.add(file);
      assertEquals(2, idx.getResourceCount());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }

    String oldId = page.getURI().getIdentifier();
    String oldPath = page.getURI().getPath();

    // Try to add a new resource with existing id
    try {
      page.getURI().setPath(UrlUtils.concat(file.getURI().getPath(), "pathext"));
      idx.add(page);
      fail("Managed to add a resource with an existing identifier");
    } catch (Throwable t) {
      // This is expected
      page.getURI().setPath(oldPath);
    }

    // Try to add a new resource with existing id
    try {
      page.getURI().setIdentifier(page.getIdentifier().substring(1) + "x");
      idx.add(page);
      fail("Managed to add a resource with an existing path");
    } catch (Throwable t) {
      // This is expected
      page.getURI().setIdentifier(oldId);
    }

  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#update(Resource)}
   * .
   */
  @Test
  public void testUpdate() throws IllegalArgumentException,
      ContentRepositoryException {

    String propertyName = "testproperty";
    String propertyValue = "testvalue";
    Pagelet p = new PageletImpl("testmodule", "testid");
    p.addProperty(propertyName, propertyValue);
    page.addPagelet(p, "stage");

    try {
      idx.add(page);
      assertEquals(1, idx.getResourceCount());
      idx.update(page);
      assertEquals(1, idx.getResourceCount());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }

    // Make sure the search index is still consistent after an update. For this,
    // test one of the constructed fields that are not returned as part of the
    // search result and therefore are likely to be lost

    SearchQuery q = new SearchQueryImpl(site).withProperty(propertyName, propertyValue);
    SearchResult result = searchIdx.getByQuery(q);
    assertEquals(1, result.getDocumentCount());
    assertEquals(page.getURI().getIdentifier(), result.getItems()[0].getId());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#delete(ch.entwine.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testDelete() {
    try {
      idx.add(page);
      idx.add(file);
      idx.delete(page.getURI());
      assertEquals(1, idx.getResourceCount());
      idx.delete(file.getURI());
      assertEquals(0, idx.getResourceCount());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getRevisions(ch.entwine.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testGetRevisions() {
    ResourceURI uri1 = new PageURIImpl(site, "/weblounge");
    ResourceURI uri2Live = new PageURIImpl(site, "/etc/weblounge");
    ResourceURI uri2Work = new PageURIImpl(site, "/etc/weblounge", Resource.WORK);

    Page page1 = new PageImpl(uri1);
    page1.setTemplate(template.getIdentifier());

    Page page2Live = new PageImpl(uri2Live);
    page2Live.setTemplate(template.getIdentifier());

    Page page2Work = new PageImpl(uri2Work);
    page2Work.setTemplate(template.getIdentifier());

    try {
      idx.add(page1);
      idx.add(page2Live);
      idx.add(page2Work);
      long[] revisions = idx.getRevisions(uri1);
      assertEquals(1, revisions.length);
      assertEquals(Resource.LIVE, revisions[0]);
      revisions = idx.getRevisions(uri2Live);
      assertEquals(2, revisions.length);
      assertEquals(Resource.LIVE, revisions[0]);
      assertEquals(Resource.WORK, revisions[1]);
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#move(ch.entwine.weblounge.common.content.ResourceURI, java.lang.String)}
   * .
   */
  @Test
  public void testMove() {
    String newPath = "/etc/weblounge";
    try {
      String id = idx.add(page).getIdentifier();
      idx.move(page.getURI(), newPath);
      assertEquals(1, idx.getResourceCount());
      assertEquals(id, idx.getIdentifier(new PageURIImpl(site, newPath)));
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#clear()}
   * .
   */
  @Test
  public void testClear() {
    try {
      idx.add(page);
      idx.clear();
      assertEquals(0, idx.getResourceCount());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#exists(ch.entwine.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testExists() {
    try {
      assertFalse(idx.exists(page.getURI()));
      String id = idx.add(page).getIdentifier();
      assertTrue(idx.exists(page.getURI()));
      assertTrue(idx.exists(new PageURIImpl(site, "/weblounge")));
      assertFalse(idx.exists(new PageURIImpl(site, "/xxx")));

      // This seems strange, but if there is an identifier, we take it
      assertTrue(idx.exists(new PageURIImpl(site, "/xxx", id)));
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.entwine.weblounge.common.content.ResourceURI, int)}
   * .
   */
  @Test
  @Ignore
  public void testListPageURIInt() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.entwine.weblounge.common.content.ResourceURI, int, long)}
   * .
   */
  @Test
  @Ignore
  public void testListPageURIIntLong() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#size()}
   * .
   */
  @Test
  public void testSize() {
    try {
      assertEquals(0, idx.getResourceCount());
      idx.add(page);
      assertEquals(1, idx.getResourceCount());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method to add, delete, add and update a page to make sure indexing
   * structures are reused in a consistent way.
   * 
   * @throws ContentRepositoryException
   * @throws IOException
   */
  @Test
  public void testExercise() throws IOException, ContentRepositoryException {
    idx.add(page);
    idx.add(otherPage);
    idx.update(page);
    idx.update(otherPage);
    idx.delete(otherPage.getURI());
    idx.update(page);
    assertTrue(idx.exists(page.getURI()));

    // Clear the index
    idx.clear();

    // Add a number of random pages
    List<Page> pages = new ArrayList<Page>();
    for (int i = 0; i < 100; i++) {
      StringBuffer b = new StringBuffer("/");
      for (int j = 0; j < (i % 4) + 1; j++) {
        b.append(UUID.randomUUID().toString());
        b.append("/");
      }
      String path = b.toString();
      String id = UUID.randomUUID().toString();
      Page p = new PageImpl(new PageURIImpl(site, path, id));
      p.setTemplate("home");
      p.setTitle("title", english);
      pages.add(p);
      ResourceURI uri = idx.add(p);
      assertEquals(id, uri.getIdentifier());
      assertEquals(path, uri.getPath());
      assertEquals(i + 1, idx.getResourceCount());
    }

    // Test if everything can be found
    for (Page p : pages) {
      assertTrue(idx.exists(p.getURI()));
      assertEquals(p.getURI().getIdentifier(), idx.getIdentifier(p.getURI()));
      assertEquals(p.getURI().getPath(), idx.getPath(p.getURI()));
      assertEquals(1, idx.getRevisions(p.getURI()).length);
    }

  }

}
