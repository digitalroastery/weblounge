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

package ch.o2it.weblounge.contentrepository.impl.index.solr;

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_PROPERTIES;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TEXT;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_FROM;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_TO;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.user.User;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr.
 */
public class PageInputDocument extends SolrUpdateableInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /** The solr supported date format. **/
  private DateFormat dateFormat = new SimpleDateFormat(SolrFields.SOLR_DATE_FORMAT);

  /**
   * Creates an input document for the given page.
   * 
   * @param page
   *          the page
   */
  public PageInputDocument(Page page) {
    init(page);
  }

  /**
   * Populates this input document with the page data.
   * 
   * @param page
   *          the page
   */
  private void init(Page page) {
    setField(ID, page.getURI().getId());
    setField(PATH, page.getURI().getPath());

    // Creation, modification and publishing information
    setField(CREATED, serializeDate(page.getCreationDate()));
    setField(CREATED_BY, serializeUser(page.getCreator()));
    setField(MODIFIED, serializeDate(page.getModificationDate()));
    setField(MODIFIED_BY, serializeUser(page.getModifier()));
    setField(PUBLISHED_FROM, serializeDate(page.getPublishFrom()));
    setField(PUBLISHED_TO, serializeDate(page.getPublishTo()));
    setField(PUBLISHED_BY, serializeUser(page.getPublisher()));

    // Language dependent fields
    for (Language l : page.languages()) {
      setField(getLocalizedFieldName(DESCRIPTION, l), page.getDescription(l, true));
      setField(getLocalizedFieldName(COVERAGE, l), page.getCoverage(l, true));
      setField(getLocalizedFieldName(RIGHTS, l), page.getRights(l, true));
      setField(getLocalizedFieldName(TITLE, l), page.getTitle(l, true));
    }

    // Pagelet elements and properties
    int i = 0;
    for (Pagelet p : page.getPagelets()) {
      for (Language l : p.languages()) {
        setField(MessageFormat.format(PAGELET_TEXT, i) + "-" + l.getIdentifier(), serializeContent(p, l));
      }
      setField(MessageFormat.format(PAGELET_PROPERTIES, i), serializeProperties(p));
      setField(MessageFormat.format(PAGELET_XML, i), p.toXml());
    }
    
    // Preview information
    // TODO: Add preview xml
  }

  /**
   * Adds the field and its value to the search index. This method is here for
   * convenience so we don't need to do null check on each and every field
   * value.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   */
  @Override
  public void setField(String fieldName, Object fieldValue) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;
    super.setField(fieldName, fieldValue);
  }

  /**
   * Returns a serialized version of the date or <code>null</code> if
   * <code>null</code> was passed in for the date.
   * 
   * @param date
   *          the date
   * @return the serialized date
   */
  private String serializeDate(Date date) {
    if (date == null)
      return null;
    return dateFormat.format(date);
  }

  /**
   * Serializes the user to a string or to <code>null</code> if
   * <code>null</code> was passed to this method.
   * 
   * @param user
   *          the user
   * @return the serialized user
   */
  private String serializeUser(User user) {
    if (user == null)
      return null;
    StringBuffer buf = new StringBuffer();
    buf.append(user.getName());
    buf.append(" <").append(user.getLogin()).append(">");
    return buf.toString();
  }

  /**
   * Returns the localized field name, which is the original field name extended
   * by an underscore and the language identifier.
   * 
   * @param fieldName
   *          the field name
   * @param language
   *          the language
   * @return the localized field name
   */
  private String getLocalizedFieldName(String fieldName, Language language) {
    StringBuffer buf = new StringBuffer(fieldName);
    buf.append("-").append(language.getIdentifier());
    return buf.toString();
  }

  /**
   * Returns a string representation of the pagelet's element content in the
   * specified language.
   * 
   * @param pagelet
   *          the pagelet
   * @param language
   *          the language
   * @return the serialized element content
   */
  private String serializeContent(Pagelet pagelet, Language language) {
    StringBuffer buf = new StringBuffer();
    for (String element : pagelet.getContentNames(language)) {
      String[] content = pagelet.getMultiValueContent(element, language, true);
      for (String c : content) {
        buf.append(element).append(":").append(c).append(" ;; ");
      }
    }
    return buf.toString();
  }

  /**
   * Returns a string representation of the pagelet's element properties.
   * 
   * @param pagelet
   *          the pagelet
   * @return the serialized element properties
   */
  private String serializeProperties(Pagelet pagelet) {
    StringBuffer buf = new StringBuffer();
    for (String property : pagelet.getPropertyNames()) {
      String[] values = pagelet.getMultiValueProperty(property);
      for (String v : values) {
        buf.append(property).append(":").append(v).append(" ;; ");
      }
    }
    return buf.toString();
  }

}
