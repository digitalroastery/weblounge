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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.site.Action;

/**
 * A <code>ActionHandlerException</code> is thrown if an exceptional
 * state is reached when executing a <code>ActionHandler</code> to
 * create the output for either a page or a single page element.
 *
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class ActionHandlerException extends RuntimeException {

	/** Serial version id */
	private static final long serialVersionUID = 1L;

	/** ActionHandler name, e. g. <code>XSLActionHandler</code> */
	private Action handler_;
	
	/** Rendering method, e. g. <code>html</code>" */
	private String method_;
	
	/** The exception that lead to this one */
	private Throwable exception_;
	
	/**
	 * Creates a new <code>ActionHandlerException</code> providing the information
	 * passed by the parameters.
	 * 
	 * @param handler the handler, e. g. <code>XLSElementActionHandler</code>
	 * @param method the output method, e. g. <code>html</code>
	 */
	public ActionHandlerException(Action handler, String method) {
		handler_ = handler;
		method_ = method;
	}

	/**
	 * Creates a new <code>ActionHandlerException</code> providing the information
	 * passed by the parameters.
	 * 
	 * @param handler the handler, e. g. <code>XLSElementActionHandler</code>
	 * @param method the output method, e. g. <code>html</code>
	 * @param t the exception caught when executing the renderer
	 */	
	public ActionHandlerException(Action handler, String method, Throwable t) {
		this(handler, method);
		exception_ = t;
	}
	
	/**
	 * Returns the handler that raised this exception.
	 * 
	 * @return the handler
	 */
	public Action getActionHandler() {
		return handler_;
	}
	
	/**
	 * Returns the output method, e. g. <code>html</code>. 
	 * 
	 * @return the output method
	 */
	public String getRenderingMethod() {
		return method_;
	}

	/**
	 * Returns the exception that lead to this <code>ActionHandlerException</code>.
	 * 
	 * @return the reason for this exception
	 */
	public Throwable getReason() {
		return exception_;
	}
	
}