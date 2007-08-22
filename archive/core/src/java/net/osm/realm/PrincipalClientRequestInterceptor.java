/**
 */

package net.osm.realm;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ORBInitInfo;

/**
 * Client-side request interceptor.
 * <p>
 * A request Interceptor is designed to intercept the flow of a 
 * request/reply sequence through the ORB at specific points so that 
 * services can query the request information and manipulate the service 
 * contexts which are propagated between clients and servers. The primary 
 * use of request Interceptors is to enable ORB services to transfer 
 * context information between clients and servers. 
 * <p>
 * The PrincipalClientRequestInterceptor established a service context to 
 * be propergated to the target of the invocation.  This service context 
 * contains credentials through which the server can undertake authenticate 
 * and execute access control decision. 
 * 
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 * @version 1.0
 */

public class PrincipalClientRequestInterceptor extends LocalObject 
implements ClientRequestInterceptor
{

   /**
    * Internal static encoded instance used with codec.
    */
    private static final Encoding encoding = new Encoding( 
		ENCODING_CDR_ENCAPS.value, 
		new Integer(1).byteValue(), 
		new Integer(2).byteValue());

   /**
    * ORB initilization info from which initial references can be 
    * resolved.
    */
    private ORBInitInfo info;

   /**
    * Slot allocated for storage of the principal under the PICurrent.
    */
    private int slot;

   /**
    * Trace policy for debugging.
    */
    private boolean trace = false;

    //===============================================================
    // constructor
    //===============================================================

   /**
    * Creation of a new <code>PrincipalClientRequestInterceptor</code> based 
    * on a supplied ORB initialitation information set and allocate context
    * slot.
    * @param info ORB initialization information
    * @param slot the allocated portable interceptor context slot
    */
    public PrincipalClientRequestInterceptor( ORBInitInfo info, int slot )
    {
	  this.info = info;
        this.slot = slot;
    }

    //===============================================================
    // ClientRequestInterceptor
    //===============================================================

   /**
    * Allows an Interceptor to query request information and modify the 
    * service context before the request is sent to the server.  The 
    * implementation ensures that a service context is established before
    * the request is released.
    *
    * @param ri client request info
    * @exception ForwardRequest
    */
    public void send_request( ClientRequestInfo ri ) throws ForwardRequest
    {
        //
	  // establish the service context 
	  //

        if( trace ) System.out.println("CLIENT-INTERCEPTOR: " + ri.operation() );
	  try
	  {
		//
		// Get the Any containing the PrincipalServiceContext from the
		// request information using the slot allocated in the constructor.
            //

            Any any = ri.get_slot( slot );
		PrincipalManagerBase manager = (PrincipalManagerBase)
			info.resolve_initial_references( "PRINCIPAL" );
		StandardPrincipal principal = manager.getLocalPrincipal( );
		OpaqueHelper.insert( any, principal.getEncoded() );

		//
		// Create a service context instance and place the Any (containing 
            // the current principal) into the service context as a byte array, 
            // and add the service context object to the client request info
            // to passed to the server.
		//

            CodecFactory codecFactory = info.codec_factory();
		Codec codec = codecFactory.create_codec( encoding );
		ServiceContext context = new ServiceContext();
	      context.context_id = RealmSingleton.SERVICE_CONTEXT_IDENTIFIER;
		context.context_data = codec.encode( any );
		ri.add_request_service_context( context, true );
            if( trace ) System.out.println("CLIENT-INTERCEPTOR done" );
	  }
        catch( Exception e )
	  {
            if( trace ) System.out.println("PCRI Service context creation error." );
		e.printStackTrace();
	  }
    }

   /**
    * Allows an Interceptor to query information during a Time-Independent 
    * Invocation (TII) polling get reply sequence.
    * 
    * @param ri client request info
    */
    public void send_poll( ClientRequestInfo ri )
    {
    }

   /**
    * Allows an Interceptor to query the information on a reply after it is 
    * returned from the server and before control is returned to the client.
    * 
    * @param ri client request info
    */
    public void receive_reply( ClientRequestInfo ri )
    {
    }

   /**
    * Indicates to the interceptor that an exception occurred.
    * 
    * @param ri client request info
    * @exception ForwardRequest
    */
    public void receive_exception( ClientRequestInfo ri ) throws ForwardRequest
    { 
    }

   /**
    * Allows an Interceptor to query the information available when a request 
    * results in something other than a normal reply or an exception.
    * 
    * @param ri client request info
    * @exception ForwardRequest
    */
    public void receive_other( ClientRequestInfo ri ) throws ForwardRequest
    { 
    }
    
    //===========================================================================
    // Interceptor
    //===========================================================================

   /**
    * Returns the name of the interceptor.
    * <p>
    * Each Interceptor may have a name that may be used administratively 
    * to order the lists of Interceptors. Only one Interceptor of a given 
    * name can be registered with the ORB for each Interceptor type. An 
    * Interceptor may be anonymous, i.e., have an empty string as the name 
    * attribute. Any number of anonymous Interceptors may be registered with 
    * the ORB.
    *
    * @return the name of the interceptor.
    */
    public java.lang.String name()
    {
        return getClass().getName();
    }

   /**
    * Provides an opportunity to destroy this interceptor.
    * The destroy method is called during <code>ORB.destroy</code>. When an 
    * application calls <code>ORB.destroy</code>, the ORB:
    * <ol>
    *   <li>waits for all requests in progress to complete</li>
    *   <li>calls the <code>Interceptor.destroy</code> operation for each 
    *       interceptor</li>
    *   <li>completes destruction of the ORB</li>
    * </ol>
    * Method invocations from within <code>Interceptor.destroy</code> on 
    * object references for objects implemented on the ORB being destroyed 
    * result in undefined behavior. However, method invocations on objects 
    * implemented on an ORB other than the one being destroyed are 
    * permitted. (This means that the ORB being destroyed is still capable 
    * of acting as a client, but not as a server.) 
    */
    public void destroy ()
    {
        if( trace ) System.out.println("DESTROY");
    }

}

//System.out.println(
//	"0x" + Integer.toHexString((int)'O') 
//	+ Integer.toHexString((int)'S')
//	+ Integer.toHexString((int)'M') + "00"
//);

