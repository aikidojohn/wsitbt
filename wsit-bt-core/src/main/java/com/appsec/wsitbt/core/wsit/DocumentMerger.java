/**
 * WSIT Build Tools (http://aikidojohn.github.com/wsitbt/)
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DocumentMerger
{
    private boolean mergeTextContent = true;

    /**
     * Merge the mergeSource element into the target element. The
     * elements must have the same namespace and localname in order
     * for the merge to proceede. An exception will be thrown if the
     * target and mergeSource elements do not have the same namespace
     * and localname.
     *
     * @param target      destination element
     * @param mergeSource source element
     */
    public void merge(Element target, Element mergeSource)
    {
        if (!matchSimpleNS(target, mergeSource))
        {
            throw new IllegalArgumentException("The target and merge elements are not the same");
        }

        mergeRecursive(target, mergeSource);
    }

    private void mergeRecursive(Element target, Element merge)
    {
        NodeList mergeChildren = merge.getChildNodes();
        for (int i = 0; i < mergeChildren.getLength(); i++)
        {
            Node mergeNode = mergeChildren.item(i);
            if (mergeNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element mergeChild = (Element) mergeNode;
                Element targetMatch = findMatchingChild(target, mergeChild);

                // if there is a match, recursively call this function, otherwise merge in the node
                if (null != targetMatch)
                {
                    mergeRecursive(targetMatch, mergeChild);
                }
                else
                {
                    Node importedChild = target.getOwnerDocument().importNode(mergeNode, true);
                    target.appendChild(importedChild);
                }
            }
            else if (mergeNode.getNodeType() == Node.TEXT_NODE && mergeTextContent)
            {
                Node importedChild = target.getOwnerDocument().importNode(mergeNode, true);
                target.appendChild(importedChild);
            }
            else if (mergeNode.getNodeType() != Node.TEXT_NODE)
            {
                Node importedChild = target.getOwnerDocument().importNode(mergeNode, true);
                target.appendChild(importedChild);
            }
        }
    }

    /**
     * Find a matching child (ignoring namespace declarations).
     * Returns null if no matching child can be found.
     *
     * @param parent  starting element
     * @param example element to look for
     * @return matching child or null if no matching child can be found
     */
    private Element findMatchingChild(Element parent, Element example)
    {
        Element targetMatch = getChildElementNS(parent, example.getNamespaceURI(), example.getLocalName());
        if (null != targetMatch)
        {
            //check that the attributes match
            NamedNodeMap attrs = example.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++)
            {
                Attr attr = (Attr) attrs.item(i);
                if (!attr.getLocalName().equals("xmlns") && !attr.getLocalName().startsWith("xmlns:"))
                {
                    Attr attrMatch = targetMatch.getAttributeNodeNS(attr.getNamespaceURI(), attr.getLocalName());
                    if (null != attrMatch && !attrMatch.getValue().equals(attr.getValue()))
                    {
                        return null;
                    }
                }
            }
            return targetMatch;
        }
        return null;
    }

    private Element getChildElementNS(Element parent, String ns, String localName)
    {
        NodeList nodes = parent.getElementsByTagNameNS(ns, localName);
        if (nodes.getLength() > 0)
        {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private boolean matchSimpleNS(Element a, Element b)
    {
        return a.getNamespaceURI().equals(b.getNamespaceURI()) && a.getLocalName().equals(b.getLocalName());
    }

    public boolean isMergeTextContent()
    {
        return mergeTextContent;
    }

    public void setMergeTextContent(boolean mergeContent)
    {
        this.mergeTextContent = mergeContent;
    }
}
