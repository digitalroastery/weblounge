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
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.IdIndex;
import ch.entwine.weblounge.contentrepository.impl.index.PathIndex;
import ch.entwine.weblounge.contentrepository.impl.index.URIIndex;
import ch.entwine.weblounge.contentrepository.impl.index.VersionIndex;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test case for the {@link ContentRepositoryIndex}.
 */
public class ContentRepositoryIndexTest {

  /** The content repository index */
  protected ContentRepositoryIndex idx = null;

  /** The index' root directory */
  protected File idxRoot = null;

  /** The structural index' root directory */
  protected File structuralIndexRootDirectory = null;

  /** The sample page */
  protected Page page = null;

  /** The other sample page */
  protected Page otherPage = null;

  /** File resource */
  protected FileResource file = null;

  /** The site */
  protected Site site = null;

  /** Page template */
  protected PageTemplate template = null;

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
   */
  @BeforeClass
  public static void setUpClass() {
    // Resource serializer
    serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());
  }

  /**
   * Sets up data structures for each test case.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    // Template
    template = EasyMock.createNiceMock(PageTemplate.class);
    EasyMock.expect(template.getIdentifier()).andReturn("templateid").anyTimes();
    EasyMock.expect(template.getStage()).andReturn("main").anyTimes();
    EasyMock.replay(template);

    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(site.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.replay(site);

    page = new PageImpl(new PageURIImpl(site, "/weblounge"));
    page.setTemplate("home");

    otherPage = new PageImpl(new PageURIImpl(site, "/weblounge/other"));
    otherPage.setTemplate("home");

    file = new FileResourceImpl(new FileResourceURIImpl(site));

    idxRoot = new File(new File(System.getProperty("java.io.tmpdir")), "index");
    structuralIndexRootDirectory = new File(idxRoot, "structure");
    FileUtils.deleteDirectory(idxRoot);

    ElasticSearchUtils.createIndexConfigurationAt(idxRoot);
    System.setProperty("weblounge.home", idxRoot.getAbsolutePath());
    idx = new FileSystemContentRepositoryIndex(site, idxRoot, serializer);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    idx.close();
    FileUtils.deleteDirectory(idxRoot);
  }

  /**
   * Tests if all files have been created.
   */
  @Test
  public void testFilesystem() {
    assertTrue(new File(structuralIndexRootDirectory, IdIndex.ID_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, PathIndex.PATH_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, URIIndex.URI_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, VersionIndex.VERSION_IDX_NAME).exists());
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
      assertEquals(1, idx.size());
      idx.add(file);
      assertEquals(2, idx.size());
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
      assertEquals(1, idx.size());
      idx.update(page);
      assertEquals(1, idx.size());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }

    // Make sure the search index is still consistent after an update. For this,
    // test one of the constructed fields that are not returned as part of the
    // search result and therefore are likely to be lost

    SearchQuery q = new SearchQueryImpl(site).withProperty(propertyName, propertyValue);
    SearchResult result = idx.find(q);
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
      assertEquals(1, idx.size());
      idx.delete(file.getURI());
      assertEquals(0, idx.size());
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
      assertEquals(1, idx.size());
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
      assertEquals(0, idx.size());
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
      assertEquals(0, idx.size());
      idx.add(page);
      assertEquals(1, idx.size());
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }
  }

  /**
   * Test method to add, delete, add and update a page to make sure indexing
   * structures are reused in a consistent way.
   */
  @Test
  public void testExercise() {
    try {
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
        assertEquals(i + 1, idx.size());
      }

      // Test if everything can be found
      for (Page p : pages) {
        assertTrue(idx.exists(p.getURI()));
        assertEquals(p.getURI().getIdentifier(), idx.getIdentifier(p.getURI()));
        assertEquals(p.getURI().getPath(), idx.getPath(p.getURI()));
        assertEquals(1, idx.getRevisions(p.getURI()).length);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      fail(t.getMessage());
    }

  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.INDEX_VERSION, idx.getIndexVersion());

    // Overwrite the version number with 0, which is an invalid value
    try {
      idx.close();

      File idIdxFile = new File(PathUtils.concat(idxRoot.getAbsolutePath(), "structure"), IdIndex.ID_IDX_NAME);
      RandomAccessFile index = new RandomAccessFile(idIdxFile, "rwd");
      index.seek(0);
      index.writeInt(0);
      index.close();

      idx = new FileSystemContentRepositoryIndex(site, idxRoot, serializer);
      assertEquals(-1, idx.getIndexVersion());
    } catch (IOException e) {
      fail("Error writing version to index");
    }

  }

}
