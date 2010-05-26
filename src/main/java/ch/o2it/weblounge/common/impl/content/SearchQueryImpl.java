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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;
import java.util.Map;
import java.util.Stack;

/**
 * Default implementation for the search query api.
 */
public class SearchQueryImpl implements SearchQuery {

  /** The site */
  protected Site site = null;

  /** Query configuration stack */
  protected Stack<Object> stack = new Stack<Object>();

  /** The object that needs to show up next */
  protected Object expectation = null;

  /**
   * Creates a new search query that is operating on the given site.
   * 
   * @param site
   *          the site
   */
  public SearchQueryImpl(Site site) {
    this.site = site;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#withLimit(int)
   */
  public SearchQuery withLimit(int limit) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getLimit()
   */
  public int getLimit() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#withOffset(int)
   */
  public SearchQuery withOffset(int offset) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getOffset()
   */
  public int getOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#and(java.util.Date)
   */
  public SearchQuery and(Date date) {
    ensureExpectation(Date.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#andProperty(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery andProperty(String propertyName, String propertyValue)
      throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#andText(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery andText(String textName, String text)
      throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#atPosition(int)
   */
  public SearchQuery atPosition(int position) throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    ensureConfigurationObject(String.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getPosition()
   */
  public int getPosition() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#inComposer(java.lang.String)
   */
  public SearchQuery inComposer(String composer) throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getComposer()
   */
  public String getComposer() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withAuthor(ch.o2it.weblounge.common.user.User)
   */
  public SearchQuery withAuthor(User author) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public SearchQuery withLanguage(Language language) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getLanguage()
   */
  public Language getLanguage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withModificationDate(java.util.Date)
   */
  public SearchQuery withModificationDate(Date date) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withModificationDateBetween(java.util.Date)
   */
  public SearchQuery withModificationDateBetween(Date date) {
    clearExpectations();
    configure(date);
    expect(Date.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withPagelet(ch.o2it.weblounge.common.content.Pagelet)
   */
  public SearchQuery withPagelet(Pagelet pagelet) {
    clearExpectations();
    configure(pagelet);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withPathPrefix(java.lang.String)
   */
  public SearchQuery withPathPrefix(String path) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getPathPrefix()
   */
  public String getPathPrefix() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withPublisher(ch.o2it.weblounge.common.user.User)
   */
  public SearchQuery withPublisher(User publisher) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withPublishingDate(java.util.Date)
   */
  public SearchQuery withPublishingDate(Date date) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withPublishingDateBetween(java.util.Date)
   */
  public SearchQuery withPublishingDateBetween(Date date) {
    clearExpectations();
    configure(date);
    expect(Date.class);
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchQuery#withText(java.lang.String)
   */
  public SearchQuery withText(String text) {
    clearExpectations();
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getText()
   */
  public String getText() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#withProperty(java.lang.String, java.lang.String)
   */
  public SearchQuery withProperty(String property, String value) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchQuery#getProperties()
   */
  public Map<String, String> getProperties() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Pushes the configuration object onto the stack.
   * 
   * @param object
   *          the object
   */
  private void configure(Object object) {
    stack.push(object);
  }

  /**
   * Sets the expectation to <code>c</code>, making sure that the next
   * configuration object will either match <code>c</code> in terms of class of
   * throw an <code>IllegalStateException</code> if it doesn't.
   * 
   * @param c
   *          the class type
   */
  private void expect(Class<?> c) {
    this.expectation = c;
  }

  /**
   * This method is called if nothing should be expected by anyone. If this is
   * not the case (e. g. some unfinished query configuration is still in place)
   * we throw an <code>IllegalStateException</code>.
   * 
   * @throws IllegalStateException
   *           if some object is expected
   */
  private void clearExpectations() throws IllegalStateException {
    if (expectation != null)
      throw new IllegalStateException("Query configuration expects " + expectation.getClass().getName());
    stack.clear();
  }

  /**
   * This method is called if a certain type of object is expected by someone.
   * If this is not the case (e. g. query configuration is in good shape, then
   * someone tries to "finish" a configuration part) we throw an
   * <code>IllegalStateException</code>.
   * 
   * @throws IllegalStateException
   *           if no or a different object is expected
   */
  private void ensureExpectation(Class<?> c) throws IllegalStateException {
    if (expectation == null)
      throw new IllegalStateException("Malformed query configuration. No " + c.getClass().getName() + " is expected at this time");
    if (!expectation.getClass().getCanonicalName().equals(c.getCanonicalName()))
      throw new IllegalStateException("Malformed query configuration. Something of type " + c.getClass().getName() + " is expected at this time");
    expectation = null;
  }

  /**
   * Make sure that an object of type <code>c</code> is on the stack, throw an
   * <code>IllegalStateException</code> otherwise.
   * 
   * @throws IllegalStateException
   *           if no object of type <code>c</code> was found on the stack
   */
  private void ensureConfigurationObject(Class<?> c)
      throws IllegalStateException {
    for (Object o : stack) {
      if (o.getClass().getCanonicalName().equals(c.getCanonicalName()))
        return;
    }
    throw new IllegalStateException("Malformed query configuration. No " + c.getClass().getName() + " is expected at this time");
  }

}
