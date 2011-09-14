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

package ch.entwine.weblounge.taglib.content;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryUnavailableException;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.ComposerTagSupport;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

/**
 * <code>ComposerTag</code> implements the handler for <code>module</code> tags
 * embedded in a jsp file. The handler will request the specified module to
 * return some resources using the requested view.
 */
public class ComposerTag extends ComposerTagSupport {

  /** Serial version uid */
  private static final long serialVersionUID = 3832079623323702494L;

  /** True if the composer should not be editable */
  protected boolean isLocked = false;

  /**
   * Sets the composer to a locked state, preventing editing at all.
   * 
   * @param value
   *          <code>true</code> to lock the composer
   */
  public void setLocked(String value) {
    isLocked = ConfigurationUtils.isTrue(value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#beforeComposer(javax.servlet.jsp.JspWriter)
   */
  @Override
  protected void beforeComposer(JspWriter writer) throws IOException,
      ContentRepositoryException, ContentRepositoryUnavailableException {
    User user = request.getUser();
    long version = request.getVersion();
    Page targetPage = getTargetPage();
    Page contentPage = getContentProvider();
    
    boolean isLocked = targetPage != null && targetPage.isLocked();
    boolean isLockedByCurrentUser = targetPage != null && isLocked && user.equals(targetPage.getLockOwner());
    boolean isWorkVersion = version == Resource.WORK;
    boolean allowContentInheritance = contentInheritanceEnabled && !isLockedByCurrentUser && !isWorkVersion;

    // Enable / disable content inheritance for this composer
    setInherit(allowContentInheritance);

    // Mark inherited composer and ghost content in locked work mode
    if (isWorkVersion && isLockedByCurrentUser) {
      if (allowContentInheritance)
        addCssClass(CLASS_INHERIT_CONTENT);
      if (targetPage != null && !targetPage.equals(contentPage))
        addCssClass(CLASS_GHOST_CONTENT);
    }

    // Let the default implementation kick in
    super.beforeComposer(writer);

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#beforePagelet(ch.entwine.weblounge.common.content.page.Pagelet,
   *      int, javax.servlet.jsp.JspWriter)
   */
  @Override
  protected int beforePagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {
    
    // Start editing support
    // FIXME temporary solution
    // if (version == Page.WORK && isLockedByCurrentUser) {
    if (RequestUtils.isEditingState(request)) {
      
      boolean hasEditor = false;
      
      Site site = getTargetPage().getURI().getSite();
      Module module = site.getModule(pagelet.getModule());
      if (module != null) {
         PageletRenderer renderer = module.getRenderer(pagelet.getIdentifier());
         if(renderer.getEditor() != null) hasEditor = true;
      }
      
      // if pagelet has no editor add a cssClass noEditor
      if(hasEditor) {
        writer.println("<div class=\"pagelet\">");
      } else {
        writer.println("<div class=\"pagelet wbl-noEditor\">");
      }
      writer.flush();
    }
    return super.beforePagelet(pagelet, position, writer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#afterPagelet(ch.entwine.weblounge.common.content.page.Pagelet,
   *      int, javax.servlet.jsp.JspWriter)
   */
  @Override
  protected void afterPagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {

    // If user is not editing this page, then we are finished with
    // the current pagelet.
    // finally {
    // FIXME temporary solution
    // if (version == Page.WORK && isLockedByCurrentUser &&
    // request.getAttribute(PageletEditorTag.ID) == null) {
    if (RequestUtils.isEditingState(request)) {
      request.setAttribute(WebloungeRequest.PAGE, targetPage);
      request.setAttribute(WebloungeRequest.PAGELET, pagelet);
      request.setAttribute(WebloungeRequest.COMPOSER, new ComposerImpl(name));
      writer.println("</div>");
      writer.flush();
    }
    super.afterPagelet(pagelet, position, writer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    isLocked = false;
  }

}
