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

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.url.PathSupport;
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
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  protected String page1path = "/test";

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

  /** The sample files */
  protected FileResource[] files = null;

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
    languages.add(LanguageSupport.getLanguage("en"));
    languages.add(LanguageSupport.getLanguage("de"));

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
      ResourceURI uri = new PageURIImpl(site, "/");
      InputStream is = this.getClass().getResourceAsStream("/page" + (i + 1) + ".xml");
      pages[i] = pageReader.read(uri, is);
    }

    // Prepare the files
    FileResourceReader fileReader = new FileResourceReader();
    files = new FileResource[2];
    for (int i = 0; i < files.length; i++) {
      ResourceURI uri = new FileResourceURIImpl(site, "/");
      InputStream is = this.getClass().getResourceAsStream("/file" + (i + 1) + ".xml");
      files[i] = fileReader.read(uri, is);
    }
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
  @Ignore
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
      repository.delete(imageURI);
      fail("Managed to remove referenced resource");
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error trying to delete a resource");
    } catch (ContentRepositoryException e) {
      // This is expected
      try {
        assertNotNull(repository.get(imageURI));
        assertEquals(resources - 1, repository.getResourceCount());
      } catch (ContentRepositoryException e1) {
        e1.printStackTrace();
        fail("Error trying to delete a resource");
      }
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#delete(ch.o2it.weblounge.common.content.ResourceURI, boolean)}
   * .
   */
  @Test
  @Ignore
  public void testDeleteResourceURIBoolean() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#move(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  @Ignore
  public void testMove() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#put(ch.o2it.weblounge.common.content.Resource)}
   * .
   */
  @Test
  @Ignore
  public void testPut() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#putContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceContent, java.io.InputStream)}
   * .
   */
  @Test
  @Ignore
  public void testPutContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.content.ResourceContent)}
   * .
   */
  @Test
  @Ignore
  public void testDeleteContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.o2it.weblounge.common.content.Resource)}
   * .
   */
  @Test
  @Ignore
  public void testStoreResourceResourceOfQextendsResourceContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#disconnect()}
   * .
   */
  @Test
  @Ignore
  public void testDisconnect() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#start()}
   * .
   */
  @Test
  @Ignore
  public void testStart() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#stop()}
   * .
   */
  @Test
  @Ignore
  public void testStop() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  @Ignore
  public void testExists() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#find(ch.o2it.weblounge.common.content.SearchQuery)}
   * .
   */
  @Test
  @Ignore
  public void testFind() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  @Ignore
  public void testGet() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getContent(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  @Ignore
  public void testGetContent() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getVersions(ch.o2it.weblounge.common.content.ResourceURI)}
   * .
   */
  @Test
  @Ignore
  public void testGetVersions() {
    fail("Not yet implemented"); // TODO
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
  @Ignore
  public void testGetResourceCount() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.AbstractContentRepository#getVersionCount()}
   * .
   */
  @Test
  @Ignore
  public void testGetVersionCount() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Adds sample pages to the search index and returns the number of documents
   * added.
   * 
   * @return the number of pages added
   */
  protected int populateRepository() {
    int count = 0;
    try {
      for (Page page : pages) {
        repository.put(page);
        count++;
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample page to the repository failed");
    }
    try {
      for (FileResource file : files) {
        List<ResourceContent> contents = new ArrayList<ResourceContent>();
        for (ResourceContent content : file.contents()) {
          contents.add(file.removeContent(content.getLanguage()));
        }
        repository.put(file);
        // TODO: Add resource contents
        count++;
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Adding sample file to the repository failed");
    }
    return count;
  }

}
