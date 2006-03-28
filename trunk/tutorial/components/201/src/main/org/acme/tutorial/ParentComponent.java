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

package org.acme.tutorial;

import java.util.logging.Logger;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;

import net.dpml.component.Provider;

/**
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class ParentComponent
{
   /**
    * Creation of a new composite component instance.
    * @param parts the parts manager
    */
    public ParentComponent( final Logger logger, final PartsManager parts ) throws Exception
    {
        String[] keys = parts.getKeys();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            ComponentHandler handler = parts.getComponentHandler( key );
            Provider provider = handler.getProvider();
            Object instance = provider.getValue( false );
            logger.info( "key: " + key + " (" + instance.getClass().getName() + ")" );
        }
    }
}
