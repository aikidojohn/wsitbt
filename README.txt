====
    WSIT Build Tools (http://wsitbt.codeplex.com)

    Copyright (c) 2011 Application Security, Inc.

    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        Application Security, Inc.
====

TODO - stuff to work on, etc

1. Refactor more duplicate classes/logic out of the ant/maven modules and into the core module under
    the package: com.appsec.wsitbt.core.holders
    See: com.appsec.wsitbt.core.holders.SecureConversation as an example.

    Classes to consider for this refactoring include:
    ant:                                        maven:
        com.appsecinc.ant.CallbackConfig           com.appsec.maven.wsit.Callback
        com.appsecinc.ant.CertStoreConfig           com.appsec.maven.wsit.Certstore
        com.appsecinc.ant.KeyStoreConfig            com.appsec.maven.wsit.Keystore
        com.appsecinc.ant.TrustStoreConfig          com.appsec.maven.wsit.Truststore

    Much of the above refactoring was previously done in Dan's local branch, so that may be a useful reference.


2. Cleanup code base according to various reports (CPD, PMD, Findbugs, etc). Probably better to do this after #2 above
   to avoid duplicating the same cleanups in ant and maven.


3. Find a way to ensure invalid parameters cause an error in maven plugin goals.