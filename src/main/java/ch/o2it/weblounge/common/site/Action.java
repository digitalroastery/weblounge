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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

/**
 * An <code>Action</code> is executed, if a request to a url occurs, that
 * has this handler registered. <br>
 * What happens when the registered url is called is as follows: If a template
 * has been registered, then the request is forwarded to that template and
 * various callbacks are made to this handler while the template is being
 * rendered. These callback are:
 * <ul>
 * <li>startPage</li>
 * <li>startComposer</li>
 * <li>startPagelet</li>
 * </ul>
 * If no template definition is found, then the request is forwarded to the
 * template that was used to send the request (by clicking a link).
 * <p>
 * <b>Note:</b> A class that implements the <code>Action</code> interface
 * has to provide a default constructor (no arguments), since action handlers
 * are created using reflection.
 */
public interface Action extends Composeable {

  /** The target url */
  final static String TARGET = "target-url";

  /** Constant indicating that the current page should be evaluated */
  final static int EVAL_PAGE = 0;

  /** Constant indicating that the current page should not be evaluated */
  final static int SKIP_PAGE = 1;

  /** Constant indicating that the includes should be evaluated */
  final static int EVAL_INCLUDES = 0;

  /** Constant indicating that the includes should not be evaluated */
  final static int SKIP_INCLUDES = 1;

  /** Constant indicating that the current composer should be evaluated */
  final static int EVAL_COMPOSER = 0;

  /** Constant indicating that the current composer should not be evaluated */
  final static int SKIP_COMPOSER = 1;

  /** Constant indicating that the current pagelet should be evaluated */
  final static int EVAL_PAGELET = 0;

  /** Constant indicating that the current pagelet should not be evaluated */
  final static int SKIP_PAGELET = 1;

  /** Constant indicating that the action will be forwarded to a template */
  final static int METHOD_TEMPLATE = 0;

  /** Constant indicating that the action will be forwarded to a url */
  final static int METHOD_URL = 1;

  /**
   * This method is called just before the call to <code>startPage()</code> is
   * made. Here, the desired rendering method is passed, as well as the request
   * and the response object.
   * <p>
   * The intention of the call to this method is, that actions may prepare
   * itself for the call to <code>startPage()</code> by extracting state
   * information from the request, opening database connections etc. It is not
   * recommended to write anything to the response object except for the case
   * that it is clear that the action cannot be executed, e. g. because the user
   * is not authorized. In this case, the method should send back the proper
   * <code>HTTP</code> error code and throw the
   * <code>ActionException</code>.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @param page
   *          the underlying page
   * @param template
   *          the template used to render the request
   * @param method
   *          the quested output method
   * @throws ActionException
   *           if this handler refuses to handle the exception
   */
  void configure(WebloungeRequest request, WebloungeResponse response,
      Page page, Renderer template, String method)
      throws ActionException;

  /**
   * This method is called by the target page and gives the action the
   * possibility to either completely take control over what is returned to the
   * user or to have the template render the page. <br>
   * However, there are a few callbacks that are performed by the resulting page
   * to give the action implementation a chance to modify certain elements on
   * the page, namely either the whole page or composer contents. before any
   * output is written to the response object. Implementing classes may return
   * one out of two values:
   * <ul>
   * <li><code>EVAL_PAGE</code> to have the page displayed as usual</li>
   * <li><code>SKIP_PAGE</code> to skip any content on this page</li>
   * </ul>
   * If <code>SKIP_PAGE</code> is returned, then the action is responsible for
   * writing any output to the response object, since control of rendering is
   * transferred completely. <br>
   * If <code>EVAL_PAGE</code> is returned, the page will control the rendering
   * of the page, and the action may only change the output of the composers or
   * pagelets.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_PAGE</code> or <code>SKIP_PAGE</code> depending
   *         on whether the action wants to render the page on its own or have
   *         the template do the rendering.
   */
  int startPage(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

  /**
   * This method is called by the target page and gives the action the
   * possibility to either replace the includes in the page header, add more
   * includes to the existing ones or have the page handle the includes.
   * 
   * Implementing classes may return one out of two values:
   * <ul>
   * <li><code>EVAL_INCLUDES</code> to have the page handle the includes</li>
   * <li><code>SKIP_INCLUDES</code> to skip any includes by this page</li>
   * </ul>
   * If <code>SKIP_INCLUDES</code> is returned, then the action is responsible
   * for writing any output to the response object, since control of rendering
   * is transferred completely. <br>
   * If <code>EVAL_INCLUDES</code> is returned, the page will control the adding
   * of includes, while the action may still add some itself.
   * 
   * <b>Note</b> This callback will only be performed if the page contains a
   * &lt;webl:inlcudes/&gt; tag in the header section.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_INCLUDES</code> or <code>SKIP_INCLUDES</code>
   */
  int startIncludes(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

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
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @throws ActionException
   *           if rendering fails
   */
  int startStage(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

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
   *          the composer identifier
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @throws ActionException
   *           if rendering fails
   */
  int startComposer(WebloungeRequest request, WebloungeResponse response,
      String composer) throws ActionException;

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
   * @param composer
   *          the composer identifier
   * @param position
   *          the pagelet position within the composer
   * @return either <code>EVAL_PAGELET</code> or <code>SKIP_PAGELET</code>
   *         depending on whether the action wants to render the pagelet on its
   *         own or have the template do the rendering.
   * @throws ActionException
   *           if rendering fails
   */
  int startPagelet(WebloungeRequest request, WebloungeResponse response,
      String composer, int position) throws ActionException;

  /**
   * Returns <code>true</code> if the given method is supported by the action.
   * The method is used to lookup a rendering method for a given action id.
   * 
   * @param method
   *          the method name
   * @return <code>true</code> if the action supports the method
   */
  boolean provides(String method);

  /**
   * Returns the supported action methods. The meaning of methods is the
   * possible output format of an action. Therefore, the methods usually include
   * <tt>html</tt>, <tt>pdf</tt> and so on.
   * 
   * @return the supported methods
   */
  String[] getFlavors();

  /**
   * This method is called after the request has been processed by the action.
   * Use this method to release any resources that might have been acquired.
   */
  void cleanup();

  /**
   * Returns the associated site or <code>null</code> if no site has been set.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the associated module or <code>null</code> if no module has been
   * set.
   * 
   * @return the module
   */
  Module getModule();

}