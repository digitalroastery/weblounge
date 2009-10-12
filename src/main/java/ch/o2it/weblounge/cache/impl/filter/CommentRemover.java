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

/**
 * Removes HTML comments from the response stream.
 */
public class CommentRemover implements StreamFilter {

  /** Temporary buffer for comment processing */
	private StringBuffer tmp = new StringBuffer();
	
	/** Current parser state */
	private int state = 0;
	
	/** True while in a comment */
	private boolean inComment = false;

	/** Comment start sequence */
	private static char commentStart[] = {'<', '!', '-', '-'};
	
	/** Comment end sequence */
	private static char commentEnd[] = {'-', '-', '>'};
	
	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#filter(java.lang.StringBuffer, java.lang.String)
	 */
	public StringBuffer filter(StringBuffer buffer, String contentType) {
		StringBuffer b = new StringBuffer(buffer.length());
		for (int i = 0; i < buffer.length(); i++) {
			char c = buffer.charAt(i);
			if (!inComment) {
				if (commentStart[state] == c) {
					state++;
					tmp.append(c);
				} else {
					if (state > 0) {
						state = 0;
						b.append(tmp);
						tmp.setLength(0);
					}
					b.append(c);
				}
				
				if (state == commentStart.length) {
					inComment = true;
					state = 0;
				}
			} else {
				tmp.append(c);
				if (commentEnd[state] == c)
					state++;
				else
					state = 0;

				if (state == commentEnd.length) {
					tmp.setLength(0);
					inComment = false;
					state = 0;
				}
			}
		}
		return b;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#flush()
	 */
	public StringBuffer flush() {
		StringBuffer b = new StringBuffer().append(tmp);
		inComment = false;
		state = 0;
		tmp.setLength(0);
		return b;
	}

	/**
	 * @see ch.o2it.weblounge.api.request.StreamFilter#close()
	 */
	public void close() {
		inComment = false;
		state = 0;
		tmp.setLength(0);
	}

}