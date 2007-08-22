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
import org.omg.Session.Link;

/**
* The Produces link is a link exposed by an Task.  The Produces link 
* contains a reference to the AbstractResource that the Task is 
* producing.
*/
public class Produces extends org.omg.Session.Produces
implements ValueFactory
{

    //=================================================================
    // constructors
    //=================================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Produces () 
    {
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * default tag value.
    */
    public Produces( AbstractResource resource ) 
    {
	  this( resource, "" );
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * tag value.
    */
    public Produces( AbstractResource resource, String tag ) 
    {
	  super.resource_state = resource;
	  super.tag = tag;
    }

    //=================================================================
    // Tagged
    //=================================================================

   /**
    * The tag method returns the string value the defines the role of a resource
    * within the scope of a usage relationship.
    * @return  String tagged usage role
    */
    public String tag()
    {
        return this.tag;
    }

    //=================================================================
    // Link
    //=================================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * is produced by the <code>Task</code> holding this Link instance.
    * @return  AbstractResource produced by the Task.
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
            return new ProducedBy( TaskHelper.narrow( resource ) );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //=================================================================
    // ValueFactory
    //=================================================================

   /**
    * Produces factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Produces() );
    }
}
