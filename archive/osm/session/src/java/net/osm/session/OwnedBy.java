// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.TaskHelper;
import org.omg.Session.User;
import org.omg.Session.Link;

/**
 * Link valuetype held by a Task containing a reference to
 * User that owns the task.
 */ 
public class OwnedBy extends org.omg.Session.OwnedBy
implements ValueFactory
{
            
    //==========================================
    // constructor
    //==========================================

   /**
    * Default constructor for stream internalization.
    */
    public OwnedBy () 
    {
    }

   /**
    * Creation of a new OwnedBy link based on a supplied User.
    */
    public OwnedBy( User resource ) 
    {
	  super.resource_state = resource;
    }

    //==========================================
    // OwnedBy
    //==========================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * can be narrowed to a <code>User</code> that is the owner of the 
    * <code>Task</code> holding this link.
    * @return  AbstractResource representing the owner of the Task.
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
            return new Owns( TaskHelper.narrow( resource ) );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //==========================================
    // ValueFactory
    //==========================================

   /**
    * OwnedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new OwnedBy() );
    }

}
