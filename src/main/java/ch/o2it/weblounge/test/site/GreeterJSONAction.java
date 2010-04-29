/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.JSONAction;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * Simple test action that is able to render a greeting on the site template.
 */
public class GreeterJSONAction extends GreeterAction implements JSONAction {

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
   * @see ch.o2it.weblounge.common.site.JSONAction#startJSON(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startJSON(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    try {
      JSONObject json = new JSONObject();
      json.put("greetings", greetings);
      IOUtils.copy(new StringReader(json.toString()), response.getWriter());
    } catch (IOException e) {
      throw new ActionException("Unable to send json response", e);
    } catch (JSONException e) {
      throw new ActionException("Unable to create json response", e);
    }
  }

}
