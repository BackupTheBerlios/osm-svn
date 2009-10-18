/*
 * Copyright 2007 Stephen J. McConnell
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

package net.osm.db.server;

import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.File;
import java.net.InetAddress;

import net.dpml.util.Logger;

import org.apache.derby.drda.NetworkServerControl;

/**
* Derby shared server implementation.
*/
public class Server extends NetworkServerControl
{
   /**
    * Deployment context.
    */
    public interface Context
    {
       /**
        * Get the hostname that this server will listen on.  Defaults
        * to localhost.
        */
        String getHost();
        
       /**
        * Get the port number to listner to.  Defaults
        * to 1527.
        */
        int getPort();
        
    }
    
    private final Logger m_logger;
    private final Context m_context;
    
   /**
    * Creation of a new shared Derby server.
    * @param logger the logging channel
    * @param context the deployment context
    * @exception if an error occurs
    */
    public Server( Logger logger, Context context ) throws Exception
    {
        super( InetAddress.getByName( context.getHost() ), context.getPort() );
        
        m_context = context;
        m_logger = logger;
    }
    
   /**
    * Start the server. This method is invoked automatically by Metro as 
    * a part of the component initialization lifecycle.
    * @exception if an error occurs
    */
    public void start() throws Exception
    {
        m_logger.info( "commencing server startup" );
        OutputAdapter adapter = new OutputAdapter();
        PrintWriter writer = new PrintWriter( adapter );
        super.start( writer );
        m_logger.info( "server startup complete" );
    }
    
   /**
    * Stop the server. This method is invoked automatically by Metro as 
    * a part of the component termination lifecycle.
    * @exception if an error occurs
    */
    public void stop() throws Exception
    {
        m_logger.info( "commencing server shutdown" );
        super.shutdown();
        m_logger.info( "server shutdown complete" );
    }
    
   /**
    * Internal adapter to handle redirection of Derby output srream 
    * messages to the logging channel assigned to the component.
    */
    private class OutputAdapter extends OutputStream
    {
        private StringWriter m_writer = new StringWriter();
        
       /**
        * Flush the characters as a string to the logging channel
        * as a debug level message.
        */
        public void flush()
        {
            synchronized( m_writer )
            {
                String message = m_writer.toString();
                if( message.endsWith( "\n" ) )
                {
                    message = message.substring( 0, message.length() - 1 );
                }
                if( message.startsWith( "Apache Derby " ) )
                {
                    int n = message.indexOf( ") " );
                    if( n > 0 )
                    {
                        message = message.substring( n + 2 );
                    }
                }
                m_writer.getBuffer().setLength( 0 );
                m_logger.debug( message );
            }
        }
        
       /**
        * Write a character to the buffer.
        * @param b the character value
        */
        public void write( int b )
        {
            synchronized( m_writer )
            {
                m_writer.write( b );
            }
        }
    }
}

