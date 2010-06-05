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
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.impl.content.PageReader;
import ch.o2it.weblounge.common.impl.content.PageURIImpl;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.impl.index.SearchIndex;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Test case for {@link SearchIndex}.
 */
public class SearchIndexTest {

  /** The search index */
  protected SearchIndex idx = null;

  /** The index root directory */
  protected File idxRoot = null;

  /** Flag to indicate read only index */
  protected boolean isReadOnly = false;

  /** The mock site */
  protected Site site = null;

  /** The sample pages */
  protected Page[] pages = null;

  /** UUID of page 1 */
  protected String uuid1 = "4bb19980-8f98-4873-a813-71b6dfab22af";

  /** UUID of page 2 */
  protected String uuid2 = "4bb19980-8f98-4873-a813-71b6dfab22ag";

  /** Path of page 1 */
  protected String path1 = "/test/";

  /** Path of page 2 */
  protected String path2 = "/a/b/c";

  /**
   * Creates the test setup.
   * 
   * @throws java.lang.Exception
   *           if setup of the index fails
   */
  @Before
  public void setUp() throws Exception {
    String rootPath = PathSupport.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    idxRoot = new File(rootPath);
    idx = new SearchIndex(idxRoot, isReadOnly);
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);

    // Prepare the page
    PageReader pageReader = new PageReader();
    pages = new Page[2];
    for (int i = 0; i < pages.length; i++) {
      PageURI uri = new PageURIImpl(site, "/");
      InputStream is = this.getClass().getResourceAsStream("/page" + (i+1) + ".xml");
      pages[i] = pageReader.read(is, uri);
    }
  }

  /**
   * Does the cleanup after each test.
   */
  @After
  public void tearDown() {
    try {
      idx.close();
    } catch (IOException e) {
      fail("Error closing search index: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithId() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site).withId(uuid1);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying by uuid");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithPath() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site).withPath(path1);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying by uuid");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#clear()}.
   */
  @Test
  public void testClear() {
    populateIndex();

    try {
      idx.clear();
    } catch (IOException e) {
      e.printStackTrace();
      fail("Clearing the index failed");
    }

    // Run a query and see if we get anything back
    try {
      SearchQuery q = new SearchQueryImpl(site);
      assertEquals(0, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying cleared index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#delete(ch.o2it.weblounge.common.content.PageURI)}
   * .
   */
  @Test
  public void testDelete() {
    int idxSize = populateIndex();

    // Delete the page
    try {
      idx.delete(pages[0].getURI());
    } catch (Exception e) {
      fail("Error adding page to the search index: " + e.getMessage());
    }

    // Test if we can query for the added document
    try {
      SearchQuery q = new SearchQueryImpl(site);
      assertEquals(idxSize - 1, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying cleared index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#add(ch.o2it.weblounge.common.content.Page)}
   * .
   */
  @Test
  public void testAdd() {
    int idxSize = populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      assertEquals(idxSize, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying for added documents");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#update(ch.o2it.weblounge.common.content.Page)}
   * .
   */
  @Test
  public void testUpdate() {
    populateIndex();
    String subject = "testsubject";
    Page page = pages[0];
    page.addSubject(subject);
    idx.update(page);
    try {
      SearchQuery q = new SearchQueryImpl(site).withSubject(subject);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying for updated document");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#move(ch.o2it.weblounge.common.content.PageURI, java.lang.String)}
   * .
   */
  @Test
  public void testMove() {
    populateIndex();
    String newPath = "/new/path/test";
    idx.move(pages[0].getURI(), newPath);
    try {
      // Make sure there is a page with the new path
      SearchQuery q = new SearchQueryImpl(site).withPath(newPath);
      assertEquals(1, idx.getByQuery(q).getItems().length);
      
      // Make sure the number of pages remains the same
      q = new SearchQueryImpl(site);
      assertEquals(pages.length, idx.getByQuery(q).getItems().length);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error querying cleared index");
    }
  }

  /**
   * Adds sample pages to the search index and returns the number of documents
   * added.
   * 
   * @return the number of pages added
   */
  protected int populateIndex() {
    int count = 0;
    try {
      for (Page page : pages) {
        idx.add(page);
        count ++;
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail("Adding sample page to the index failed");
    }
    return count;
  }

}
