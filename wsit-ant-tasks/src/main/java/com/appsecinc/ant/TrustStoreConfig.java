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
package com.appsecinc.ant;

public final class TrustStoreConfig
{
    private String callbackHandler;
    private String certSelector;
    private String peeralias;
    private String location;
    private String type;
    private String storepass;

    public String getCallbackHandler()
    {
        return callbackHandler;
    }

    public void setCallbackHandler(String callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public String getCertSelector()
    {
        return certSelector;
    }

    public void setCertSelector(String certSelector)
    {
        this.certSelector = certSelector;
    }

    public String getPeeralias()
    {
        return peeralias;
    }

    public void setPeeralias(String peeralias)
    {
        this.peeralias = peeralias;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getStorepass()
    {
        return storepass;
    }

    public void setStorepass(String storepass)
    {
        this.storepass = storepass;
    }

}
