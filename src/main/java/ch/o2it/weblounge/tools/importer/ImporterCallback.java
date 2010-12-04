/*
 * ImporterCallback.java
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

public interface ImporterCallback {

	public boolean fileImported(File f) throws Exception;
	
	public boolean folderImported(File f) throws Exception;
	
}