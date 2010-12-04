/*
 * PageImporterCallback.java
 *
 * Copyright 2007 by O2 IT Engineering
 * Zurich, Switzerland (CH)
 * All rights reserved.
 *
 * This software is confidential and proprietary information ("Confidential
 * Information").  You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into.
 */

package ch.o2it.weblounge.tools.importer;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public class ResourceImporterCallback extends AbstractImporterCallback {

	/**
	 * Creates a new callback for page imports.
	 * 
	 * @param src the source root directory
	 * @param dest the destination root directory
	 */
	ResourceImporterCallback(String src, String dest, Map<String,Collection<?>> clipboard) {
		super(src, dest);
	}

	/**
	 * This method is called if a page file has been found.
	 * 
	 * @see ch.o2it.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
	 */
	public boolean fileImported(File f) {
		return true;
	}

}