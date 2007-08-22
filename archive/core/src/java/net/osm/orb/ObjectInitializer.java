/**
 */

package net.osm.orb;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * Establishes an interceptor that places the object identifier 
 * in an incomming request into a thread local variable.  The 
 * interceptor is made available as an ORB initial references 
 * using the name "ObjectIdentifier".
 * @see ObjectIdentifierService
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 * @version 1.0
 */

public class ObjectInitializer extends LocalObject 
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

    private ObjectIdentifierService service = new ObjectIdentifierService();

    //====================================================================
    // ORBInitializer
    //====================================================================

   /**
    * Registers the ObjectIdentifierServerRequestInterceptor as an interceptor
    * on the current ORB.
    * @param info the ORBInitInfo
    */
    public void pre_init( ORBInitInfo info ) 
    {
        if( trace ) System.out.println("ObjectInitializer/pre_init");
	  try	
        {
		info.add_server_request_interceptor( 
			new ObjectIdentifierServerRequestInterceptor( service ) );
	  }
	  catch ( DuplicateName duplicateName ) 
        {
		final String error =  "unable to initalize object interceptor due to a DuplicateName.";
            throw new INITIALIZE( error );
	  }
	  catch ( Exception e ) 
        {
		final String error =  "unable to initalize object interceptor - cause: ";
            throw new INITIALIZE( error + e );
	  }
    }
	
   /**
    * Registers the object identifier service as an initial reference.
    * @param info the ORBInitInfo
    */
    public void post_init( ORBInitInfo info ) 
    {
        if( trace ) System.out.println("ObjectInitializer/post_init");
	  try	
        {
	  	info.register_initial_reference( 
			"ObjectIdentifier", service );
	  }
	  catch ( InvalidName invalidName ) 
        {
            throw new INITIALIZE("Unable to initalize orb due to an InvalidName.");
	  }
    }
}
