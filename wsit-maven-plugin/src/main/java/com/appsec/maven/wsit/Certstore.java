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

public final class Certstore
{
    /**
     * Specify the cerstore callback handler class.
     *
     * @parameter
     */
    private String callbackHandler;

    /**
     * The certificate selector
     *
     * @parameter
     */
    private String certSelector;

    public String getCallbackHandler()
    {
        return callbackHandler;
    }

    public String getCertSelector()
    {
        return certSelector;
    }

    public void setCallbackHandler(String callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public void setCertSelector(String certSelector)
    {
        this.certSelector = certSelector;
    }
}
