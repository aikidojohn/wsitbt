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
package com.appsec.wsitbt.core.holders;

/**
 *
 * @author jhite
 */
public final class SecureConversation
{
    /**
     * Set to true if the client should automatically renew the Secure
     * Conversation Token automatically. The default value is false.
     * 
     * //Do not remove this annotation. it's required for maven
     * @parameter
     */
    private boolean renewExpiredSCT;

    public static final long DEFAULT_LIFETIME = 300000;
    /**
     * Set the lifetime of the Secure Conversation Token. The default value
     * is 300000 (5 minutes). See {@link #DEFAULT_LIFETIME}.
     * 
     * //Do not remove this annotation. it's required for maven
     * @parameter
     */
    private long lifetime = DEFAULT_LIFETIME;

    public long getLifetime()
    {
        return lifetime;
    }

    public void setLifetime(final long lifetime)
    {
        this.lifetime = lifetime;
    }

    public boolean isRenewExpiredSCT()
    {
        return renewExpiredSCT;
    }

    public void setRenewExpiredSCT(final boolean renewExpiredSCT)
    {
        this.renewExpiredSCT = renewExpiredSCT;
    }
}
