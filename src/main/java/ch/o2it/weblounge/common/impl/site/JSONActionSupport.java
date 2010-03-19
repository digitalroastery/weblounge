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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.JSONAction;

import java.io.IOException;

/**
 * This class is the default implementation for an <code>JSONAction</code>. The
 * implementations of the <code>startJSON()</code> is empty, so nothing is
 * returned as the <code>JSON</code> body of the response. Therefore, subclasses
 * need to overwrite this method in order to return meaningful content.
 * <p>
 * <b>Note:</b> Be aware of the fact that actions are pooled, so make sure to
 * implement the <code>activate()</code> and <code>passivate()</code> method
 * accordingly and include the respective super implementations.
 */
public class JSONActionSupport extends AbstractActionSupport implements JSONAction {

  /**
   * This implementation always returns {@link Action#EVAL_REQUEST} and simply
   * sets the content type on the response to
   * <code>text/json;charset=utf-8</code>.
   * <p>
   * This means that subclasses should either overwrite this method to specify a
   * different encoding or make sure that everything that is written to the
   * response is encoded to <code>utf-8</code>.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return {@link Action#EVAL_REQUEST}
   */
  @Override
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    response.setContentType("text/json;charset=utf-8");
    return EVAL_REQUEST;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.JSONAction#startJSON(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startJSON(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    return;
  }

}
