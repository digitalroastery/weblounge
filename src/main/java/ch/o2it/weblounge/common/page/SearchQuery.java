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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * This interface defines a fluent api for a query object used to lookup pages
 * in the {@link PageRepository}.
 */
public interface SearchQuery {

  /**
   * Return pages that contain the given text.
   * 
   * @param text
   *          the text to look up
   * @return the query extended by this criterion
   */
  SearchQuery withText(String text);

  /**
   * Return only pages with hits in the specified language.
   * 
   * @param language
   *          the language
   * @return the query extended by this criterion
   */
  SearchQuery withLanguage(Language language);

  /**
   * Return only pages that have been created or modified by the specified
   * author.
   * 
   * @param author
   *          the author
   * @return the query extended by this criterion
   */
  SearchQuery withAuthor(User author);

  /**
   * Return only pages that have been published by the specified publisher.
   * 
   * @param publisher
   *          the publisher
   * @return the query extended by this criterion
   */
  SearchQuery withPublisher(User publisher);

  /**
   * Return pages that have been published on the given date.
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
   * Return pages with a publishing date of <code>date</code> or later.
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
   * Return pages that have been modified on the given date.
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
   * Return pages with a modification date of <code>date</code> or later.
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
   * This method is used to specify the end of a date range, started by a call
   * to either one of {@link #withPublishingDateBetween(Date)} or
   * {@link #withModificationDateBetween(Date)}.
   * 
   * @param date
   *          the end date
   * @return the query extended by this criterion
   */
  SearchQuery and(Date date);

  /**
   * Only return pages that are located on or below the given path in the page
   * tree.
   * 
   * @param path
   *          the path in the site tree
   * @return the query extended by this criterion
   */
  SearchQuery withPathPrefix(String path);

  /**
   * Return pages that contain the specified pagelet.
   * <p>
   * Note that you can specify the location where the pagelet needs to be by a
   * subsequent call to {@link #inComposer(String)} and {@link #atPosition(int)}.
   * 
   * @param pagelet
   *          the pagelet
   * @return the query extended by this criterion
   */
  SearchQuery withPagelet(Pagelet pagelet);

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
   * Only return pages that contain a certain pagelet (see
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
   * Only return pages that contain a certain pagelet (see
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
  SearchQuery andText(String textName, String text)
      throws IllegalStateException;

}
