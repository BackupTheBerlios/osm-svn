/**
 */

package net.osm.realm;

import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Codec;

import net.osm.util.ExceptionHelper;


/**
 * Server-side request interceptor.
 * <p>
 * A request Interceptor is designed to intercept the flow of a 
 * request/reply sequence through the ORB at specific points so that 
 * services can query the request information and manipulate the service 
 * contexts which are propagated between clients and servers. The primary 
 * use of request Interceptors is to enable ORB services to transfer 
 * context information between clients and servers.
 */
public class PrincipalServerRequestInterceptor extends LocalObject 
implements ServerRequestInterceptor
{

    //=================================================================
    // static
    //=================================================================

    private static final Encoding encoding = new Encoding( 
	ENCODING_CDR_ENCAPS.value, new Integer(1).byteValue(), 
      new Integer(2).byteValue());
    
    private static InheritableThreadLocal carrier = new InheritableThreadLocal();

   /**
    * Static operation that returns the security principal associated 
    * with the current thread of execution.
    * @return StandardPrincipal the active security principal
    */
    public static StandardPrincipal getCarrierPrincipal()
    {
        return (StandardPrincipal) carrier.get();
    }

    //=================================================================
    // state
    //=================================================================

   /**
    * The certificate factory from which the certificate chain will
    * be created.
    */ 
    private CertificateFactory factory;

    private ORBInitInfo info;

    private int slot;

    private boolean trace = false;



    //=================================================================
    // constructor
    //=================================================================

    public PrincipalServerRequestInterceptor( ORBInitInfo info, int slot )
    {
	  this.info = info;
        this.slot = slot;
        try
	  {
            factory = CertificateFactory.getInstance("X.509");
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException(
	        "PrincipalServerRequestInterceptor - failed to establish a certificate factory.", e );
	  }
    }

    //=================================================================
    // ServerRequestInterceptor
    //=================================================================

   /**
    * Allows the interceptor to process service context information.
    * <p>
    * At this interception point, Interceptors must get their service 
    * context information from the incoming request transfer it to 
    * <code>PortableInterceptor.Current</code>'s slots.  
    * <p>
    * This interception point is called before the servant manager is called. 
    * Operation parameters are not yet available at this point. This 
    * interception point may or may not execute in the same thread as 
    * the target invocation. 
    * <p>
    * This interception point may throw a system exception. If it does, 
    * no other Interceptors' <code>receive_request_service_contexts</code> 
    * operations are called. Those Interceptors on the Flow Stack are 
    * popped and their <code>send_exception</code> interception points are 
    * called. 
    * <p>
    * This interception point may also throw a <code>ForwardRequest</code> 
    * exception.  If an Interceptor throws this exception, no other 
    * Interceptors' <code>receive_request_service_contexts</code> operations 
    * are called. Those Interceptors on the Flow Stack are popped and 
    * their <code>send_other</code> interception points are called. 
    * <p>
    * Compliant Interceptors shall properly follow 
    * <code>completion_status</code> semantics if they throw a system 
    * exception from this interception point. The 
    * <code>completion_status</code> shall be COMPLETED_NO.
    * 
    * @param ri Information about the current request being intercepted.
    * @exception ForwardRequest If thrown, indicates to the ORB that a
    *      retry of the request should occur with the new object given in
    *      the exception.
    */
    public void receive_request_service_contexts( ServerRequestInfo ri ) throws ForwardRequest
    {
	  Any any = null;
	  try
	  {

            //
	      // Extract the client supplied service context and establish
            // the identity of the invoking principal.
	      //

		if( trace ) System.out.println("\nSERVICE CONTEXT INTERCEPTOR");
            ServiceContext context = ri.get_request_service_context( 
			RealmSingleton.SERVICE_CONTEXT_IDENTIFIER );
            CodecFactory codecFactory = info.codec_factory();
	      Codec codec = codecFactory.create_codec( encoding );
		any = codec.decode( context.context_data );
		byte[] encoded = OpaqueHelper.extract( any );

		ByteArrayInputStream inputstream = new ByteArrayInputStream( encoded );
		CertPath path = factory.generateCertPath( inputstream );

	      if( trace ) System.out.println(
		  "CERTIFICATE PATH LENGTH: " + path.getCertificates().size() );

		// 
		// Put the principal into the PICurrent
		// using the previously allocated slot.
		//

	      Any a = ri.get_slot( this.slot );
	      PrincipalContextHelper.insert( a, new PrincipalContextBase( path ) );
            ri.set_slot( this.slot, a );

		if( trace ) System.out.println("SERVICE CONTEXT INTERCEPTOR done\n");

        }
	  catch( BAD_PARAM badParam )
	  {
		System.out.println("\nUnknown Principal - no service context supplied.\n");
	  }
	  catch( MARSHAL marshal )
	  {
		String error = "";
		if( any.type().kind() == org.omg.CORBA.TCKind.tk_null )
		{
		    error = ExceptionHelper.packException(
			"Service context slot is empty.", marshal );
		}
		else if( any.type() != OpaqueHelper.type() )
		{
		    error = ExceptionHelper.packException(
			"Service context does not contain a Opaque value.", marshal );
		}
		else
		{
		    error = ExceptionHelper.packException(
			"Unexpected marshalling exception while resolving principal.", marshal );
		}
            throw new MARSHAL( error, 0, CompletionStatus.COMPLETED_NO );
	  }
        catch( Exception e )
	  {
		String error = ExceptionHelper.packException(
		    "Unexpected exception while receiving service context", e );
		ExceptionHelper.printMessage( error );
		throw new BAD_PARAM( error, 0, CompletionStatus.COMPLETED_NO );
	  }
    }

   /**
    * Allows an Interceptor to query request information after all the 
    * information, including operation parameters, are available. This 
    * interception point shall execute in the same thread as the target 
    * invocation.
    * <p>
    * This interception point may throw a system exception. If it does, no 
    * other Interceptors' <code>receive_request</code> operations are 
    * called. Those Interceptors on the Flow Stack are popped and their 
    * <code>send_exception</code> interception points are called. 
    * <p>
    * This interception point may also throw a <code>ForwardRequest</code> 
    * exception.  If an Interceptor throws this exception, no other 
    * Interceptors' <code>receive_request</code> operations are called. 
    * Those Interceptors on the Flow Stack are popped and their 
    * <code>send_other</code> interception points are called.
    * <p>
    * Compliant Interceptors shall properly follow 
    * <code>completion_status</code> semantics if they throw a system 
    * exception from this interception point. The 
    * <code>completion_status</code> shall be <code>COMPLETED_NO</code>.
    * 
    * @param ri Information about the current request being intercepted.
    * @exception ForwardRequest If thrown, indicates to the ORB that a
    *     retry of the request should occur with the new object given in
    *     the exception.
    */
    public void receive_request( ServerRequestInfo ri) throws ForwardRequest
    {
        try
	  {
	      Any a = ri.get_slot( this.slot );
	      PrincipalContext context = PrincipalContextHelper.extract( a );
		StandardPrincipal principal = new StandardPrincipalBase( context );
		carrier.set( principal );
        }
	  catch( Throwable e )
	  {
		e.printStackTrace();
        }
    }

   /**
    * Allows an Interceptor to query reply information and modify the 
    * reply service context after the target operation has been invoked 
    * and before the reply is returned to the client. This interception 
    * point shall execute in the same thread as the target invocation. 
    * <p>
    * This interception point may throw a system exception. If it does, 
    * no other Interceptors' <code>send_reply</code> operations are called. 
    * The remaining Interceptors in the Flow Stack shall have their 
    * <code>send_exception</code> interception point called. 
    * <p>
    * Compliant Interceptors shall properly follow 
    * <code>completion_status</code> semantics if they throw a 
    * system exception from this interception point. The 
    * <code>completion_status</code> shall be <code>COMPLETED_YES</code>.
    * 
    * @param ri Information about the current request being intercepted.
    */
    public void send_reply( ServerRequestInfo ri )
    {
    }

   /**
    * Allows an Interceptor to query the exception information and modify 
    * the reply service context before the exception is thrown to the client. 
    * When an exception occurs, this interception point is called. This 
    * interception point shall execute in the same thread as the target 
    * invocation. 
    * <p>
    * This interception point may throw a system exception. This has the 
    * effect of changing the exception which successive Interceptors 
    * popped from the Flow Stack receive on their calls to 
    * <code>send_exception</code>. The exception thrown to the client will 
    * be the last exception thrown by an Interceptor, or the original 
    * exception if no Interceptor changes the exception. 
    * <p>
    * This interception point may also throw a <code>ForwardRequest</code> 
    * exception.  If an Interceptor throws this exception, no other 
    * Interceptors' <code>send_exception</code> operations are called. The 
    * remaining Interceptors in the Flow Stack shall have their 
    * <code>send_other</code> interception points called. 
    * <p>
    * If the <code>completion_status</code> of the exception is not 
    * <code>COMPLETED_NO</code>, then it is inappropriate for this 
    * interception point to throw a <code>ForwardRequest</code> exception. 
    * The request's at-most-once semantics would be lost. 
    * <p>
    * Compliant Interceptors shall properly follow 
    * <code>completion_status</code> semantics if they throw a system 
    * exception from this interception point. If the original exception 
    * is a system exception, the <code>completion_status</code> of the new 
    * exception shall be the same as on the original. If the original 
    * exception is a user exception, then the <code>completion_status</code> 
    * of the new exception shall be <code>COMPLETED_YES</code>.
    * 
    * @param ri Information about the current request being intercepted.
    * @exception ForwardRequest If thrown, indicates to the ORB that a
    *     retry of the request should occur with the new object given in
    *     the exception.
    */
    public void send_exception( ServerRequestInfo ri) throws ForwardRequest
    {
    }

   /**
    * Allows an Interceptor to query the information available when a 
    * request results in something other than a normal reply or an 
    * exception. For example, a request could result in a retry 
    * (e.g., a GIOP Reply with a <code>LOCATION_FORWARD</code> status was 
    * received). This interception point shall execute in the same thread 
    * as the target invocation. 
    * <p>
    * This interception point may throw a system exception. If it does, 
    * no other Interceptors' <code>send_other</code> operations are called. 
    * The remaining Interceptors in the Flow Stack shall have their 
    * <code>send_exception</code> interception points called. 
    * <p>
    * This interception point may also throw a <code>ForwardRequest</code> 
    * exception.  If an Interceptor throws this exception, successive 
    * Interceptors' <code>send_other</code> operations are called with 
    * the new information provided by the <code>ForwardRequest</code> 
    * exception. 
    * <p>
    * Compliant Interceptors shall properly follow 
    * <code>completion_status</code> semantics if they throw a system 
    * exception from this interception point. The 
    * <code>completion_status</code> shall be <code>COMPLETED_NO</code>.
    * 
    * @param ri Information about the current request being intercepted.
    * @exception ForwardRequest If thrown, indicates to the ORB that a
    *     retry of the request should occur with the new object given in
    *     the exception.
    */
    public void send_other( ServerRequestInfo ri ) throws ForwardRequest
    {
    }

    //===========================================================================
    // InterceptorOperations
    //===========================================================================

   /**
    *  Returns the name of the interceptor.
    *  <p>
    *  Each Interceptor may have a name that may be used administratively 
    *  to order the lists of Interceptors. Only one Interceptor of a given 
    *  name can be registered with the ORB for each Interceptor type. An 
    *  Interceptor may be anonymous, i.e., have an empty string as the name 
    *  attribute. Any number of anonymous Interceptors may be registered with 
    *  the ORB.
    * 
    *  @return the name of the interceptor.
    */
    public String name()
    {
        return getClass().getName();
    }

   /**
    *  Provides an opportunity to destroy this interceptor.
    *  The destroy method is called during <code>ORB.destroy</code>. When an 
    *  application calls <code>ORB.destroy</code>, the ORB:
    *  <ol>
    *    <li>waits for all requests in progress to complete</li>
    *    <li>calls the <code>Interceptor.destroy</code> operation for each 
    *        interceptor</li>
    *    <li>completes destruction of the ORB</li>
    *  </ol>
    *  Method invocations from within <code>Interceptor.destroy</code> on 
    *  object references for objects implemented on the ORB being destroyed 
    *  result in undefined behavior. However, method invocations on objects 
    *  implemented on an ORB other than the one being destroyed are 
    *  permitted. (This means that the ORB being destroyed is still capable 
    *  of acting as a client, but not as a server.) 
    */
    public void destroy ()
    {
        if( trace ) System.out.println("DESTROY");
    }

}
