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

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.LOCALIZED_FULLTEXT;

import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr. This implementation provides utility methods
 * that will ease handling of objects such as dates or users and help prevent
 * posting of <code>null</code> values.
 */
public abstract class AbstractInputDocument extends SolrInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /**
   * Adds the field and its value to the search index. This method is here for
   * convenience so we don't need to do null check on each and every field
   * value.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   * @param addToFulltext
   *          <code>true</code> to add the contents to the fulltext field as
   *          well
   */
  public void addField(String fieldName, Object fieldValue,
      boolean addToFulltext) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;
    if (fieldValue.getClass().isArray()) {
      Object[] fieldValues = (Object[]) fieldValue;
      for (Object v : fieldValues) {
        super.addField(fieldName, v);
      }
    } else {
      super.addField(fieldName, fieldValue);
    }

    // Add to fulltext
    if (addToFulltext) {
      String fulltext = (String) super.getFieldValue(SolrFields.FULLTEXT);
      if (fieldValue.getClass().isArray()) {
        Object[] fieldValues = (Object[]) fieldValue;
        for (Object v : fieldValues) {
          fulltext = StringUtils.join(new Object[] { fulltext, v.toString() }, " ");
        }
      } else {
        fulltext = StringUtils.join(new Object[] {
            fulltext,
            fieldValue.toString() }, " ");
      }
      super.setField(SolrFields.FULLTEXT, fulltext);
    }
  }

  /**
   * Adds the field and its value to the indicated field of the search index as
   * well as to the language-sensitive fulltext field. The implementation
   * performs a <code>null</code> test and silently returns if <code>null</code>
   * was passed in.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   * @param language
   *          the language
   * @param addToFulltext
   *          <code>true</code> to add the contents to the fulltext field as
   *          well
   */
  public void addField(String fieldName, Object fieldValue, Language language,
      boolean addToFulltext) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;

    addField(fieldName, fieldValue);

    // Add to fulltext
    if (addToFulltext) {

      // Update the localized fulltext
      String localizedFieldName = getLocalizedFieldName(LOCALIZED_FULLTEXT, language);
      String localizedFulltext = (String) super.getFieldValue(localizedFieldName);
      if (fieldValue.getClass().isArray()) {
        Object[] fieldValues = (Object[]) fieldValue;
        for (Object v : fieldValues) {
          localizedFulltext = StringUtils.join(new Object[] {
              localizedFulltext,
              v.toString() }, " ");
        }
      } else {
        localizedFulltext = StringUtils.join(new Object[] {
            localizedFulltext,
            fieldValue.toString() }, " ");
      }
      super.setField(localizedFieldName, localizedFulltext);

    }
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
  protected String getLocalizedFieldName(String fieldName, Language language) {
    return MessageFormat.format(fieldName, language.getIdentifier());
  }

  /**
   * Returns a string representation of the pagelet's element content in the
   * specified language. If <code>format</code> is <code>true</code> then the
   * content is formatted as <code>field=&lt;value&gt;;;</code>, otherwise just
   * the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @param language
   *          the language
   * @return the serialized element content
   */
  protected String[] serializeContent(Pagelet pagelet, Language language) {
    List<String> result = new ArrayList<String>();
    for (String element : pagelet.getContentNames(language)) {
      String[] content = pagelet.getMultiValueContent(element, language, true);
      for (String c : content) {
        StringBuffer buf = new StringBuffer();
        buf.append(element).append(":= ").append(c);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns a string representation of the pagelet's element properties. If
   * <code>format</code> is <code>true</code> then the property is formatted as
   * <code>field=&lt;value&gt;;;</code>, otherwise just the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @return the serialized element properties
   */
  protected String[] serializeProperties(Pagelet pagelet) {
    List<String> result = new ArrayList<String>();
    for (String property : pagelet.getPropertyNames()) {
      for (String v : pagelet.getMultiValueProperty(property)) {
        StringBuffer buf = new StringBuffer();
        buf.append(property).append(":=").append(v);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

}
