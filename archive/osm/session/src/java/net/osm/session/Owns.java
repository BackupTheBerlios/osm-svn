// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource ;
import org.omg.Session.UserHelper;
import org.omg.Session.Link;
import org.omg.Session.Task ;


/**
 * Link valuetype held by a User containing a reference to
 * Task that is owned by the user.
 */ 
public class Owns extends org.omg.Session.Owns
implements ValueFactory
{
    
    //===============================================================
    // constructors
    //===============================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Owns () 
    {
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * default tag value.
    */
    public Owns( Task resource ) 
    {
	  super.resource_state = resource;
    }

    //===============================================================
    // Link
    //===============================================================

   /**
    * The resource operation returns the <code>Task</code> that 
    * is owned by the <code>User</code> holding this Link instance.
    * @return  AbstractResource narrowable to Task that is owned by the user.
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
            return new OwnedBy( UserHelper.narrow( resource ) );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    
    //===============================================================
    // ValueFactory
    //===============================================================

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Owns() );
    }

}
