

package net.osm.gateway;

import java.io.File;
import java.util.LinkedList;
import java.util.Iterator;
import java.net.URL;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;

import org.apache.orb.ORB;
import org.apache.orb.util.IOR;

/**
 * ConnectionMonitor is a runnable object that is provided with a URL 
 * that is used to establish a connection with the enterprise gateway
 * server.  The implementation handles URL lookup, resolution of an 
 * object reference, and periodic checking of the object existence.
 */
public class ConnectionMonitor extends AbstractLogEnabled 
implements Initializable, Disposable, Runnable
{

    //=================================================================
    // static 
    //=================================================================

    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;

    private static final int INITIALIZATION = 0;
    private static final int EXECUTION = 1;

    //=================================================================
    // state
    //=================================================================

   /**
    * Reference to the thread we will execute within.
    */
    private Thread m_thread;

   /**
    * Flag indicating disposal state which will result in the 
    * termination of the thread.
    */
    private boolean m_disposed = false;

   /**
    * Reference to the connection status
    */
    private int m_status = DISCONNECTED;

   /**
    * Reference to the current mode of execution.
    */
    private int m_phase = INITIALIZATION;

   /**
    * The corbaloc path.
    */
    private URL m_url;

   /**
    * The object resolved from the IOR
    */
    private org.omg.CORBA.Object m_object;

   /**
    * The ORB.
    */
    //private ORB m_orb;

    private int m_last; 

    //=======================================================================
    // Constructor
    //=======================================================================

   /**
    * Creation of a new connection monotor.
    * @param url a corbaloc URL
    */
    public ConnectionMonitor( URL url )
    {
       m_url = url;
    }

    //=======================================================================
    // Initializable
    //=======================================================================

   /**
    * Invoked by the container to request component initialization.
    */
    public void initialize( ) throws Exception
    {
        if( getLogger().isDebugEnabled() ) 
          getLogger().debug( "Initiating monitor on path: " + m_url );
        m_thread = new Thread( this );
        m_thread.start();
    }

    //=================================================================
    // Runnable
    //=================================================================

   /**
    * Method invoked by the initize implementation.  The run method
    * periodically checks for a connection to a target object.
    */
    public void run()
    {
        while( !m_disposed )
        {
            int pause = 10000;
            switch( m_phase )
            {
              case INITIALIZATION:
                try
                {
                    m_object = (org.omg.CORBA.Object) m_url.getContent();
                    m_phase = EXECUTION;
                    pause = 0;
                }
                catch( Throwable e )
                {
                    m_status = DISCONNECTED;
                }
              case EXECUTION:
                try
                {
                    if( m_object._non_existent() )
                    {
                        m_status = DISCONNECTED;
                        m_phase = INITIALIZATION;
                        pause = 10000;
                    }
                    else
                    {
                        m_status = CONNECTED;
                        pause = 10000;
                    }
                }
                catch( Throwable e )
                {
                    m_status = DISCONNECTED;
                    m_phase = INITIALIZATION;
                }
            }
            if( m_status != m_last )
            {
                if( m_status == DISCONNECTED )
                {
                    final String message = "getaway disconnected";
                    getLogger().debug( message );
                    System.out.println( message );
                }
                else
                {
                    final String message = "getaway connected";
                    getLogger().debug( message );
                    System.out.println( message );
                }
                m_last = m_status;
            }
            if(( pause > 0 ) && ( !m_disposed ))
            {
                try
                {
                    Thread.currentThread().sleep( pause );
                }
                catch( Throwable e )
                {
                }
            }
        }
    }

    //=================================================================
    // ConnectionManager
    //=================================================================

   /**
    * Return the current connection status.
    */
    public boolean getConnectedState()
    {
        return m_status != DISCONNECTED;
    }

   /**
    * Return the object of the conection.
    */
    public org.omg.CORBA.Object getObject()
    {
        return m_object;
    }

    //=================================================================
    // Disposable 
    //=================================================================

   /**
    * Clean up state members.
    */ 
    public void dispose()
    {
        m_status = DISCONNECTED;
	  if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug("disconnecting");
	      getLogger().debug("disposal");
        }
	  m_disposed = true;
    }
}
