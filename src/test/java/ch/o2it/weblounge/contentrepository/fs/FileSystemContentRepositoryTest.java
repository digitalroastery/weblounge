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

package ch.o2it.weblounge.contentrepository.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageContentImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceReader;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.o2it.weblounge.contentrepository.impl.PageSerializer;
import ch.o2it.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link FileSystemContentRepositoryTest}.
 */
public class FileSystemContentRepositoryTest {

  /** The content repository */
  protected FileSystemContentRepository repository = null;

  /** The repository root directory */
  protected File repositoryRoot = null;

  /** The mock site */
  protected Site site = null;

  /** Page template */
  protected PageTemplate template = null;

  /** UUID of page 1 */
  protected String page1uuid = "4bb19980-8f98-4873-a813-71b6dfab22af";

  /** Path of page 1 */
  protected String page1path = "/test/";

  /** URI of page 1 */
  protected ResourceURI page1URI = null;

  /** UUID of page 2 */
  protected String page2uuid = "4bb19980-8f98-4873-a813-71b6dfab22ag";

  /** Path of page 2 */
  protected String page2path = "/a/b/c";

  /** URI of page 2 */
  protected ResourceURI page2URI = null;

  /** UUID of the first image resource */
  protected String imageUuid = "4bb19980-8f98-4873-a813-71b6dfab22as";

  /** Path of image 1 */
  protected String imagePath = "/images/a";

  /** URI of image 1 */
  protected ResourceURI imageURI = null;

  /** UUID of the second image resource */
  protected String documentUuid = "abc19980-8f98-4873-a813-71b6dfab22ag";

  /** Path of image 2 */
  protected String documentPath = "/documents/a";

  /** URI of image 2 */
  protected ResourceURI documentURI = null;

  /** The sample pages */
  protected Page[] pages = null;

  /** The sample file */
  protected FileResource file = null;

  /** The jpeg sample file */
  protected ImageResource jpeg = null;

  /** The jpeg image content path */
  protected static final String jpegContentPath = "/image.jpg";
  
  /** The size of the jpeg image */
  protected final long jpegFileSize = 73642L;

  /** The jpeg image content url */
  protected static URL jpegContentURL = null;

  /** The jpeg image content object */
  protected ImageContent jpegContent = null;

  /** The png content path */
  protected static final String pngContentPath = "/image.png";

  /** The png content url */
  protected static URL pngContentURL = null;
  
  /** The size of the png image */
  protected final long pngFileSize = 543037L;

  /** The png content object */
  protected ImageContent pngContent = null;

  /** English */
  protected Language english = LanguageUtils.getLanguage("en");

  /** German */
  protected Language german = LanguageUtils.getLanguage("de");

  /** Italian */
  protected Language french = LanguageUtils.getLanguage("fr");

  /**
   * Sets up everything valid for all test runs.
   * 
   * @throws Exception
   *           if setup fails
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
    jpegContentURL = FileSystemContentRepositoryTest.class.getResource(jpegContentPath);
    pngContentURL = FileSystemContentRepositoryTest.class.getResource(pngContentPath);
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    String rootPath = PathSupport.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    repositoryRoot = new File(rootPath);

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
    EasyMock.expect(site.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.replay(site);

    // Connect to the repository
    repository = new FileSystemContentRepository();
    Dictionary<String, Object> repositoryProperties = new Hashtable<String, Object>();
    repositoryProperties.put(FileSystemContentRepository.OPT_ROOT_DIR, repositoryRoot.getAbsolutePath());
    repositoryProperties.put(Site.class.getName(), site);
    repository.connect(repositoryProperties);
    repository.start();

    // Resource serializers
    ResourceSerializerServiceImpl serializerService = new ResourceSerializerServiceImpl();
    ResourceSerializerFactory.setResourceSerializerService(serializerService);
    serializerService.registerSerializer(new PageSerializer());
    serializerService.registerSerializer(new FileResourceSerializer());
    serializerService.registerSerializer(new ImageResourceSerializer());

    // Setup uris
    page1URI = new PageURIImpl(site, page1path, page1uuid);
    page2URI = new PageURIImpl(site, page2path, page2uuid);
    imageURI = new ImageResourceURIImpl(site, imagePath, imageUuid);
    documentURI = new FileResourceURIImpl(site, documentPath, documentUuid);

    // Prepare the pages
    PageReader pageReader = new PageReader();
    pages = new Page[2];
    for (int i = 0; i < pages.length; i++) {
      InputStream is = this.getClass().getResourceAsStream("/page" + (i + 1) + ".xml");
      pages[i] = pageReader.read(is, site);
    }

    // Prepare the sample file
    FileResourceReader fileReader = new FileResourceReader();
    InputStream fileIs = this.getClass().getResourceAsStream("/file.xml");
    file = fileReader.read(fileIs, site);
    IOUtils.closeQuietly(fileIs);

    // Prepare the sample image
    ImageResourceReader imageReader = new ImageResourceReader();
    InputStream imageIs = this.getClass().getResourceAsStream("/image.xml");
    jpeg = imageReader.read(imageIs, site);
    IOUtils.closeQuietly(imageIs);
    
    jpegContent = new ImageContentImpl(jpegContentURL.getFile(), german, "image/jpeg", 1000, 666);
    pngContent = new ImageContentImpl(pngContentURL.getFile(), english, "text/pdf", 1000, 666);
  }

  /**
   * Does the cleanup after each test.
   */
  @After
  public void tearDown() {
    try {
      repository.disconnect();
      FileUtils.deleteQuietly(repositoryRoot);
    } catch (ContentRepositoryException e) {
      fail("Error disconnecting content repository: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository#connect(java.util.Dictionary)}
   * .
   */
  @Test
  public void testConnect() {
    try {
      repository.disconnect();
      FileUtils.deleteQuietly(repositoryRoot);
    } catch (ContentRepositoryException e) {
      fail("Error disconnecting content repository: " + e.getMessage());
    }

    // Connect to the repository without site
    try {
      Dictionary<String, Object> repositoryProperties = new Hashtable<String, Object>();
      repositoryProperties.put(FileSystemContentRepository.OPT_ROOT_DIR, repositoryRoot.getAbsolutePath());
      repository.connect(repositoryProperties);
      fail("Managed to connect repository without site");
    } catch (ContentRepositoryException e) {
      // This is expected
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository#index()}
   * .
   */
  @Test
  public void testIndex() {
    try {
      int resources = populateRepository();
      repository.index();
      assertEquals(resources, repository.getResourceCount());
    } catch (ContentRepositoryException e) {
      fail("Error while indexing repository");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository#getRootDirectory()}
   * .
   */
  @Test
  public void testGetRootDirectory() {
    assertEquals(new File(repositoryRoot, site.getIdentifier()), repository.getRootDirectory());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testDeleteResourceURI() {
    int resources = populateRepository();
    try {
      repository.delete(documentURI);
      assertNull(repository.get(documentURI));
      assertEquals(resources - 1, repository.getResourceCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error deleting resource");
    }

    // Delete image resource which is referenced by page
    try {
      if (repository.delete(imageURI)) {
        fail("Managed to remove referenced resource");
      }
      assertNotNull(repository.get(imageURI));
      assertEquals(resources - 1, repository.getResourceCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error trying to delete a resource");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI, boolean)}
   * .
   */
  @Test
  public void testDeleteResourceURIBoolean() {
    ResourceURI workURI = new PageURIImpl(site, page1path, page1uuid, Resource.WORK);
    Page workPage = new PageImpl(workURI);
    int resources = populateRepository();
    int revisions = resources;

    // Add resources and additional work resource
    try {
      repository.put(workPage);
      revisions++;
      assertEquals(resources, repository.getResourceCount());
      assertEquals(revisions, repository.getRevisionCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error adding documents to the repository");
    }

    // Remove all versions of the page
    try {
      repository.delete(workURI, true);
      assertEquals(resources - 1, repository.getResourceCount());
      assertEquals(revisions - 2, repository.getRevisionCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error deleting document from the repository");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#move(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testMove() {
    int resources = populateRepository();
    String oldPath = page1URI.getPath();
    String newPath = "/new/path";
    ResourceURI newURI = new PageURIImpl(site, newPath, page1URI.getId());
    try {
      repository.move(page1URI, newURI);
      assertEquals(resources, repository.getResourceCount());
      assertNull(repository.get(new PageURIImpl(site, oldPath)));
      assertNotNull(repository.get(new PageURIImpl(site, newPath)));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error moving resource");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#put(ch.o2it.weblounge.common.content.Resource)}
   * .
   */
  @Test
  public void testPut() {
    int resources = populateRepository();
    String newId = "4bb19980-8f98-4873-0000-71b6dfab22af";

    // Try to add a duplicate resource
    try {
      repository.put(file);
      assertEquals(resources, repository.getResourceCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error moving resource");
    }

    // Try to add a new resource
    try {
      file.getURI().setIdentifier(newId);
      file.getURI().setPath(UrlSupport.concat(file.getURI().getPath(), "2"));
      repository.put(file);
      assertEquals(resources + 1, repository.getResourceCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error moving resource");
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#putContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceContent, java.io.InputStream)}
   * .
   */
  @Test
  public void testPutContent() {
    populateRepository();
    
    // Add content items
    try {
      Resource<?> r = repository.putContent(imageURI, jpegContent, jpegContentURL.openStream());
      assertEquals(1, repository.get(imageURI).contents().size());
      assertEquals(jpegFileSize, r.getContent(jpegContent.getLanguage()).getSize());
      r = repository.putContent(imageURI, pngContent, pngContentURL.openStream());
      assertEquals(2, repository.get(imageURI).contents().size());
      assertEquals(pngFileSize, r.getContent(pngContent.getLanguage()).getSize());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error adding content");
    }

    // Try to add content items to non-existing resources
    try {
      String newfilename = "newimage.jpeg";
      String mimetype = "image/png";
      Language language = jpegContent.getLanguage();
      ImageContent updatedContent = new ImageContentImpl(newfilename, language, mimetype, 1000, 600);
      Resource<?> r = repository.putContent(imageURI, updatedContent, pngContentURL.openStream());
      ResourceContent c = r.getContent(language);
      assertEquals(pngFileSize, c.getSize());
      assertEquals(mimetype, c.getMimetype());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error replacing content");
    }

    // Try to add content items to non-existing resources
    try {
      ResourceURI uri = new ImageResourceURIImpl(site, "/x/y/z");
      repository.putContent(uri, jpegContent, jpegContentURL.openStream());
      fail("Managed to add content to non-existing resource");
    } catch (Exception e) {
      // Expected
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceContent)}
   * .
   */
  @Test
  public void testDeleteContent() {
    populateRepository();
    
    // Add content items
    try {
      repository.putContent(imageURI, jpegContent, jpegContentURL.openStream());
      repository.putContent(imageURI, pngContent, pngContentURL.openStream());
      assertEquals(2, repository.get(imageURI).contents().size());
      
      // Does not exist
      assertEquals(0, repository.deleteContent(documentURI, jpegContent).contents().size());

      // Should exist
      assertEquals(1, repository.deleteContent(imageURI, jpegContent).contents().size());
      
      // Try that again, should not change anything
      assertEquals(1, repository.deleteContent(imageURI, jpegContent).contents().size());
      
      assertEquals(0, repository.deleteContent(imageURI, pngContent).contents().size());
      assertEquals(0, repository.get(imageURI).contents().size());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error deleting content");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#start()}
   * .
   */
  @Test
  public void testStart() {
    try {
      repository.start();
      fail("Managed to start the repository twice");
    } catch (ContentRepositoryException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#stop()}
   * .
   */
  @Test
  public void testStop() {
    try {
      repository.stop();
      repository.stop();
      fail("Managed to stop the repository twice");
    } catch (ContentRepositoryException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testExists() {
    populateRepository();

    // Test for existing resources
    try {
      assertTrue(repository.exists(page1URI));
      assertTrue(repository.exists(documentURI));
      assertTrue(repository.exists(imageURI));

      documentURI.setIdentifier("4bb19980-8f98-4873-0000-71b6dfab22af");
      assertFalse(repository.exists(documentURI));
    } catch (ContentRepositoryException e) {
      fail("Error checking for resource existence");
    }

    // Test for non-existing resources
    try {
      documentURI.setIdentifier("4bb19980-8f98-4873-0000-71b6dfab22af");
      assertFalse(repository.exists(documentURI));
    } catch (ContentRepositoryException e) {
      fail("Error checking for resource existence");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#find(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  public void testFind() {
    populateRepository();
    SearchQuery q = null;
    try {
      q = new SearchQueryImpl(site).withTemplate("default");
      assertEquals(1, repository.find(q).getDocumentCount());
      assertEquals(1, repository.find(q).getHitCount());
    } catch (ContentRepositoryException e) {
      fail("Error searching");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testGet() {
    populateRepository();
    try {
      Resource<?> r = repository.get(page1URI);
      assertNotNull(r);
      assertEquals(page1URI.getId(), r.getIdentifier());
      assertNull(repository.get(new PageURIImpl(site, "/abc")));
      assertNull(repository.get(new PageURIImpl(site, null, "a-b-c-d")));
      assertNull(repository.get(new PageURIImpl(page1URI, Resource.WORK)));
    } catch (ContentRepositoryException e) {
      e.printStackTrace();
      fail("Error trying to get resource");
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetContent() {
    populateRepository();
    
    // Add content items
    try {
      repository.putContent(imageURI, jpegContent, jpegContentURL.openStream());
      repository.putContent(imageURI, pngContent, pngContentURL.openStream());
      
      assertEquals(2, repository.get(imageURI).contents().size());
      assertNotNull(repository.getContent(imageURI, german));
      assertNotNull(repository.getContent(imageURI, english));
      assertNull(repository.getContent(imageURI, french));
      assertNull(repository.getContent(documentURI, german));      
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error adding content");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getVersions(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testGetVersions() {
    ResourceURI live1URI = new PageURIImpl(site, "/weblounge");
    ResourceURI live2URI = new PageURIImpl(site, "/etc/weblounge");
    ResourceURI work2URI = new PageURIImpl(site, "/etc/weblounge", Resource.WORK);

    Page page1Live = new PageImpl(live1URI);
    Page page2Live = new PageImpl(live2URI);
    Page page2Work = new PageImpl(work2URI);

    try {
      // Add the pages to the index
      repository.put(page1Live);
      repository.put(page2Live);
      repository.put(page2Work);

      // Check the versions
      assertEquals(1, repository.getVersions(live1URI).length);
      assertEquals(2, repository.getVersions(live2URI).length);
      assertEquals(2, repository.getVersions(work2URI).length);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getLanguages(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  public void testGetLanguages() {
    ResourceURI live1URI = new PageURIImpl(site, "/weblounge");
    ResourceURI live2URI = new PageURIImpl(site, "/etc/weblounge");
    ResourceURI work2URI = new PageURIImpl(site, "/etc/weblounge", Resource.WORK);

    Page page1Live = new PageImpl(live1URI);
    page1Live.setTitle("title", english);

    Page page2Live = new PageImpl(live2URI);
    page2Live.setTitle("title", english);
    page2Live.setTitle("titel", german);

    Page page2Work = new PageImpl(work2URI);
    page2Work.setTitle("title", english);
    page2Work.setTitle("titel", german);
    page2Work.setTitle("titre", french);

    try {
      // Add the pages to the index
      repository.put(page1Live);
      repository.put(page2Live);
      repository.put(page2Work);

      // Try to get the languages back
      assertEquals(page1Live.languages().size(), repository.getLanguages(live1URI).length);
      assertEquals(english, repository.getLanguages(live1URI)[0]);

      assertEquals(page2Live.languages().size(), repository.getLanguages(live2URI).length);
      assertTrue(page2Live.languages().containsAll(Arrays.asList(repository.getLanguages(live2URI))));

      assertEquals(page2Work.languages().size(), repository.getLanguages(work2URI).length);
      assertTrue(page2Work.languages().containsAll(Arrays.asList(repository.getLanguages(work2URI))));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  @Ignore
  public void testListResourceURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, long)}
   * .
   */
  @Test
  @Ignore
  public void testListResourceURILong() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, int)}
   * .
   */
  @Test
  @Ignore
  public void testListResourceURIInt() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, int, long)}
   * .
   */
  @Test
  @Ignore
  public void testListResourceURIIntLong() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getResourceCount()}
   * .
   */
  @Test
  public void testGetResourceCount() {
    int count = populateRepository();
    assertEquals(count, repository.getResourceCount());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getRevisionCount()}
   * .
   */
  @Test
  public void testGetRevisionCount() {
    int count = populateRepository();
    assertEquals(count, repository.getRevisionCount());

    ResourceURI page1WorkURI = new PageURIImpl(page1URI, Resource.WORK);
    Page page2Work = new PageImpl(page1WorkURI);

    try {
      repository.put(page2Work);
      assertEquals(count + 1, repository.getRevisionCount());
      repository.delete(page1URI, true);
      assertEquals(count - 1, repository.getRevisionCount());
      repository.delete(page2URI);
      assertEquals(count - 2, repository.getRevisionCount());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  /**
   * Adds sample pages to the search index and returns the number of documents
   * added.
   * 
   * @return the number of pages added
   */
  protected int populateRepository() {
    int count = 0;

    // Add the pages
    try {
      for (Page page : pages) {
        repository.put(page);
        count++;
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample page to the repository failed");
    }

    // Add the file
    try {
      List<ResourceContent> contents = new ArrayList<ResourceContent>();
      for (ResourceContent content : file.contents()) {
        contents.add(file.removeContent(content.getLanguage()));
      }
      repository.put(file);
      // TODO: Add resource contents
      count++;
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample file to the repository failed");
    }

    // Add the image
    try {
      List<ResourceContent> contents = new ArrayList<ResourceContent>();
      for (ResourceContent content : jpeg.contents()) {
        contents.add(jpeg.removeContent(content.getLanguage()));
      }
      repository.put(jpeg);
      // TODO: Add resource contents
      count++;
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample image to the repository failed");
    }

    return count;
  }

}
