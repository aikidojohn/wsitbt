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

public final class KeyStoreConfig
{
    private String callbackHandler;
    private String aliasSelector;
    private String alias;
    private String location;
    private String type;
    private String storepass;
    private String keypass;

    public String getCallbackHandler()
    {
        return callbackHandler;
    }

    public void setCallbackHandler(String callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public String getAliasSelector()
    {
        return aliasSelector;
    }

    public void setAliasSelector(String aliasSelector)
    {
        this.aliasSelector = aliasSelector;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
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

    public String getKeypass()
    {
        return keypass;
    }

    public void setKeypass(String keypass)
    {
        this.keypass = keypass;
    }
}
