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

public final class CallbackConfig
{
    private String xwssCallbackHandler;
    private String jmacCallbackHandler;
    private String usernameHandler;
    private String passwordHandler;
    private String samlHandler;

    public String getXwssCallbackHandler()
    {
        return xwssCallbackHandler;
    }

    public void setXwssCallbackHandler(String xwssCallbackHandler)
    {
        this.xwssCallbackHandler = xwssCallbackHandler;
    }

    public String getJmacCallbackHandler()
    {
        return jmacCallbackHandler;
    }

    public void setJmacCallbackHandler(String jmacCallbackHandler)
    {
        this.jmacCallbackHandler = jmacCallbackHandler;
    }

    public String getUsernameHandler()
    {
        return usernameHandler;
    }

    /**
     * Optional - The username handler to be included in the autogenerated policy.
     *
     * @param usernameHandler
     */
    public void setUsernameHandler(String usernameHandler)
    {
        this.usernameHandler = usernameHandler;
    }


    public String getPasswordHandler()
    {
        return passwordHandler;
    }

    /**
     * Optional - The password handler to be included in the autogenerated policy.
     *
     * @param passwordHandler
     */
    public void setPasswordHandler(String passwordHandler)
    {
        this.passwordHandler = passwordHandler;
    }

    public String getSamlHandler()
    {
        return samlHandler;
    }

    /**
     * Optional - The SAML handler to be included in the autogenerated policy.
     *
     * @param samlHandler
     */
    public void setSamlHandler(String samlHandler)
    {
        this.samlHandler = samlHandler;
    }
}
