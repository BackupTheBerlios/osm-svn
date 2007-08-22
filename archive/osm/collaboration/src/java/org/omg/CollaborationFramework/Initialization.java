// Thu Nov 23 07:22:01 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Initialization is a type of Transitional that declares the potential for establishment of the
 * active_state as the State instance containing a Trigger that contains an Action that contains an
 * Initialization. The containing State corresponds to the initalization target. The Trigger containing
 * the Initialization may declare a priority value. The value of priority is considered in the event of
 * implicit initialization arising from client invocation of the Processor start operation. When invoking
 * start, the Initialization with the highest priority and non-conflicting constraints set is inferred.
 * Alternatively, a CollaborationProcessor may be explicitly initialized by referencing the
 * Initialization’s containing Action label under the apply operations.
 */

public class Initialization
implements Transitional, StreamableValue, ValueFactory
{
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public Initialization() {}

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return InitializationHelper.type();
    }
    
    public void _read(InputStream _is)
    {
    }
    
    public void _write(OutputStream _os)
    {
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Initialization:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Initialization() );
    }

}
