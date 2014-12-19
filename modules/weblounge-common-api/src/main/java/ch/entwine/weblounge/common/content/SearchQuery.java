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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * This interface defines a fluent api for a query object used to lookup
 * resources in the <code>ContentRepository</code>.
 */
public interface SearchQuery {

  /**
   * Sort order definitions.
   */
  public enum Order {
    None, Ascending, Descending
  }

  /** The search quantifier */
  public enum Quantifier {
    All, Any
  };

  /**
   * Returns the contextual site for this query.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Sets the number of results that are returned.
   * 
   * @param limit
   *          the number of results
   * @return the search query
   */
  SearchQuery withLimit(int limit);

  /**
   * Returns the number of results that are returned, starting at the offset
   * returned by <code>getOffset()</code>. If no limit was specified, this
   * method returns <code>-1</code>.
   * 
   * @return the maximum number of results
   */
  int getLimit();

  /**
   * Sets the starting offset. Search results will be returned starting at that
   * offset and until the limit is reached, as specified by
   * <code>getLimit()</code>.
   * 
   * @param offset
   *          the starting offset
   * @return the search query
   * @see
   */
  SearchQuery withOffset(int offset);

  /**
   * Returns the starting offset within the search result or <code>0</code> if
   * no offset was specified.
   * 
   * @return the offset
   */
  int getOffset();

  /**
   * Specifies the action that is going to applied to the search results. As a
   * result, this method will only return entries that allow the requesting user
   * the corresponding access to the resource.
   * <p>
   * Note that if this action is called multiple times, only items are being
   * returned that allow for <i>all<>/i> actions by the current user.
   * 
   * @param action
   *          the action to be applied
   * @return the search query
   */
  SearchQuery withAction(Action action);

  /**
   * Returns the actions that have been specified as requirements for the
   * individual items of the search result.
   * <p>
   * If no actions have been specified, an empty array is returned.
   * 
   * @return the list of required actions
   */
  Action[] getActions();

  /**
   * Enables boosting based on creation/modification date of resources so that
   * recent documents are ranked higher than old ones.
   * 
   * @return the search query
   */
  SearchQuery withRecencyPriority();

  /**
   * Returns whether priority boosting has been enabled.
   * 
   * @return <code>true</code> if recent documents are ranked higher
   */
  boolean getRecencyPriority();

  /**
   * Return the resources with the given identifier.
   * 
   * @param resourceId
   *          the identifier to look up
   * @return the query extended by this criterion
   */
  SearchQuery withIdentifier(String resourceId);

  /**
   * Returns the identifier or <code>null</code> if no identifier was specified.
   * 
   * @return the identifier
   */
  String[] getIdentifier();

  /**
   * Return the resources with the given path.
   * 
   * @param path
   *          the path to look up
   * @return the query extended by this criterion
   */
  SearchQuery withPath(String path);

  /**
   * Returns the path or <code>null</code> if no path was specified.
   * 
   * @return the path
   */
  String getPath();

  /**
   * Return the resources with the given template.
   * 
   * @param template
   *          the template to look up
   * @return the query extended by this criterion
   */
  SearchQuery withTemplate(String template);

  /**
   * Returns the template or <code>null</code> if no template was specified.
   * 
   * @return the template
   */
  String getTemplate();

  /**
   * Return resources which are marked as stationaries.
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withStationary();

  /**
   * Returns <code>true</code> if stationary resources should be included.
   * 
   * @return <code>true</code> to include stationaries
   */
  boolean isStationary();

  /**
   * Return the resources with the given origin.
   * 
   * @param origin
   *          the origin to look up
   * @return the query extended by this criterion
   */
  SearchQuery withOrigin(String origin);

  /**
   * Return the resources with the given original identifier.
   * 
   * @param originalIdentifier
   *          the original identifier to look up
   * @return the query extended by this criterion
   */
  SearchQuery withOriginalIdentifier(String originalIdentifier);

  /**
   * Returns the origin or <code>null</code> if no origin was specified.
   * 
   * @return the origin
   */
  String getOrigin();

  /**
   * Returns the original identifier or <code>null</code> if no original identifier was specified.
   * 
   * @return the original identifier
   */
  String getOriginalIdentifier();

  /**
   * Return the resources which have an external representation at the given
   * url.
   * 
   * @param source
   *          the source to look up
   * @return the query extended by this criterion
   */
  SearchQuery withExternalLocation(URL url);

  /**
   * Returns the source or <code>null</code> if no external location was
   * specified.
   * 
   * @return the source
   */
  URL getExternalLocation();

  /**
   * Return the resources with the given layout.
   * 
   * @param layout
   *          the layout to look up
   * @return the query extended by this criterion
   */
  SearchQuery withLayout(String layout);

  /**
   * Returns the layout or <code>null</code> if no layout was specified.
   * 
   * @return the layout
   */
  String getLayout();

  /**
   * Return the resources with the given flavor.
   *
   * @param flavor
   *          the flavor to look up
   * @return the query extended by this criterion
   */
  SearchQuery withFlavor(String flavor);

  /**
   * Returns the flavor or {@code null} if no flavor was specified.
   *
   * @return the flavor
   */
  String getFlavor();

  /**
   * Return the resources with the given types.
   * 
   * @param types
   *          the resource types to look up
   * @return the query extended by this criterion
   */
  SearchQuery withTypes(String... types);

  /**
   * Returns the resources except the ones with the given types.
   * 
   * @param types
   *          the resource types to block
   * @return the query extended by this criterion
   */
  SearchQuery withoutTypes(String... types);

  /**
   * Returns the resource types or or an empty array if no types was specified.
   * 
   * @return the type
   */
  String[] getTypes();

  /**
   * Returns the blocked resource types or or an empty array if no types was
   * specified.
   * 
   * @return the type
   */
  String[] getWithoutTypes();

  /**
   * Return resources that contain the given text either in the page header or
   * in one of the pagelets.
   * 
   * @param text
   *          the text to look up
   * @return the query extended by this criterion
   */
  SearchQuery withText(String text);

  /**
   * Return resources that contain the given text either in the page header or
   * in one of the pagelets.
   * 
   * @param wildcardSearch
   *          <code>True</code> to perform a (much slower) wildcard search
   * @param text
   *          the text to look up
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withText(boolean wildcardSearch, String text);

  /**
   * Return resources that contain the given text either in the page header or
   * in one of the pagelets.
   * <p>
   * Depending on the quantifier, either resources are returned that contain at
   * least one of the terms are only resources containing all of the terms.
   * 
   * @param text
   *          the text to look up
   * @param quantifier
   *          whether all or some of the terms need to be matched
   * @param fuzzy
   *          <code>true</code> to perform a fuzzy search
   * @return the query extended by this criterion
   */
  SearchQuery withText(boolean fuzzy, Quantifier quantifier, String... text);

  /**
   * Returns the search terms or an empty collection if no terms were specified.
   * 
   * @return the terms
   */
  Collection<SearchTerms<String>> getTerms();

  /**
   * Returns the search text or <code>null</code> if no text was specified.
   * 
   * @return the text
   */
  String getQueryString();

  /**
   * Return resources that contain the given text in the fulltext search field.
   * <p>
   * Note that this search field is not intended to serve frontend applications
   * but rather backend purposes.
   * 
   * @param text
   *          the text to look up
   * @return the query extended by this criterion
   */
  SearchQuery withFulltext(String text);

  /**
   * Return resources that contain the given text in the fulltext search field.
   * <p>
   * Note that this search field is not intended to serve frontend applications
   * but rather backend purposes.
   * 
   * @param fuzzy
   *          <code>true</code> to perform a fuzzy search
   * @param text
   *          the text to look up
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withFulltext(boolean fuzzy, String text);

  /**
   * Return resources that contain the given text in the fulltext search field.
   * <p>
   * Note that this search field is not intended to serve frontend applications
   * but rather backend purposes.
   * 
   * @param fuzzy
   *          <code>true</code> to perform a fuzzy search
   * @param quantifier
   *          whether documents need to match all or just one of the text
   *          elements
   * @param text
   *          the text to look up
   * @return the query extended by this criterion
   */
  SearchQuery withFulltext(boolean fuzzy, Quantifier quantifier, String... text);

  /**
   * Returns the fulltext search terms or <code>null</code> if no text was
   * specified.
   * 
   * @return the text
   */
  Collection<SearchTerms<String>> getFulltext();

  /**
   * Returns <code>true</code> if the current search operation should be
   * performed using fuzzy searching.
   * 
   * @return <code>true</code> if fzzy search should be used
   */
  boolean isFuzzySearch();

  /**
   * Returns resources that match the search query <i>and</i> and the text
   * filter.
   * 
   * @param filter
   *          the filter text
   * @return the search query
   */
  SearchQuery withFilter(String filter);

  /**
   * Returns the filter expression.
   * 
   * @return the filter
   */
  String getFilter();

  /**
   * Specifies an element within a pagelet.
   * 
   * @param element
   *          the element name
   * @param value
   *          the element value
   * @return the search query
   */
  SearchQuery withElement(String element, String value);

  /**
   * Returns the elements text or <code>null</code> if no elements were
   * specified.
   * 
   * @return the text
   */
  Map<String, String> getElements();

  /**
   * Sets the properties and their values to search for.
   * 
   * @param property
   *          the property name
   * @param value
   *          the property value
   * @return the search query
   */
  SearchQuery withProperty(String property, String value);

  /**
   * Returns the properties and their values or <code>null</code> if no
   * properties were specified.
   * 
   * @return the properties
   */
  Map<String, String> getProperties();

  /**
   * Return only resources with hits in the specified language.
   * 
   * @param language
   *          the language
   * @return the query extended by this criterion
   */
  SearchQuery withLanguage(Language language);

  /**
   * Returns the language or <code>null</code> if no language was specified.
   * 
   * @return the language
   */
  Language getLanguage();

  /**
   * Only returns resources that contain the specified subject.
   * 
   * @param subject
   *          the subject
   * @return the query extended by this criterion
   */
  SearchQuery withSubject(String subject);

  /**
   * Returns only resources that match the given subjects. Depending on
   * <code>quantifier</code>, either all or some of the terms need to be
   * present.
   * 
   * @param quantifier
   *          the quantifier
   * @param subjects
   *          the list of subjects
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withSubjects(Quantifier quantifier, String... subjects);

  /**
   * Returns the subjects as a collection of search terms or <code>null</code>
   * if no subjects have been specified.
   * 
   * @return the subjects
   */
  Collection<SearchTerms<String>> getSubjects();

  /**
   * Only returns resources that contain the specified series. Note that this
   * method may be called multiple times in order to specify more than one
   * series.
   * 
   * @param series
   *          the series
   * @return the query extended by this criterion
   */
  SearchQuery withSeries(String series);

  /**
   * Returns the series or an empty array if no series have been specified.
   * 
   * @return the series
   */
  String[] getSeries();

  /**
   * Return only resources that have been created or modified by the specified
   * author.
   * 
   * @param author
   *          the author
   * @return the query extended by this criterion
   */
  SearchQuery withAuthor(User author);

  /**
   * Returns the author or <code>null</code> if no author has been specified.
   * 
   * @return the author
   */
  User getAuthor();

  /**
   * Return only resources that have been created by the specified user.
   * 
   * @param creator
   *          the creator
   * @return the query extended by this criterion
   */
  SearchQuery withCreator(User creator);

  /**
   * Returns the creator or <code>null</code> if no creator has been specified.
   * 
   * @return the creator
   */
  User getCreator();

  /**
   * Return only resources that have been modified by the specified user.
   * 
   * @param modifier
   *          the modifier
   * @return the query extended by this criterion
   */
  SearchQuery withModifier(User modifier);

  /**
   * Returns the modifier or <code>null</code> if no modifier has been
   * specified.
   * 
   * @return the modifier
   */
  User getModifier();

  /**
   * Return resources that have never been published.
   * <p>
   * Note that this method throws an <code>IllegalStateException</code> if used
   * in conjunction with {@link #withPublishingDate(Date)},
   * {@link #withPublishingDateBetween(Date)} or {@link #and(Date)}.
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withoutPublication();

  /**
   * Returns <code>true</code> if resources must not have a publishing date.
   * 
   * @return <code>true</code> if resources need to be unpublished
   */
  boolean getWithoutPublication();

  /**
   * Return only resources that have been published by the specified publisher.
   * 
   * @param publisher
   *          the publisher
   * @return the query extended by this criterion
   */
  SearchQuery withPublisher(User publisher);

  /**
   * Returns the publisher or <code>null</code> if no publisher has been
   * specified.
   * 
   * @return the publisher
   */
  User getPublisher();

  /**
   * Return resources that have been published on the given date.
   * <p>
   * Note that this method throws an <code>IllegalStateException</code> if used
   * in conjunction with {@link #withPublishingDateBetween(Date)} or
   * {@link #and(Date)}.
   * 
   * @param date
   *          the publishing date
   * @return the query extended by this criterion
   */
  SearchQuery withPublishingDate(Date date);

  /**
   * Return resources with a publishing date of <code>date</code> or later.
   * <p>
   * Note that this method cannot be used without a subsequent call to
   * {@link #and(Date)} in order to specify the end date.
   * 
   * @param date
   *          the publishing start date
   * @return the query extended by this criterion
   */
  SearchQuery withPublishingDateBetween(Date date);

  /**
   * Returns the publishing date or <code>null</code> if no publishing date has
   * been specified.
   * 
   * @return the publishing date
   */
  Date getPublishingDate();

  /**
   * Returns the end of the range for the publishing date or <code>null</code>
   * if no end date has been specified.
   * 
   * @return the publishing end date
   */
  Date getPublishingDateEnd();

  /**
   * Return resources that have never been modified.
   * <p>
   * Note that this method throws an <code>IllegalStateException</code> if used
   * in conjunction with {@link #withModificationDate(Date)},
   * {@link #withModificationDateBetween(Date)} or {@link #and(Date)}.
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withoutModification();

  /**
   * Returns <code>true</code> if resources must not have a modification date.
   * 
   * @return <code>true</code> if resources need to be unmodified
   */
  boolean getWithoutModification();

  /**
   * Return resources that have been modified on the given date.
   * <p>
   * Note that this method throws an <code>IllegalStateException</code> if used
   * in conjunction with {@link #withModificationDateBetween(Date)} or
   * {@link #and(Date)}.
   * 
   * @param date
   *          the modification date
   * @return the query extended by this criterion
   */
  SearchQuery withModificationDate(Date date);

  /**
   * Return resources with a modification date of <code>date</code> or later.
   * <p>
   * Note that this method cannot be used without a subsequent call to
   * {@link #and(Date)} in order to specify the end date.
   * 
   * @param date
   *          the modification start date
   * @return the query extended by this criterion
   */
  SearchQuery withModificationDateBetween(Date date);

  /**
   * Returns the modification date or <code>null</code> if no modification date
   * has been specified.
   * 
   * @return the modification date
   */
  Date getModificationDate();

  /**
   * Returns the end of the range for the modification date or <code>null</code>
   * if no end date has been specified.
   * 
   * @return the modification end date
   */
  Date getModificationDateEnd();

  /**
   * Return resources that have been created on the given date.
   * <p>
   * Note that this method throws an <code>IllegalStateException</code> if used
   * in conjunction with {@link #withCreationDateBetween(Date)} or
   * {@link #and(Date)}.
   * 
   * @param date
   *          the Creation date
   * @return the query extended by this criterion
   */
  SearchQuery withCreationDate(Date date);

  /**
   * Return resources with a Creation date of <code>date</code> or later.
   * <p>
   * Note that this method cannot be used without a subsequent call to
   * {@link #and(Date)} in order to specify the end date.
   * 
   * @param date
   *          the Creation start date
   * @return the query extended by this criterion
   */
  SearchQuery withCreationDateBetween(Date date);

  /**
   * Returns the creation date or <code>null</code> if no creation date has been
   * specified.
   * 
   * @return the creation date
   */
  Date getCreationDate();

  /**
   * Returns the end of the range for the creation date or <code>null</code> if
   * no end date has been specified.
   * 
   * @return the creation end date
   */
  Date getCreationDateEnd();

  /**
   * This method is used to specify the end of a date range, started by a call
   * to either one of {@link #withPublishingDateBetween(Date)},
   * {@link #withModificationDateBetween(Date)} or
   * {@link #withCreationDateBetween(Date)}.
   * 
   * @param date
   *          the end date
   * @return the query extended by this criterion
   */
  SearchQuery and(Date date);

  /**
   * Return only resources that have been locked by the specified user.
   * 
   * @param lockOwner
   *          the user holding the lock
   * @return the query extended by this criterion
   */
  SearchQuery withLockOwner(User lockOwner);

  /**
   * Return only resources that have been locked by somebody.
   * 
   * @return the query extended by this criterion
   */
  SearchQuery withLockOwner();

  /**
   * Returns the lock owner or <code>null</code> if no lock owner has been
   * specified.
   * 
   * @return the lock owner
   */
  User getLockOwner();

  /**
   * Only return resources that are located on or below the given path in the
   * page tree.
   * 
   * @param path
   *          the path in the site tree
   * @return the query extended by this criterion
   */
  SearchQuery withPathPrefix(String path);

  /**
   * Returns the path prefix.
   * 
   * @return the prefix
   */
  String getPathPrefix();

  /**
   * Return resources that contain the specified pagelet.
   * <p>
   * Note that you can specify the location where the pagelet needs to be as
   * additional elements or properties by a subsequent call to
   * {@link #inComposer(String)} {@link #atPosition(int)},
   * {@link #andElement(String, String)} and
   * {@link #andProperty(String, String)}.
   * 
   * @param pagelet
   *          the pagelet
   * @return the query extended by this criterion
   */
  SearchQuery withPagelet(Pagelet pagelet);

  /**
   * Return resources that contain all or any of the specified pagelets.
   * <p>
   * Note that you can specify the location where the pagelet needs to be as
   * additional elements or properties by a subsequent call to
   * {@link #inComposer(String)} {@link #atPosition(int)},
   * {@link #andElement(String, String)} and
   * {@link #andProperty(String, String)}.
   * 
   * @param pagelets
   *          the pagelets
   * @return the query extended by this criterion
   */
  SearchQuery withPagelets(Quantifier quantifier, Pagelet... pagelets);

  /**
   * Returns the list of required pagelets, along with their elements,
   * properties and location information as a collection of search terms or
   * <code>null</code> if not pagelets have been specified.
   * 
   * @return the pagelets
   */
  Collection<SearchTerms<Pagelet>> getPagelets();

  /**
   * This method may be called after a call to {@link #withPagelet(Pagelet)} in
   * order to specify the composer that the pagelet needs to be in.
   * 
   * @param composer
   *          the composer name
   * @return the query extended by this criterion
   * @throws IllegalStateException
   *           if no pagelet has been specified before
   */
  SearchQuery inComposer(String composer) throws IllegalStateException;

  /**
   * This method may be called after a call to {@link #withPagelet(Pagelet)} in
   * order to specify that the pagelet needs to be in the stage composer.
   * 
   * @return the query extended by this criterion
   * @throws IllegalStateException
   *           if no pagelet has been specified before
   */
  SearchQuery inStage() throws IllegalStateException;

  /**
   * This method may be called after a call to {@link #inComposer(String)} in
   * order to specify the pagelet's position within that composer.
   * 
   * @param position
   *          the pagelet position within the composer
   * @return the query extended by this criterion
   * @throws IllegalStateException
   *           if no composer has been specified before
   */
  SearchQuery atPosition(int position) throws IllegalStateException;

  /**
   * Only return resources that contain a certain pagelet (see
   * {@link #withPagelet(Pagelet)} that features the given property with the
   * indicated value.
   * <p>
   * Note that the property value needs to match exactly. Also, you need to
   * specify the pagelet right before calling this method, otherwise an
   * <code>IllegalStateException</code> will be thrown:
   * 
   * <pre>
   * SearchQuery q = new SearchQuery();
   * q.withPagelet(p).andProperty(&quot;hello&quot;, &quot;world&quot;);
   * </pre>
   * 
   * @param propertyName
   *          name of the property
   * @param propertyValue
   *          property value
   * @return the query extended by this criterion
   * @throws IllegalStateException
   *           if the pagelet has not been specified before
   */
  SearchQuery andProperty(String propertyName, String propertyValue)
      throws IllegalStateException;

  /**
   * Only return resources that contain a certain pagelet (see
   * {@link #withPagelet(Pagelet)} that features the given text with the
   * indicated value in any language.
   * <p>
   * Note that partial matches are considered a hit as well. Also, you need to
   * specify the pagelet right before calling this method, otherwise an
   * <code>IllegalStateException</code> will be thrown:
   * 
   * <pre>
   * SearchQuery q = new SearchQuery();
   * q.withPagelet(p).andProperty(&quot;hello&quot;, &quot;world&quot;);
   * </pre>
   * 
   * @param textName
   *          name of the text field
   * @param text
   *          the actual text
   * @return the query extended by this criterion
   * @throws IllegalStateException
   *           if the pagelet has not been specified before
   */
  SearchQuery andElement(String textName, String text)
      throws IllegalStateException;

  /**
   * Return the resources with the given filename in their content section.
   * 
   * @param filename
   *          the filename to look up
   * @return the query extended by this criterion
   */
  SearchQuery withFilename(String filename);

  /**
   * Returns the filename or <code>null</code> if no filename was specified.
   * 
   * @return the filename
   */
  String getFilename();

  /**
   * Return the resources with the given mime type in their content section.
   * 
   * @param mimetype
   *          the mime type to look up
   * @return the query extended by this criterion
   */
  SearchQuery withMimetype(String mimetype);

  /**
   * Returns the mime type or <code>null</code> if no mime type was specified.
   * 
   * @return the mime type
   */
  String getMimetype();

  /**
   * Asks the search index to order the results by creation date rather than by
   * relevance.
   * 
   * @param order
   *          the sort order
   * @return the search query
   */
  SearchQuery sortByCreationDate(Order order);

  /**
   * Returns the sort order for the date if specified. If this field is not to
   * be sorted by creation date, {@link Order#None} is returned.
   * 
   * @return the sort order
   */
  Order getCreationDateSortOrder();

  /**
   * Asks the search index to order the results by modification date rather than
   * by relevance.
   * 
   * @param order
   *          the sort order
   * @return the search query
   */
  SearchQuery sortByModificationDate(Order order);

  /**
   * Returns the sort order for the date if specified. If this field is not to
   * be sorted by modification date, {@link Order#None} is returned.
   * 
   * @return the sort order
   */
  Order getModificationDateSortOrder();

  /**
   * Asks the search index to order the results by publishing date rather than
   * by relevance.
   * 
   * @param order
   *          the sort order
   * @return the search query
   */
  SearchQuery sortByPublishingDate(Order order);

  /**
   * Returns the sort order for the date if specified. If this field is not to
   * be sorted by publishing date, {@link Order#None} is returned.
   * 
   * @return the sort order
   */
  Order getPublishingDateSortOrder();

  /**
   * Asks the search index to return only resources with the indicated version.
   * 
   * @param version
   *          the version
   * @return the search query
   */
  SearchQuery withVersion(long version);

  /**
   * Asks the search index to return only resources with the indicated version
   * if available else return the available version.
   * 
   * @param preferredVersion
   *          the preferredVersion
   * @return the search query
   */
  SearchQuery withPreferredVersion(long preferredVersion);

  /**
   * Returns the resource version.
   * 
   * @return the version
   */
  long getVersion();

  /**
   * Return the preferred resource version.
   * 
   * @return the preferred version
   */
  long getPreferredVersion();

  /**
   * Returns the fields that should be returned by the query. If all fields
   * should be returned, the method will return an empty array.
   * 
   * @return the names of the fields to return
   */
  String[] getFields();

  /**
   * Adds a field that needs to be returned by the query. If no fields are being
   * set, all fields will be returned.
   * 
   * @param field
   *          the field name
   * @return the query
   */
  SearchQuery withField(String field);

  /**
   * Adds the fields that need to be returned by the query. If no fields are
   * being set, all fields will be returned.
   * 
   * @param fields
   *          the field names
   * @return the query
   */
  SearchQuery withFields(String... fields);

}
