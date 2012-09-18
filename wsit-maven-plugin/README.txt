====
    WSIT Build Tools (http://aikidojohn.github.com/wsitbt/)

    Copyright (c) 2011 Application Security, Inc.

    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        Application Security, Inc.
====

Maven WSIT plugin

This plugin provides goals to generate client and server side configuration.

Goals
----------------

client - generates client side configuration. It combines a wsdl with wsit specific 
         elements that specify security information.
         
service - Generates service side configuration. It combines a wsdl with wsit 
          specific elements that control security. It can combine an existing policy
          fragment with the wsd.
          
clean - Strips all wsit specific elements from a wsit configuration file. 