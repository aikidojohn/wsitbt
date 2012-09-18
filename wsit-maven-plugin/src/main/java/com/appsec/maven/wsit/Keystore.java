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

public final class Keystore
{
    /**
     * Keystore callback handler class.
     *
     * @parameter
     */
    private String callbackHandler;

    /**
     * Keystore alias selector class.
     *
     * @parameter
     */
    private String aliasSelector;

    /**
     * Keystore alias.
     *
     * @parameter
     */
    private String alias;

    /**
     * Keystore location. This can be specified instead of a callback handler and alias selector.
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

    /**
     * Key password.
     *
     * @parameter
     */
    private String keypass;

    public String getCallbackHandler()
    {
        return callbackHandler;
    }

    public String getAliasSelector()
    {
        return aliasSelector;
    }

    public String getAlias()
    {
        return alias;
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

    public String getKeypass()
    {
        return keypass;
    }

    public void setCallbackHandler(String callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public void setAliasSelector(String aliasSelector)
    {
        this.aliasSelector = aliasSelector;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
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

    public void setKeypass(String keypass)
    {
        this.keypass = keypass;
    }
}
