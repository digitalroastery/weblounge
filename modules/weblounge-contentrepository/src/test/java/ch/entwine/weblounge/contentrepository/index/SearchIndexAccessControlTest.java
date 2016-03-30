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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import ch.entwine.weblounge.common.NotImplementedException;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.security.AccessRuleImpl;
import ch.entwine.weblounge.common.impl.security.AllowAllAccessRule;
import ch.entwine.weblounge.common.impl.security.DenyAllAccessRule;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.security.AccessRule;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Rule;
import ch.entwine.weblounge.common.security.Securable.Order;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.search.impl.elasticsearch.ElasticSearchUtils;

/**
 * Unit tests to ensure only resources a given user has the appropriate access
 * rights are returned from the search index.
 */
public class SearchIndexAccessControlTest {

  /* the users */
  private static final User USER_WITHOUT_ANY_ROLES = new WebloungeUserImpl("user_without_any_roles");
  private static final User USER_WITH_ROLE_STUDENT = new WebloungeUserImpl("user_with_role_student");
  private static final User USER_WITH_ROLE_TEACHER = new WebloungeUserImpl("user_with_role_teacher");
  private static final User USER_WITH_ROLES_STUDENT_TEACHER = new WebloungeUserImpl("user_with_roles_student_teacher");

  /* the roles */
  private static final Role ROLE_STUDENT = new RoleImpl("weblounge:student");
  private static final Role ROLE_TEACHER = new RoleImpl("weblounge:teacher");

  /* the access rules */
  private static final AccessRule ALL_ALLOW_READ = new AllowAllAccessRule(SystemAction.READ);
  private static final AccessRule ALL_DENY_READ = new DenyAllAccessRule(SystemAction.READ);
  private static final AccessRule ROLE_STUDENT_ALLOW_READ = new AccessRuleImpl(ROLE_STUDENT, SystemAction.READ, Rule.Allow);
  private static final AccessRule ROLE_STUDENT_DENY_READ = new AccessRuleImpl(ROLE_STUDENT, SystemAction.READ, Rule.Deny);
  private static final AccessRule ROLE_TEACHER_ALLOW_READ = new AccessRuleImpl(ROLE_TEACHER, SystemAction.READ, Rule.Allow);
  private static final AccessRule ROLE_TEACHER_DENY_READ = new AccessRuleImpl(ROLE_TEACHER, SystemAction.READ, Rule.Deny);

  private static Site site;
  private static File idxRoot;
  private static SearchIndexImplStub idx;
  private static SearchQuery q;

  /* the test pages */
  private Page pageAllowDeny;
  private Page pageDenyAllow;

  @BeforeClass
  public static void setUpClass() throws Exception {

    USER_WITH_ROLE_STUDENT.addPublicCredentials(ROLE_STUDENT);
    USER_WITH_ROLE_TEACHER.addPublicCredentials(ROLE_TEACHER);
    USER_WITH_ROLES_STUDENT_TEACHER.addPublicCredentials(ROLE_STUDENT);
    USER_WITH_ROLES_STUDENT_TEACHER.addPublicCredentials(ROLE_TEACHER);

    site = new SiteImpl();
    site.setIdentifier("test");

    q = new SearchQueryImpl(site).withAction(SystemAction.READ).withTypes(Page.TYPE);

    // Resource serializer
    ResourceSerializerServiceImpl serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());

    String rootPath = PathUtils.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    idxRoot = new File(rootPath);
    System.setProperty("weblounge.home", rootPath);
    ElasticSearchUtils.createIndexConfigurationAt(idxRoot);
    idx = SearchIndexImplStub.mkSearchIndexImplStub();
    idx.bindResourceSerializerService(serializer);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    idx.close();
    FileUtils.deleteQuietly(idxRoot);
  }

  @Before
  public void setUp() throws Exception {
    PageReader reader = new PageReader();
    try (InputStream is = getClass().getResourceAsStream("/page.xml")) {
      pageAllowDeny = reader.read(is, site);
      pageAllowDeny.setAllowDenyOrder(Order.AllowDeny);
    }
    try (InputStream is = getClass().getResourceAsStream("/page.xml")) {
      pageDenyAllow = reader.read(is, site);
      pageDenyAllow.setAllowDenyOrder(Order.DenyAllow);
    }
  }

  @After
  public void tearDown() throws Exception {
    idx.clear();
  }

  // ##########################################################################
  // Unit tests for resources with Allow-Deny order
  // ##########################################################################

  /** Unit test for test case A.01 */
  @Test
  public void testAllowDenyWithoutAcl() throws Exception {
    idx.add(pageAllowDeny);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertTrue(accessDecisions[0]);
    assertTrue(accessDecisions[1]);
    assertTrue(accessDecisions[2]);
    assertTrue(accessDecisions[3]);
  }

  /** Unit test for test case A.02 */
  @Test
  public void testAllowDenyAllowAll() throws Exception {
    pageAllowDeny.addAccessRule(ALL_ALLOW_READ);
    idx.add(pageAllowDeny);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertTrue(accessDecisions[0]);
    assertTrue(accessDecisions[1]);
    assertTrue(accessDecisions[2]);
    assertTrue(accessDecisions[3]);
  }

  /** Unit test for test case A.03 */
  @Test
  public void testAllowDenyOneAllow() throws Exception {
    pageAllowDeny.addAccessRule(ROLE_STUDENT_ALLOW_READ);
    idx.add(pageAllowDeny);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertFalse(accessDecisions[0]);
    assertTrue(accessDecisions[1]);
    assertFalse(accessDecisions[2]);
    assertTrue(accessDecisions[3]);
  }

  /** Unit test for test case A.03 */
  @Test
  public void testAllowDenyTwoAllow() throws Exception {
    pageAllowDeny.addAccessRule(ROLE_STUDENT_ALLOW_READ);
    pageAllowDeny.addAccessRule(ROLE_TEACHER_ALLOW_READ);
    idx.add(pageAllowDeny);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertFalse(accessDecisions[0]);
    assertTrue(accessDecisions[1]);
    assertTrue(accessDecisions[2]);
    assertTrue(accessDecisions[3]);
  }
  

  
  // ##########################################################################
  // Unit tests for resources with Deny-Allow order
  // ##########################################################################
  

  /** Unit test for test case D.01 */
  @Test(expected = NotImplementedException.class)
  public void testDenyAllowWithoutAcl() throws Exception {
    idx.add(pageDenyAllow);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertFalse(accessDecisions[0]);
    assertFalse(accessDecisions[1]);
    assertFalse(accessDecisions[2]);
    assertFalse(accessDecisions[3]);
  }
 
  /** Unit test for test case D.02 */
  @Test(expected = NotImplementedException.class)
  public void testDenyAllowDenyAll() throws Exception {
    pageDenyAllow.addAccessRule(ALL_DENY_READ);
    idx.add(pageDenyAllow);
    
    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertFalse(accessDecisions[0]);
    assertFalse(accessDecisions[1]);
    assertFalse(accessDecisions[2]);
    assertFalse(accessDecisions[3]);
  }

  /** Unit test for test case D.03 */
  @Test(expected = NotImplementedException.class)
  public void testDenyAllowOneDeny() throws Exception {
    pageDenyAllow.addAccessRule(ROLE_STUDENT_DENY_READ);
    idx.add(pageDenyAllow);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertTrue(accessDecisions[0]);
    assertFalse(accessDecisions[1]);
    assertTrue(accessDecisions[2]);
    assertFalse(accessDecisions[3]);
  }

  /** Unit test for test case D.04 */
  @Test(expected = NotImplementedException.class)
  public void testDenyAllowTwoDeny() throws Exception {
    pageDenyAllow.addAccessRule(ROLE_STUDENT_DENY_READ);
    pageDenyAllow.addAccessRule(ROLE_TEACHER_DENY_READ);
    idx.add(pageDenyAllow);

    boolean[] accessDecisions = getAccessDecisionsForUsers(q);
    assertTrue(accessDecisions[0]);
    assertFalse(accessDecisions[1]);
    assertFalse(accessDecisions[2]);
    assertFalse(accessDecisions[3]);
  }

  /**
   * Run the query in the security context of the given user.
   * 
   * @param user
   *          the user to pass to the security context
   * @param q
   *          the query to execute
   * @return the result of the query
   * @throws ContentRepositoryException
   *           if executing the search operation fails
   */
  private SearchResult runQueryWithUser(User user, SearchQuery q)
      throws ContentRepositoryException {
    SecurityUtils.setConfigured(true);
    final User originalUser = SecurityUtils.getUser();
    try {
      SecurityUtils.setUser(user);
      return idx.getByQuery(q);
    } finally {
      SecurityUtils.setUser(originalUser);
      SecurityUtils.setConfigured(false);
    }
  }

  private boolean[] getAccessDecisionsForUsers(SearchQuery q) throws Exception {
    boolean[] accessDecisions = new boolean[4];
    SearchResult result;

    result = runQueryWithUser(USER_WITHOUT_ANY_ROLES, q);
    accessDecisions[0] = (result.getDocumentCount() > 0) ? true : false;

    result = runQueryWithUser(USER_WITH_ROLE_STUDENT, q);
    accessDecisions[1] = (result.getDocumentCount() > 0) ? true : false;

    result = runQueryWithUser(USER_WITH_ROLE_TEACHER, q);
    accessDecisions[2] = (result.getDocumentCount() > 0) ? true : false;

    result = runQueryWithUser(USER_WITH_ROLES_STUDENT_TEACHER, q);
    accessDecisions[3] = (result.getDocumentCount() > 0) ? true : false;

    return accessDecisions;
  }

}
