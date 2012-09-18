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
package com.appsec.wsitbt.core.holders;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * @author Dan Rollo
 *         Date: 7/12/11
 *         Time: 11:03 AM
 */
public final class SecureConversationTest {

    @Test
    public void testCreate() {
        final SecureConversation secureConversation = new SecureConversation();
        assertFalse(secureConversation.isRenewExpiredSCT());
        assertEquals(SecureConversation.DEFAULT_LIFETIME, secureConversation.getLifetime());
    }

    @Test
    public void testSetters() {
        final SecureConversation secureConversation = new SecureConversation();

        secureConversation.setRenewExpiredSCT(true);
        assertTrue(secureConversation.isRenewExpiredSCT());

        secureConversation.setLifetime(0);
        assertEquals(0, secureConversation.getLifetime());
    }
}
