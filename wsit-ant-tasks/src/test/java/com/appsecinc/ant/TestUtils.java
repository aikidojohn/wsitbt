/**
 * WSIT Build Tools (http://wsitbt.codeplex.com)
 *
 * Copyright (c) 2011 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package com.appsecinc.ant;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class TestUtils 
{
	private TestUtils(){}
	
	/**
	 * @return a new temp directory that exists and is marked deleteOnExit.
	 */
	public static File createRandomTempDir()
	{
		File tempDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
		tempDir.mkdir();
		tempDir.deleteOnExit();
		return tempDir;
	}
	
	/**
	 * Deletes the specified directory and any files it and directories contains.
	 * @param dir
	 */
	public static void deleteDir(File dir) 
	{
		for (String filename : dir.list()) {
			File file = new File(dir.getAbsolutePath() + "/" + filename);
			if (file.isDirectory()) {
				deleteDir(file);
			} 
			else {
				file.delete();
			}
		}
		dir.delete();
	}
	
	public static Document loadXmlFile(File in) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		dbfac.setNamespaceAware(true);
		dbfac.setValidating(false);
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(in);
	}
}
