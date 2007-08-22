// Sun Dec 17 15:21:20 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.Link;
import org.omg.Session.TaskHelper;

/**
* Consumes is a Link held by a Task that references an AbstractResource it is 
* consuming.  The inverse of this association is the Link ConsumedBy, held by 
* the consumed AbstractResource, referencing the Task that is consuming it. 
*/

public class Consumes extends org.omg.Session.Consumes
implements ValueFactory
{
    
    //========================================================================
    // Constructors
    //========================================================================

   /**
    * Construct a new non-initialized <code>Consumes</code> link.  This
    * constructor is used during internalization of a new <code>Consumes</code> link
    * from a serialized valuetype stream.
    */
    public Consumes () 
    {
    }

   /**
    * Construct a new <code>Consumes</code> link based on a supplied 
    * <code>AbstractResource</code> and default usage role.
    * @param resource the <code>AbstractResource</code> that is consumed by the 
    * <code>Task</code> holding this link.
    * @exception <code>NullPointerException</code> 
    * if resource is <code>null</code>.
    */
    public Consumes ( AbstractResource resource ) 
    {
        if( resource == null ) throw new NullPointerException(
          "Null resource argument supplied to Consumes constructor.");
	  super.resource_state = resource;
	  super.tag = "";
    }

    
   /**
    * Construct a new <code>Consumes</code> link based on a supplied 
    * <code>AbstractResource</code> and role name.
    * @param resource the <code>AbstractResource</code> that is consumed by the 
    * <code>Task</code> holding this link.
    * @param tag a <code>String</code> declaring the role of the role of the 
    * resource in this association.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */

    public Consumes ( AbstractResource resource, String tag ) {

        if( resource == null ) throw new NullPointerException(
          "Null resource argument supplied to Consumes constructor.");
	  super.resource_state = resource;

	  if(tag == null) 
        {
		super.tag = "";
	  }
        else 
        {
	      super.tag = tag;
	  }
    }

    //========================================================================
    // Tagged
    //========================================================================
    
   /**
    * The tag method returns the string value that defines the role of a resource
    * within the scope of a consumption relationship.
    * @return String tagged consumption role
    */

    public String tag() 
    {
	  return super.tag;
    }

    //========================================================================
    // Link
    //========================================================================
    
    /**
     * The resource operation returns the <code>AbstractResource</code> that 
     * a <code>Task</code> is consuming.
     * @return AbstractResource that this link declares a consumption dependency on.
     */
    public AbstractResource resource() {
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
            return new ConsumedBy( TaskHelper.narrow( resource ) );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 


    //========================================================================
    // ValueBase
    //========================================================================
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) 
    {
        return is.read_value( new Consumes() );
    }

}
