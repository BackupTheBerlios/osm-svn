/**
 */

package net.osm.realm;

import java.math.BigInteger;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import net.osm.util.ExceptionHelper;
import net.osm.realm.StandardPrincipal;

/**
 * Manages access to the the current security credentials in effect at the 
 * time of a client invocation.  A client application invokes the <code>setLocalPrincipal</code>
 * method on establishment or change of the acting principal.  When the client 
 * makes an invocation on a server, a client interceptor invokes the 
 * <code>getLocalPrincipal</code> and places the encoded form of the principal
 * into a service context.  A server interceptor is activated when the rquest arrives
 * at the server and the service context is unpacked, internalized into a native 
 * certificate path and placed into a <code>PrincipalContext</code> instance held by
 * the PI Current.  The request is forwarded to the application.  The application can 
 * use the resolve_initial_references operation to access this object and invoke the 
 * <code>getPrincipal</code> operation.  The <code>getPrincipal</code> operation 
 * returns a new <code>StandardPrincipal</code instance that can be used by the 
 * application to perform authentication and access decisions.
 */
public class PrincipalManagerBase extends LocalObject implements PrincipalManager
{

    private int slot;

    private ORBInitInfo info;

    private StandardPrincipal principal;

   /**
    * Creates a new <code>PrincipalManagerBase</code> iniilized with 
    * with a slot and ORB initialization information.
    */
    public PrincipalManagerBase( ORBInitInfo info, int slot )
    {
        this.info = info;
	  this.slot = slot;
    }

   /**
    * Set the standard principal.  This method is invoked by the 
    * client to establish the invoking principal.  The  
    * client interceptor reads the path when establishing the 
    * service context.
    *
    * @param principal the client principal 
    */
    public void setLocalPrincipal( StandardPrincipal principal )
    {
	  this.principal = principal;
    }

   /**
    * Return the principal.
    * @return StandardPrincipal the current principal
    */
    public StandardPrincipal getLocalPrincipal( )
    {
	  return principal;
    }

   /**
    * Get the <code>StandardPrincipal</code.  This operation is invoked by the server. The 
    * implementation extracts the encoded principal from the PrincipalContext
    * instance established by the server interceptor under the PICurrent.
    *
    * @exception RealmRuntimeException if the Portable Interceptor Current is not found.
    * @return StandardPrincipal the principal identitying the invoking client
    */
    public StandardPrincipal getPrincipal( ) throws RealmRuntimeException
    {
	  String error = null;
	  Current current = null;
	  Any any = null;

	  try
	  {
	      current = getPICurrent();
	  }
	  catch( InvalidName e )
	  {
	  	throw new RuntimeException( 
		      "PrincipalManagerBase, Unable to resolve the PICurrent", e );
	  }

        synchronized( current )
        {
            try
	      {
	          any = current.get_slot( slot );
		    PrincipalContext pc = PrincipalContextHelper.extract( any );
		    principal = new StandardPrincipalBase( pc );
		    return principal;
	      }
	      catch( InvalidSlot e )
	      {
		    throw new RealmRuntimeException( 
		      "PrincipalManagerBase, Unable to resolve an allocated slot in the PICurrent", e );
		}
	      catch( org.omg.CORBA.MARSHAL e )
	      {
		    if( any.type().kind() == org.omg.CORBA.TCKind.tk_null )
		    {
		        throw new MissingPrincipalException();
		    }
		    else if( any.type() != PrincipalContextHelper.type() )
		    {
		        throw new RealmRuntimeException( 
		          "PrincipalManagerBase, PICurrent slot does not contain a PrincipalContext.", e );
		    }
		    else
		    {
		        throw new RuntimeException( 
		  	    "PrincipalManagerBase, Unexpected marshalling exception while resolving principal.", e );
		    }
            }
	      catch( Throwable e )
            {
		    throw new RealmRuntimeException( 
		       "PrincipalManagerBase, Unexpected exception while resolving principal.", e );
            }
        }
    }

    private Current getPICurrent() throws InvalidName
    {
        return CurrentHelper.narrow( info.resolve_initial_references("PICurrent"));
    }
}
