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
package com.appsec.wsitbt.core.wsit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.After;
import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class DocumentMergerTest
{
	private File tempDir;
	
	@Before
	public void setUp()
	{
		tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
		assertTrue(tempDir.mkdir());
		tempDir.deleteOnExit();
	}
	
	@After
	public void tearDown()
	{
		for (String filename : tempDir.list()) {
			File file = new File(tempDir.getAbsolutePath() + "/" + filename);
			assertTrue(file.delete());
		}
		assertTrue(tempDir.delete());
	}

	@Test
	public void testMergeTopLevelChildren() throws Exception
	{
		File targetFile = new File(getClass().getResource("target_1.xml").toURI());
		File tempTarget = new File(tempDir.getAbsolutePath() + "/" + targetFile.getName());
		
		copyFile(targetFile, tempTarget);
		
		Document targetDoc = loadXmlFile(tempTarget);
		Document mergeDoc = loadXmlFile(getClass().getResourceAsStream("source_1.xml"));
		
		DocumentMerger merge = new DocumentMerger();
		merge.merge(targetDoc.getDocumentElement(), mergeDoc.getDocumentElement());
		
		NodeList nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "binding");
		assertEquals(1, nodes.getLength());
		
		nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "service");
		assertEquals(1, nodes.getLength());
		
		nodes = targetDoc.getElementsByTagNameNS("http://wsp.com", "Policy");
		assertEquals(1, nodes.getLength());
		
		writeXml(tempTarget, targetDoc);
	}
	
	@Test
	public void testMergeDeepChildren() throws Exception
	{
		File targetFile = new File(getClass().getResource("target_1.xml").toURI());
		File tempTarget = new File(tempDir.getAbsolutePath() + "/" + targetFile.getName());
		
		copyFile(targetFile, tempTarget);
		
		Document targetDoc = loadXmlFile(tempTarget);
		Document mergeDoc = loadXmlFile(getClass().getResourceAsStream("source_2.xml"));
		
		DocumentMerger merge = new DocumentMerger();
		merge.merge(targetDoc.getDocumentElement(), mergeDoc.getDocumentElement());
		
		NodeList nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "binding");
		assertEquals(1, nodes.getLength());
		
		Element binding = (Element)nodes.item(0);
		nodes = binding.getElementsByTagNameNS("http://soap.com", "binding");
		assertEquals(1, nodes.getLength());
		
		nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "service");
		assertEquals(1, nodes.getLength());
		
		writeXml(tempTarget, targetDoc);
	}
	
	@Test
	public void testMergeTextContent() throws Exception
	{
		File targetFile = new File(getClass().getResource("target_1.xml").toURI());
		File tempTarget = new File(tempDir.getAbsolutePath() + "/" + targetFile.getName());
		
		copyFile(targetFile, tempTarget);
		
		Document targetDoc = loadXmlFile(tempTarget);
		Document mergeDoc = loadXmlFile(getClass().getResourceAsStream("source_3.xml"));
		
		DocumentMerger merge = new DocumentMerger();
		merge.merge(targetDoc.getDocumentElement(), mergeDoc.getDocumentElement());
		
		NodeList nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "binding");
		assertEquals(1, nodes.getLength());
		Element binding = (Element)nodes.item(0);
		assertEquals("Original TextTextContent", binding.getTextContent());
		
		nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "service");
		assertEquals(1, nodes.getLength());
		
		writeXml(tempTarget, targetDoc);
	}
	
	@Test
	public void testDisableMergeTextContent() throws Exception
	{
		File targetFile = new File(getClass().getResource("target_1.xml").toURI());
		File tempTarget = new File(tempDir.getAbsolutePath() + "/" + targetFile.getName());
		
		copyFile(targetFile, tempTarget);
		
		Document targetDoc = loadXmlFile(tempTarget);
		Document mergeDoc = loadXmlFile(getClass().getResourceAsStream("source_3.xml"));
		
		DocumentMerger merge = new DocumentMerger();
		merge.setMergeTextContent(false);
		merge.merge(targetDoc.getDocumentElement(), mergeDoc.getDocumentElement());
		
		NodeList nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "binding");
		assertEquals(1, nodes.getLength());
		Element binding = (Element)nodes.item(0);
		assertEquals("Original Text", binding.getTextContent());
		
		nodes = targetDoc.getElementsByTagNameNS("http://wsdl.com", "service");
		assertEquals(1, nodes.getLength());
		
		writeXml(tempTarget, targetDoc);
	}
	
	private void writeXml(final File fileOut, final Document doc) throws TransformerException, IOException
	{
		final TransformerFactory transfac = TransformerFactory.newInstance();
        final Transformer transformer = transfac.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        final FileOutputStream out = new FileOutputStream(fileOut);
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(out));
        } finally {
        	out.flush();
        	out.close();
        }
	}
	
	private Document loadXmlFile(final InputStream in) throws ParserConfigurationException, SAXException, IOException
	{
		final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		dbfac.setNamespaceAware(true);
		dbfac.setValidating(false);
        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(in);
	}
	
	private Document loadXmlFile(final File in) throws ParserConfigurationException, SAXException, IOException
	{
		final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		dbfac.setNamespaceAware(true);
		dbfac.setValidating(false);
        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(in);
	}
	
	private void copyFile(final File sourceFile, final File destFile) throws IOException
	{
		if (!destFile.exists()) {
			assertTrue(destFile.createNewFile());
		}

		final FileChannel source = new FileInputStream(sourceFile).getChannel();
        try {

            final FileChannel destination = new FileOutputStream(destFile).getChannel();
            try {
                destination.transferFrom(source, 0, source.size());
            } finally {
                destination.close();
            }

        } finally {
            source.close();
		}
	}
}
