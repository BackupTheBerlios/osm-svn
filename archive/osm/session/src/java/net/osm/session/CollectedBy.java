// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.Workspace;
import org.omg.Session.AbstractResource;
import org.omg.Session.Link;

/**
 * Link valuetype held by a resource containing a reference to
 * containineg Workspace.
 */ 
public class CollectedBy extends org.omg.Session.CollectedBy
implements ValueFactory
{
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public CollectedBy () 
    {
    }

   /**
    * Creation of a new Collects link based on a supplied Workspace.
    */

    public CollectedBy( Workspace resource )
    {
	  super.resource_state = resource;
    }

    //==========================================================
    // CollectedBy
    //==========================================================

   /**
    * The resource operation returns the <code>Workspace</code> 
    * that collects the resource holding this link.
    * @return  AbstractResource
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
            return new Collects( resource );
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
    * CollectedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CollectedBy() );
    }
}
