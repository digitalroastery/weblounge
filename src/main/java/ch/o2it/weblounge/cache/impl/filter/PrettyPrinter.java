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

package ch.o2it.weblounge.cache.impl.filter;

import ch.o2it.weblounge.cache.StreamFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Pretty printer for html output. This class uses the implementation of the w3c library
 * <code>JTidy</code>.
 */
public class PrettyPrinter implements StreamFilter {

	/** the logger */
	protected static Logger log = LoggerFactory.getLogger(TidyReport.class.getName());
	
	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#filter(java.lang.StringBuffer, java.lang.String)
	 */
	public StringBuffer filter(StringBuffer buffer, String contentType) {
		if ("text/html".equals(contentType)) {
			Tidy tidy = new Tidy();
			StringBuffer outBuffer = new StringBuffer(buffer.length());
			InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			// PENDING Read from filter options
			tidy.setTidyMark(false);
			tidy.setFixBackslash(true);
			tidy.setFixComments(true);
			tidy.setIndentContent(true);
			tidy.setMakeClean(true);
			tidy.setSmartIndent(true);
			tidy.setUpperCaseAttrs(false);
			tidy.setUpperCaseTags(false);
			tidy.pprint(tidy.parseDOM(is, null), os);
			outBuffer.append(os.toString());
			return outBuffer;
		}
		return buffer;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#flush()
	 */
	public StringBuffer flush() {
		return null;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#close()
	 */
	public void close() { }

}