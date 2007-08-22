/*
 */
package net.osm.pss;

import org.omg.CORBA.ORB;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Parameter;
import org.omg.CosPersistentState.Session;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Utility class that provides support for the establishment of PSS 
 * connector and session instances based on supplied configurations.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class PersistanceHandler
{
    private static final boolean trace = false;

    private static final int STORAGE_HOME_MODE = 0;
    private static final int STORAGE_TYPE_MODE = 1;
    private static final String STORAGE_HOME_KEY = "home";
    private static final String STORAGE_TYPE_KEY = "storage";

    public static void register( Connector connector, Configuration config ) throws Exception
    {
        register( connector, config, STORAGE_HOME_MODE );
        register( connector, config, STORAGE_TYPE_MODE );
    }

    private static void register( Connector connector, Configuration config, int mode ) throws Exception
    {
        try
        {
		String key = "";
		Configuration[] children = null;
		if( mode == STORAGE_HOME_MODE )
		{
                children = config.getChildren( STORAGE_HOME_KEY );
            }
	      else
	      {
                children = config.getChildren( STORAGE_TYPE_KEY );
	      }

		if( trace ) System.out.println("persistence set length: " + children.length );
		for( int i = 0; i< children.length; i++ )
            {
		    String psdl = "";
		    String value = "";
                Configuration c = children[i];
                try
                {
                    psdl = c.getAttribute( "psdl" );
			  if( trace ) System.out.println("registering: " + psdl );
                }
                catch (ConfigurationException e)
                {
			  String error = "missing 'psdl' attribute";
			  throw new Exception( error, e );
                }

                try
                {
                    value = c.getAttribute( "class" );
                }
                catch (ConfigurationException e)
                {
			  String error = "missing 'class' attribute";
			  throw new Exception( error, e );
                }

		    try
		    {
	  	        if( mode == STORAGE_HOME_MODE )
		        {
                        connector.register_storage_home_factory( psdl, Class.forName( value ) );
                    }
	              else
	              {
                        connector.register_storage_object_factory( psdl, Class.forName( value ) );
	              }
                }
                catch (Throwable throwable)
                {
			  String error = "unexpected PSS exception";
			  throw new Exception( error, throwable );
                }
            }
        }
        catch( Throwable throwable )
        {
	      String error = "unexpected exception while processing PSS for ";
		if( mode == STORAGE_HOME_MODE )
		{
	  	     throw new Exception( error + STORAGE_HOME_KEY, throwable );
            }
	      else
	      {
	  	     throw new Exception( error + STORAGE_TYPE_KEY, throwable );
	      }
        }
    }
}



