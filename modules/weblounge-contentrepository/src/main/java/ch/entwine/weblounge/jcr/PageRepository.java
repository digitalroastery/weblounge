/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * TODO: Comment PageRepository
 */
public class PageRepository extends ResourceRepository {

  /** The logging facility */
  Logger log = LoggerFactory.getLogger(PageRepository.class);

  public Page addPage(ResourceURI uri, Page page)
      throws ContentRepositoryException {
    Node resNode = getResourcesNode(uri.getSite());

    try {
      String pagePath = page.getPath();
      if (StringUtils.endsWith(pagePath, "/"))
        pagePath = StringUtils.removeEnd(pagePath, "/");
      Node pageNode = resNode.addNode(pagePath.substring(pagePath.lastIndexOf('/') + 1, pagePath.length()), "webl:Resource");
      pageNode.setProperty("webl:layout", page.getLayout());
      pageNode.setProperty("webl:template", page.getTemplate());
      resNode.getSession().save();
    } catch (RepositoryException e) {
      log.warn("Page could not be added: {}", e.getMessage());
      return null;
    }

    return page;
  }

}
