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

import ch.entwine.weblounge.common.content.PageContent;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.jcr.impl.serializer.JCRPageResourceSerializer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

/**
 * TODO: Comment PageRepository
 */
public class PageRepository extends AbstractResourceRepository {

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(PageRepository.class);
  
  public Page createPage(String path) {
    
  }
  
  public boolean deletePage(ResourceURI uri) {
    
  }

  /**
   * Adds the given page to the repository.
   * 
   * @param uri
   * @param page
   * @return
   * @throws ContentRepositoryException
   */
  public Page createPage(ResourceURI uri, Page page) throws ContentRepositoryException {
    Resource<?> res = addResource(uri, page);
    return (Page) res;
  }
  

  // TODO Do we need the URI parameter?
  public Page updatePage(ResourceURI uri, Page page) {
    
    try {
      Session session = getSession();

      String pagePath = page.getPath();
      if (StringUtils.endsWith(pagePath, "/"))
        pagePath = StringUtils.removeEnd(pagePath, "/");
      pagePath = pagePath.substring(pagePath.lastIndexOf('/') + 1, pagePath.length());

      Node resNode = getResourcesNode(session, uri.getSite());
      Node pageNode = resNode.getNode(pagePath);

      VersionManager versionMgr = session.getWorkspace().getVersionManager();
      versionMgr.checkout(pageNode.getPath());

      //storePageInNode(pageNode, page);
      JCRPageResourceSerializer serializer = new JCRPageResourceSerializer();
      serializer.store(pageNode, page);
      
      // Make node versionable
      // QUESTION Do we need to add the mix-in here again?
      //pageNode.addMixin(JcrConstants.MIX_VERSIONABLE);
      
      session.save();

      checkinResourceWithVersionLabel(versionMgr, pageNode, uri);
    } catch (PathNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ContentRepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return page;
  }

  public PageResource getPage(ResourceURI uri) throws ContentRepositoryException {

    try {
      Session session = getSession();
      Node pageNode;

      if (uri.getPath() != null) {
        log.debug("Lookup page by given path '{}'", uri.getPath());
        pageNode = session.getNode(JCRResourceUtils.getAbsNodePath(uri));
        log.info("Page node '{}' found by given path '{}'", pageNode.getIdentifier(), uri.getPath());
      } else if (uri.getIdentifier() != null) {
        log.debug("Lookup page by given identifier '{}'", uri.getIdentifier());
        pageNode = session.getNodeByIdentifier(uri.getIdentifier());
        log.info("Page node '{}' found by given identifier '{}'", pageNode.getIdentifier(), uri.getIdentifier());
      } else {
        throw new ContentRepositoryException("No page found!");
      }

      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory versionHistory = versionManager.getVersionHistory(pageNode.getPath());

      String versionLabel = getJCRVersionLabel(uri.getVersion());
      if (!versionHistory.hasVersionLabel(versionLabel))
        throw new ContentRepositoryException("No version '" + versionLabel + "' found");
      Version version = versionHistory.getVersionByLabel(versionLabel);

      pageNode = version.getFrozenNode();
      Page page = new PageImpl(uri);
      page.setIdentifier(pageNode.getIdentifier());
      page.setPath(pageNode.getPath());
      page.setTemplate(pageNode.getProperty("template").getString());

      return page;
    } catch (ItemNotFoundException e) {
      throw new ContentRepositoryException(e);
    } catch (RepositoryException e) {
      throw new ContentRepositoryException(e);
    }
  }
  
  public PageContent getPageContent(ResourceURI uri) {
     return super.getRepresentation(uri, PageContent.class);
  }
  
  public PageContent updatePageContent(ResourceURI uri, PageContent content) {
    
  }

  private String getJCRVersionLabel(long version) {
    if (version == Resource.LIVE)
      return "webl:live";
    if (version == Resource.WORK)
      return "webl:work";
    return String.valueOf(version);
  }

  private void checkinResourceWithVersionLabel(VersionManager versionMgr,
      Node node, ResourceURI uri) throws RepositoryException {

    Version version = versionMgr.checkin(node.getPath());
    VersionHistory versHist = versionMgr.getVersionHistory(node.getPath());

    if (uri.getVersion() == Resource.LIVE) {
      versHist.addVersionLabel(version.getName(), "webl:live", true);
      log.info("Label 'webl:live' added to JCR version '{}' of node '{}'", version.getName(), node.getIdentifier());
    } else if (uri.getVersion() == Resource.WORK) {
      versHist.addVersionLabel(version.getName(), "webl:work", true);
      log.info("Label 'webl:work' added to JCR version '{}' of node '{}'", version.getName(), node.getIdentifier());
    } else {
      versHist.addVersionLabel(version.getName(), String.valueOf(uri.getVersion()), true);
      log.info("Label '{}' added to JCR version '{}' of node '{}'", new Object[] {
          uri.getVersion(),
          version.getName(),
          node.getIdentifier() });
    }
  }

}
