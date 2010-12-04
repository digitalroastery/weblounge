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

import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class PageImporterCallback extends AbstractImporterCallback {

	/** The page id */
	static long pageId = 1L;
	
	/**
	 * Creates a new callback for page imports.
	 * 
	 * @param src the source root directory
	 * @param dest the destination root directory
	 */
	PageImporterCallback(String src, String dest, Map<String,Collection<?>> clipboard) {
		super(src, dest);
	}

	/**
	 * This method is called if a page file has been found.
	 * 
	 * @see ch.o2it.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
	 */
	public boolean fileImported(File f) {
		try {
			File dest = createDestination(f);
			if (!"workflow.xml".equals(f.getName()) && !"history.xml".equals(f.getName())) {
				copy(f, dest);
				Node root = getXmlRoot(f);
				String partition = XPathHelper.valueOf(root, "/page/@partition");
				String path = XPathHelper.valueOf(root, "/page/@path");
				if ("index.xml".equals(f.getName())) {
					System.out.println("Imported page " + partition + " -> " + path);
				}
			} else {
				copy(f, dest);
			}
			return true;
		} catch (IOException e) {
			System.err.println("Error creating target file for " + f + ": " + e.getMessage());
			return false;
		} catch (ParserConfigurationException e) {
			System.err.println("Error parsing " + f + ": " + e.getMessage());
			return false;
		} catch (SAXException e) {
			System.err.println("Error creating sax parser for  " + f + ": " + e.getMessage());
			return false;
		}
	}

}