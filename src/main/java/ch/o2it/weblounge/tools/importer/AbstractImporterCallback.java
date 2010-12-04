/*
 * AbstractImporterCallback.java
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

import ch.o2it.weblounge.common.impl.url.PathUtils;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class AbstractImporterCallback implements ImporterCallback {

	protected String srcDir = null;
	protected String destDir = null;
	
	AbstractImporterCallback(String src, String dest) {
		this.srcDir = src;
		this.destDir = dest;
	}
	
	public abstract boolean fileImported(File f) throws Exception;

	/**
	 * This default implementation only creates the folder in the destination.
	 * 
	 * @see ch.o2it.weblounge.tools.importer.ImporterCallback#folderImported(java.io.File)
	 */
	public boolean folderImported(File f) {
		try {
			return createDestination(f) != null;
		} catch (IOException e) {
			System.err.println("Error creating target directory for " + f + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Takes the file argument, strips off the src directory prefix and creates the
	 * remainder under the destination path.
	 * 
	 * @param f the file
	 * @return the destination file
	 */
	protected File createDestination(File f) throws IOException {
		String path = f.getAbsolutePath();
		path = path.substring(srcDir.length());
		File dest = new File(PathUtils.concat(destDir, path));
		if (f.isDirectory())
			return (dest.exists() || dest.mkdirs()) ? dest : null;
		else {
			if (dest.exists())
				dest.delete();
			return dest.createNewFile() ? dest : null;
		}
	}
	
	/**
	 * Copies the contents of file <code>src</code> to <code>dest</code>.
	 * 
	 * @param f the file
	 * @return the destination file
	 */
	protected void copy(File src, File dest) throws IOException {
		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			is = new FileInputStream(src);
			os = new FileOutputStream(dest);
			byte buf[] = new byte[1024];
			int i;
			while ((i = is.read(buf)) != -1)
				os.write(buf, 0, i);
			os.flush();
		} catch (IOException e) {
			System.err.println("Unable to copy " + src.getPath() + " to " + dest.getPath() + ": " + e.getMessage());
			dest.delete();
		} finally {
			if (is != null)
				try { is.close(); } catch (IOException e) { /* ignore */ }
			if (os != null)
				try { os.close(); } catch (IOException e) { /* ignore */ }
		}
	}
	
	/**
	 * Parses file <code>f</code> and returns the document root node.
	 * 
	 * @param f the file to parse
	 * @return the root node
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected Node getXmlRoot(File f) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		return docBuilder.parse(f);
	}
	
}