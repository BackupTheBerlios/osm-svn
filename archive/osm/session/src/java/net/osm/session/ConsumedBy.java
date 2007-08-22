// Sun Dec 17 16:02:38 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.Workspace;
import org.omg.Session.AbstractResource;
import org.omg.Session.Task;
import org.omg.Session.Link;

/**
 * Link valuetype held by a resource containing a reference to
 * Task that is consuming the resource.
 */ 
public class ConsumedBy extends org.omg.Session.ConsumedBy
implements ValueFactory
{
    
    //==================================================================
    // constructors
    //==================================================================

   /**
    * Construct a new non-initialized <code>ConsumedBy</code> link.  This
    * constructor is used during internalization of a new <code>ConsumedBy</code> link
    * from a serialized valuetype stream.
    */
    public ConsumedBy () {
    }

   /**
    * Construct a new <code>ConsumedBy</code> link based on a supplied 
    * <code>Task</code>.
    *
    * @param resource the <code>Task</code> that is consuming the 
    * <code>AbstractResource</code> holding this link.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */
    public ConsumedBy ( Task resource ) {
	  this( resource, "" );
    }

   /**
    * Construct a new <code>ConsumedBy</code> link based on a supplied 
    * <code>Task</code> and role name.
    * @param resource the <code>Task</code> that is consuming the 
    * <code>AbstractResource</code> holding this link.
    * @param tag a <code>String</code> declaring the role of the <code>Task</code> 
    * in this association.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */
    public ConsumedBy ( Task resource, String tag ) {

	  if (resource == null) throw new IllegalArgumentException(
          "Null Task reference supplied to ComsumedBy constructor.");

        super.resource_state = resource;
	  if( tag == null ) 
        {
		super.tag = "";
	  }
        else
        {
	      super.tag = tag;
	  }
    }

    //==================================================================
    // Tagged
    //==================================================================
    
   /**
    * The tag method returns the string value that defines the role of a Task
    * within the scope of a consumption relationship.
    * @return String tagged consumption role
    */
    public String tag() 
    {
	  return super.tag;
    }

    //==================================================================
    // Link
    //==================================================================
    
    /**
     * The resource operation returns the <code>Task</code> that 
     * a <code>AbstractResource</code> holding this link is consumed by.
     * @return <code>AbstractResource</code> that can be narrowed to a <code>Task</code> that 
     * is consuming the <code>AbstractResource</code> holding this link.
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
            return new Consumes( resource );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 


    //==================================================================
    // implementation of ValueFactory
    //==================================================================
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ConsumedBy() );
    }

}
