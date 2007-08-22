// Tue Dec 19 00:44:43 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;

/**
Controls is a link held by an implementation of Master that references zero to many Slave
instances.
*/

public class Controls
    implements Management, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //

   /**
    * A reference to an AbstractResource implementing the Slave interface.
    * An implementation of Master may hold 0..* Controls link instances,
    * representing the strong aggregation relationship from a Master to
    * subsidiary Slaves.
    */
    
    public Slave resource_state;
    

    /**
    * Null argument constructor used during stream internalization.
    */

    public Controls( ) 
    {
    }

    /**
    * Creation of a new ControlledBy link based on the supplied Master.
    */

    public Controls( Slave slave ) 
    {
	  this.resource_state = slave;
    }

    //
    // implementation of Link
    //

    /**
     * The resource operation returns the <code>AbstractResource</code> reference 
     * corresponding to a concrete resource supporting the Slave interface.
     * @return AbstractResource corresponding to a concrete Slave.
     */   

    public AbstractResource resource( ) {
        return (AbstractResource) resource_state;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return ControlsHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        resource_state = SlaveHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        SlaveHelper.write(_os, resource_state);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Controls:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Controls() );
    }

}
