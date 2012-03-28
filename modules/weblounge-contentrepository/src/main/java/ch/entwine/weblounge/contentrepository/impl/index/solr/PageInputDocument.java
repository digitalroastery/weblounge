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

package ch.entwine.weblounge.contentrepository.impl.index.solr;

import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.CREATED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.CREATED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.MODIFIED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.MODIFIED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.OWNED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.OWNED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_CONTENTS;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_CONTENTS_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_PROPERTIES;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_PROPERTY_VALUE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_TYPE_COMPOSER;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_TYPE_COMPOSER_POSITION;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_XML_COMPOSER;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PAGELET_XML_COMPOSER_POSITION;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PREVIEW_XML;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PUBLISHED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.PUBLISHED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.TEMPLATE;

import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.language.Language;

import java.text.MessageFormat;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr.
 */
public class PageInputDocument extends ResourceInputDocument {

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
   * @param the
   *          stage composer
   */
  protected void init(Page page) {
    super.init(page);

    // Page-level
    addField(TEMPLATE, page.getTemplate(), false);
    addField(SolrSchema.STATIONARY, page.isStationary(), false);

    // Determine the stage composer
    String stage = null;
    PageTemplate template = page.getURI().getSite().getTemplate(page.getTemplate());
    if (template != null) {
      stage = template.getStage();
    }

    // Pagelet elements and properties
    for (Composer composer : page.getComposers()) {
      String composerId = composer.getIdentifier();
      addComposerFields(composer, composerId);
      if (composerId.equals(stage)) {
        addComposerFields(composer, SearchQueryImpl.STAGE_COMPOSER);
      }
    }

    // Preview information
    StringBuffer preview = new StringBuffer();
    preview.append("<composer id=\"preview\">");
    for (Pagelet p : page.getPreview()) {
      preview.append(p.toXml());
    }
    preview.append("</composer>");
    addField(PREVIEW_XML, preview.toString(), false);
  }

  /**
   * Depending on the composer content and the provided target id, adds the
   * necessary fields to the input document.
   * 
   * @param composer
   *          the composer
   * @param composerId
   *          the composer identifier
   */
  protected void addComposerFields(Composer composer, String composerId) {
    int i = 0;
    for (Pagelet p : composer.getPagelets()) {
      for (Language l : p.languages()) {
        addField(PAGELET_CONTENTS, serializeContent(p, l), true);
        addField(getLocalizedFieldName(PAGELET_CONTENTS_LOCALIZED, l), serializeContent(p, l), true);
      }

      for (String property : p.getPropertyNames()) {
        addField(PAGELET_PROPERTY_VALUE, p.getProperty(property), true);
      }

      addField(PAGELET_PROPERTIES, serializeProperties(p), false);
      addField(MessageFormat.format(PAGELET_XML_COMPOSER, composerId), p.toXml(), false);
      addField(MessageFormat.format(PAGELET_XML_COMPOSER_POSITION, i), p.toXml(), false);
      addField(MessageFormat.format(PAGELET_TYPE_COMPOSER, composerId), p.getModule() + "/" + p.getIdentifier(), false);
      addField(MessageFormat.format(PAGELET_TYPE_COMPOSER_POSITION, i), p.getModule() + "/" + p.getIdentifier(), false);

      // Workflow related
      addField(OWNED_BY, SolrUtils.serializeUserId(p.getOwner()), false);
      addField(OWNED_BY_NAME, SolrUtils.serializeUserName(p.getOwner()), true);
      addField(CREATED_BY, SolrUtils.serializeUserId(p.getCreator()), false);
      addField(CREATED_BY_NAME, SolrUtils.serializeUserName(p.getCreator()), true);
      addField(MODIFIED_BY, SolrUtils.serializeUserId(p.getModifier()), false);
      addField(MODIFIED_BY_NAME, SolrUtils.serializeUserName(p.getModifier()), true);
      addField(PUBLISHED_BY, SolrUtils.serializeUserId(p.getPublisher()), false);
      addField(PUBLISHED_BY_NAME, SolrUtils.serializeUserName(p.getPublisher()), true);

      i++;
    }
  }

}
