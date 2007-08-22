// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

import org.omg.Session.User;
import org.omg.Session.AbstractResource;
import org.omg.Session.TaskHelper;
import org.omg.Session.Link;

/**
 * Link held by a Task referencing the executing processor.
 */
public class DefaultExecutedBy extends net.osm.session.ExecutedBy
implements ValueFactory
{

    //========================================================================
    // constructors
    //========================================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public DefaultExecutedBy () 
    {
    }

   /**
    * Creation of a new ExecutedBy link based on a supplied AbstractResource.
    */
    public DefaultExecutedBy( AbstractResource resource ) 
    {
	  super.resource_state = resource;
    }

    //========================================================================
    // Link
    //========================================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * is acting as the processor to the <code>Task</code> holding this link.
    * @return  AbstractResource acting as the processor
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
            return new DefaultExecutes( TaskHelper.narrow( resource ) );
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
        return is.read_value( new DefaultExecutedBy() );
    }
}
