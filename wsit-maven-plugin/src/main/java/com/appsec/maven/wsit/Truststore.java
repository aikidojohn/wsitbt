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
package com.appsec.maven.wsit;

public final class Truststore
{

    /**
     * Trustore callback handler class.
     *
     * @parameter
     */
    private String callbackHandler;

    /**
     * Certificate selector class.
     *
     * @parameter
     */
    private String certSelector;

    /**
     * Peer alias. Allows you to select a single trusted certificate from the store.
     *
     * @parameter
     */
    private String peeralias;

    /**
     * Keystore location. This can be specified instead of a callback handler.
     *
     * @parameter
     */
    private String location;

    /**
     * Keystore type.
     *
     * @parameter default-value="JKS"
     */
    private String type;

    /**
     * Keystore password.
     *
     * @parameter
     */
    private String storepass;

    public String getCallbackHandler()
    {
        return callbackHandler;
    }

    public String getCertSelector()
    {
        return certSelector;
    }

    public String getPeeralias()
    {
        return peeralias;
    }

    public String getLocation()
    {
        return location;
    }

    public String getType()
    {
        return type;
    }

    public String getStorepass()
    {
        return storepass;
    }

    public void setCallbackHandler(String callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public void setCertSelector(String certSelector)
    {
        this.certSelector = certSelector;
    }

    public void setPeeralias(String peeralias)
    {
        this.peeralias = peeralias;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setStorepass(String storepass)
    {
        this.storepass = storepass;
    }
}
