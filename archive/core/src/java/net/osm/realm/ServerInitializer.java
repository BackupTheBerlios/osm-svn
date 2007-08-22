/**
 */

package net.osm.realm;

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
implements ORBInitializer 
{

    //====================================================================
    // static
    //====================================================================
	
    private static final boolean trace = false;

    //====================================================================
    // state
    //====================================================================

    private int slot;
	
    //====================================================================
    // ORBInitializer
    //====================================================================

   /**
    * initialize
    * @param info the ORBInitInfo
    */
    public void pre_init( ORBInitInfo info ) 
    {
        if( trace ) System.out.println("ServerInitializer/pre_init");
	  try	
        {
		slot = info.allocate_slot_id();
		info.add_server_request_interceptor( 
			new PrincipalServerRequestInterceptor( info, slot ) );
	  }
	  catch ( DuplicateName duplicateName ) 
        {
            throw new INITIALIZE("Unable to initalize principal current due to a DuplicateName.");
	  }
	  catch ( Exception e ) 
        {
            throw new INITIALIZE("Unable to initalize server.");
	  }
    }
	
   /**
    * post_init register PSS initial refenrence
    * @param info the ORBInitInfo
    */
    public void post_init( ORBInitInfo info ) 
    {
        if( trace ) System.out.println("ServerInitializer/post_init");
	  try	
        {
	  	info.register_initial_reference( 
			"PrincipalManager", new PrincipalManagerBase( info, slot ) );
	  	info.register_initial_reference( 
			"AccessController", new AccessManager( ) );
	  }
	  catch ( InvalidName invalidName ) 
        {
            throw new INITIALIZE("Unable to initalize orb due to an InvalidName.");
	  }
    }
}
