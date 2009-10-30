/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.o2it.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO: Comment WebloungeDispatcherServlet
 */
public class WebloungeDispatcherServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 8939686825567275614L;

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(WebloungeDispatcherServlet.class);

  /** The sites that are online */
  private SiteTracker siteTracker = null;

  /**
   * Creates a new instance of the weblounge dispatcher servlet.
   */
  WebloungeDispatcherServlet(SiteTracker siteTracker) {
    if (siteTracker == null)
      throw new IllegalArgumentException("Site tracker cannot be null");
    this.siteTracker = siteTracker;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doDelete(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Site site = getSiteByRequest(request);
    if (site != null) {
      // TODO: Add object pooling for request and response
      WebloungeRequestImpl webloungeRequest = new WebloungeRequestImpl(request);
      WebloungeResponseImpl webloungeResponse = new WebloungeResponseImpl(response);
      webloungeRequest.init(site);
      site.dispatch(webloungeRequest, webloungeResponse);
    } else {
      super.doGet(request, response);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doHead(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doOptions(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doOptions(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doPost(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doPut(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doTrace(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.doTrace(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
   */
  @Override
  protected long getLastModified(HttpServletRequest req) {
    // TODO Auto-generated method stub
    return super.getLastModified(req);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    super.service(request, response);
  }

  /**
   * Returns the site that is being targeted by <code>request</code> or
   * <code>null</code> if either no site was found or the site is disabled right
   * now.
   * 
   * @param request
   *          the http request
   * @return the target site or <code>null</code>
   */
  private Site getSiteByRequest(HttpServletRequest request) {
    Site site = siteTracker.getSiteByName(request.getServerName());
    if (site != null && !site.isEnabled()) {
      log_.debug("Ignoring request for disabled site " + site);
      return null;
    }
    return site;
  }

  /**
   * {@inheritDoc}
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Weblounge Dispatcher Servlet";
  }
  
}