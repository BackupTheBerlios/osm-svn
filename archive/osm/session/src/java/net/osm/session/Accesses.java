// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

import org.omg.Session.Link;
import org.omg.Session.Access;
import org.omg.Session.AccessesHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.UserHelper;

/**
 * Link valuetype held by a User containing a reference to
 * Workspace that the user has access to.
 */ 
public class Accesses extends org.omg.Session.Accesses
implements ValueFactory
{

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The Workspace that a User Accesses.
    */
    public Workspace resource_state;
    
    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public Accesses() 
    {
    }

   /**
    * Creation of a new Accesses link based on a supplied Workspace.
    */
    public Accesses( Workspace resource ) {
	  this.resource_state = resource;
    }

    //==========================================================
    // Accesses
    //==========================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that is
    * admistered by the <code>User</code> holding this link.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

   /**
    * Factory operation through which the inverse link can be created.
    * @param resource the <code>AbstractResource</code> to bind as the 
    * source of the inverse relationship.
    */
    public Link inverse( final AbstractResource resource )
    {
        try
        {
            return new AccessedBy( UserHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

   /**
    * Accesses factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Accesses() );
    }

}
