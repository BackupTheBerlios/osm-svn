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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * A server.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class DefaultServer implements Server
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
        * Return the port.
        * @param defaultPort the default port
        * @return the port.
        */
        int getPort( int defaultPort );
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final int m_port;
    
    private List m_listeners = new LinkedList();

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new singleton component instance.
    * @param context a context implementation fullfilling the context criteria
    */
    public DefaultServer( final Context context )
    {
        m_port = context.getPort( 1234 );
        
        // setup port listener
    }

    //------------------------------------------------------------------
    // Server
    //------------------------------------------------------------------
   
   /**
    * Add a listener.
    * @param listener the listener
    */
    public void addListener( Listener listener )
    {
        synchronized( m_listeners )
        {
            if( m_listeners.contains( listener ) )
            {
                throw new IllegalStateException( "Listener already registered." );
            }
            m_listeners.add( listener );
        }
    }
    
    //------------------------------------------------------------------
    // testcase hook
    //------------------------------------------------------------------
    
   /** 
    * Rather than fully implementing this component we are cheating by 
    * providing this hook so that the testcase can fire the notification.
    * @param message the message supplied by the testcase to be forwarded 
    *    by the implementation to registereed listeners
    */
    public void triggerNotify( String message )
    {
        synchronized( m_listeners )
        {
            Iterator iterator = m_listeners.iterator();
            while( iterator.hasNext() )
            {
                Listener listener = (Listener) iterator.next();
                listener.notify( message );
            }
        }
    }
}
