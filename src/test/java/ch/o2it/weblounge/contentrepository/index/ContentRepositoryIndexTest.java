/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.impl.content.file.FileResourceImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.PageSerializer;
import ch.o2it.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.IdIndex;
import ch.o2it.weblounge.contentrepository.impl.index.PathIndex;
import ch.o2it.weblounge.contentrepository.impl.index.URIIndex;
import ch.o2it.weblounge.contentrepository.impl.index.VersionIndex;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * Test case for the {@link ContentRepositoryIndex}.
 */
public class ContentRepositoryIndexTest {
  
  /** The content repository index */
  protected ContentRepositoryIndex idx = null;
  
  /** The index' root directory */
  protected File indexRootDirectory = null;

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
  
  /** English */
  protected Language english = LanguageSupport.getLanguage("en"); 

  /** German */
  protected Language german = LanguageSupport.getLanguage("de"); 

  /** Italian */
  protected Language french = LanguageSupport.getLanguage("fr"); 
  
  /**
   * Sets up data structures for each test case.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexRootDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "index");
    structuralIndexRootDirectory = new File(indexRootDirectory, "structure");
    FileUtils.deleteDirectory(indexRootDirectory);
    idx = new FileSystemContentRepositoryIndex(indexRootDirectory);

    PageTemplate t = EasyMock.createNiceMock(PageTemplate.class);
    EasyMock.expect(t.getStage()).andReturn("main");
    EasyMock.replay(t);

    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getTemplate("home")).andReturn(t);
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.replay(site);

    // Resource serializers
    ResourceSerializerServiceImpl serializerService = new ResourceSerializerServiceImpl();
    ResourceSerializerFactory.setResourceSerializerService(serializerService);
    serializerService.registerSerializer(new PageSerializer());
    serializerService.registerSerializer(new FileResourceSerializer());
    serializerService.registerSerializer(new ImageResourceSerializer());

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
    idx.close();
    FileUtils.deleteDirectory(indexRootDirectory);
  }

  /**
   * Tests if all files have been created.
   */
  @Test
  public void testFilesystem() {
    assertTrue(new File(structuralIndexRootDirectory, URIIndex.URI_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, IdIndex.ID_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, PathIndex.PATH_IDX_NAME).exists());
    assertTrue(new File(structuralIndexRootDirectory, VersionIndex.VERSION_IDX_NAME).exists());
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#add(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testAdd() {
    try {
      idx.add(page);
      assertEquals(1, idx.size());
      idx.add(file);
      assertEquals(2, idx.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    String oldId = page.getURI().getId();
    String oldPath = page.getURI().getPath();

    // Try to add a new resource with existing id
    try {
      page.getURI().setPath(UrlSupport.concat(file.getURI().getPath(), "pathext"));
      idx.add(page);
      fail("Managed to add a resource with an existing identifier");
    } catch (Exception e) {
      // This is expected
      page.getURI().setPath(oldPath);
    }

    // Try to add a new resource with existing id
    try {
      page.getURI().setIdentifier(page.getIdentifier().substring(1) + "x");
      idx.add(page);
      fail("Managed to add a resource with an existing path");
    } catch (Exception e) {
      // This is expected
      page.getURI().setIdentifier(oldId);
    }

  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#delete(ch.o2it.weblounge.common.content.ResourceURI)}.
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
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getRevisions(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetRevisions() {
    ResourceURI uri1 = new PageURIImpl(site, "/weblounge");
    ResourceURI uri2Live = new PageURIImpl(site, "/etc/weblounge");
    ResourceURI uri2Work = new PageURIImpl(site, "/etc/weblounge", Resource.WORK);
    try {
      idx.add(new PageImpl(uri1));
      idx.add(new PageImpl(uri2Live));
      idx.add(new PageImpl(uri2Work));
      long[] revisions = idx.getRevisions(uri1);
      assertEquals(1, revisions.length);
      assertEquals(Resource.LIVE, revisions[0]);
      revisions = idx.getRevisions(uri2Live);
      assertEquals(2, revisions.length);
      assertEquals(Resource.LIVE, revisions[0]);
      assertEquals(Resource.WORK, revisions[1]);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getLanguages(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetLanguages() {
    ResourceURI live1URI = new PageURIImpl(site, "/weblounge");
    ResourceURI live2URI = new PageURIImpl(site, "/etc/weblounge");

    Page page1Live = new PageImpl(live1URI);
    page1Live.setTitle("title", english);
    
    Page page2Live = new PageImpl(live2URI);
    page2Live.setTitle("title", english);
    page2Live.setTitle("titel", german);
    
    try {
      // Add the pages to the index
      idx.add(page1Live);
      idx.add(page2Live);
      
      // Try to get the languages back
      assertEquals(page1Live.languages().size(), idx.getLanguages(live1URI).length);
      assertEquals(english, idx.getLanguages(live1URI)[0]);
      assertEquals(page2Live.languages().size(), idx.getLanguages(live2URI).length);
      assertTrue(page2Live.languages().containsAll(Arrays.asList(idx.getLanguages(live2URI))));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#move(ch.o2it.weblounge.common.content.ResourceURI, java.lang.String)}.
   */
  @Test
  public void testMove() {
    String newPath = "/etc/weblounge";
    try {
      String id = idx.add(page).getId();
      idx.move(page.getURI(), newPath);
      assertEquals(1, idx.size());
      assertEquals(id, idx.getIdentifier(new PageURIImpl(site, newPath)));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#clear()}.
   */
  @Test
  public void testClear() {
    try {
      idx.add(page);
      idx.clear();
      assertEquals(0, idx.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#exists(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testExists() {
    try {
      assertFalse(idx.exists(page.getURI()));
      String id = idx.add(page).getId();
      assertTrue(idx.exists(page.getURI()));
      assertTrue(idx.exists(new PageURIImpl(site, "/weblounge")));
      assertFalse(idx.exists(new PageURIImpl(site, "/xxx")));
      
      // This seems strange, but if there is an identifier, we take it
      assertTrue(idx.exists(new PageURIImpl(site, "/xxx", id)));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.o2it.weblounge.common.content.ResourceURI, int)}.
   */
  @Test
  @Ignore
  public void testListPageURIInt() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.o2it.weblounge.common.content.ResourceURI, int, long)}.
   */
  @Test
  @Ignore
  public void testListPageURIIntLong() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#size()}.
   */
  @Test
  public void testSize() {
    try {
      assertEquals(0, idx.size());
      idx.add(page);
      assertEquals(1, idx.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method to add, delete, add and update a page to make sure indexing
   * structures are reused in a consistent way.
   */
  @Test
  public void testAddDeleteAddUpdate() {
    try {
      idx.add(page);
      idx.add(otherPage);
      idx.update(page);
      idx.update(otherPage);
      idx.delete(otherPage.getURI());
      idx.update(page);
      assertTrue(idx.exists(page.getURI()));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getIndexVersion()}.
   */
  @Test
  public void testIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.INDEX_VERSION, idx.getIndexVersion());
    
    // Overwrite the version number with 0, which is an invalid value
    try {
      idx.close();

      File idIdxFile = new File(PathSupport.concat(indexRootDirectory.getAbsolutePath(), "structure"), IdIndex.ID_IDX_NAME);
      RandomAccessFile index = new RandomAccessFile(idIdxFile, "rwd");
      index.seek(0);
      index.writeInt(0);
      index.close();

      idx = new FileSystemContentRepositoryIndex(indexRootDirectory);
      assertEquals(-1, idx.getIndexVersion());
    } catch (IOException e) {
      fail("Error writing version to index");
    }

  }
  
}
