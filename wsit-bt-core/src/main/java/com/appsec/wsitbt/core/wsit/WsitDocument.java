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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class WsitDocument
{
    private Document doc;
    private DocumentBuilder docBuilder;
    private DocumentMerger docMerger;
    private final Set<String> policyIds = new HashSet<String>();

    private WsitDocument() throws ParserConfigurationException
    {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setNamespaceAware(true);
        dbfac.setValidating(false);
        docBuilder = dbfac.newDocumentBuilder();
        docMerger = new DocumentMerger();
    }

    public static WsitDocument parse(File wsdl) throws ParserConfigurationException, SAXException, IOException
    {
        return parse(wsdl, false);
    }

    public static WsitDocument parse(File wsdl, boolean stripPolicies) throws ParserConfigurationException, SAXException, IOException
    {
        WsitDocument wsit = new WsitDocument();
        wsit.doc = wsit.docBuilder.parse(wsdl);
        if (stripPolicies)
        {
            wsit.stripPolicies();
        }
        else
        {
            wsit.mergePolicyIds(wsit.doc);
        }
        return wsit;
    }

    public static WsitDocument newDocument(String nameAttr) throws ParserConfigurationException
    {
        WsitDocument wsit = new WsitDocument();

        wsit.doc = wsit.docBuilder.newDocument();
        Element defs = wsit.doc.createElementNS(Namespace.WSDL, "definitions");
        defs.setAttribute("name", nameAttr);
        wsit.doc.appendChild(defs);

        return wsit;
    }

    public void mergePolicy(File policy) throws SAXException, IOException
    {
        Document wsitPolicy = docBuilder.parse(policy);
        mergePolicy(wsitPolicy);
    }

    public void mergePolicy(Document policy) throws SAXException, IOException
    {
        mergePolicyIds(policy);
        docMerger.merge(doc.getDocumentElement(), policy.getDocumentElement());
    }

    public void setBindingPolicy(String policyName)
    {
        if (null == policyName || policyName.isEmpty())
        {
            return;
        }
        if (!policyIds.contains(policyName))
        {
            throw new IllegalArgumentException("Policy '" + policyName + "' does not exist.");
        }

        Element target = doc.getDocumentElement();
        Element binding = getChildElementNS(target, Namespace.WSDL, "binding");
        if (null != binding)
        {
            Element policyRef = doc.createElementNS(Namespace.WSP, "wsp:PolicyReference");
            policyRef.setAttribute("URI", "#" + policyName);
            NodeList nodes = binding.getChildNodes();
            if (nodes.getLength() > 0)
            {
                binding.insertBefore(policyRef, nodes.item(0));
            }
            else
            {
                binding.appendChild(policyRef);
            }
        }
    }

    private void setPolicy(String elementName, String policyName)
    {
        if (null == policyName || policyName.isEmpty())
        {
            return;
        }
        if (!policyIds.contains(policyName))
        {
            throw new IllegalArgumentException("Policy '" + policyName + "' does not exist.");
        }

        Element target = doc.getDocumentElement();
        Element binding = getChildElementNS(target, Namespace.WSDL, "binding");
        if (null != binding)
        {
            NodeList nodes = binding.getElementsByTagNameNS(Namespace.WSDL, elementName);
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Element elem = (Element) nodes.item(i);
                Element policyRef = doc.createElementNS(Namespace.WSP, "wsp:PolicyReference");
                policyRef.setAttribute("URI", "#" + policyName);
                elem.appendChild(policyRef);
            }
        }
    }
    
    public void setInputPolicy(String policyName)
    {
    	setPolicy("input", policyName);
    }

    public void setOutputPolicy(String policyName)
    {
    	setPolicy("output", policyName);
    }

    public void setFaultPolicy(String policyName)
    {
    	setPolicy("fault", policyName);
    }

    public void importWsitDocument(WsitDocument wsitDoc, String location)
    {
        //Make sure that it isn't already imported
        NodeList nodes = doc.getElementsByTagNameNS(Namespace.WSDL, "import");
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Element e = (Element) nodes.item(i);
            String loc = e.getAttribute("location");
            if (loc.equals(location))
            {
                //the import already exists, so return.
                return;
            }
        }
        Element target = wsitDoc.doc.getDocumentElement();
        String tns = target.getAttribute("targetNamespace");

        Element imp = doc.createElementNS(Namespace.WSDL, "import");
        imp.setAttribute("location", location);
        imp.setAttribute("namespace", tns);
        doc.getDocumentElement().appendChild(imp);
    }

    /**
     * Saves the WSIT document to the given location.
     * 
     * @param output
     * @throws TransformerException
     * @throws IOException
     */
    public void save(File output) throws TransformerException, IOException
    {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer transformer = transfac.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(output);
            transformer.transform(new DOMSource(doc), new StreamResult(out));
        }
        finally
        {
            if (out != null)
            {
                out.flush();
                out.close();
            }
        }
    }

    public void stripWsitConfiguration()
    {
        Element target = doc.getDocumentElement();
        removeAllElementsNS(target, Namespace.SC);
        removeAllElementsNS(target, Namespace.SC1);
    }

    public Document getDocument()
    {
        return this.doc;
    }

    public Set<String> getPolicyIds()
    {
        return Collections.unmodifiableSet(this.policyIds);
    }

    /**
     * Strips all Policy and PolicyReference elements from the WSIT document.
     */
    private void stripPolicies()
    {
        policyIds.clear();

        Element target = doc.getDocumentElement();
        removeAllElementsNS(target, Namespace.WSP, "Policy");
        removeAllElementsNS(target, Namespace.WSP, "PolicyReference");
    }

    /**
     * Merges policy ids from the given document into the current
     * list of policy ids.
     * 
     * @param wsitDoc
     */
    private void mergePolicyIds(Document wsitDoc)
    {
        Element def = wsitDoc.getDocumentElement();
        NodeList policies = def.getElementsByTagNameNS(Namespace.WSP, "Policy");
        for (int i = 0; i < policies.getLength(); i++)
        {
            Element policyElem = (Element) policies.item(i);
            String id = policyElem.getAttributeNS(Namespace.WSU, "Id");
            policyIds.add(id);
        }
    }

    /**
     * Returns the first child element of {@code element} with given
     * localName and namespace URI in document order.
     *
     * @param element
     * @param uri
     * @param localName
     * @return
     */
    private Element getChildElementNS(Node element, String uri, String localName)
    {
        NodeList nodes = ((Element) element).getElementsByTagNameNS(uri, localName);
        if (nodes.getLength() < 0)
        {
            return null;
        }

        return (Element) nodes.item(0);
    }

    /**
     * Remove all elements with the given local name and namespace URI starting with the
     * given node.
     * 
     * @param node
     * @param uri
     * @param localName
     */
    private void removeAllElementsNS(Node node, String uri, String localName)
    {
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(node);
        while (!queue.isEmpty())
        {
            Node elem = queue.remove();
            if (elem.getNodeType() == Node.ELEMENT_NODE && elem.getNamespaceURI().equals(uri) && elem.getLocalName().equals(localName))
            {
                elem.getParentNode().removeChild(elem);
            }
            else
            {
                NodeList children = elem.getChildNodes();
                for (int i = 0; i < children.getLength(); i++)
                {
                    queue.add(children.item(i));
                }
            }
        }
    }

    /**
     * Remove all elements with the given namespace URI starting with the
     * given node.
     * 
     * @param node
     * @param uri
     */
    private void removeAllElementsNS(Node node, String uri)
    {
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(node);
        while (!queue.isEmpty())
        {
            Node elem = queue.remove();
            if (elem.getNodeType() == Node.ELEMENT_NODE && elem.getNamespaceURI().equals(uri))
            {
                elem.getParentNode().removeChild(elem);
            }
            else
            {
                NodeList children = elem.getChildNodes();
                for (int i = 0; i < children.getLength(); i++)
                {
                    queue.add(children.item(i));
                }
            }
        }
    }
}
