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

package ch.o2it.weblounge.dispatcher.impl.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.o2it.weblounge.common.impl.util.NullOutputStream;
import ch.o2it.weblounge.common.impl.util.UploadedFile;
import ch.o2it.weblounge.dispatcher.impl.request.DecoderException;

/**
 * Decodes a HTTP <code>POST</code> request with content-type &quot;multipart/
 * form-data&quot;. These requests are used to upload files from a HTTP user
 * agent to a HTTP server. This technique is described in detail in the
 * following RFCs:<ul>
 *
 * <li>RFC1867:	Form-based File Upload in HTML
 * <li>RFC1521:	MIME (Multipurpose Internet Mail Extensions) Part One:
 *				Mechanisms for Specifying and Describing 
 *				the Format of Internet Message Bodies</ul>
 * 
 * @version	1.0
 * @author	Daniel Steiner
 * @see ftp://ftp.rfc-editor.org/in-notes/rfc1867.txt
 * @see ftp://ftp.rfc-editor.org/in-notes/rfc1521.txt
 */
public class MultipartFormdataDecoder {

	/** 
	 * the parser states.
	 * note: do not reorder these states unless you know what you do!!!
	 */
	private static final int STATE_INIT = 0;
	private static final int STATE_END = 1;
	private static final int STATE_READ_HEADER = 2;
	private static final int STATE_READ_BODY = 3;
	private static final int STATE_READ_BOUNDARY = 4;
	private static final int STATE_MAX_ADVANCE = 5;
	private static final int STATE_ERROR = 6;
	private static final int STATE_INTERNAL_ERROR = 7;
	private static final int TRANSFORM_INIT = 8;
	private static final int TRANSFORM_READ_HEADER = 9;
	private static final int TRANSFORM_READ_BODY = 10;
	private static final int TRANSFORM_READ_BOUNDARY = 11;
	private static final int ANALYSE_HEADER = 12;
	private static final int COPY_BODY = 13;
	private static final int END_BODY = 14;

	/** 
	 * descriptive strings for the parser states.
	 * (used for debugging purposes)
	 */
	private static final String stateNames[] =
		{
			"STATE_INIT",
			"STATE_END",
			"STATE_READ_HEADER",
			"STATE_READ_BODY",
			"STATE_READ_BOUNDARY",
			"(STATE_MAX_ADVANCE)",
			"STATE_ERROR",
			"STATE_INTERNAL_ERROR",
			"TRANSFORM_INIT",
			"TRANSFORM_READ_HEADER",
			"TRANSFORM_READ_BODY",
			"TRANSFORM_READ_BOUNDARY",
			"ANALYSE_HEADER",
			"COPY_BODY",
			"END_BODY" };

	/** the request body types */
	private static final int TYPE_UNKNOWN = 0;
	private static final int TYPE_FIELD = 1;
	private static final int TYPE_FILE = 2;

	/** 
	 * the read buffer selector values.
	 * note: these are not arbitrary valus, but indices into an array.
	 */
	private static final int BUFFER_FRONT = 0;
	private static final int BUFFER_BACK = 1;
	private static final int BUFFER_COUNT = 2;

	/** the character encoding of the request */
	private static final String encoding = "ISO-8859-1";

	/** the size of the read buffer */
	private static final int BUFFER_SIZE = 5 * 1024;

	/** the logging facility */
	private static final Logger log =
		LoggerFactory.getLogger(MultipartFormdataDecoder.class.getName());

	/** the prefix used for temporary files */
	private static String prefix = "upload";

	/** the input stream from where the request body is read */
	private InputStream is;

	/** the length of the request body (in bytes) */
	private int contentLength;

	/** the content type of the request body */
	private String contentType;

	/** the form parameters */
	private Properties params = new Properties();

	/** the uploaded files */
	private List files = new ArrayList();

	/**
	 * Create a new <code>MultipartFormdataDecoder</code>.
	 * 
	 * @param contentLength the value of the content length field of the
	 * request header
	 * @param contentType the value of the content type field of the request 
	 * header
	 * @param is the <code>InputStream</code> from where the request body is
	 * read
	 */
	public MultipartFormdataDecoder(
		String contentType,
		int contentLength,
		InputStream is) {
		this.is = is;
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	/**
	 * Decode the multipart/formdata request body.
	 * 
	 * @throws DecoderException if an error occurs while parsing the request
	 */
	public void decode() throws DecoderException {
		/* first, do some general checks... */
		log.info("Checking external parameters...");
		if (contentLength < 0) {
			log.info("Content length is less than 0.");
			throw new DecoderException(DecoderException.MISSING_CONTENT_LENGTH);
		}
		if (contentType == null) {
			log.info("Content type is null.");
			throw new DecoderException(DecoderException.MISSING_CONTENT_TYPE);
		}
		if (!contentType.startsWith("multipart/form-data")) {
			log.info(
				"Content type doesn't start with \"multipart/form-data\".");
			throw new DecoderException(DecoderException.INVALID_CONTENT_TYPE);
		}
		if (contentType.indexOf("boundary=") == -1) {
			log.info("Content type doesn't contain a boundary.");
			throw new DecoderException(DecoderException.INVALID_CONTENT_TYPE);
		}

		/* extract the mime multipart boundary */
		log.info("Extracting boundary...");
		byte boundary[] = null;
		try {
			String boundaryStr =
				contentType.substring(
					contentType.indexOf("boundary=") + "boundary=".length());
			if (boundaryStr == null
				|| boundaryStr.length() <= 0
				|| boundaryStr.length() > BUFFER_SIZE) {
				log.info("Invalid length of boundary.");
				throw new DecoderException(
					DecoderException.INVALID_CONTENT_TYPE);
			}
			boundary = ("--" + boundaryStr).getBytes(encoding);
			log.info("Found boundary: " + new String(boundaryStr) + ".");
		} catch (IndexOutOfBoundsException e) {
			log.info("Index out of range for boundary.");
			throw new DecoderException(DecoderException.INVALID_CONTENT_TYPE);
		} catch (UnsupportedEncodingException e) {
			log.info("Unsupported encoding for boundary.");
			throw new DecoderException(DecoderException.INTERNAL_ERROR);
		}

		/* at least the request header should be ok by now. let's parse the
		 * request body */
		log.info("Initializing the parser...");
		try {
			/* initialize the parser */
			int pos, length = 0;
			int state = TRANSFORM_INIT, oldState = STATE_ERROR;

			/* some temporary values */
			byte data[][] = new byte[BUFFER_COUNT][BUFFER_SIZE];
			byte buf[] = data[BUFFER_FRONT];
			int reads[][] = new int[BUFFER_COUNT][1];
			int read[] = reads[BUFFER_FRONT];
			OutputStream os = null;
			StringBuffer header = new StringBuffer();
			String contentType = null, origName = null, fieldName = null;
			String fileName = null;
			int boundaryPos = 0, startPos = 0, bodyLength = 0, write = 0;
			int nextState = STATE_INTERNAL_ERROR;
			int bufferSelector =
				(buf == data[BUFFER_FRONT]) ? BUFFER_FRONT : BUFFER_BACK;
			int headerType = TYPE_UNKNOWN;
			byte cur = 0, last = 0;

			/* read the request body */
			log.info("Starting the  parser...");
			while ((read[0] = is.read(buf)) != -1) {
				log.info("Read " + read[0] + " bytes.");

				if (read[0] == 0)
					throw new DecoderException(DecoderException.INTERNAL_ERROR);

				pos = 0;
				length += read[0];

				/* parse the data */
				while (pos < read[0] || state > STATE_MAX_ADVANCE) {

					if (state < STATE_MAX_ADVANCE) {
						last = cur;
						cur = buf[pos];
					}

					if (state != oldState) {
						log.info(
							"Changed state to "
								+ stateNames[state]
								+ " at input position "
								+ (length - read[0] + pos)
								+ ".");
						oldState = state;
					}

					switch (state) {
						case TRANSFORM_INIT :
							boundaryPos = 0;
							state = STATE_INIT;
							break;

						case STATE_INIT :
							if (boundaryPos < boundary.length)
								if (cur == boundary[boundaryPos])
									/* reading the boundary */
									++boundaryPos;
								else {
									/* found some invalid bytes inside the
									 * boundary */
									log.debug(
										"Invalid character at position: "
											+ boundaryPos
											+ ".");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length)
								if (cur == '\r' || cur == '-')
									/* expect either a "\r\n" or "--\r\n" */
									++boundaryPos;
								else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 1)
								if (cur == '-' && last == '-')
									/* expect boundary + "--\r\n") */
									++boundaryPos;
								else if (cur == '\n' && last == '\r') {
									/* we've found the complete boundary */
									log.debug("Boundary found.");
									state = TRANSFORM_READ_HEADER;
								} else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 1.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 2)
								if (cur == '\r')
									/* expect boundary + "--\r\n") */
									++boundaryPos;
								else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 2.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 3)
								if (cur == '\n') {
									/* we have reached the end of the request! */
									log.debug("Final boundary found.");
									state = STATE_END;
								} else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 3.");
									state = STATE_ERROR;
								}
							else {
								/* this should not happen, since we should have
								 * chagned parser state earlier */
								log.debug("We've read past the boundary.");
								state = STATE_INTERNAL_ERROR;
							}
							break;

						case TRANSFORM_READ_HEADER :
							header.setLength(0);
							origName = null;
							fieldName = null;
							contentType = null;
							headerType = TYPE_UNKNOWN;
							state = STATE_READ_HEADER;
							break;

						case STATE_READ_HEADER :
							switch (cur) {
								case '\r' :
									/* expect: "\r\n" */
									break;
								case '\n' :
									state =
										(last == '\r')
											? ANALYSE_HEADER
											: STATE_ERROR;
									break;
								default :
									header.append((char) cur);
							}
							break;

						case ANALYSE_HEADER :
							if (header.length() == 0) {
								/* we've seen the last line of the header.
								 * Let's find out what to do next... */
								log.debug("Last line of header.");
								switch (headerType) {
									case TYPE_FIELD :
									case TYPE_FILE :
										log.debug("Valid part type.");
										state = TRANSFORM_READ_BODY;
										break;
									case TYPE_UNKNOWN :
										log.debug("Unknown part type.");
										state = STATE_ERROR;
										break;
									default :
										log.debug("Invalid part type.");
										state = STATE_INTERNAL_ERROR;
								}
								break;
							}

							/* let's figure out what to do with the header */
							log.debug("Analysing header: " + header + "...");
							if (header
								.toString()
								.indexOf("Content-Disposition")
								!= -1
								&& header.toString().indexOf("form-data") != -1) {
								log.debug("Found content disposition header.");
								state = STATE_READ_HEADER;
								if (headerType != TYPE_UNKNOWN) {
									/* don't allow multiple Content-Disposition
									 * headers */
									log.debug(
										"Multiple content disposition headers detected.");
									state = STATE_ERROR;
								} else {
									int i = 0;

									/* a Content-Disposition header must contain
									 * the field name. */
									if ((i =
										header.toString().indexOf("name="))
										== -1) {
										log.debug("No field name found.");
										state = STATE_ERROR;
									} else if (
										(fieldName =
											extractQuotedString(header, i))
											== null) {
										log.debug("Field name is empty.");
										state = STATE_ERROR;
									} else
										log.debug(
											"Field name: " + fieldName + ".");

									/* figure out what header type we have */
									if ((i =
										header.toString().indexOf("filename="))
										== -1) {
										log.debug(
											"No file name found: assuming type field.");
										headerType = TYPE_FIELD;
									} else {
										log.debug(
											"File name found: assuming type file.");
										headerType = TYPE_FILE;
										if ((origName =
											extractQuotedString(header, i))
											== null) {
											log.debug("File name is empty.");
											state = STATE_ERROR;
										} else {
											/* thanks to Micro$oft we need to
											 * 'disect' the filename... */
											origName = realFileName(origName);
											log.debug(
												"File name: " + origName + ".");
										}
									}
								}
							} else if (
								header.toString().indexOf("Content-Type")
									!= -1) {
								log.debug("Found content type header.");
								try {
									contentType =
										header
											.substring(
												header.toString().indexOf(":")
													+ 1)
											.trim();
									log.debug(
										"Content type: " + contentType + ".");
									state = STATE_READ_HEADER;
								} catch (IndexOutOfBoundsException e) {
									log.debug("Invalid content type header.");
									state = STATE_ERROR;
								}
							} else {
								/* ignore unknown header */
								log.debug("Found unknown header: ignoring.");
								state = STATE_READ_HEADER;
							}

							if (state == STATE_READ_HEADER)
								/* more headers to follow... */
								header.setLength(0);
							break;

						case TRANSFORM_READ_BODY :
							startPos = pos;
							bodyLength = 0;
							bufferSelector =
								(buf == data[BUFFER_FRONT])
									? BUFFER_FRONT
									: BUFFER_BACK;
							state = STATE_READ_BODY;
							log.debug("Opening output stream.");
							switch (headerType) {
								case TYPE_FILE :
									log.debug("Reading type file.");
									if (origName.length() == 0) {
										log.info(
											"No filename specified. Discarding data.");
										os = new NullOutputStream();
									} else {
										fileName =
											File
												.createTempFile(
													"weblounge",
													null)
												.getPath();
										os = new FileOutputStream(fileName);
										log.debug(
											"Using temporary file: "
												+ fileName
												+ ".");
									}
									break;
								case TYPE_FIELD :
									log.debug("Reading type field.");
									fileName = null;
									os = new ByteArrayOutputStream();
									break;
								default :
									log.debug("Reading unknown type.");
									state = STATE_INTERNAL_ERROR;
							}
							break;

						case STATE_READ_BODY :
							if (cur == '\r')
								/* have we found the boundary? */
								state = TRANSFORM_READ_BOUNDARY;
							else if (buf != data[bufferSelector]) {
								/* end of buffer reached */
								--pos;
								state = COPY_BODY;
							} else
								/* just an other valid chracter... */
								++bodyLength;
							break;

						case COPY_BODY :
							write =
								(startPos + bodyLength
									> reads[bufferSelector][0])
									? reads[bufferSelector][0] - startPos
									: bodyLength;
							log.debug("Writing " + write + " bytes.");
							os.write(data[bufferSelector], startPos, write);
							bufferSelector =
								(buf == data[BUFFER_FRONT])
									? BUFFER_FRONT
									: BUFFER_BACK;
							bodyLength -= write;
							if (bodyLength > 0) {
								log.debug(
									"Wrapped writing "
										+ bodyLength
										+ " bytes.");
								os.write(data[bufferSelector], 0, bodyLength);
							}
							startPos = pos;
							bodyLength = 0;
							state = STATE_READ_BODY;
							break;

						case END_BODY :
							state = nextState;
							log.debug("Writing " + bodyLength + " bytes.");
							os.write(
								data[bufferSelector],
								startPos,
								bodyLength);
							os.flush();
							switch (headerType) {
								case TYPE_FILE :
									if (origName.length() == 0) {
										log.info(
											"No filename specified. Ignoring upload.");
										os = new ByteArrayOutputStream();
									} else {
										log.debug("Registering file.");
										os.close();
										files.add(
											new UploadedFile(
												fileName,
												origName,
												fieldName,
												contentType));
									}
									break;
								case TYPE_FIELD :
									log.debug("Registering field.");
									if (params
										.setProperty(
											fieldName,
											(
												(
													ByteArrayOutputStream) os)
														.toString(
												encoding))
										!= null) {
										log.warn(
											"Duplicate field: "
												+ fieldName
												+ ".");
										state = STATE_ERROR;
									}
									break;
								default :
									log.debug("Registering unknown.");
									state = STATE_INTERNAL_ERROR;
							}
							log.debug("Closing output stream.");
							os.close();
							os = null;
							break;

						case TRANSFORM_READ_BOUNDARY :
							boundaryPos = -1;
							nextState = STATE_INTERNAL_ERROR;
							state = STATE_READ_BOUNDARY;
							break;

						case STATE_READ_BOUNDARY :
							if (boundaryPos < -1) {
								/* something went terribly wrong */
								log.debug("Boundary position too small.");
								state = STATE_INTERNAL_ERROR;
							} else if (boundaryPos == -1)
								if (cur == '\n')
									/* there's a \r\n in front of the
									 * boundary */
									++boundaryPos;
								else {
									/* not the boundary */
									log.debug(
										"Invalid character at position: "
											+ boundaryPos
											+ ".");
									state = STATE_READ_BODY;
									--pos;
									bodyLength += boundaryPos + 2;
								}
							else if (boundaryPos < boundary.length)
								if (cur == boundary[boundaryPos])
									/* reading the boundary */
									++boundaryPos;
								else {
									/* not the boundary */
									log.debug(
										"Invalid character at position: "
											+ boundaryPos
											+ ".");
									state = STATE_READ_BODY;
									--pos;
									bodyLength += boundaryPos + 2;
								}
							else if (boundaryPos == boundary.length)
								if (cur == '\r' || cur == '-')
									/* expect either a "\r\n" or "--\r\n" */
									++boundaryPos;
								else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 1)
								if (cur == '-' && last == '-')
									/* expect boundary + "--\r\n") */
									++boundaryPos;
								else if (cur == '\n' && last == '\r') {
									/* we've found the complete boundary */
									log.debug("Boundary found.");
									state = END_BODY;
									nextState = TRANSFORM_READ_HEADER;
								} else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 1.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 2)
								if (cur == '\r')
									/* expect boundary + "--\r\n") */
									++boundaryPos;
								else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 2.");
									state = STATE_ERROR;
								}
							else if (boundaryPos == boundary.length + 3)
								if (cur == '\n') {
									/* we have reached the end of the request! */
									log.debug("Final boundary found.");
									state = END_BODY;
									nextState = STATE_END;
								} else {
									/* there seems to be some garbage after the 
									 * boundary */
									log.debug(
										"Invalid char at boundary.length + 3.");
									state = STATE_ERROR;
								}
							else {
								/* this should not happen, since we should have
								 * chagned parser state earlier */
								log.debug("We've read past the boundary.");
								state = STATE_INTERNAL_ERROR;
							}
							break;

						case STATE_END :
							/* the parser has reached its final state, but
							 * there's more input... Let's signal an error. */
						case STATE_ERROR :
							throw new DecoderException(
								DecoderException.PARSER_ERROR);

						case STATE_INTERNAL_ERROR :
						default :
							/* something went wrong :-( */
							throw new DecoderException(
								DecoderException.INTERNAL_ERROR);
					}

					/* only advance in input, if we're not transforming state */
					if (oldState < STATE_MAX_ADVANCE)
						++pos;

				}

				/* swap the read buffer */
				buf =
					data[(buf == data[BUFFER_FRONT])
						? BUFFER_BACK
						: BUFFER_FRONT];
				read =
					reads[(read == reads[BUFFER_FRONT])
						? BUFFER_BACK
						: BUFFER_FRONT];
			}

			/* some final sanity checks to be sure, we've read the complete
			 * request */
			if (state != STATE_END) {
				log.info("Premature end of input.");
				throw new DecoderException(DecoderException.PARSER_ERROR);
			}
			if (length != contentLength) {
				log.info("Final content length check failed.");
				throw new DecoderException(
					DecoderException.INVALID_CONTENT_LENGTH);
			}

		} catch (IOException e) {
			log.warn("I/O Exception.", e);
			throw new DecoderException(DecoderException.IO_ERROR);
		} catch (IndexOutOfBoundsException e) {
			log.warn("Index out of bounds exception.", e);
			throw new DecoderException(DecoderException.INTERNAL_ERROR);
		} catch (Exception e) {
			log.warn("General Exception.", e);
			throw new DecoderException(DecoderException.INTERNAL_ERROR);
		}
		log.info("Parser terminated normally.");
	}

	/**
	 * Extracts the filename from a complete pathname.
	 * @param s the complete pathname
	 * @return the filename
	 */
	private String realFileName(String s) {
		if (s == null)
			return null;
		try {
			int i = 0;
			if ((i = s.lastIndexOf('/')) != -1
				|| (i = s.lastIndexOf('\\')) != -1)
				return s.substring(i + 1);
		} catch (IndexOutOfBoundsException e) {}
		return s;
	}

	/**
	 * Extracts the next quoted substring from the given string.
	 * 
	 * @param s the string from where to extract the quoted substring
	 * @param i only find substrings after this position
	 * @return the next quoted substring with the quotes removed
	 */
	private String extractQuotedString(StringBuffer s, int i) {
		if (s == null)
			return null;
		int start = s.toString().indexOf("\"", i);
		if (start == -1)
			return null;
		int end = s.toString().indexOf("\"", start + 1);
		if (end == -1)
			return null;
		try {
			return s.substring(start + 1, end);
		} catch (IndexOutOfBoundsException e) {}
		return null;
	}

	/**
	 * Returns an iterator for the uploaded files. The objects referenced by the
	 * iterator are of type <code>MultipartFormdataDecoder.UploadedFile</code>.
	 * 
	 * @return iterator of uploaded files
	 * @see UploadedFile
	 */
	public Iterator getFiles() {
		return files.iterator();
	}

	/**
	 * Returns the params.
	 * @return Properties
	 */
	public Properties getParams() {
		return params;
	}

	/**
	 * Returns the prefix for temparary filenames.
	 * @return the prefix
	 */
	public static String getPrefix() {
		return prefix;
	}

	/**
	 * Sets the prefix for temparary filenames.
	 * @param prefix the prefix to set
	 */
	public static void setPrefix(String prefix) {
		MultipartFormdataDecoder.prefix = prefix;
	}

}
