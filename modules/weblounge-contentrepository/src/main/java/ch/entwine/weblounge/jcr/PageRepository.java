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

import java.util.Calendar;

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

  public Page createPage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    String absParentNodePath = JCRResourceUtils.getAbsParentNodePath(uri);

    Session session = getSession();

    try {
      if (!session.nodeExists(absParentNodePath))
        throw new ContentRepositoryException("Parent Resource for new Page '" + uri.toString() + "' does not exist.");

      Node parentNode = session.getNode(absParentNodePath);
      Node pageNode = parentNode.addNode(JCRResourceUtils.getNodeName(uri));
      pageNode.setProperty("resource-type", Page.TYPE);

      Node created = pageNode.addNode("webl:created");
      created.setProperty("date", Calendar.getInstance());
      // TODO Add creator/owner, but which user shall we take?

      Page page = new PageImpl(uri);
      page.setIdentifier(pageNode.getIdentifier());

      session.save();

      return page;
    } catch (RepositoryException e) {
      log.warn("New page '{}' could not be created", uri);
      throw new ContentRepositoryException("New page could not be created", e);
    }
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

      // storePageInNode(pageNode, page);
      JCRPageResourceSerializer serializer = new JCRPageResourceSerializer();
      serializer.store(pageNode, page);

      // Make node versionable
      // TODO Do we need to add the mix-in here again?
      // pageNode.addMixin(JcrConstants.MIX_VERSIONABLE);

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

  public boolean deletePage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    Session session = getSession();
    try {
      session.removeItem(JCRResourceUtils.getAbsNodePath(uri));
      session.save();

      log.info("Successfuly removed Page '{}'", uri);
      return true;
    } catch (RepositoryException e) {
      log.warn("Error while deleting Page '{}': {}", uri, e.getMessage());
      throw new ContentRepositoryException("Error while deleting page", e);
    }
  }

  // public PageResource getPage(ResourceURI uri) throws
  // ContentRepositoryException {
  public Page getPage(ResourceURI uri) throws ContentRepositoryException {

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
    // return super.getRepresentation(uri, PageContent.class);
    return null;
  }

  public PageContent updatePageContent(ResourceURI uri, PageContent content) {
    return null;
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
