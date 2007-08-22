// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

import org.omg.Session.Link;
import org.omg.Session.User;
import org.omg.Session.AbstractResource;
import org.omg.Session.WorkspaceHelper;

/**
 * Link valuetype held by a Workspace containing a reference to
 * User accesing the workspace.
 */ 
public class AccessedBy extends org.omg.Session.AccessedBy
implements ValueFactory
{

    //========================================================================
    // constructors
    //========================================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public AccessedBy () 
    {
    }

   /**
    * Creation of a new AccessedBy link based on a supplied User.
    */
    public AccessedBy( User resource ) 
    {
	  super.resource_state = resource;
    }

    //========================================================================
    // Link
    //========================================================================

   /**
    * The resource operation returns the <code>User</code> that 
    * is authorized to access the Workspace holding this link.
    * @return  AbstractResource representing the user holding the right of access.
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
            return new Accesses( WorkspaceHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 


    //========================================================================
    // ValueFactory
    //========================================================================

   /**
    * AccessedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new AccessedBy() );
    }
}
