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
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.jcr.impl.serializer.JCRPageResourceSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * TODO: Comment PageRepository
 */
public class PageRepository extends AbstractResourceRepository {

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(PageRepository.class);

  /**
   * Creates an empty page with the given path. The identifier which is may be
   * given by the uri will not be respected. The newly created identifier is set
   * on the page before it is returned.
   * <p>
   * A page can only be created on a path with an existing parent resource.
   * Otherwise a {@link ContentRepositoryException} is thrown.
   * 
   * @param uri
   *          URI with site and path information
   * @return the empty page
   * @throws ContentRepositoryException
   *           if no parent resource exists or if there's any other error
   */
  public Page createPage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    String absParentNodePath = JCRResourceUtils.getAbsParentNodePath(uri);
    Session session = getSession();

    try {
      if (!session.nodeExists(absParentNodePath)) {
        log.warn("Parent Resource for new page '{}' does not exist", uri);
        throw new ContentRepositoryException("Parent Resource for new page '" + uri + "' does not exist.");
      }

      Node parentNode = session.getNode(absParentNodePath);

      if (parentNode.hasNode(JCRResourceUtils.getNodeName(uri))) {
        log.warn("There is already a resource at the given path '{}'", uri.getPath());
        throw new ContentRepositoryException("There is already a resource at the given path '" + uri.getPath() + "'");
      }

      Node pageNode = parentNode.addNode(JCRResourceUtils.getNodeName(uri));
      pageNode.setProperty("resource-type", Page.TYPE);

      Node created = pageNode.addNode("webl:created");
      created.setProperty("date", Calendar.getInstance());
      // TODO Add creator/owner, but which user shall we take?

      Page page = new PageImpl(uri);
      page.setIdentifier(pageNode.getIdentifier());

      session.save();

      log.info("Successfully created new page '{}'", uri);
      return page;
    } catch (RepositoryException e) {
      log.warn("New page '{}' could not be created", uri);
      throw new ContentRepositoryException("New page could not be created", e);
    }
  }

  /**
   * Update an existing page with new content. The identifier of the given page
   * must match the identifier of the page stored in the repository. Otherwise a
   * {@link ContentRepositoryException} is thrown.
   * 
   * @param page
   *          page to update
   * @return the updated page
   * @throws ContentRepositoryException
   *           if the identifiers do not match or if the page does not exist at
   *           all
   */
  public Page updatePage(Page page) throws ContentRepositoryException {
    if (page == null)
      throw new IllegalArgumentException("Page must not be null");

    try {
      Session session = getSession();

      String absPagePath = JCRResourceUtils.getAbsNodePath(page.getURI());
      Node pageNode = session.getNode(absPagePath);

      if (!pageNode.getIdentifier().equals(page.getIdentifier())) {
        log.warn("Tryed updating page '{}' but identifier of page ({}) and identifier of Node ({}) do not match!", new Object[] {
            page,
            page.getIdentifier(),
            pageNode.getIdentifier() });
        throw new ContentRepositoryException("Identifier of page and identifier of node do not match");
      }

      // VersionManager versionMgr = session.getWorkspace().getVersionManager();
      // versionMgr.checkout(pageNode.getPath());

      // storePageInNode(pageNode, page);
      JCRPageResourceSerializer serializer = new JCRPageResourceSerializer();
      serializer.store(pageNode, page);

      // Make node versionable
      // TODO Do we need to add the mix-in here again?
      // pageNode.addMixin(JcrConstants.MIX_VERSIONABLE);

      session.save();

      // checkinResourceWithVersionLabel(versionMgr, pageNode, uri);
    } catch (PathNotFoundException e) {
      log.warn("Tryied updating non existing page '{}'", page);
      throw new ContentRepositoryException("Tryed updating non existing page");
    } catch (RepositoryException e) {
      log.warn("Page '{}' could not be updated", page);
      throw new ContentRepositoryException("Page could not be updated", e);
    }

    log.info("Successfully updated page '{}'", page);
    return page;
  }

  /**
   * Deletes the given page from the repository. A page can only be deleted if
   * it has no more sub-pages. The homepage of a site can never be deleted.
   * 
   * @param uri
   *          page to remove
   * @throws ContentRepositoryException
   *           if there's any error while deleting the page
   */
  public void deletePage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    if ("/".equals(uri.getPath())) {
      log.warn("Tried to delete homepage ({})", uri);
      throw new ContentRepositoryException("Tried to delete homepage");
    }

    Session session = getSession();
    try {
      String absNodePath = JCRResourceUtils.getAbsNodePath(uri);
      if (!session.nodeExists(absNodePath)) {
        log.warn("Tryed to delete non-existing page '{}'", uri);
        throw new ContentRepositoryException("Tryed to delete non-existing page");
      }

      Node pageNode = session.getNode(absNodePath);
      NodeIterator subNodes = pageNode.getNodes();
      while (subNodes.hasNext()) {
        Node subNode = subNodes.nextNode();
        // TODO Find a better way to check for sub-pages
        if (!subNode.getName().startsWith("webl:")) {
          log.warn("Cannot delete page '{}' with existing sub-pages", uri);
          throw new ContentRepositoryException("Cannot delete page with existing sub-pages");
        }
      }

      pageNode.remove();
      session.save();

      log.info("Successfully deleted page '{}'", uri);
    } catch (RepositoryException e) {
      log.warn("Error while deleting page '{}': {}", uri, e.getMessage());
      throw new ContentRepositoryException("Error while deleting page", e);
    }
  }

  /**
   * Returns the page for the given URI.
   * 
   * @param uri
   *          page to return
   * @return the page
   * @throws ContentRepositoryException
   *           if the page does not exists or if the given identifier does not
   *           match the identifier of the page-node
   */
  public Page getPage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Parameter 'uri' must not be null");

    try {
      Session session = getSession();

      String absNodePath = JCRResourceUtils.getAbsNodePath(uri);
      if (!session.nodeExists(absNodePath)) {
        log.warn("Requested page '{}' does not exist", uri);
        throw new ContentRepositoryException("Requested page does not exist");
      }

      Node pageNode = session.getNode(absNodePath);
      if (!pageNode.getIdentifier().equals(uri.getIdentifier())) {
        log.warn("Tryed getting page '{}' but identifier of URI ({}) and identifier of Node ({}) do not match!", new Object[] {
            uri,
            uri.getIdentifier(),
            pageNode.getIdentifier() });
        throw new ContentRepositoryException("Identifier of page and identifier of node do not match");
      }

      // VersionManager versionManager =
      // session.getWorkspace().getVersionManager();
      // VersionHistory versionHistory =
      // versionManager.getVersionHistory(pageNode.getPath());
      //
      // String versionLabel = getJCRVersionLabel(uri.getVersion());
      // if (!versionHistory.hasVersionLabel(versionLabel))
      // throw new ContentRepositoryException("No version '" + versionLabel +
      // "' found");
      // Version version = versionHistory.getVersionByLabel(versionLabel);
      //
      // pageNode = version.getFrozenNode();

      Page page = new PageImpl(uri);
      page.setIdentifier(pageNode.getIdentifier());
      page.setPath(pageNode.getPath());
      page.setTemplate(pageNode.getProperty("template").getString());
      page.setLayout(pageNode.getProperty("layout").getString());
      page.setStationary(pageNode.getProperty("stationary").getBoolean());

      return page;
    } catch (RepositoryException e) {
      log.warn("Error while getting page '{}'", uri);
      throw new ContentRepositoryException(e);
    }
  }

  /**
   * Checks if a page exists, returns false otherwise.
   * 
   * @param uri
   *          page to check
   * @return true, if page exists
   * @throws ContentRepositoryException
   *           if there's any problem while checking
   */
  public boolean existsPage(ResourceURI uri) throws ContentRepositoryException {
    if (uri == null)
      throw new IllegalArgumentException("Page uri must not be null");

    try {
      Session session = getSession();

      if (session.nodeExists(JCRResourceUtils.getAbsNodePath(uri)))
        return true;
    } catch (RepositoryException e) {
      log.warn("Error while checking if page '{}' exists", uri);
      throw new ContentRepositoryException("Error while checking if page exists", e);
    }

    return false;
  }

  public PageContent getPageContent(ResourceURI uri) {
    // return super.getRepresentation(uri, PageContent.class);
    return null;
  }

  public PageContent updatePageContent(ResourceURI uri, PageContent content) {
    return null;
  }

}
