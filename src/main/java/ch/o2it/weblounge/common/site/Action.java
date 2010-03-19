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
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.url.WebUrl;

import java.io.IOException;

/**
 * An <code>Action</code> is java code that can be mounted to a specific url and
 * that will be executed if these conditions hold:
 * <ol>
 * <li>The request is directly targeted at the action's mountpoint</li>
 * <li>The action supports the requested flavor</li>
 * </ol>
 * <p>
 * Once a request hits the mountpoint of an action, the action is then called in
 * this order:
 * <ol>
 * <li>{@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)}</li>
 * <li>{@link #startResponse(WebloungeRequest, WebloungeResponse)}</li>
 * </ol>
 * By implementing an extended interface such as {@link HTMLAction},
 * {@link XMLAction} or {@link JSONAction}, more sophisticated patterns for
 * request handling and output generation are supported.
 * <p>
 * <b>Note:</b> A class that implements the <code>Action</code> interface must
 * provide a default constructor (no arguments), since action handlers are
 * created using reflection.
 */
public interface Action extends Composeable, Customizable {

  /** Constant indicating that the current request should be evaluated */
  int EVAL_REQUEST = 0;

  /** Constant indicating that the current request should not be evaluated */
  int SKIP_REQUEST = 1;

  /** the default valid time is 60 minutes */
  long DEFAULT_VALID_TIME = 60L * 60L * 1000L;

  /** the default recheck time is 1 minute */
  long DEFAULT_RECHECK_TIME = 60L * 1000L;

  /**
   * This method is the first call to an action once a request is dispatched to
   * it. Here, the requested flavor is passed as well as the request and the
   * response object in order to allow the action to validate the request and
   * prepare data that it might need to satisfy subsequent calls regarding the
   * processing of the request.
   * <p>
   * <b>Note:</b> It is not recommended to write anything to the response object
   * yet, except for the case that it is obvious that the action cannot be
   * executed, e. g. because the user is not authorized. In this case, the
   * method should send back the proper <code>HTTP</code> error code and throw
   * an {@link ActionException}.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @param flavor
   *          the quested output flavor
   * @throws ActionException
   *           if this action cannot handle the request
   */
  void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException;

  /**
   * This method is called after the action was able to validate the request and
   * prepare for further request processing in
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)} and
   * is considered the perfect place to write <code>HTTP</code> headers such as
   * the content type to the response.
   * <p>
   * The action now has two choices: it can either completely take over control
   * in serving the request by writing everything to the response and then
   * returning {@link #SKIP_REQUEST}. Or it could just write the necessary
   * <code>HTTP</code> headers to the response and then return
   * {@link #EVAL_REQUEST}, in which case the action request handler will
   * continue with the callback protocol depending on the actual type of the
   * action (<code>HTMLAction</code>, <code>XMLAction</code> or
   * <code>JSONAction</code>).
   * <p>
   * <b>Note:</b> the action request handler that is calling this action will
   * already take care of those <code>HTTP</code> headers that deal with
   * caching. If you would like to influence this behavior, make sure to
   * implement <code>getRecheckTime()</code> accordingly rather than setting the
   * headers yourself.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_REQUEST</code> or <code>SKIP_REQUEST</code>
   * @throws IOException
   *           if writing to the response fails
   * @throws ActionException
   *           if a processing error occurs while handling the request
   */
  int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException;

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