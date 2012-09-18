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
import java.net.URL;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class WsitDocumentTest
{

	@Test
	public void testStripPolicies() throws Exception
	{
		File file = new File(getClass().getResource("PingServiceWithPolicy.xml").toURI());
		WsitDocument wsit = WsitDocument.parse(file, true);
		Document doc= wsit.getDocument();
		
		NodeList nodes = doc.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(0, nodes.getLength());
		
		doc.getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		assertEquals(0, nodes.getLength());
	}
	
	@Test
	public void testExceptionIfPolicyDoesNotExist() throws Exception
	{
		File file = new File(getClass().getResource("PingService.xml").toURI());
		File policyFile = new File(getClass().getResource("ServicePolicy.xml").toURI());
		WsitDocument wsit = WsitDocument.parse(file, true);
		wsit.mergePolicy(policyFile);
		try {
			wsit.setBindingPolicy("Does Not Exist");
			fail("Expected exception when specifying non-existant binding policy");
		}
		catch (Exception e) {
		}
		
		try {
			wsit.setInputPolicy("Does Not Exist");
			fail("Expected exception when specifying non-existant input policy");
		}
		catch (Exception e) {
		}
		
		try {
			wsit.setOutputPolicy("Does Not Exist");
			fail("Expected exception when specifying non-existant output policy");
		}
		catch (Exception e) {
		}
	}
	
	@Test
	public void testPolicyGetsMerged() throws Exception
	{
		URL url = WsitDocumentTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitDocumentTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitDocument wsit = WsitDocument.parse(wsdl, true);
		wsit.mergePolicy(policy);
		wsit.setBindingPolicy("WSTrustPolicy");
		wsit.setInputPolicy("InputPolicy");
		wsit.setOutputPolicy("OutputPolicy");
		wsit.setFaultPolicy("FaultPolicy");
		
		Document doc = wsit.getDocument();
		NodeList nodes = doc.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(19, nodes.getLength());
			
		nodes = doc.getElementsByTagNameNS(Namespace.WSDL, "binding");
		assertEquals(1, nodes.getLength());
		
		Element binding = (Element)nodes.item(0);
		nodes = binding.getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		assertEquals(13, nodes.getLength());
	}
}
