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

import ch.o2it.weblounge.dispatcher.impl.http.MultipartFormdataDecoder;

/**
 * Signals a deocder exception in <code>MultipartFormdataDecoder</code>
 * 
 * @version	1.0
 * @author	Daniel Steiner
 * @see MultipartFormdataDecoder
 */
public class DecoderException extends Exception {

	/** decoder exception types */
	public static final int INVALID_CONTENT_TYPE = 1;
	public static final int MISSING_CONTENT_TYPE = 2;
	public static final int INVALID_CONTENT_LENGTH = 3;
	public static final int MISSING_CONTENT_LENGTH = 4;
	public static final int PARSER_ERROR = 5;
	public static final int INTERNAL_ERROR = 6;
	public static final int IO_ERROR = 7;
	
	/** the decoder exception type */
	protected int type;
	
	/**
	 * Constructor for DecoderException.
	 * @param type the type of the decoder exception
	 */
	public DecoderException(int type) {
		super();
		this.type = type;
	}

	/**
	 * Constructor for DecoderException.
	 * @param type the type of the decoder exception
	 * @param message the detail message
	 */
	public DecoderException(int type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * Returns the decoder exception type.
	 * @return the type of the decoder exception
	 */
	public int getType() {
		return type;
	}

}
