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

package ch.entwine.weblounge.test.site;

import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.JSONAction;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test action that is able to render a greeting on the site template.
 */
public class GreeterJSONAction extends GreeterHTMLAction implements JSONAction {

  /**
   * Creates an extension of the <code>GreeterAction</code> that can handle
   * <code>JSON</code> requests.
   */
  public GreeterJSONAction() {
    clearFlavors();
    addFlavor(RequestFlavor.JSON);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.JSONAction#startJSON(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public void startJSON(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    try {
      String language = RequestUtils.getParameter(request, LANGUAGE_PARAM);
      Map<String, String> data = new HashMap<String, String>();
      data.put(language, greeting);
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(response.getWriter(), data);
    } catch (IOException e) {
      throw new ActionException("Unable to send json response", e);
    }
  }

}
