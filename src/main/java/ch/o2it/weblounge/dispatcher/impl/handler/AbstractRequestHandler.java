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

package ch.o2it.weblounge.dispatcher.impl.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.request.RequestHandler;
import ch.o2it.weblounge.common.request.RequestHandlerConfiguration;
import ch.o2it.weblounge.common.request.WebloungeRequest;

/**
 * This is an abstract base implementation for request handlers.
 * 
 * @version $Revision: 1090 $ $Date: 2009-09-14 19:43:19 +0200 (Mon, 14 Sep 2009) $
 * @author Daniel Steiner
 */
public abstract class AbstractRequestHandler implements RequestHandler {

	/** the handler configuration */
	protected RequestHandlerConfiguration config;
	
	/**
	 * @see ch.o2it.weblounge.common.request.RequestHandler#configure(ch.o2it.weblounge.common.request.RequestHandlerConfiguration)
	 */
	public final void configure(RequestHandlerConfiguration config)
		throws ConfigurationException {
		this.config = config;
		configureHandler(config);
	}
	
	
	/**
	 * @see ch.o2it.weblounge.common.request.RequestHandler#getIdentifier()
	 */
	public String getIdentifier() {
		return config.getIdentifier();
	}
	
	
	/**
	 * @see ch.o2it.weblounge.common.request.RequestHandler#getName()
	 */
	public String getName() {
		return config.getName();
	}
	
	
	/**
	 * @see ch.o2it.weblounge.common.request.RequestHandler#getDescription()
	 */
	public String getDescription() {
		return config.getDescription();
	}
	
	/**
	 * Configures the request handler.
	 * 
	 * @param config the handler configuration
	 * @throws ConfigurationException if the configuration is invalid
	 */
	protected abstract void configureHandler(RequestHandlerConfiguration config)
		throws ConfigurationException;

	/**
	 * Checks if the parameter <code>parameter</code> is present in the request and
	 * is not equal to the empty string.
	 * 
	 * @return <code>true</code> if the parameter is available
	 */
	protected boolean parameterExists(WebloungeRequest request, String parameter) {
		String p = request.getParameter(parameter);
		return (p != null && !p.equals(""));
	}

	/**
	 * Checks if the parameter <code>parameter</code> is present in the request and
	 * is not equal to the empty string. In this case, the parameter itself is returned,
	 * <code>null</code> otherwise.
	 * <p>
	 * Note that this method includes the check for <tt>hidden</tt> parameters.
	 * 
	 * @return the parameter value or <code>null</code> if the parameter is not available
	 */
	protected String getParameter(WebloungeRequest request, String parameter) {
		String p = request.getParameter(parameter);
		if (p != null) {
			try {
				p = URLDecoder.decode(p.trim(), "UTF-8");
			} catch (UnsupportedEncodingException e) { }
		}
		return (p != null && !p.trim().equals("")) ? p : null;
	}

	/**
	 * Checks if the parameter <code>parameter</code> is present in the request and
	 * is not equal to the empty string. In this case, the parameter itself is returned,
	 * <code>defaultValue</code> otherwise.
	 * <p>
	 * Note that this method includes the check for <tt>hidden</tt> parameters.
	 * 
	 * @return the parameter value or <code>defaultValue</code> if the parameter is not available
	 */
	protected String getParameter(WebloungeRequest request, String parameter, String defaultValue) {
		String p = getParameter(request, parameter);
		return (p != null) ? p : defaultValue;
	}

	/**
	 * Checks if the parameter <code>parameter</code> is present in the request and
	 * is not equal to the empty string. In this case, the parameter itself is returned,
	 * <code>null</code> otherwise.
	 * <p>
	 * Note that this method includes the check for <tt>hidden</tt> parameters.
	 * 
	 * @return <code>null</code> if the parameter is not available
	 */
	protected String getRequiredParameter(WebloungeRequest request, String parameter) throws IllegalArgumentException {
		String p = getParameter(request, parameter);
		if (p == null) {
			throw new IllegalArgumentException(parameter);
		}
		return p;
	}
	
	/**
	 * This method returns without noise if one of the specified parameters can be found in the
	 * request and is not equal to the empty string. Otherwise, a <code>IllegalArgumentException</code>
	 * is thrown.
	 * 
	 * @param request the request
	 * @param parameters the parameter list
	 * @throws IllegalArgumentException if none of the given argument can be found in the request
	 */
	protected void requireAny(WebloungeRequest request, String[] parameters) throws IllegalArgumentException {
		if (parameters == null)
			return;
		for (int i=0; i < parameters.length; i++) {
			if (getParameter(request, parameters[i]) != null)
				return;
		}
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i=0; i < parameters.length; i++) {
			buf.append(parameters[i]);
			if (i < parameters.length - 1)
				buf.append(" | ");
		}
		buf.append("]");
		throw new IllegalArgumentException(buf.toString());
	}

}