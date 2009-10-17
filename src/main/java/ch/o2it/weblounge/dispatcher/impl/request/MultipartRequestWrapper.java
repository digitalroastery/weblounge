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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.o2it.weblounge.common.impl.util.UploadedFile;
import ch.o2it.weblounge.dispatcher.impl.http.MultipartFormdataDecoder;

/**
 * This wrapper wraps <tt>multipart/form-data</tt> requests.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 1.0
 */

public class MultipartRequestWrapper extends WebloungeRequestImpl {

	/** the request decoder for Http11 multipart/form-data request */
	private MultipartFormdataDecoder decoder_;

	/** the decoding state */
	private int state_;
	
	// Logging
	
	/** the class name, used for the loggin facility */
	private final static String className = MultipartRequestWrapper.class.getName();
	
	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);
	
	/**
	 * Creates a new wrapper for a <code>HttpServletRequest</code>, wrapping
	 * <tt>multipart/form-data</tt> request.
	 * 
	 * @param request the request to wrap
	 */
	public MultipartRequestWrapper(HttpServletRequest request) {
		super(request);
		state_ = HttpServletResponse.SC_ACCEPTED;
		decode();
	}
	
	/**
	 * Returns an iteration of files uploaded with this request. The
	 * element type of this iterator is <code>UploadedFile</code>.
	 * 
	 * @return an iteration of files
	 */
	public Iterator files() {
		return decoder_.getFiles();
	}
	
	/**
	 * Returns the names of the parameters found in the request.
	 * 
	 * @return the parameter names
	 */
	public Enumeration getParameterNames() {
		return decoder_.getParams().propertyNames();
	}
	
	/**
	 * Returns the parameter for the given name. Note that this method returns
	 * <code>null</code> if no parameter is associated with <code>name</code>.
	 * 
	 * @param name the parameter name
	 * @return the parameter value
	 */
	public String getParameter(String name) {
		return decoder_.getParams().getProperty(name);
	}
	
   /**
    * Returns a multivalue parameter. If no parameter is found using the given name,
    * then this method returns <code>null</code>. If it is not a multivalue parameter,
    * then an array of size <tt>1</tt> is returned.
    * 
    * @param name the parameter name
    * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
    */
	public String[] getParameterValues(String name) {
		String v = getParameter(name);
		if (v == null) {
			return new String[] { "" };
		} else {
			// HACK: Implement real multivalue support!
			return new String[] { decoder_.getParams().getProperty(name) };
		}
	}
	
	/**
	 * Returns the encoding state, which is {@link javax.servlet.http.HttpServletResponse#SC_ACCEPTED}
	 * in case of successful decoding or the appropriate return code which may
	 * be send as the <tt>http</tt> response code.
	 * 
	 * @return the decoding state
	 */
	public int getState() {
		return state_;
	}

	/**
	 * Decodes the request and separates from it the files and parameters.
	 */
	private void decode() {
		try {
			decoder_ = new MultipartFormdataDecoder(
				getRequest().getContentType(),
				getRequest().getContentLength(),
				getRequest().getInputStream()
			);
			decoder_.decode();

			for (Iterator i = decoder_.getFiles(); i.hasNext();) {
				UploadedFile file = (UploadedFile)i.next();
				decoder_.getParams().put(file.getFieldName(), file.getOriginalName());
			}
			
		} catch (DecoderException e) {
			switch (e.getType()) {
				case DecoderException.MISSING_CONTENT_LENGTH :
					state_= HttpServletResponse.SC_LENGTH_REQUIRED;
					break;
				case DecoderException.INVALID_CONTENT_LENGTH :
				case DecoderException.PARSER_ERROR :
					state_= HttpServletResponse.SC_BAD_REQUEST;
					break;
				case DecoderException.MISSING_CONTENT_TYPE :
				case DecoderException.INVALID_CONTENT_TYPE :
					state_= HttpServletResponse.SC_FORBIDDEN;
					break;
				case DecoderException.IO_ERROR :
				case DecoderException.INTERNAL_ERROR :
				default :
					state_ = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
			log_.error("Errore while decoding multipart form data!", e);
			return;
		} catch (IOException e) {
			log_.debug("Internal server error while decoding multipart form data!");
			state_ = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		log_.debug("Decoding finished successfully");
	}

}