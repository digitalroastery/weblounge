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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletURIImpl;
import ch.entwine.weblounge.common.impl.url.UrlUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Default implementation for the search query api.
 */
public class SearchQueryImpl implements SearchQuery {

  /** Name of the stage composer */
  public static final String STAGE_COMPOSER = "#stage#";
  
  /** The site */
  protected Site site = null;

  /** The search language */
  protected Language language = null;

  /** Query configuration stack */
  protected Stack<Object> stack = new Stack<Object>();

  /** The object that needs to show up next */
  protected Class<?> expectation = null;

  /** The uuid */
  protected String id = null;

  /** The path */
  protected String path = null;

  /** The type */
  protected String type = null;

  /** The template */
  protected String template = null;

  /** The layout */
  protected String layout = null;

  /** The list of required pagelets */
  protected List<Pagelet> pagelets = new ArrayList<Pagelet>();

  /** The list of required subjects */
  protected List<String> subjects = new ArrayList<String>();

  /** The properties */
  protected Map<String, String> properties = new HashMap<String, String>();

  /** The elements */
  protected Map<String, String> elements = new HashMap<String, String>();

  /** The last method called */
  protected String lastMethod = null;

  /** The creation date */
  protected Date creationDateFrom = null;

  /** The end of the range for the creation date */
  protected Date creationDateTo = null;

  /** The modification date */
  protected Date modificationDateFrom = null;

  /** The end of the range for the modification date */
  protected Date modificationDateTo = null;

  /** The publishing date */
  protected Date publishingDateFrom = null;

  /** The end of the range for the publishing date */
  protected Date publishingDateTo = null;

  /** The author */
  protected User author = null;

  /** The creator */
  protected User creator = null;

  /** The modifier */
  protected User modifier = null;

  /** The publisher */
  protected User publisher = null;

  /** The path prefix */
  protected String pathPrefix = null;
  
  /** The filename */
  protected String filename = null;

  /** The mime type */
  protected String mimetype = null;

  /** Query terms */
  protected String text = null;

  /** The query offset */
  protected int offset = 0;

  /** The query limit */
  protected int limit = -1;
  
  /** True when using faceted search */
  protected boolean subjectFacetEnabled = false;
  
  /** Creation date order relation */
  protected Order creationDateSearchOrder = Order.None;

  /** Modification date order relation */
  protected Order modificationDateSearchOrder = Order.None;

  /** Publication date order relation */
  protected Order publicationDateSearchOrder = Order.None;

  /**
   * Creates a new search query that is operating on the given site.
   * 
   * @param site
   *          the site
   */
  public SearchQueryImpl(Site site) {
    this(site, site.getDefaultLanguage());
  }

  /**
   * Creates a new search query that is operating on the given site.
   * 
   * @param site
   *          the site
   * @param language
   *          the search language
   */
  public SearchQueryImpl(Site site, Language language) {
    this.site = site;
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withLimit(int)
   */
  public SearchQuery withLimit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getLimit()
   */
  public int getLimit() {
    return limit;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withOffset(int)
   */
  public SearchQuery withOffset(int offset) {
    this.offset = Math.max(0, offset);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getOffset()
   */
  public int getOffset() {
    return offset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withIdentifier(java.lang.String)
   */
  public SearchQuery withIdentifier(String id) {
    if (id == null)
      throw new IllegalArgumentException("Id cannot be null");
    this.id = id;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getIdentifier()
   */
  public String getIdentifier() {
    return id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPath(java.lang.String)
   */
  public SearchQuery withPath(String path) {
    if (path == null)
      throw new IllegalArgumentException("Path cannot be null");
    this.path = UrlUtils.trim(path);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPath()
   */
  public String getPath() {
    return path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withTemplate(java.lang.String)
   */
  public SearchQuery withTemplate(String template) {
    this.template = template;
    return this;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getTemplate()
   */
  public String getTemplate() {
    return template;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#withLayout(java.lang.String)
   */
  public SearchQuery withLayout(String layout) {
    this.layout = layout;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getLayout()
   */
  public String getLayout() {
    return layout;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#withType(java.lang.String)
   */
  public SearchQuery withType(String type) {
    this.type = type;
    return this;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#and(java.util.Date)
   */
  public SearchQuery and(Date date) {
    ensureExpectation(Date.class);
    Date startDate = (Date) stack.peek();
    if (startDate.equals(date) || startDate.after(date))
      throw new IllegalStateException("End date must be after start date");
    if ("withCreationDateBetween".equals(lastMethod))
      creationDateTo = date;
    else if ("withModificationDateBetween".equals(lastMethod))
      modificationDateTo = date;
    else if ("withPublishingDateBetween".equals(lastMethod))
      publishingDateTo = date;
    clearExpectations();
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#andProperty(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery andProperty(String propertyName, String propertyValue)
      throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    Pagelet pagelet = (Pagelet) stack.peek();
    pagelet.setProperty(propertyName, propertyValue);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#andElement(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery andElement(String textName, String text)
      throws IllegalStateException {
    if (language == null)
      throw new IllegalStateException("You need to specify the query language first");
    ensureConfigurationObject(Pagelet.class);
    Pagelet pagelet = (Pagelet) stack.peek();
    pagelet.setContent(textName, text, language);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#atPosition(int)
   */
  public SearchQuery atPosition(int position) throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    Pagelet pagelet = (Pagelet) stack.peek();
    PageletURI uri = pagelet.getURI();
    if (uri == null) {
      PageTemplate template = site.getDefaultTemplate();
      String stage = null;
      if (template != null)
        stage = template.getStage();
      uri = new PageletURIImpl(null, stage, position);
    } else {
      uri.setPosition(position);
    }
    pagelet.setURI(uri);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#inComposer(java.lang.String)
   */
  public SearchQuery inComposer(String composer) throws IllegalStateException {
    ensureConfigurationObject(Pagelet.class);
    Pagelet pagelet = (Pagelet) stack.peek();
    PageletURI uri = pagelet.getURI();
    if (uri == null) {
      uri = new PageletURIImpl(null, composer, -1);
    } else {
      uri.setComposer(composer);
    }
    pagelet.setURI(uri);
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#inStage()
   */
  public SearchQuery inStage() throws IllegalStateException {
    return inComposer(STAGE_COMPOSER);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withAuthor(ch.entwine.weblounge.common.security.User)
   */
  public SearchQuery withAuthor(User author) {
    clearExpectations();
    this.author = author;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getAuthor()
   */
  public User getAuthor() {
    return author;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public SearchQuery withLanguage(Language language) {
    clearExpectations();
    this.language = language;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withSubject(java.lang.String)
   */
  public SearchQuery withSubject(String subject) {
    subjects.add(subject);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getSubjects()
   */
  public String[] getSubjects() {
    return subjects.toArray(new String[subjects.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withCreationDate(java.util.Date)
   */
  public SearchQuery withCreationDate(Date date) {
    clearExpectations();
    creationDateFrom = date;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withCreationDateBetween(java.util.Date)
   */
  public SearchQuery withCreationDateBetween(Date date) {
    clearExpectations();
    configure(date);
    creationDateFrom = date;
    expect(Date.class);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getCreationDate()
   */
  public Date getCreationDate() {
    return creationDateFrom;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getCreationDateEnd()
   */
  public Date getCreationDateEnd() {
    return creationDateTo;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withModificationDate(java.util.Date)
   */
  public SearchQuery withModificationDate(Date date) {
    clearExpectations();
    modificationDateFrom = date;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withModificationDateBetween(java.util.Date)
   */
  public SearchQuery withModificationDateBetween(Date date) {
    clearExpectations();
    configure(date);
    modificationDateFrom = date;
    expect(Date.class);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getModificationDate()
   */
  public Date getModificationDate() {
    return modificationDateFrom;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getModificationDateEnd()
   */
  public Date getModificationDateEnd() {
    return modificationDateTo;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPagelet(ch.entwine.weblounge.common.content.page.Pagelet)
   */
  public SearchQuery withPagelet(String module, String id) {
    clearExpectations();
    if (module == null)
      throw new IllegalArgumentException("Module identifier must not be null");
    if (id == null)
      throw new IllegalArgumentException("Pagelet identifier must not be null");
    Pagelet pagelet = new PageletImpl(module, id);
    pagelets.add(pagelet);
    configure(pagelet);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPagelets()
   */
  public Pagelet[] getPagelets() {
    return pagelets.toArray(new Pagelet[pagelets.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPathPrefix(java.lang.String)
   */
  public SearchQuery withPathPrefix(String path) {
    clearExpectations();
    this.pathPrefix = path;
    if (pathPrefix == null)
      throw new IllegalArgumentException("Path prefix must not be null");
    if (!pathPrefix.startsWith(WebUrl.separator))
      pathPrefix = WebUrl.separatorChar + pathPrefix;
    if (!pathPrefix.endsWith(WebUrl.separator))
      pathPrefix = pathPrefix + WebUrl.separatorChar;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPathPrefix()
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withCreator(ch.entwine.weblounge.common.security.User)
   */
  public SearchQuery withCreator(User creator) {
    clearExpectations();
    this.creator = creator;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getCreator()
   */
  public User getCreator() {
    return creator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withModifier(ch.entwine.weblounge.common.security.User)
   */
  public SearchQuery withModifier(User modifier) {
    clearExpectations();
    this.modifier = modifier;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getModifier()
   */
  public User getModifier() {
    return modifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPublisher(ch.entwine.weblounge.common.security.User)
   */
  public SearchQuery withPublisher(User publisher) {
    clearExpectations();
    this.publisher = publisher;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPublisher()
   */
  public User getPublisher() {
    return publisher;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPublishingDate(java.util.Date)
   */
  public SearchQuery withPublishingDate(Date date) {
    clearExpectations();
    this.publishingDateFrom = date;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withPublishingDateBetween(java.util.Date)
   */
  public SearchQuery withPublishingDateBetween(Date date) {
    clearExpectations();
    configure(date);
    this.publishingDateFrom = date;
    expect(Date.class);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPublishingDate()
   */
  public Date getPublishingDate() {
    return publishingDateFrom;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPublishingDateEnd()
   */
  public Date getPublishingDateEnd() {
    return publishingDateTo;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withText(java.lang.String)
   */
  public SearchQuery withText(String text) {
    clearExpectations();
    this.text = text;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getText()
   */
  public String getText() {
    return text;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withElement(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery withElement(String element, String value) {
    elements.put(element, value);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getElements()
   */
  public Map<String, String> getElements() {
    return elements;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#withProperty(java.lang.String,
   *      java.lang.String)
   */
  public SearchQuery withProperty(String property, String value) {
    properties.put(property, value);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchQuery#getProperties()
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#withFilename(java.lang.String)
   */
  public SearchQuery withFilename(String filename) {
    this.filename = filename;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getFilename()
   */
  public String getFilename() {
    return filename;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#withMimetype(java.lang.String)
   */
  public SearchQuery withMimetype(String mimetype) {
    this.mimetype = mimetype;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getMimetype()
   */
  public String getMimetype() {
    return mimetype;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#sortByCreationDate(ch.entwine.weblounge.common.content.SearchQuery.Order)
   */
  public SearchQuery sortByCreationDate(Order order) {
    if (order == null)
      creationDateSearchOrder = Order.None;
    else
      creationDateSearchOrder = order;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getCreationDateSortOrder()
   */
  public Order getCreationDateSortOrder() {
    return creationDateSearchOrder;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#sortByModificationDate(ch.entwine.weblounge.common.content.SearchQuery.Order)
   */
  public SearchQuery sortByModificationDate(Order order) {
    if (order == null)
      modificationDateSearchOrder = Order.None;
    else
      modificationDateSearchOrder = order;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getModificationDateSortOrder()
   */
  public Order getModificationDateSortOrder() {
    return modificationDateSearchOrder;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#sortByPublishingDate(ch.entwine.weblounge.common.content.SearchQuery.Order)
   */
  public SearchQuery sortByPublishingDate(Order order) {
    if (order == null)
      publicationDateSearchOrder = Order.None;
    else
      publicationDateSearchOrder = order;
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#getPublishingDateSortOrder()
   */
  public Order getPublishingDateSortOrder() {
    return publicationDateSearchOrder;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#withSubjectFacet()
   */
  public SearchQuery withSubjectFacet() {
    subjectFacetEnabled = true;
    return this;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.SearchQuery#isSubjectFacetEnabled()
   */
  public boolean isSubjectFacetEnabled() {
    return subjectFacetEnabled;
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
    lastMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
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
    if (!expectation.getCanonicalName().equals(c.getCanonicalName()))
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
      if (c.isAssignableFrom(o.getClass()))
        return;
    }
    throw new IllegalStateException("Malformed query configuration. No " + c.getClass().getName() + " is expected at this time");
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "*:" + (text != null ? text : "*");
  }

}
