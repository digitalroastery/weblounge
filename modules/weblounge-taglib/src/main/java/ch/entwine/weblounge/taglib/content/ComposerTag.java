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

import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryUnavailableException;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.request.WebloungeRequest;
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
  protected boolean isComposerLocked = false;

  /**
   * Sets the composer to a locked state, preventing editing at all.
   * 
   * @param value
   *          <code>true</code> to lock the composer
   */
  public void setLocked(String value) {
    isComposerLocked = ConfigurationUtils.isTrue(value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#beforeComposer(javax.servlet.jsp.JspWriter)
   */
  @Override
  protected void beforeComposer(JspWriter writer) throws IOException,
      ContentRepositoryException, ContentRepositoryUnavailableException {

    // Mark inherited composer and ghost content in locked work mode
    if (RequestUtils.isEditingState(request)) {
      if (contentInheritanceEnabled)
        addCssClass(CLASS_INHERIT_CONTENT);
      if (getContent().length == 0)
        addCssClass(CLASS_GHOST_CONTENT);
    }

    // Mark composer as locked
    if (isComposerLocked)
      addCssClass(CLASS_LOCKED);

    // Let the default implementation kick in
    super.beforeComposer(writer);

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.ComposerTagSupport#beforePagelet(Pagelet,
   *      int, JspWriter, boolean)
   */
  @Override
  protected int beforePagelet(Pagelet pagelet, int position, JspWriter writer,
      boolean isGhostContent) throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {

    if (RequestUtils.isEditingState(request)) {

      boolean hasEditor = false;

      Site site = getTargetPage().getURI().getSite();
      Module module = site.getModule(pagelet.getModule());
      if (module != null) {
        PageletRenderer renderer = module.getRenderer(pagelet.getIdentifier());
        if (renderer.getEditor() != null)
          hasEditor = true;
      }

      writer.print("<div class=\"pagelet ");

      // if pagelet has no editor add a cssClass wbl-noEditor
      if (!hasEditor)
        writer.print("wbl-noEditor");
      // if pagelet is ghost content add a cssClass ghost
      if (isGhostContent)
        writer.print("ghost");

      writer.print("\">");
      writer.newLine();
      writer.flush();
    }
    return super.beforePagelet(pagelet, position, writer, isGhostContent);
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

    if (RequestUtils.isEditingState(request)) {
      request.setAttribute(WebloungeRequest.PAGE, targetPage);
      request.setAttribute(WebloungeRequest.PAGELET, pagelet);
      request.setAttribute(WebloungeRequest.COMPOSER, new ComposerImpl(id));
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
    isComposerLocked = false;
  }

}
