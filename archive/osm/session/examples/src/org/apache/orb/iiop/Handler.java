
package org.apache.orb.iiop;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.omg.CORBA.ORB;

import org.apache.orb.corbaloc.CorbalocURLConnection;

/**
 * Corbaloc URL protocol handler.  
 */
public class Handler extends URLStreamHandler
{
    /**
     * the current ORB.
     */
     private ORB m_orb;

    /**
     * The connection object.
     */
     private URLConnection m_connection;

    /**
     * Creation of a new handler.  This constructor is typically called
     * by the CorbalocHandlerFactory.
     * @param orb the current ORB
     * @exception NullPointerException if the supplied orb parameter is null
     */
     public Handler( ORB orb ) throws NullPointerException
     {
         if( null == orb )
         {
             throw new NullPointerException("Illegal null orb argument.");
         }
         m_orb = orb;
     }

    /**
     * Opens a connection to the specified URL.
     *
     * @param url A URL to open a connection to.
     * @return The established connection.
     * @throws IOException If a connection failure occurs.
     */
    protected URLConnection openConnection( final URL url )
      throws IOException
    {
        if( m_connection != null ) return m_connection;
        m_connection = new CorbalocURLConnection( m_orb, url );
        m_connection.connect();
        return m_connection;
    }

    protected int getDefaultPort()
    {
        return 2809;
    }

    protected String toExternalForm( URL url )
    {
	  StringBuffer result = new StringBuffer( "corbaloc:iiop:1.2@" );
        result.append( url.getAuthority() );
        if (url.getFile() != null )
        {
            result.append(url.getFile());
        }
	  if (url.getQuery() != null ) 
        {
	      result.append("?");
            result.append(url.getQuery());
	  }
	  if (url.getRef() != null ) 
        {
	      result.append("#");
            result.append(url.getRef());
	  }
	  return result.toString();
    }
}
