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

package ch.entwine.weblounge.contentrepository.fs;

import static ch.entwine.weblounge.common.content.Resource.LIVE;
import static ch.entwine.weblounge.common.content.Resource.WORK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Order;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepository;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link DocumentVersionTest}.
 */
public class DocumentVersionTest {

  /** The content repository */
  protected FileSystemContentRepository repository = null;

  /** The repository root directory */
  protected File repositoryRoot = null;

  /** The mock site */
  protected Site site = null;

  /** Page template */
  protected PageTemplate template = null;

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
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    String rootPath = PathUtils.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    repositoryRoot = new File(rootPath);
    FileUtils.deleteQuietly(repositoryRoot);

    // Create the index configuration
    System.setProperty("weblounge.home", rootPath);
    TestUtils.startTesting();
    ElasticSearchUtils.createIndexConfigurationAt(repositoryRoot);

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
    EasyMock.expect(site.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(site.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.expect(site.getModules()).andReturn(new Module[] {}).anyTimes();
    EasyMock.expect(site.getDefaultLanguage()).andReturn(LanguageUtils.getLanguage("de")).anyTimes();
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("admin")).anyTimes();
    EasyMock.replay(site);

    // Connect to the repository
    repository = new FileSystemContentRepository();
    repository.setSerializer(serializer);
    repository.setEnvironment(Environment.Production);
    Dictionary<String, Object> repositoryProperties = new Hashtable<String, Object>();
    repositoryProperties.put(FileSystemContentRepository.OPT_ROOT_DIR, repositoryRoot.getAbsolutePath());
    repository.updated(repositoryProperties);
    repository.connect(site);

    // Remove the default home page
    repository.delete(new PageURIImpl(site, "/"), true);
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
   * Test method for {@link SearchQuery#withPreferredVersion(long)}.
   */
  @Test
  public void testWithPreferredVersion() throws Exception {
    SearchResult result = null;

    // Make sure the homepage doesn't get into our way
    repository.delete(new PageURIImpl(site, "/"), true);

    SearchQuery workPreferredQuery = new SearchQueryImpl(site).withPreferredVersion(WORK).sortByCreationDate(Order.Ascending);
    SearchQuery livePreferredQuery = new SearchQueryImpl(site).withPreferredVersion(LIVE).sortByCreationDate(Order.Ascending);
    SearchQuery workOnlyQuery = new SearchQueryImpl(site).withVersion(WORK).sortByCreationDate(Order.Ascending);
    SearchQuery liveOnlyQuery = new SearchQueryImpl(site).withVersion(LIVE).sortByCreationDate(Order.Ascending);

    // Test empty repository
    assertEquals(0, repository.find(workPreferredQuery).getDocumentCount());
    assertEquals(0, repository.find(livePreferredQuery).getDocumentCount());
    assertEquals(0, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(0, repository.find(liveOnlyQuery).getDocumentCount());

    // Create URI and pages and add them to the repository
    ResourceURI liveOnlyURI = new PageURIImpl(site, "/liveonly", LIVE);
    ResourceURI liveAndWorkLiveURI = new PageURIImpl(site, "/liveandwork", LIVE);
    ResourceURI liveAndWorkWorkURI = new PageURIImpl(site, "/liveandwork", WORK);
    ResourceURI workOnlyURI = new PageURIImpl(site, "/workonly", WORK);

    Page liveOnly = new PageImpl(liveOnlyURI);
    liveOnly.setTemplate(template.getIdentifier());
    Page liveAndWorkLive = new PageImpl(liveAndWorkLiveURI);
    liveAndWorkLive.setTemplate(template.getIdentifier());
    Page liveAndWorkWork = new PageImpl(liveAndWorkWorkURI);
    liveAndWorkWork.setTemplate(template.getIdentifier());
    Page workOnly = new PageImpl(workOnlyURI);
    workOnly.setTemplate(template.getIdentifier());

    // Add the live only live page
    repository.put(liveOnly);
    assertEquals(0, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(1, repository.find(liveOnlyQuery).getDocumentCount());
    result = repository.find(workPreferredQuery);
    assertEquals(1, result.getDocumentCount());
    ResourceSearchResultItem searchResultItem = (ResourceSearchResultItem) result.getItems()[0];
    assertEquals(LIVE, searchResultItem.getResourceURI().getVersion());
    result = repository.find(livePreferredQuery);
    assertEquals(1, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());

    // Add the work only work page
    repository.put(workOnly);
    assertEquals(1, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(1, repository.find(liveOnlyQuery).getDocumentCount());
    result = repository.find(workPreferredQuery);
    assertEquals(2, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    result = repository.find(livePreferredQuery);
    assertEquals(2, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());

    // Add the live and work live page
    repository.put(liveAndWorkLive);
    assertEquals(1, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(2, repository.find(liveOnlyQuery).getDocumentCount());
    result = repository.find(workPreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());
    result = repository.find(livePreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());

    // Add the live and work work page
    repository.put(liveAndWorkWork);
    assertEquals(2, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(2, repository.find(liveOnlyQuery).getDocumentCount());
    result = repository.find(workPreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());
    result = repository.find(livePreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());

    // Lock the page, which will trigger a call to index.update()
    repository.lock(liveAndWorkWorkURI, new UserImpl("user"));
    assertEquals(2, repository.find(workOnlyQuery).getDocumentCount());
    assertEquals(2, repository.find(liveOnlyQuery).getDocumentCount());
    result = repository.find(workPreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());
    result = repository.find(livePreferredQuery);
    assertEquals(3, result.getDocumentCount());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[0]).getResourceURI().getVersion());
    assertEquals(WORK, ((ResourceSearchResultItem) result.getItems()[1]).getResourceURI().getVersion());
    assertEquals(LIVE, ((ResourceSearchResultItem) result.getItems()[2]).getResourceURI().getVersion());
  }

}
