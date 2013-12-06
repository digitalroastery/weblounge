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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedMap;

/**
 * Test class for {@link PageRepository}
 */
public class PageRepositoryTest extends ResourceRepositoryTestBase {

  /** The page repository */
  private static PageRepository pageRepository = null;

  @BeforeClass
  public static void setUpPageRepository() {
    // JCR repository
    pageRepository = new PageRepositoryStub();
    pageRepository.bindRepository(getRepository());
    pageRepository.bindSite(site);
    pageRepository.bindJCRResourceSerializerRegistry(new JCRResourceSerializerRegistryStub());
  }

  /**
   * Test for {@link PageRepository#createPage(ResourceURI)}
   */
  @Test
  public void testCreatePage() throws Exception {
    // Test creating page with null value
    try {
      pageRepository.createPage(null);
      fail("Creating page with a null-value URI should throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    // Test creating page with no parent page
    try {
      pageRepository.createPage(new ResourceURIImpl(Page.TYPE, site, "/no/parent/page"));
      fail("Creating a page on a path without existing parent-page should throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    // Test creating page
    ResourceURI uri = new ResourceURIImpl(Page.TYPE, site, "/test-create-page");
    Date beforeCreation = new Date();
    Page createdPage = pageRepository.createPage(uri);
    assertNotNull("Created page must have its identifier set", createdPage.getIdentifier());
    assertEquals("Path of created page must equal to given uri", uri.getPath(), createdPage.getPath());
    assertTrue("Creation date must be set on creation", beforeCreation.getTime()/1000 <= createdPage.getCreationDate().getTime()/1000);

    // Test creating sub-page
    ResourceURI subUri = new ResourceURIImpl(Page.TYPE, site, "/test-create-page/sub-page");
    try {
      pageRepository.createPage(subUri);
    } catch (ContentRepositoryException e) {
      fail("Creating a page with a valid parent-page should not fail");
    }

    // Test creating existing page
    try {
      pageRepository.createPage(uri);
      fail("Adding a page with a path that already exists must fail");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }
  }

  /**
   * Test for {@link PageRepository#updatePage(Page)}
   */
  @Test
  public void testUpdatePage() throws Exception {
    // Test updating page with null value
    try {
      pageRepository.updatePage(null);
      fail("Updating page wit a null-value should throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    ResourceURI uri1 = new ResourceURIImpl(Page.TYPE, site, "/test-update-page");
    page1.setPath(uri1.getPath());

    // Test updating non-existing page
    try {
      pageRepository.updatePage(page1);
      fail("Updating a non-existing page should throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    // Test updating page (default)
    Page createdPage1 = pageRepository.createPage(uri1);
    pageRepository.updatePage(createdPage1);
    assertNotNull("Returned page must not be null", pageRepository.updatePage(createdPage1));

    // Test updating sub-page
    ResourceURI uri2 = new ResourceURIImpl(Page.TYPE, site, "/test-update-page/sub-page");
    page1.setPath(uri2.getPath());
    Page createdPage2 = pageRepository.createPage(uri2);
    pageRepository.updatePage(createdPage2);

    // Test updating page with non-matching identifier
    createdPage1.setIdentifier("00000000-0000-0000-0000-000000000000");
    try {
      pageRepository.updatePage(createdPage1);
      fail("Updating page with a non-matching identifier must fail");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }
  }

  /**
   * Test for {@link PageRepository#getPage(ResourceURI)}
   */
  @Test
  public void testGetPage() throws Exception {
    // Test getting page with null value
    try {
      pageRepository.getPage(null);
      fail("Getting a page with a null-value uri must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    ResourceURI uri1 = page1.getURI();
    uri1.setPath("/test-get-page");

    // Test getting non-existing page
    try {
      pageRepository.getPage(uri1);
      fail("Trying to get a page wich not exists must throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    // Create page before getting it
    Page createdPage = pageRepository.createPage(uri1);
    page1.setIdentifier(createdPage.getIdentifier());
    page1.setPath(createdPage.getPath());
    pageRepository.updatePage(page1);

    // Test getting page
    Page page = pageRepository.getPage(uri1);
    assertNotNull("Returned page must not be null", page);
    assertEquals(page1.getTemplate(), page.getTemplate());
    assertEquals(page1.getLayout(), page.getLayout());
    assertEquals(page1.isStationary(), page.isStationary());

    // Test getting page with non-equivalent identifier
    uri1.setIdentifier("00000000-0000-0000-0000-000000000000");
    try {
      pageRepository.getPage(uri1);
      fail("Getting a page with a non-equivalent identifier must fail");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }
  }

  /**
   * Test for {@link PageRepository#existsPage(ResourceURI)}
   */
  @Test
  public void testExistsPage() throws Exception {
    // Test with null-value
    try {
      pageRepository.existsPage(null);
      fail("Checking if a page exists with a null-value uri must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    ResourceURI notExisting = new ResourceURIImpl(Page.TYPE, site, "/test-exists-page-not-existing");
    ResourceURI existing = new ResourceURIImpl(Page.TYPE, site, "/test-exists-page-existing");

    pageRepository.createPage(existing);

    assertEquals("Page should not exist...", false, pageRepository.existsPage(notExisting));
    assertEquals("Page should exist...", true, pageRepository.existsPage(existing));
  }

  /**
   * Test for {@link PageRepository#deletePage(ResourceURI)}
   */
  @Test
  public void testDeletePage() throws Exception {
    // Test deleting with null value
    try {
      pageRepository.deletePage(null);
      fail("Deleting a page with a null-value uri must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    // Test deleting homepage
    try {
      pageRepository.deletePage(new ResourceURIImpl(Page.TYPE, site, "/"));
      fail("Deleting homepage of a site must throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    ResourceURI uri1 = page1.getURI();
    uri1.setPath("/test-delete-page");

    // Test deleting non-existing page
    try {
      pageRepository.deletePage(uri1);
      fail("Deleting a non-existing page must throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    page1.setIdentifier(pageRepository.createPage(uri1).getIdentifier());
    page1.setPath(uri1.getPath());
    pageRepository.updatePage(page1);

    ResourceURI uri2 = page2.getURI();
    uri2.setPath("/test-delete-page/sub-page");
    page2.setIdentifier(pageRepository.createPage(uri2).getIdentifier());
    page2.setPath(uri2.getPath());
    pageRepository.updatePage(page2);

    try {
      pageRepository.deletePage(uri1);
      fail("Deleting a page with existing sub-pages must throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // Nothing to do
    }

    pageRepository.deletePage(uri2);
    assertEquals("Page should no longer exits", false, pageRepository.existsPage(uri2));
    pageRepository.deletePage(uri1);
    assertEquals("Page should no longer exits", false, pageRepository.existsPage(uri1));
  }

  /**
   * Test for {@link PageRepository#getVersions(ResourceURI)}
   */
  @Test
  public void testGetVersions() throws Exception {
    try {
      pageRepository.getVersions(null);
      fail("Trying to get versions of null-value uri");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    ResourceURI uri1 = page1.getURI();
    uri1.setPath("/test-get-versions");

    try {
      pageRepository.getVersions(uri1);
      fail("Getting versions of non-existing resource must throw a ContentRepositoryException");
    } catch (ContentRepositoryException e) {
      // TODO ResourceNotFoundException, see issue #301
      // Nothing to do
    }

    page1.setIdentifier(pageRepository.createPage(uri1).getIdentifier());
    page1.setPath(uri1.getPath());
    pageRepository.updatePage(page1);

    SortedMap<String, Calendar> versions1 = pageRepository.getVersions(uri1);
    assertEquals(1, versions1.size());
    assertTrue(versions1.containsKey("1.0"));

    ResourceURI uri2 = page2.getURI();
    uri2.setPath("/test-get-versions/sub-page");
    page2.setIdentifier(pageRepository.createPage(uri2).getIdentifier());
    page2.setPath(uri2.getPath());
    pageRepository.updatePage(page2);

    SortedMap<String, Calendar> versions2 = pageRepository.getVersions(uri2);
    assertEquals(1, versions2.size());
    versions1 = pageRepository.getVersions(uri1);
    assertEquals(2, versions1.size());

    pageRepository.updatePage(page1);
    pageRepository.updatePage(page2);
    versions1 = pageRepository.getVersions(uri1);
    assertEquals(3, versions1.size());
    assertTrue(versions1.containsKey("1.0"));
    assertTrue(versions1.containsKey("1.1"));
    assertTrue(versions1.containsKey("1.2"));
    versions2 = pageRepository.getVersions(uri2);
    assertEquals(2, versions2.size());
    assertTrue(versions2.containsKey("1.0"));
    assertTrue(versions2.containsKey("1.1"));

    assertTrue(versions1.get("1.0").getTimeInMillis() < versions1.get("1.1").getTimeInMillis());
  }
}
