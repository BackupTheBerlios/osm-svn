/*
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.prefs;

import java.util.EventObject;
import java.net.PasswordAuthentication; 

/**
 * An event pertaining to a modification to a host model base url, 
 * index, request identifier or connection credentials.
 */
public class PasswordAuthenticationEvent extends EventObject
{
    private final PasswordAuthentication m_authentication;

    public PasswordAuthenticationEvent( PasswordAuthenticationModel source, PasswordAuthentication auth )
    {
        super( source );
        m_authentication = auth;
    }
    
    public PasswordAuthenticationModel getPasswordAuthenticationModel()
    {
        return (PasswordAuthenticationModel) getSource();
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
        return m_authentication;
    }
}
