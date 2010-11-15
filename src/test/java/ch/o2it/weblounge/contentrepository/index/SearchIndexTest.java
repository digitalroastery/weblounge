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

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceReader;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.PageSerializer;
import ch.o2it.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.o2it.weblounge.contentrepository.impl.index.SearchIndex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
  
  /** Page template */
  protected PageTemplate template = null;

  /** The mock site */
  protected Site site = null;

  /** The sample pages */
  protected Page[] pages = null;

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

  /** Filename */
  protected String filename = "image.jpg";

  /** Mimetype */
  protected String mimetype = "image/jpeg";

  /** Element key */
  protected String elementId = "title";

  /** Element value */
  protected String elementValue = "joyeux";

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
    EasyMock.expect(site.getTemplate((String)EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(site.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.replay(site);
    
    // Resource serializers
    ResourceSerializerServiceImpl serializerService = new ResourceSerializerServiceImpl();
    ResourceSerializerFactory.setResourceSerializerService(serializerService);
    serializerService.registerSerializer(new PageSerializer());
    serializerService.registerSerializer(new FileResourceSerializer());
    serializerService.registerSerializer(new ImageResourceSerializer());

    // Prepare the pages
    PageReader pageReader = new PageReader();
    pages = new Page[2];
    for (int i = 0; i < pages.length; i++) {
      InputStream is = this.getClass().getResourceAsStream("/page" + (i+1) + ".xml");
      pages[i] = pageReader.read(is, site);
      IOUtils.closeQuietly(is);
    }

    // Prepare the sample file
    FileResourceReader fileReader = new FileResourceReader();
    InputStream fileIs = this.getClass().getResourceAsStream("/file.xml");
    file = fileReader.read(fileIs, site);
    IOUtils.closeQuietly(fileIs);
    
    // Prepare the sample image
    ImageResourceReader imageReader = new ImageResourceReader();
    InputStream imageIs = this.getClass().getResourceAsStream("/image.xml");
    image = imageReader.read(imageIs, site);
    IOUtils.closeQuietly(imageIs);
  }

  /**
   * Does the cleanup after each test.
   */
  @After
  public void tearDown() {
    try {
      idx.close();
      FileUtils.deleteQuietly(idxRoot);
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
      SearchQuery q = new SearchQueryImpl(site).withIdentifier(uuid1);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
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
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by path");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithTemplate() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site).withTemplate("default");
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by template");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithAuthor() {
    populateIndex();
    try {
      User amelie = new UserImpl("amelie");
      SearchQuery q = new SearchQueryImpl(site).withAuthor(amelie);
      SearchResult result = idx.getByQuery(q);
      assertEquals(4, result.getItems().length);
      assertEquals(4, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by author");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithCreator() {
    populateIndex();
    try {
      User hans = new UserImpl("hans");
      SearchQuery q = new SearchQueryImpl(site).withCreator(hans);
      SearchResult result = idx.getByQuery(q);
      assertEquals(4, result.getItems().length);
      assertEquals(4, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by creator");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithCreationDate() {
    populateIndex();
    try {
      Date date = WebloungeDateFormat.parseStatic("2009-01-07T20:05:41Z");
      SearchQuery q = new SearchQueryImpl(site).withCreationDate(date);
      SearchResult result = idx.getByQuery(q);
      assertEquals(1, result.getItems().length);
      assertEquals(1, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by creation date");
    } catch (ParseException e) {
      e.printStackTrace();
      fail("Error parsing creation date");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithModifier() {
    populateIndex();
    try {
      User amelie = new UserImpl("amelie");
      SearchQuery q = new SearchQueryImpl(site).withModifier(amelie);
      SearchResult result = idx.getByQuery(q);
      assertEquals(4, result.getItems().length);
      assertEquals(4, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by modifier");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithModificationDate() {
    populateIndex();
    try {
      Date date = WebloungeDateFormat.parseStatic("2009-02-18T22:06:40Z");
      SearchQuery q = new SearchQueryImpl(site).withModificationDate(date);
      SearchResult result = idx.getByQuery(q);
      assertEquals(1, result.getItems().length);
      assertEquals(1, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by modification date");
    } catch (ParseException e) {
      e.printStackTrace();
      fail("Error parsing modification date");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithPublisher() {
    populateIndex();
    try {
      User amelie = new UserImpl("amelie");
      SearchQuery q = new SearchQueryImpl(site).withPublisher(amelie);
      SearchResult result = idx.getByQuery(q);
      assertEquals(1, result.getItems().length);
      assertEquals(1, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by publisher");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithPublishingDate() {
    populateIndex();
    try {
      Date date = WebloungeDateFormat.parseStatic("2006-05-05T17:58:21Z");
      SearchQuery q = new SearchQueryImpl(site).withPublishingDate(date);
      SearchResult result = idx.getByQuery(q);
      assertEquals(1, result.getItems().length);
      assertEquals(1, result.getHitCount());
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by publishing date");
    } catch (ParseException e) {
      e.printStackTrace();
      fail("Error parsing publishing date");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithSubjects() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      String[] subjects = new String[] { "Other topic", "Topic a" };
      for (String subject : subjects)
        q.withSubject(subject);
      assertEquals(2, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by subject");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testGetWithContent() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      q.withElement(elementId, elementValue);
      assertEquals(2, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by element");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithProperty() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      q.withProperty("resourceid", imageid);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by property");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithFilename() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      q.withFilename(filename);
      assertEquals(2, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by filename");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#getByQuery(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test 
  public void testGetWithMimetype() {
    populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      q.withMimetype(mimetype);
      assertEquals(2, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying by mimetype");
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
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying cleared index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#delete(ch.o2it.weblounge.common.content.ResourceURI)}
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
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying cleared index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#add(ch.o2it.weblounge.common.content.page.Page)}
   * .
   */
  @Test
  public void testAdd() {
    int idxSize = populateIndex();
    try {
      SearchQuery q = new SearchQueryImpl(site);
      assertEquals(idxSize, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying for added documents");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#update(ch.o2it.weblounge.common.content.page.Page)}
   * .
   */
  @Test
  public void testUpdate() {
    populateIndex();
    String subject = "testsubject";
    Page page = pages[0];
    page.addSubject(subject);

    // Post the update
    try {
      idx.update(page);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error updating document in search index");
    }

    // Check if the index actually reflects the updated data
    try {
      SearchQuery q = new SearchQueryImpl(site).withSubject(subject);
      assertEquals(1, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error querying for updated document");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.SearchIndex#move(ch.o2it.weblounge.common.content.ResourceURI, java.lang.String)}
   * .
   */
  @Test 
  public void testMove() {
    int resources = populateIndex();
    String newPath = "/new/path/test";
    
    // Post the update
    try {
      idx.move(pages[0].getURI(), newPath);
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error updating document in search index");
    }

    // Make sure there is a page with the new path
    try {
      SearchQuery q = new SearchQueryImpl(site).withPath(newPath);
      assertEquals(1, idx.getByQuery(q).getItems().length);
      
      // Make sure the number of pages remains the same
      q = new SearchQueryImpl(site);
      assertEquals(resources, idx.getByQuery(q).getItems().length);
    } catch (ContentRepositoryException e) {
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
    
    // Add the pages
    try {
      for (Page page : pages) {
        idx.add(page);
        count ++;
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample page to the index failed");
    }
    
    // Add the file
    try {
      idx.add(file);
      count ++;
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample file to the index failed");
    }
    
    // Add the image
    try {
      idx.add(image);
      count ++;
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample image to the index failed");
    }
    return count;
  }

}
