
package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

import org.omg.Session.Access;
import org.omg.Session.AccessesHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.Link;
import org.omg.Session.Task;

//import net.osm.session.task.Task;

/**
 * Link held by a processor referencing the Task it executes.
 */
public class DefaultExecutes extends net.osm.session.Executes
implements ValueFactory
{

    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public DefaultExecutes() 
    {
    }

   /**
    * Creation of a new Executes link based on a supplied Task.
    */
    public DefaultExecutes( Task task ) {
	  super.resource_state = task;
    }

    //==========================================================
    // Executes
    //==========================================================

   /**
    * The resource operation returns the <code>Task</code> that is
    * executed by the <code>AbstractResource</code> holding this link.
    * @return AbstractResource a <code>Task</code> the is processed 
    * by the <code>AbstractResource</code> holding this link.
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
            return new DefaultExecutedBy( resource );
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
        return is.read_value( new DefaultExecutes() );
    }

}
