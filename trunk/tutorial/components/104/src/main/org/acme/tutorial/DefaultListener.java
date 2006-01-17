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

/**
 * A server.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class DefaultListener implements Listener
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

   /**
    * The construction criteria.
    */
    public interface Context
    {
       /**
        * Return the server.
        * @return the server.
        */
        Server getServer();
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final Logger m_logger;
    private final Server m_server;
    
    private int m_count = 0;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new singleton component instance.
    * @param logger an assigned logging channel
    * @param context a context implementation fullfilling the context criteria
    */
    public DefaultListener( final Logger logger, final Context context )
    {
        m_logger = logger;
        m_server = context.getServer();
        m_server.addListener( this );
    }

    //------------------------------------------------------------------
    // Server
    //------------------------------------------------------------------
   
   /**
    * Handle notification from the server.
    * @param message the nofication
    */
    public void notify( String message )
    {
        m_logger.debug( message );
        m_count++;
    }
    
    //------------------------------------------------------------------
    // Test case hook
    //------------------------------------------------------------------
    
   /**
    * Return the number of time the notify method has been invoked.
    * @return the notification count
    */
    public int getCount()
    {
        return m_count;
    }
}
