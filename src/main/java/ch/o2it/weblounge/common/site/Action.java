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

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * An <code>Action</code> is executed, if a request to a url occurs, that has
 * this handler registered. <br>
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
 * <b>Note:</b> A class that implements the <code>Action</code> interface has to
 * provide a default constructor (no arguments), since action handlers are
 * created using reflection.
 */
public interface Action extends Composeable, Customizable {

  /** The target url */
  final static String TARGET = "target-url";

  /** Constant indicating that the current request should be evaluated */
  final static int EVAL_REQUEST = 0;

  /** Constant indicating that the current request should not be evaluated */
  final static int SKIP_REQUEST = 1;

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

  /** the default valid time is 60 minutes */
  long DEFAULT_VALID_TIME = 60L * 60L * 1000L;

  /** the default recheck time is 1 minute */
  long DEFAULT_RECHECK_TIME = 60L * 1000L;

  /**
   * This method is called just before the call to <code>startPage()</code> is
   * made. Here, the desired rendering method is passed, as well as the request
   * and the response object.
   * <p>
   * The intention of this method is that actions may prepare themselves for the
   * subsequent calls to <code>startPage()</code> by extracting state
   * information from the request, opening database connections etc.
   * <p>
   * It is not recommended to write anything to the response object yet, except
   * for the case that it is obvious that the action cannot be executed, e. g.
   * because the user is not authorized. In this case, the method should send
   * back the proper <code>HTTP</code> error code and throw an
   * {@link ActionException}.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @param flavor
   *          the quested output flavor
   * @throws ActionException
   *           if this handler refuses to handle the exception
   */
  void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException;

  /**
   * This method is called by the action request handler and gives the action
   * the possibility to either completely take control over what is returned to
   * the user or to have the template render the page. This is also the perfect
   * place to write <code>HTTP</code> headers to the response.
   * <p>
   * However, there are a few callbacks that are performed by the resulting page
   * to give the action implementation a chance to modify certain elements on
   * the page, namely either the whole page or composer contents. before any
   * output is written to the response object. Implementing classes may return
   * one out of two values:
   * <ul>
   * <li><code>EVAL_REQUEST</code> to have the page displayed as usual</li>
   * <li><code>SKIP_REQUEST</code> to skip any content on this page</li>
   * </ul>
   * If <code>SKIP_REQUEST</code> is returned, then the action is responsible
   * for writing any output to the response object, since control of rendering
   * is transferred completely. <br>
   * If <code>EVAL_REQUEST</code> is returned, the page will control the
   * rendering of the page, and the action may only change the output of the
   * composers or pagelets.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_REQUEST</code> or <code>SKIP_REQUEST</code>
   *         depending on whether the action wants to continue rendering the
   *         page or have the template do the rendering.
   */
  int startHTMLResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

  /**
   * This method is called by the {@link ActionRequestHandler} if and only if
   * <ol>
   * <li>the action supports the {@link RequestFlavor#XML} request flavor</li>
   * <li>the user specifies that same flavor in his request</li>
   * <li>the action returns {@link #EVAL_REQUEST} upon the preceding call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)}</li>
   * </ol>
   * <p>
   * Implementers should use this method to set all relevant <code>HTTP</code>
   * headers (other than <code>Content-Type", "text/xml; charset=utf-8</code>,
   * which is set by the request handler itself) and write the <code>XML</code>
   * data to the response.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @throws ActionException
   *           if generating the <code>XML</code> response results in an error
   */
  void startXMLResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

  /**
   * This method is called by the {@link ActionRequestHandler} if and only if
   * <ol>
   * <li>the action supports the {@link RequestFlavor#JSON} request flavor</li>
   * <li>the user specifies that same flavor in his request</li>
   * <li>the action returns {@link #EVAL_REQUEST} upon the preceding call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)}</li>
   * </ol>
   * <p>
   * Implementers should use this method to set all relevant <code>HTTP</code>
   * headers (other than <code>Content-Type", "text/json; charset=utf-8</code>,
   * which is set by the request handler itself) and write the <code>JSON</code>
   * data to the response.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @throws ActionException
   *           if generating the <code>JSON</code> response results in an error
   */
  void startJSONResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException;

  /**
   * This method is called by the target page and gives the action the
   * possibility to either replace the includes in the page header, add more
   * includes to the existing ones or have the page handle the includes.
   * <p>
   * Note that this callback relies on the existence of the corresponding
   * tag in the <code>&lt;head&gt;</code> section of the page template.
   * <p>
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
  int startPageIncludes(WebloungeRequest request, WebloungeResponse response)
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
   * Returns the url to this action.
   * 
   * @return the action url
   */
  WebUrl getUrl();

  /**
   * Adds the given flavor to the list of supported flavors.
   * 
   * @param flavor
   *          the flavor to add
   */
  void addFlavor(RequestFlavor flavor);

  /**
   * Removes the given flavor from the list of supported flavors.
   * 
   * @param flavor
   *          the flavor to remove
   */
  void removeFlavor(RequestFlavor flavor);

  /**
   * Returns the supported content flavors. The meaning of flavors are the
   * possible output formats of an action. Common flavors include include
   * <code>HTML</code>, <code>XML</code> or <code>JSON</code>.
   * 
   * @return the supported content flavors
   */
  RequestFlavor[] getFlavors();

  /**
   * Returns <code>true</code> if the action supports the given content flavor.
   * Common flavors might be <code>HTML</code>, <code>XML</code> or
   * <code>JSON</code>.
   * 
   * @param flavor
   *          the flavor
   * @return <code>true</code> if the action can create the requested content
   *         flavor
   */
  boolean supportsFlavor(RequestFlavor flavor);

  /**
   * Sets the action mountpoint.
   * 
   * @param path
   *          the mountpoint
   */
  void setPath(String path);

  /**
   * Returns the mountpoint used to call the action. The path is interpreted
   * relative to the site root.
   * <p>
   * The extension can either be empty, <code>/*</code> or <code>/**</code>,
   * depending on whether to match only the mountpoint (e. g. <code>/news</code>
   * ), to match the mountpoint and any direct children (e. g.
   * <code>/news/today</code>) or the mountpoint and any subsequent urls.
   * 
   * @return the action mountpoint
   */
  String getPath();

  /**
   * Sets the uri of the page that is used to render the action.
   * 
   * @param uri
   *          the page uri
   */
  void setPageURI(PageURI uri);

  /**
   * Returns the uri of the page that is used to deliver the initial content for
   * this action or <code>null</code> if no page has been set.
   * 
   * @return the page uri
   */
  PageURI getPageURI();

  /**
   * Sets the page that is used to render the action. This method is called
   * right before the action is executed by a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, Renderer, String)}
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
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, Renderer, String)}
   * .
   * 
   * @return the page
   */
  Page getPage();

  /**
   * Sets the page template that is used to render the action content. This
   * method is called right before the action is executed by a call to
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, Renderer, String)}
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
   * {@link #configure(WebloungeRequest, WebloungeResponse, Page, Renderer, String)}
   * .
   * 
   * @return the page template
   */
  PageTemplate getTemplate();

  /**
   * This method is used at initialization time and sets the site that was used
   * to define this action.
   * 
   * @param site
   *          the site
   */
  void setSite(Site site);

  /**
   * Returns the associated site or <code>null</code> if no site has been set.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * This method is used at initialization time and sets the module that was
   * used to define this action.
   * 
   * @param module
   *          the module
   */
  void setModule(Module module);

  /**
   * Returns the associated module or <code>null</code> if no module has been
   * set.
   * 
   * @return the module
   */
  Module getModule();

  /**
   * Notifies the action that it is about to be used. Actions are pooled
   * resources and this callback indicates that the action was taken out of the
   * pool of currently idle instances.
   * <p>
   * <b>Note:</b> Subclasses need to make sure to call the super implementation
   * in order to not interfere with their ancestor's implementation.
   */
  void activate();

  /**
   * Notifies the action that it is about to be put back into the pool of
   * currently idle actions. Use this callback to release any resources that the
   * action might be holding to up until now.
   * <p>
   * <b>Note:</b> Subclasses need to make sure to call the super implementation
   * in order to not interfere with their ancestor's implementation.
   */
  void passivate();

}