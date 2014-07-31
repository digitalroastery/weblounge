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

package ch.entwine.weblounge.common.site;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;

import java.io.IOException;

/**
 * An <code>HTMLAction</code> is java code that can be mounted to a specific url
 * and that will be executed if these conditions hold:
 * <ol>
 * <li>The request is directly targeted at the action's mountpoint</li>
 * <li>The action supports the
 * {@link ch.entwine.weblounge.common.request.RequestFlavor#HTML} request flavor
 * </li>
 * <li>The client specifies that same flavor in his request</li>
 * </ol>
 * <p>
 * Once a request hits the mountpoint of an action, the action can then be
 * called in various ways, depending on whether it is rendered using a template
 * or not. The life cycle using a template follows this pattern:
 * <ol>
 * <li>
 * {@link #configure(WebloungeRequest, WebloungeResponse, ch.entwine.weblounge.common.request.RequestFlavor)}
 * </li>
 * <li>{@link #startResponse(WebloungeRequest, WebloungeResponse)}</li>
 * <li>{@link #startHeader(WebloungeRequest, WebloungeResponse)}</li>
 * <li>{@link #startStage(WebloungeRequest, WebloungeResponse)}</li>
 * <li>{@link #startComposer(WebloungeRequest, WebloungeResponse, String)}</li>
 * <li>{@link #startPagelet(WebloungeRequest, WebloungeResponse, String, int)}</li>
 * </ol>
 * <p>
 * Note that the calls to <code>startHeader()</code>, <code>startStage()</code>,
 * <code>startComposer()</code> and <code>startPagelet()</code> are depending on
 * the return value of preceding calls. See the respective method descriptions
 * for more details.
 * <p>
 * If no template definition is found, then the request is forwarded to the
 * template that was used to send the request (by clicking a link).
 * <p>
 * <b>Note:</b> A class that implements the <code>Action</code> interface has to
 * provide a default constructor (no arguments), since action handlers are
 * created using reflection.
 */
public interface HTMLAction extends Action {

  /** Request parameter name to specify the target page */
  String TARGET_PAGE = "target-page";

  /** Request parameter name to specify the target template */
  String TARGET_TEMPLATE = "target-template";

  /** Constant indicating that the header includes should be evaluated */
  int EVAL_HEADER = 0;

  /** Constant indicating that the header includes should not be evaluated */
  int SKIP_HEADER = 1;

  /** Constant indicating that the current composer should be evaluated */
  int EVAL_COMPOSER = 0;

  /** Constant indicating that the current composer should not be evaluated */
  int SKIP_COMPOSER = 1;

  /** Constant indicating that the current pagelet should be evaluated */
  int EVAL_PAGELET = 0;

  /** Constant indicating that the current pagelet should not be evaluated */
  int SKIP_PAGELET = 1;

  /** Request parameter name for information messages */
  String WARNINGS = "weblwarnings";

  /** Request parameter name for information messages */
  String INFOS = "weblinfos";

  /** Request parameter name for information messages */
  String ERRORS = "weblerrors";

  /**
   * This method is called by the target page and gives the action the
   * possibility to either replace, add or remove <code>HTML</code> elements to
   * and from the <code>&lt;head&gt;</code> section of the page.
   * <p>
   * Note that when this action is used to render content on a page, this method
   * will only be called if the page contains the corresponding
   * {@link HTMLHeaderTag} tag in the <code>&lt;head&gt;</code> section of the
   * page template.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @throws IOException
   *           if writing to the response fails
   * @throws ActionException
   *           if a processing error occurs while handling the request
   * @return either <code>EVAL_HEADER</code> or <code>SKIP_HEADER</code>
   *         depending on whether the action wants to render the header on its
   *         own or have the template do the rendering.
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement
   */
  int startHeader(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException;

  /**
   * This method is called when the rendering of the composer with the given
   * composer identifier starts. Again, the action has the possibility to have
   * the page render the composer or to do it on its own. <br>
   * The valid return codes for this method are:
   * <ul>
   * <li><code>EVAL_COMPOSER</code> to display the composer as usual</li>
   * <li><code>SKIP_COMPOSER</code> to skip any content on this composer</li>
   * </ul>
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param composer
   *          the stage composer
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @throws IOException
   *           if writing to the response fails
   * @throws ActionException
   *           if a processing error occurs while handling the request
   */
  int startStage(WebloungeRequest request, WebloungeResponse response,
      Composer composer) throws IOException, ActionException;

  /**
   * This method is called when the rendering of the composer with the given
   * composer identifier starts. Again, the action has the possibility to have
   * the page render the composer or to do it on its own. <br>
   * The valid return codes for this method are:
   * <ul>
   * <li><code>EVAL_COMPOSER</code> to display the composer as usual</li>
   * <li><code>SKIP_COMPOSER</code> to skip any content on this composer</li>
   * </ul>
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param composer
   *          the composer
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @throws IOException
   *           if writing to the response fails
   * @throws ActionException
   *           if a processing error occurs while handling the request
   */
  int startComposer(WebloungeRequest request, WebloungeResponse response,
      Composer composer) throws IOException, ActionException;

  /**
   * This method is called when the rendering of the composer with the given
   * composer identifier starts. Again, the action has the possibility to have
   * the page render the composer or to do it on its own. <br>
   * The valid return codes for this method are:
   * <ul>
   * <li><code>EVAL_PAGELET</code> to display the pagelet as specified by the
   * template</li>
   * <li><code>SKIP_PAGELET</code> to have the pagelet rendered by this method</li>
   * </ul>
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param pagelet
   *          the pagelet
   * @return either <code>EVAL_PAGELET</code> or <code>SKIP_PAGELET</code>
   *         depending on whether the action wants to render the pagelet on its
   *         own or have the template do the rendering.
   * @throws IOException
   *           if writing to the response fails
   * @throws ActionException
   *           if a processing error occurs while handling the request
   */
  int startPagelet(WebloungeRequest request, WebloungeResponse response,
      Pagelet pagelet) throws IOException, ActionException;

  /**
   * Sets the uri of the page that is used to render the action.
   * 
   * @param uri
   *          the page uri
   */
  void setPageURI(ResourceURI uri);

  /**
   * Returns the uri of the page that is used to deliver the initial content for
   * this action or <code>null</code> if no page has been set.
   * 
   * @return the page uri
   */
  ResourceURI getPageURI();

  /**
   * Sets the page that is used to render the action. This method is called
   * right before the action is executed by a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, ch.entwine.weblounge.common.content.Renderer, String)}
   * .
   * 
   * @param page
   *          the page
   */
  void setPage(Page page);

  /**
   * Returns the page that is used to deliver the initial content for this
   * action or <code>null</code> if no page has been set. In any case, the page
   * will be set prior to a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, ch.entwine.weblounge.common.content.Renderer, String)}
   * .
   * 
   * @return the page
   */
  Page getPage();

  /**
   * Sets the page template that is used to render the action content. This
   * method is called right before the action is executed by a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, ch.entwine.weblounge.common.content.Renderer, String)}
   * .
   * 
   * @param template
   *          the page template
   */
  void setTemplate(PageTemplate template);

  /**
   * Returns the page template that is used to render the action content or
   * <code>null</code> if no template has been specified. In any case, the
   * template will be set prior to a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, ch.entwine.weblounge.common.content.Renderer, String)}
   * .
   * 
   * @return the page template
   */
  PageTemplate getTemplate();

  /**
   * Sets the page template that is used by this action by default to render the
   * action content.
   * 
   * @param template
   *          the default page template
   */
  void setDefaultTemplate(PageTemplate template);

  /**
   * Returns the default page template that is used to render the action content
   * or <code>null</code> if no default template has been specified.
   * 
   * @return the default page template
   */
  PageTemplate getDefaultTemplate();

}