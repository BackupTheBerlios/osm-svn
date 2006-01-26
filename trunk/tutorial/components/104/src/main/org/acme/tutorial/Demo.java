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

import net.dpml.logging.Logger;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;

/**
 * The demo class is used to aggregate a collection of components and 
 * provide some hooks for the testcase.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class Demo
{
    private final PartsManager m_parts;
    private final Logger m_logger;
    
   /**
    * Creation of a new demo component.
    * @param logger a logging channel
    * @param parts the internal parts manager
    */
    public Demo( Logger logger, PartsManager parts )
    {
        m_logger = logger;
        m_parts = parts;
    }
    
   /**
    * Hook for testcase to fire a message noytify request to the 
    * server which in turn fires notifiy requests to listeners.
    * @param message the notification message
    * @return the number of notify messages fired to the listener
    * @exception Exception if something goes wrong
    */
    public int test( String message ) throws Exception
    {
        m_logger.debug( "test: " + message );
        getServer().triggerNotify( message );
        return getListener().getCount();
    }
    
    DefaultServer getServer() throws Exception
    {
        ComponentHandler handler = m_parts.getComponentHandler( "server" );
        return (DefaultServer) handler.getProvider().getValue( false );
    }
    
    DefaultListener getListener() throws Exception
    {
        ComponentHandler handler = m_parts.getComponentHandler( "listener" );
        return (DefaultListener) handler.getProvider().getValue( false );
    }
}
