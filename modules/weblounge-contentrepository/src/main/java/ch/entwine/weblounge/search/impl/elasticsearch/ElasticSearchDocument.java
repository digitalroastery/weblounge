/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.search.impl.elasticsearch;

import static ch.entwine.weblounge.search.impl.IndexSchema.FULLTEXT;
import static ch.entwine.weblounge.search.impl.IndexSchema.LOCALIZED_FULLTEXT;
import static ch.entwine.weblounge.search.impl.IndexSchema.LOCALIZED_TEXT;
import static ch.entwine.weblounge.search.impl.IndexSchema.TEXT;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Document that encapsulates weblounge resources and prepares them to be
 * indexed in an elastic search node.
 */
public final class ElasticSearchDocument extends HashMap<String, Object> {

  /** Extension for fuzzy field names */
  public static final String FUZZY_FIELDNAME_EXTENSION = "_fuzzy";
  
  /** Serial version uid */
  private static final long serialVersionUID = 2687550418831284487L;

  /** The resource URI */
  private ResourceURI uri = null;

  /**
   * Creates a new elastic search document based on the resource metadata.
   * 
   * @param resource
   *          the resource metadata
   */
  public ElasticSearchDocument(ResourceURI uri,
      List<ResourceMetadata<?>> resource) {
    this.uri = uri;

    for (ResourceMetadata<?> entry : resource) {
      String metadataKey = entry.getName();

      // Store the metadata element as is
      put(metadataKey, entry.getValues());

      // Add to backend facing fulltext?
      if (entry.addToFulltext()) {
        addToFulltext(entry, FULLTEXT, LOCALIZED_FULLTEXT);
      }

      // Add to frontend facing fulltext?
      if (uri.getVersion() == Resource.LIVE && entry.addToText()) {
        addToFulltext(entry, TEXT, LOCALIZED_TEXT);
      }

    }

  }

  /**
   * Adds the resource metadata to the designated fulltext fields.
   * 
   * @param item
   *          the metadata item
   * @param fulltextFieldName
   *          the fulltext field name
   * @param localizedFulltextFieldName
   *          the localized fulltext field name
   */
  private void addToFulltext(ResourceMetadata<?> item,
      String fulltextFieldName, String localizedFulltextFieldName) {

    // Get existing fulltext entries
    Collection<String> fulltext = (Collection<String>) get(fulltextFieldName);
    if (fulltext == null) {
      fulltext = new ArrayList<String>();
      put(fulltextFieldName, fulltext);
      put(fulltextFieldName + FUZZY_FIELDNAME_EXTENSION, fulltext);
    }

    // Language neutral elements
    for (Object value : item.getValues()) {
      if (value.getClass().isArray()) {
        Object[] fieldValues = (Object[]) value;
        for (Object v : fieldValues) {
          fulltext.add(v.toString());
        }
      } else {
        fulltext.add(value.toString());
      }
    }

    // Add localized metadata values
    for (Language language : item.getLocalizedValues().keySet()) {
      // Get existing fulltext entries
      String localizedFieldName = MessageFormat.format(localizedFulltextFieldName, language.getIdentifier());
      Collection<String> localizedFulltext = (Collection<String>) get(localizedFieldName);
      if (fulltext == null) {
        fulltext = new ArrayList<String>();
        put(localizedFieldName, fulltext);
      }
      Collection<?> values = item.getLocalizedValues().get(language);
      for (Object value : values) {
        if (value.getClass().isArray()) {
          Object[] fieldValues = (Object[]) value;
          for (Object v : fieldValues) {
            localizedFulltext.add(v.toString());
          }
        } else {
          localizedFulltext.add(value.toString());
        }
      }
    }

  }

  /**
   * Returns the document's associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return uri.getSite();
  }

  /**
   * Returns the identifier.
   * 
   * @return the identifier
   */
  public String getUID() {
    return uri.getUID();
  }

  /**
   * Returns the document's type.
   * 
   * @return the type
   */
  public String getType() {
    return uri.getType();
  }

}
