/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.osm.http.impl;

import java.net.URI;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.osm.http.spi.RealmContext;

import org.mortbay.jetty.security.UserRealm;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * Hash user realm with enhanced keystore resolution semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="realm", lifestyle=SINGLETON )
@Services( UserRealm.class )
public class HashUserRealm extends org.mortbay.jetty.security.HashUserRealm
{
   /**
    * Creation of a new hash user realm.
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public HashUserRealm( RealmContext context ) throws Exception
    {
        String name = context.getName();
        super.setName( name );
        URI config = context.getURI();
        setConfig( config.toASCIIString() );
    }
}
