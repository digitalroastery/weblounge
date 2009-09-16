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

package ch.o2it.weblounge.dispatcher.impl.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.site.Site;

public class WebdavRequestWrapper extends HttpServletRequestWrapper {

	/**
	 * Creates a new WebDAV request to be used in conjunction with the slide
	 * webdav server.
	 * 
	 * @param request the original request
	 */
	public WebdavRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	/**
	 * This method is implemented to fool the slide implementation. It is not obvious how to
	 * tell slide where its root url is. Since slide appends this path to the root context if its
	 * <code>isDefaultServlet()</code> method returns false, this is an easy way to move
	 * slide's root context to the repository mountpoint. If the attribute <code>slide-request</code>
	 * is set to <code>true</code>, then the servlet path equals the site's repository mountpoint,
	 * otherwise this method is handled by the super implementation.
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		Site site = RequestSupport.getSite((HttpServletRequest)getRequest());
		return UrlSupport.concat(super.getServletPath(), site.getRepository().getMountpoint().getPath());
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		Site site = RequestSupport.getSite((HttpServletRequest)getRequest());
		String path = super.getPathInfo();
		String mountpoint = UrlSupport.concat("/", site.getRepository().getMountpoint().getPath());
		if (path != null && path.startsWith(mountpoint)) {
			path = path.substring(mountpoint.length());
			if (path.equals("")) {
				path = "/";
			}
		}
		return path;
	}

}