/**
 */

package net.osm.realm;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * Establishes the principal client and server interceptors.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 * @version 1.0
 */

public class ServerInitializer extends LocalObject 
implements ORBInitializer, LogEnabled
{

    //====================================================================
    // state
    //====================================================================

    private int slot;
    private Logger m_logger;

    //=======================================================================
    // LogEnabled
    //=======================================================================
    
   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
	
    //====================================================================
    // ORBInitializer
    //====================================================================

   /**
    * initialize
    * @param info the ORBInitInfo
    */
    public void pre_init( ORBInitInfo info ) 
    {
        getLogger().debug( "pre_init" );
	  try	
        {
		slot = info.allocate_slot_id();
		info.add_server_request_interceptor( 
			new PrincipalServerRequestInterceptor( info, slot ) );
	  }
	  catch ( Throwable e ) 
        {
            final String error = "Unable to initalize server interceptor.";
            getLogger().error( error, e );
            throw new INITIALIZE( error );
	  }
        getLogger().debug( "pre_init complete" );
    }
	
   /**
    * post_init register PSS initial refenrence
    * @param info the ORBInitInfo
    */
    public void post_init( ORBInitInfo info ) 
    {
        getLogger().debug( "post_init" );
	  try	
        {
	  	info.register_initial_reference( 
			"PrincipalManager", new PrincipalManagerBase( info, slot ) );
	  	info.register_initial_reference( 
			"AccessController", new AccessManager( ) );
	  }
	  catch ( InvalidName e ) 
        {
            final String error = "Post initalize failure in server interceptor.";
            getLogger().error( error, e );
            throw new INITIALIZE( error );
	  }
        getLogger().debug( "post_init complete" );
    }
}
