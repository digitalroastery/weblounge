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

package ch.o2it.weblounge.common.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.content.page.PageletImpl;
import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Test case for {@link SearchQueryImpl}.
 */
public class SearchQueryImplTest {

  /** The query object under test */
  protected SearchQueryImpl query = null;

  /** The mock site */
  protected Site site = null;

  /** The German language */
  protected Language german = new LanguageImpl(new Locale("de"));

  /** The first result item */
  protected int offset = 30;

  /** The maximum number of result items to include */
  protected int limit = 10;
  
  /** The test pagelet */
  protected Pagelet pagelet = null;

  /** The date */
  protected Date date = new Date();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    pagelet = new PageletImpl("module", "id");
    query = new SearchQueryImpl(site);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#SearchQueryImpl(ch.o2it.weblounge.common.site.Site, ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testSearchQueryImplSiteLanguage() {
    query = new SearchQueryImpl(site, german);
    assertEquals(german, query.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, query.getSite());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withLimit(int)}
   * .
   */
  @Test
  public void testWithLimit() {
    query.withLimit(limit);
    assertEquals(limit, query.getLimit());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withOffset(int)}
   * .
   */
  @Test
  public void testWithOffset() {
    query.withOffset(offset);
    assertEquals(offset, query.getOffset());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withIdentifier(java.lang.String)}
   * .
   */
  @Test
  public void testWithId() {
    String id = "4bb19980-8f98-4873-a813-71b6dfab22af";
    query.withIdentifier(id);
    assertEquals(id, query.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPath(java.lang.String)}
   * .
   */
  @Test
  public void testWithPath() {
    String path = "/test";
    query.withPath(path);
    assertEquals(PathSupport.trim(path), query.getPath());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withTemplate(java.lang.String)}
   * .
   */
  @Test
  public void testWithTemplate() {
    String template = "news";
    query.withTemplate(template);
    assertEquals(template, query.getTemplate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withType(java.lang.String)}
   * .
   */
  @Test
  public void testWithType() {
    String type = "page";
    query.withType(type);
    assertEquals(type, query.getType());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#and(java.util.Date)}
   * .
   */
  @Test
  public void testAndDate() {
    Date endDate = new Date(date.getTime() + Times.MS_PER_MONTH);

    // Creation date
    query.withCreationDateBetween(date).and(endDate);
    assertEquals(date, query.getCreationDate());
    assertEquals(endDate, query.getCreationDateEnd());
    
    query.withModificationDateBetween(date).and(endDate);
    assertEquals(date, query.getModificationDate());
    assertEquals(endDate, query.getModificationDateEnd());
    
    query.withPublishingDateBetween(date).and(endDate);
    assertEquals(date, query.getPublishingDate());
    assertEquals(endDate, query.getPublishingDateEnd());

    // Test and(date) without withDateBetween(date)
    try {
      query = new SearchQueryImpl(site);
      query.and(endDate);
      fail("Was able to specify end date without start date");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test with(date).andDate(date). Should fail, since users are requested to
    // call withDateBetween(date).andDate(date);
    try {
      query = new SearchQueryImpl(site);
      query.withCreationDate(endDate);
      query.and(date);
      fail("Was able to used and(date) without withDateBetween()");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test order of start and end date
    try {
      query = new SearchQueryImpl(site);
      query.withCreationDateBetween(endDate).and(date);
      fail("Was able to swap start date and end date");
    } catch (IllegalStateException e) {
      // This is expected
    }

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#andProperty(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAndProperty() {
    String propertyName = "propertyName";
    String propertyValue = "propertyValue";
    
    // Test andProperty(key, value) without withPagelet(module, id)
    try {
      query = new SearchQueryImpl(site);
      query.andProperty(propertyName, propertyValue);
      fail("Was able to specify pagelet property without pagelet");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test proper behavior
    query = new SearchQueryImpl(site);
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    query.andProperty(propertyName, propertyValue);
    assertEquals(1, query.getPagelets().length);
    assertEquals(propertyValue, query.getPagelets()[0].getProperty(propertyName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#andElement(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAndElement() {
    String textName = "textName";
    String textValue = "text";
    
    // Test andText(key, value) without withPagelet(module, id)
    try {
      query = new SearchQueryImpl(site);
      query.andElement(textName, textValue);
      fail("Was able to specify pagelet text without pagelet");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test proper behavior
    query = new SearchQueryImpl(site, german);
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    query.andElement(textName, textValue);
    assertEquals(1, query.getPagelets().length);
    assertEquals(textValue, query.getPagelets()[0].getContent(textName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#atPosition(int)}
   * .
   */
  @Test
  public void testAtPosition() {
    String composer = "main";
    int position = 5;
    
    // Test atPosition(composer) without withPagelet(module, id)
    try {
      query = new SearchQueryImpl(site);
      query.atPosition(position);
      fail("Was able to specify pagelet position without pagelet");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test proper behavior with pagelet only
    query = new SearchQueryImpl(site);
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    query.atPosition(position);
    assertEquals(1, query.getPagelets().length);
    assertEquals(position, query.getPagelets()[0].getURI().getPosition());

    // Test proper behavior with pagelet only
    query = new SearchQueryImpl(site);
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    query.inComposer(composer);
    query.atPosition(position);
    assertEquals(1, query.getPagelets().length);
    assertEquals(composer, query.getPagelets()[0].getURI().getComposer());
    assertEquals(position, query.getPagelets()[0].getURI().getPosition());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#inComposer(java.lang.String)}
   * .
   */
  @Test
  public void testInComposer() {
    String composer = "main";
    
    // Test inComposer(composer) without withPagelet(module, id)
    try {
      query = new SearchQueryImpl(site);
      query.inComposer(composer);
      fail("Was able to specify composer without pagelet");
    } catch (IllegalStateException e) {
      // This is expected
    }

    // Test proper behavior
    query = new SearchQueryImpl(site);
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    query.inComposer(composer);
    assertEquals(1, query.getPagelets().length);
    assertEquals(composer, query.getPagelets()[0].getURI().getComposer());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withAuthor(ch.o2it.weblounge.common.user.User)}
   * .
   */
  @Test
  public void testWithAuthor() {
    User author = new UserImpl("john");
    query.withAuthor(author);
    assertEquals(author, query.getAuthor());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withLanguage(ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testWithLanguage() {
    query.withLanguage(german);
    assertEquals(german, query.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withSubject(java.lang.String)}
   * .
   */
  @Test
  public void testWithSubject() {
    String subject = "subject";
    String otherSubject = "other subject";
    query.withSubject(subject);
    query.withSubject(otherSubject);
    assertEquals(2, query.getSubjects().length);
    List<String> subjects = Arrays.asList(query.getSubjects());
    assertTrue(subjects.contains(subject));
    assertTrue(subjects.contains(otherSubject));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withCreationDate(java.util.Date)}
   * .
   */
  @Test
  public void testWithCreationDate() {
    Date date = new Date();
    query.withCreationDate(date);
    assertEquals(date, query.getCreationDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withCreationDateBetween(java.util.Date)}
   * .
   */
  @Test
  public void testWithCreationDateBetween() {
    Date date = new Date();
    query.withCreationDateBetween(date);
    assertEquals(date, query.getCreationDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withModificationDate(java.util.Date)}
   * .
   */
  @Test
  public void testWithModificationDate() {
    Date date = new Date();
    query.withModificationDate(date);
    assertEquals(date, query.getModificationDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withModificationDateBetween(java.util.Date)}
   * .
   */
  @Test
  public void testWithModificationDateBetween() {
    Date date = new Date();
    query.withModificationDateBetween(date);
    assertEquals(date, query.getModificationDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPagelet(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testWithPagelet() {
    query.withPagelet(pagelet.getModule(), pagelet.getIdentifier());
    assertEquals(1, query.getPagelets().length);
    assertEquals(pagelet, query.getPagelets()[0]);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPathPrefix(java.lang.String)}
   * .
   */
  @Test
  public void testWithPathPrefix() {
    String prefix = "/path/prefix";
    query.withPathPrefix(prefix);
    assertEquals(prefix, query.getPathPrefix());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withCreator(ch.o2it.weblounge.common.user.User)}
   * .
   */
  @Test
  public void testWithCreator() {
    User author = new UserImpl("john");
    query.withAuthor(author);
    assertEquals(author, query.getAuthor());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withModifier(ch.o2it.weblounge.common.user.User)}
   * .
   */
  @Test
  public void testWithModifier() {
    User modifier = new UserImpl("john");
    query.withModifier(modifier);
    assertEquals(modifier, query.getModifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPublisher(ch.o2it.weblounge.common.user.User)}
   * .
   */
  @Test
  public void testWithPublisher() {
    User publisher = new UserImpl("john");
    query.withPublisher(publisher);
    assertEquals(publisher, query.getPublisher());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPublishingDate(java.util.Date)}
   * .
   */
  @Test
  public void testWithPublishingDate() {
    Date date = new Date();
    query.withPublishingDate(date);
    assertEquals(date, query.getPublishingDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withPublishingDateBetween(java.util.Date)}
   * .
   */
  @Test
  public void testWithPublishingDateBetween() {
    Date date = new Date();
    query.withPublishingDate(date);
    assertEquals(date, query.getPublishingDate());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withText(java.lang.String)}
   * .
   */
  @Test
  public void testWithText() {
    String text = "text";
    query = new SearchQueryImpl(site, german);
    query.withText(text);
    assertEquals(text, query.getText());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withElement(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testWithElement() {
    String textName = "textName";
    String textValue = "text";
    query = new SearchQueryImpl(site, german);
    query.withElement(textName, textValue);
    assertEquals(1, query.getElements().size());
    assertEquals(textValue, query.getElements().get(textName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.SearchQueryImpl#withProperty(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testWithProperty() {
    String propertyName = "propertyName";
    String propertyValue = "propertyValue";
    query = new SearchQueryImpl(site, german);
    query.withProperty(propertyName, propertyValue);
    assertEquals(1, query.getProperties().size());
    assertEquals(propertyValue, query.getProperties().get(propertyName));
  }

}
