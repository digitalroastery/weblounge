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
package ch.entwine.weblounge.contentrepository.impl.index.elasticsearch;

import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.LOCALIZED_FULLTEXT;

import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.index.IndexSchema;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Document that encapsulates weblounge resources and prepares them to be
 * indexed in an elastic search node.
 */
public final class ElasticSearchDocument extends HashMap<String, Object> {

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

    List<String> fulltext = new ArrayList<String>();

    for (ResourceMetadata<?> entry : resource) {
      String metadataKey = entry.getName();

      // Store the metadata element as is
      put(metadataKey, entry.getValues());

      // Add to fulltext?
      if (entry.addToFulltext()) {

        // Language neutral elements
        for (Object value : entry.getValues()) {
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
        for (Language language : entry.getLocalizedValues().keySet()) {
          List<?> values = entry.getLocalizedValues().get(language);
          for (Object value : values) {
            String localizedFieldName = MessageFormat.format(LOCALIZED_FULLTEXT, language.getIdentifier());
            String localizedFulltext = StringUtils.trimToEmpty((String) get(localizedFieldName));
            if (value.getClass().isArray()) {
              Object[] fieldValues = (Object[]) value;
              for (Object v : fieldValues) {
                localizedFulltext = StringUtils.join(new Object[] {
                    localizedFulltext,
                    v.toString() }, " ");
              }
            } else {
              localizedFulltext = StringUtils.join(new Object[] {
                  localizedFulltext,
                  value.toString() }, " ");
            }
            put(localizedFieldName, localizedFulltext);
          }
        }
      }

    }

    put(IndexSchema.FULLTEXT, fulltext);
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
    return uri.getIdentifier() + "." + uri.getVersion();
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
