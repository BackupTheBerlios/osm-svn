// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.Link;

import net.osm.collaboration.CollaborationRuntimeException;

/**
ControlledBy is a link held by a Slave implementation that references zero to one
Master instances. The relationship from master to slave is one of strong aggregation – removal of
the Master implies removal of all Slaves. Using the control relationship, it is possible for a Processor
to expose a sub-process hierarchy that can be navigated by a client.
*/

public class ControlledBy
    implements Management, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    /**
    * A reference to an AbstractResource implementing the Master interface.
    * An implementation of Master may hold 0..1 ControlledBy link instances
    * representing the parent processor.
    */

    public Master resource_state;
    

    /**
    * Null argument constructor used during stream internalization.
    */

    public ControlledBy( ) 
    {
    }

    /**
    * Creation of a new ControlledBy link based on the supplied Master.
    */

    public ControlledBy( Master master ) 
    {
	  this.resource_state = master;
    }

    //
    // implementation of Link
    //

    /**
     * The resource operation returns the <code>AbstractResource</code> reference 
     * corresponding to a concrete Task reference.
     * @return AbstractResource corresponding to a concrete Task reference.
     */   

    public AbstractResource resource( ) {
        return (AbstractResource) resource_state;
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
            return new Controls( SlaveHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new CollaborationRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return ControlledByHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        resource_state = MasterHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        MasterHelper.write(_os, resource_state);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/ControlledBy:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ControlledBy() );
    }

}
