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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ActionException;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * This support implementation provides special methods for encoding
 * <code>Ajax</code> responses.
 */
public abstract class AbstractAjaxAction extends ActionSupport {

  /**
   * Method that is called when the handler can start sending the response.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   */
  public abstract void startAjaxResponse(WebloungeRequest request,
      WebloungeResponse response);

  /**
   * This method prepares the action handler for the next upcoming request by
   * passing it the request and response for first analysis as well as the
   * desired output method.
   * <p>
   * It is not recommended that subclasses use this method to write anything to
   * the response. The call serves the single purpose acquire resources and set
   * up for the call to <code>startPage</code>.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @param method
   *          the output method
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#configure(WebloungeRequest,
   *      WebloungeResponse, String)
   */
  public final void configure(WebloungeRequest request,
      WebloungeResponse response, String method) {
    super.configure(request, response, method);
    response.setHeader("Expires", "Mon, 4 Jun 2007 00:00:00 GMT");
    response.setHeader("Last-Modified", WebloungeDateFormat.formatStatic(new Date()));
    response.setHeader("Cache-Control", "no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    startAjaxResponse(request, response);
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the page.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_PAGE</code> or <code>SKIP_PAGE</code> depending
   *         on whether the action wants to render the page on its own or have
   *         the template do the rendering.
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#startPage(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse)
   */
  public final int startPage(WebloungeRequest request,
      WebloungeResponse response) throws ActionException {
    return SKIP_PAGE;
  }

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
  public final int startIncludes(WebloungeRequest request,
      WebloungeResponse response) throws ActionException {
    return SKIP_INCLUDES;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the composer.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public final int startStage(WebloungeRequest request,
      WebloungeResponse response) throws ActionException {
    return SKIP_COMPOSER;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the composer.
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
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public final int startComposer(WebloungeRequest request,
      WebloungeResponse response, String composer) throws ActionException {
    return SKIP_COMPOSER;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the pagelet.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param composer
   *          the composer identifier
   * @param position
   *          the pagelet position
   * @return either <code>EVAL_PAGELET</code> or <code>SKIP_PAGELET</code>
   *         depending on whether the action wants to render the pagelet on its
   *         own or have the template do the rendering.
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#startPagelet(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String,
   *      int)
   */
  public final int startPagelet(WebloungeRequest request,
      WebloungeResponse response, String composer, int position)
      throws ActionException {
    return SKIP_PAGELET;
  }

  /**
   * This method is called when the request is finished and the action handler
   * is no longer needed. Use this method to release any resources that have
   * been acquired to process the request.
   * 
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#cleanup()
   */
  public void cleanup() {
  }

  /**
   * Sends back an ajax - compatible element response, which will look similar
   * to this one:
   * 
   * <pre>
   * &lt;ajax-response&gt; 
   *     &lt;response type=&quot;element&quot; id=&quot;personInfo&quot;&gt; 
   *         &lt;div class=&quot;person&quot;&gt;
   *             &lt;span class=&quot;personName&quot;&gt;Mr. Pat Barnes&lt;/span&gt;
   *             &lt;span class=&quot;personAddress&quot;&gt;1743 1st Avenue Boston, Boston 71204-2345&lt;/span&gt;
   *         &lt;/div&gt; 
   *     &lt;/response&gt; 
   * &lt;/ajax-response&gt;
   * </pre>
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param id
   *          the ajax id
   * @param body
   *          the ajax response body
   * @throws IOException
   *           if writing to the response failed
   */
  protected void sendAjaxElement(WebloungeRequest request,
      WebloungeResponse response, String id, String body) throws IOException {
    sendAjaxResponse(request, response, "element", id, body);
  }

  /**
   * Sends back an ajax - compatible object response, which will look similar to
   * this one:
   * 
   * <pre>
   * &lt;ajax-response&gt; 
   *     &lt;response type=&quot;element&quot; id=&quot;personInfo&quot;&gt; 
   *     &lt;person fullName=&quot;Pat Barnes&quot; streetAddress=&quot;1743 1st Avenue&quot; city=&quot;Boston&quot; state=&quot;Boston&quot; zipcode=&quot;71204-2345&quot; /&gt;
   *     &lt;/response&gt; 
   * &lt;/ajax-response&gt;
   * </pre>
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param id
   *          the ajax id
   * @param body
   *          the ajax response body
   * @throws IOException
   *           if writing to the response failed
   */
  protected void sendAjaxObject(WebloungeRequest request,
      WebloungeResponse response, String id, String body) throws IOException {
    sendAjaxResponse(request, response, "object", id, body);
  }

  /**
   * Sends back an ajax - compatible object response, which will look similar to
   * this one:
   * 
   * <pre>
   * &lt;ajax-response&gt; 
   *     &lt;response type=&quot;element&quot; id=&quot;personInfo&quot;&gt; 
   *     &lt;person fullName=&quot;Pat Barnes&quot; streetAddress=&quot;1743 1st Avenue&quot; city=&quot;Boston&quot; state=&quot;Boston&quot; zipcode=&quot;71204-2345&quot; /&gt;
   *     &lt;/response&gt; 
   * &lt;/ajax-response&gt;
   * </pre>
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param uri
   *          the ajax id
   * @param body
   *          the ajax response body
   * @throws IOException
   *           if writing to the response failed
   */
  protected void sendAjaxResponse(WebloungeRequest request,
      WebloungeResponse response, List<AjaxResponsePart> parts)
      throws IOException {
    StringBuilder s = new StringBuilder();
    s.append("<ajax-response>");
    for (AjaxResponsePart part : parts) {
      s.append("<response type=\"" + part.getType() + "\" id=\"" + part.getId() + "\">");
      s.append(part.getBody());
      s.append("</response>");
    }
    s.append("<ajax-response>");
    response.getOutputStream().print(s.toString());
  }

  /**
   * Sends back an ajax - compatible object response.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param type
   *          the response type, either "element" or "object"
   * @param id
   *          the ajax id
   * @param body
   *          the ajax response body
   * @throws IOException
   *           if writing to the response failed
   */
  private void sendAjaxResponse(WebloungeRequest request,
      WebloungeResponse response, String type, String id, String body)
      throws IOException {
    StringBuilder s = new StringBuilder();
    s.append("<ajax-response>");
    s.append("<response type=\"" + type + "\" id=\"" + id + "\">");
    s.append(body);
    s.append("</response>");
    s.append("</ajax-response>");
    response.getOutputStream().print(s.toString());
  }

  /**
   * Creates an element response part for ajax.
   * 
   * @param id
   *          the response id
   * @param body
   *          the response body
   * @return the ajax response part
   */
  protected AjaxResponsePart createAjaxElement(String id, String body) {
    return new AjaxResponsePart(id, body, "element");
  }

  /**
   * Creates an object response part for ajax.
   * 
   * @param id
   *          the response id
   * @param body
   *          the response body
   * @return the ajax response part
   */
  protected AjaxResponsePart createAjaxObject(String id, String body) {
    return new AjaxResponsePart(id, body, "object");
  }

  /**
   * Sends back a JSON - compatible object response, which will look similar to
   * this one:
   * 
   * <pre>
   * {
   *     firstname : john;
   *     lastname : doe;
   * }
   * </pre>
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param json
   *          the json object
   * @throws IOException
   *           if writing to the response failed
   */
  protected void sendJSONresponse(WebloungeRequest request,
      WebloungeResponse response, JSONObject json) throws IOException {
    response.setContentType("text/json");
    response.getOutputStream().print(json.toString());
  }

  /**
   * Small wrapper for AJAX response parts.
   */
  protected class AjaxResponsePart {

    /** The response id */
    String id = null;

    /** The response body */
    String body = null;

    /** The response type */
    String type = null;

    /**
     * Creates a new <code>AjaxResponse</code> with the given identifier,
     * 
     * 
     * @param id
     *          the id
     * @param body
     *          the response body
     * @pararm type the response type
     */
    public AjaxResponsePart(String id, String body, String type) {
      this.id = id;
      this.body = body;
      this.type = type;
    }

    /**
     * @return Returns the body.
     */
    public String getBody() {
      return body;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
      return id;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
      return type;
    }

  }

}