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

public final class CertStoreConfig
{
    private String callbackHandler;
    private String certSelector;

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
}
