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

package ch.entwine.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.content.page.PageTemplateImpl;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.WebloungeAdminImpl;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Security;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Test case for {@link SiteImpl}.
 */
public class SiteImplTest {

  /** The site instance under test */
  protected Site site = null;

  /** Site identifier */
  protected final String identifier = "dev";

  /** Sets the site enabled state */
  protected final boolean enabled = true;

  /** Site description */
  protected final String name = "Main site";

  /** Site administrator */
  protected WebloungeUserImpl administrator = null;

  /** Site administrator login */
  protected final String administratorLogin = "blogadmin";

  /** Site administrator name */
  protected final String administratorName = "Web Office";

  /** Site administrator email */
  protected final String administratorEmail = "weboffice@nowhere.com";

  /** Site administrator password */
  protected final String administratorPassword = "secret";

  /** Default page template */
  protected PageTemplate defaultTemplate = null;

  /** Default template id */
  protected final String defaultTemplateId = "default";

  /** Default template recheck time */
  protected final long defaultTemplateRecheckTime = Times.MS_PER_DAY + 10 * Times.MS_PER_MIN;

  /** Default template valid time */
  protected final long defaultTemplateValidTime = Times.MS_PER_WEEK + Times.MS_PER_DAY + Times.MS_PER_HOUR + Times.MS_PER_MIN;;

  /** Default template url */
  protected final String defaultTemplateUrl = "file://template/default.jsp";

  /** Default template stage */
  protected final String defaultTemplateStage = "boxes";

  /** Default template English name */
  protected final String defaultTemplateName = "Default template";

  /** Mobile page template */
  protected PageTemplate mobileTemplate = null;

  /** Mobile template id */
  protected final String mobileTemplateId = "mobile";

  /** Mobile template url */
  protected final String mobileTemplateUrl = "file://template/mobile.jsp";

  /** Mobile template class name */
  protected final String mobileTemplateClass = "ch.entwine.weblounge.common.impl.site.JSPTemplate";

  /** Mobile template German name */
  protected final String mobileTemplateNameGerman = "Mobile";

  /** Mobile template English name */
  protected final String mobileTemplateName = "Mobile";

  /** The site hostnames */
  protected List<String> hostnames = new ArrayList<String>();

  /** Default hostname */
  protected URL defaultURL = null;

  /** Default hostname */
  protected URL fallbackURL = null;

  /** Default hostname */
  protected URL localhost = null;

  /** Portrait image style */
  protected ImageStyle portraitImageStyle = null;

  /** High resolution image style */
  protected ImageStyle highresImageStyle = null;

  /** The English language */
  protected static final Language ENGLISH = new LanguageImpl(new Locale("en"));

  /** The German language */
  protected static final Language GERMAN = new LanguageImpl(new Locale("de"));

  /** The admin role */
  protected String adminRole = "my-admin";

  /** The publisher role */
  protected String publisherRole = "my-publisher";

  /** The editor role */
  protected String editorRole = "my-editor";

  /** The guest role */
  protected String guestRole = "my-guest";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    site = new SiteImpl();
    site.setIdentifier(identifier);
    site.setAutoStart(enabled);
    site.setName(name);
    site.setAdministrator(administrator);
    site.setDefaultTemplate(defaultTemplate);
    site.addTemplate(mobileTemplate);
    site.setDefaultLanguage(GERMAN);
    site.addLanguage(ENGLISH);
    site.setDefaultURL(defaultURL);
    site.addURL(fallbackURL);
    site.addURL(localhost);
    site.addLocalRole(Security.SITE_ADMIN_ROLE, adminRole);
    site.addLocalRole(Security.PUBLISHER_ROLE, publisherRole);
    site.addLocalRole(Security.EDITOR_ROLE, editorRole);
    site.addLocalRole(Security.GUEST_ROLE, guestRole);
  }

  /**
   * Sets up preliminary data structures.
   * 
   * @throws Exception
   */
  protected void setupPrerequisites() throws Exception {
    // Urls
    defaultURL = new URL("http://www.weblounge.org");
    fallbackURL = new URL("http://*.nowhere.com");
    localhost = new URL("http://localhost:8080");
    
    // Administrator
    administrator = new WebloungeAdminImpl(administratorLogin);
    administrator.setName(administratorName);
    administrator.setEmail(administratorEmail);
    administrator.addPrivateCredentials(new PasswordImpl(administratorPassword, DigestType.plain));
    // Default template
    defaultTemplate = new PageTemplateImpl(defaultTemplateId, new URL(defaultTemplateUrl));
    defaultTemplate.setRecheckTime(defaultTemplateRecheckTime);
    defaultTemplate.setValidTime(defaultTemplateValidTime);
    defaultTemplate.setComposeable(true);
    defaultTemplate.setStage(defaultTemplateStage);
    defaultTemplate.setName(defaultTemplateName);
    // Mobile template
    mobileTemplate = new PageTemplateImpl(mobileTemplateId, new URL(mobileTemplateUrl));
    mobileTemplate.setComposeable(true);
    mobileTemplate.setName(mobileTemplateName);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#setIdentifier(java.lang.String)}
   * .
   */
  @Test
  public void testSetIdentifier() {
    site.setIdentifier("1ab_2ABC3-.0");
    site.setIdentifier("1");
    site.setIdentifier("a");
    try {
      site.setIdentifier("Test id with spaces and,strange/characters");
      fail("Site accepted identifier with spaces in it");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
    try {
      site.setIdentifier(".abc");
      fail("Site accepted identifier starting with a special character");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
    try {
      site.setIdentifier("");
      fail("Site accepted an empty identifier");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, site.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#isStartedAutomatically()}
   * .
   */
  @Test
  public void testIsStartedAutomatically() {
    assertEquals(enabled, site.isStartedAutomatically());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getAdministrator()} .
   */
  @Test
  public void testGetAdministrator() {
    assertEquals(administrator, site.getAdministrator());
    assertEquals(administratorName, site.getAdministrator().getName());
    assertEquals(administratorEmail, site.getAdministrator().getEmail());
    assertEquals(administratorLogin, site.getAdministrator().getLogin());
    assertTrue(site.getAdministrator().canLogin());
    
    Set<Object> passwords = site.getAdministrator().getPrivateCredentials(Password.class);
    for (Object o : passwords) {
      Password password = (Password)o;
      assertEquals(administratorPassword, password.getPassword());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getName(ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetName() {
    assertEquals(name, site.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#addTemplate(ch.entwine.weblounge.common.content.page.PageTemplate)}
   * .
   */
  @Test
  public void testAddTemplate() throws Exception {
    site.addTemplate(defaultTemplate);
    assertEquals(2, site.getTemplates().length);
    site.addTemplate(new PageTemplateImpl("test", new URL(defaultTemplateUrl)));
    assertEquals(3, site.getTemplates().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#removeTemplate(ch.entwine.weblounge.common.content.page.PageTemplate)}
   * .
   */
  @Test
  public void testRemoveTemplate() {
    site.removeTemplate(defaultTemplate);
    assertEquals(1, site.getTemplates().length);
    assertTrue(site.getDefaultTemplate() == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getTemplate(java.lang.String)}
   * .
   */
  @Test
  public void testGetTemplate() throws Exception {
    PageTemplate d = site.getTemplate(defaultTemplateId);
    assertNotNull(d);
    assertEquals(defaultTemplateId, d.getIdentifier());
    assertEquals(new URL(defaultTemplateUrl), d.getRenderer());
    assertEquals(defaultTemplateRecheckTime, d.getRecheckTime());
    assertEquals(defaultTemplateValidTime, d.getValidTime());
    assertEquals(defaultTemplateStage, d.getStage());
    assertEquals(defaultTemplateName, d.getName());

    PageTemplate m = site.getTemplate(mobileTemplateId);
    assertNotNull(m);
    assertEquals(mobileTemplateId, m.getIdentifier());
    assertEquals(new URL(mobileTemplateUrl), m.getRenderer());
    assertEquals(Renderer.DEFAULT_RECHECK_TIME, m.getRecheckTime());
    assertEquals(Renderer.DEFAULT_VALID_TIME, m.getValidTime());
    assertEquals(PageTemplate.DEFAULT_STAGE, m.getStage());
    assertEquals(mobileTemplateName, m.getName());

    assertTrue(site.getTemplate("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getTemplates()}.
   */
  @Test
  public void testGetTemplates() {
    assertNotNull(site.getTemplates());
    assertEquals(2, site.getTemplates().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getDefaultTemplate()}.
   */
  @Test
  public void testGetDefaultTemplate() {
    assertEquals(defaultTemplate, site.getDefaultTemplate());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#addLanguage(ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testAddLanguage() {
    site.addLanguage(ENGLISH);
    assertEquals(2, site.getLanguages().length);
    site.addLanguage(new LanguageImpl(new Locale("fr")));
    assertEquals(3, site.getLanguages().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#removeLanguage(ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testRemoveLanguage() {
    site.removeLanguage(GERMAN);
    assertEquals(1, site.getLanguages().length);
    assertTrue(site.getDefaultLanguage() == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getLanguage(java.lang.String)}
   * .
   */
  @Test
  public void testGetLanguage() {
    assertEquals(GERMAN, site.getLanguage(GERMAN.getIdentifier()));
    assertTrue(site.getLanguage("fr") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getLanguages()}.
   */
  @Test
  public void testGetLanguages() {
    assertEquals(2, site.getLanguages().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#supportsLanguage(ch.entwine.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testSupportsLanguage() {
    assertTrue(site.supportsLanguage(GERMAN));
    assertTrue(site.supportsLanguage(ENGLISH));
    assertFalse(site.supportsLanguage(new LanguageImpl(new Locale("fr"))));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getDefaultLanguage()}.
   */
  @Test
  public void testGetDefaultLanguage() {
    assertEquals(GERMAN, site.getDefaultLanguage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#removeURL(URL)}
   * .
   */
  @Test
  public void testRemoveURL() throws Exception {
    site.removeURL(new URL("http://test"));
    assertEquals(3, site.getURLs().length);
    site.removeURL(defaultURL);
    assertEquals(2, site.getURLs().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getURLs()}.
   */
  @Test
  public void testGetURLs() {
    assertEquals(3, site.getURLs().length);
    assertEquals(defaultURL, site.getURLs()[0]);
    assertEquals(fallbackURL, site.getURLs()[1]);
    assertEquals(localhost, site.getURLs()[2]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getURL()}.
   */
  @Test
  public void testGetURL() {
    assertEquals(defaultURL, site.getURL());
    site.removeURL(defaultURL);
    assertEquals(2, site.getURLs().length);
    assertTrue(site.getURL() == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    assertEquals(defaultURL, site.getURL());
    site.removeURL(defaultURL);
    site.removeURL(fallbackURL);
    site.removeURL(localhost);
    assertNull(site.getURL());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#removeLayout(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testRemoveLayout() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getLayout(java.lang.String)}
   * .
   */
  @Test
  @Ignore
  public void testGetLayout() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getLayouts()}.
   */
  @Test
  @Ignore
  public void testGetLayouts() {
    assertEquals(0, site.getLayouts().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.site.SiteImpl#getLocalRole(String)}
   * .
   */
  @Test
  public void testGetLocalRole() {
    assertEquals(adminRole, site.getLocalRole(Security.SITE_ADMIN_ROLE));
    assertEquals(publisherRole, site.getLocalRole(Security.PUBLISHER_ROLE));
    assertEquals(editorRole, site.getLocalRole(Security.EDITOR_ROLE));
    assertEquals(guestRole, site.getLocalRole(Security.GUEST_ROLE));

    String nonExistingRole = "test";
    assertEquals(nonExistingRole, site.getLocalRole(nonExistingRole));
  }

}
