// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.UserHelper;
import org.omg.Session.Link;

/**
 * Link valuetype held by a User containing a reference to
 * resource that the user administers.
 */ 
public class Administers extends org.omg.Session.Administers
implements ValueFactory
{
 
    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public Administers() 
    {
    }

   /**
    * Creation of a new Administers link based on a supplied Workspace.
    */
    public Administers( Workspace resource ) 
    {
	  super.resource_state = resource;
    }

    //========================================================================
    // Link
    //========================================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * is adminstered by the <code>User</code> holding this link.
    * @return  AbstractResource representing the user holding the right of adminstration.
    */
    public AbstractResource resource()
    {
	  return super.resource_state;
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
            return new AdministeredBy( UserHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //==========================================================
    // ValueFactory
    //==========================================================
        
   /**
    * Administers factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Administers() );
    }

}
