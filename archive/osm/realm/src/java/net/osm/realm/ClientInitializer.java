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
 * Establishes an initial reference local object named "PRINCIPAL" containing
 * the current security principal.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 * @version 1.0
 */

public class ClientInitializer extends LocalObject 
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
    * Establishes the initial reference to an empty <code>PrincipalManager</code> accessible
    * through the <code>orb.register_initial_references</code> method, using the keyword
    * "PRINCIPAL".
    * @param info the ORBInitInfo
    */
    public void pre_init( ORBInitInfo info ) 
    {
        if( getLogger() == null ) throw new IllegalStateException(
          "ClientInitializer has not been supplied with a logger.");

        getLogger().debug( "pre_init" );
	  try	
        {
		slot = info.allocate_slot_id();
		info.add_client_request_interceptor( 
			new PrincipalClientRequestInterceptor( info, slot ) );
	  }
	  catch ( DuplicateName duplicateName ) 
        {
            throw new INITIALIZE("Unable to initalize principal current due to a DuplicateName.");
	  }
	  catch ( Exception e ) 
        {
            throw new INITIALIZE("Unable to initalize client.");
	  }
        getLogger().debug( "pre_init complete" );
    }
	
   /**
    * post_init register PSS initial refenrence
    * @param info the ORBInitInfo
    */
    public void post_init ( ORBInitInfo info ) 
    {
        getLogger().debug( "post_init" );
	  try	
        {
	  	info.register_initial_reference( 
			"PRINCIPAL", new PrincipalManagerBase( info, slot ) );
	  }
	  catch ( InvalidName invalidName ) 
        {
            throw new INITIALIZE("Unable to initalize principal current due to an InvalidName.");
	  }
        getLogger().debug( "post_init complete" );
    }
}
