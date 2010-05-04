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

import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.impl.content.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test case for the {@link ContentRepositoryIndex}.
 */
public class ContentRepositoryIndexTest {
  
  /** The content repository index */
  protected ContentRepositoryIndex idx = null;
  
  /** The index root directory */
  protected File indexRootDirectory = null;
  
  /** The site */
  protected Site site = null;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexRootDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "index");
    FileUtils.deleteDirectory(indexRootDirectory);
    idx = new FileSystemContentRepositoryIndex(indexRootDirectory);
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    idx.close();
    FileUtils.deleteDirectory(indexRootDirectory);
  }

  /**
   * Tests if all files have been created.
   */
  @Test
  public void testFilesystem() {
    assertTrue(new File(indexRootDirectory, FileSystemContentRepositoryIndex.URI_IDX_NAME).exists());
    assertTrue(new File(indexRootDirectory, FileSystemContentRepositoryIndex.ID_IDX_NAME).exists());
    assertTrue(new File(indexRootDirectory, FileSystemContentRepositoryIndex.PATH_IDX_NAME).exists());
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#add(ch.o2it.weblounge.common.content.PageURI)}.
   */
  @Test
  public void testAdd() {
    PageURI uri = new PageURIImpl(site, "/weblounge");
    try {
      idx.add(uri);
      assertEquals(1, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#delete(ch.o2it.weblounge.common.content.PageURI)}.
   */
  @Test
  public void testDelete() {
    PageURI uri = new PageURIImpl(site, "/weblounge");
    try {
      idx.add(uri);
      idx.delete(uri);
      assertEquals(0, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#getRevisions(ch.o2it.weblounge.common.content.PageURI)}.
   */
  @Test
  @Ignore
  public void testGetRevisions() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#update(ch.o2it.weblounge.common.content.PageURI, java.lang.String)}.
   */
  @Test
  public void testUpdate() {
    PageURI uri = new PageURIImpl(site, "/weblounge");
    String newPath = "/etc/weblounge";
    try {
      String id = idx.add(uri).getId();
      idx.update(uri, newPath);
      assertEquals(1, idx.size());
      assertEquals(id, idx.toId(new PageURIImpl(site, newPath)));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#clear()}.
   */
  @Test
  public void testClear() {
    PageURI uri = new PageURIImpl(site, "/weblounge");
    try {
      idx.add(uri);
      idx.clear();
      assertEquals(0, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#exists(ch.o2it.weblounge.common.content.PageURI)}.
   */
  @Test
  public void testExists() {
    PageURI uri = new PageURIImpl(site, "/weblounge");
    try {
      assertFalse(idx.exists(uri));
      String id = idx.add(uri).getId();
      assertTrue(idx.exists(uri));
      assertTrue(idx.exists(new PageURIImpl(site, "/weblounge")));
      assertFalse(idx.exists(new PageURIImpl(site, "/xxx")));
      
      // This seems strange, but if there is an identifier, we take it
      assertTrue(idx.exists(new PageURIImpl(site, "/xxx", id)));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.o2it.weblounge.common.content.PageURI, int)}.
   */
  @Test
  @Ignore
  public void testListPageURIInt() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.contentrepository.impl.index.ContentRepositoryIndex#list(ch.o2it.weblounge.common.content.PageURI, int, long)}.
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
    PageURI uri = new PageURIImpl(site, "/weblounge");
    try {
      assertEquals(0, idx.size());
      idx.add(uri);
      assertEquals(1, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
