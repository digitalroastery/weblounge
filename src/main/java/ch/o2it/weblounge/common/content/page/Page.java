/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.content.page;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;

/**
 * A <code>Page</code> encapsulates all data that is attached to a site url and
 * that can be edited in terms of composers and pagelets. All the content may
 * also be supplied in multiple languages.
 * <p>
 * During lifetime, the page keeps track of creation, modification and
 * publishing processes. Note that a page can exist in many versions, with a few
 * of them being special:
 * <ul>
 * <li>{@link LIVE}: the live version of the page</li>
 * <li>{@link WORK}: the work version of the page</li>
 * </ul>
 */
public interface Page extends Resource<ResourceContent> {

  /** The resource type */
  public static final String TYPE = "page";

  /**
   * Sets the layout that should be applied to this page. The layout controls
   * which pagelets to place into a composer by default, which ones to protect
   * and which ones to allow for editing.
   * 
   * @param layout
   *          the page layout
   */
  void setLayout(String layout);

  /**
   * Returns the identifier of the layout associated with this page.
   * 
   * @return the associated layout
   */
  String getLayout();

  /**
   * Sets the page template. The parameter <code>template</code> represents the
   * identifier of a renderer that is used to render the page.
   * 
   * @param template
   *          the template to use
   */
  void setTemplate(String template);

  /**
   * Returns the identifier of the template that is used to render this page.
   * 
   * @return the template
   */
  String getTemplate();

  /**
   * Returns the composer identified by <code>composerId</code> or
   * <code>null</code> if that composer is not found.
   * 
   * @return the composer
   */
  Composer getComposer(String composerId);

  /**
   * Returns a list of composers on this page. Note that there is no way of
   * knowing for sure whether this list is complete, since it only lists those
   * composers that have at least one pagelet in them.
   * 
   * @return the composers
   */
  Composer[] getComposers();

  /**
   * Adds <code>pagelet</code> as the last pagelet in the specified composer and
   * returns it with an updated {@link PageletURI}.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer to put the pagelet
   * @return the updated pagelet
   */
  Pagelet addPagelet(Pagelet pagelet, String composer);

  /**
   * Adds <code>pagelet</code> as the last pagelet in the specified composer and
   * returns it with an updated {@link PageletURI}.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer to put the pagelet
   * @param index
   *          the position where the pagelets needs to be put
   * @return the updated pagelet
   * @throws IndexOutOfBoundsException
   *           if <code>index</code> is either smaller than <code>zero</code> or
   *           equals or larger than the number of pagelets already contained in
   *           the composer
   */
  Pagelet addPagelet(Pagelet pagelet, String composer, int index)
      throws IndexOutOfBoundsException;

  /**
   * Removes the pagelet at position <code>index</code> from the specified
   * composer and returns it.
   * <p>
   * <b>Note:</b> The uris of all subsequent pagelet will be updated with their
   * new position (<code>current - 1</code>).
   * 
   * @param composer
   *          the composer
   * @param index
   *          position of the pagelet within the composer
   * @return the removed pagelet
   * @throws IndexOutOfBoundsException
   *           if <code>index</code> is either smaller than <code>zero</code> or
   *           equals or larger than the number of pagelets already contained in
   *           the composer
   */
  Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException;

  /**
   * Returns all pagelets from this page.
   * 
   * @return the pagelets
   */
  Pagelet[] getPagelets();

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer);

  /**
   * Returns the pagelets of the given module and renderer that are contained in
   * the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @param module
   *          the module identifier
   * @param id
   *          the renderer id
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer, String module, String id);

  /**
   * Returns all pagelets from the main composer (stage) as defined by the page
   * template that form the page preview.
   * 
   * @return the preview pagelets
   */
  Pagelet[] getPreview();

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  void addPageContentListener(PageContentListener listener);

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  void removePageContentListener(PageContentListener listener);

}