

package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.lang.Long;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;

/**
* Duration is a utility valuetype that contains a possibly null UTC time interval.
*/

public class Duration
implements StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    /**
    * UTC time interval.
    */
    public long value;
    
    //
    // constructors
    //
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public Duration(){}

    /**
    * Creation of a time interval value based on a supplied string where 
    * the string represents a millisecond interval.
    * @param period long milliseconds
    */
    public Duration ( long period ) 
    {
        this.value = period;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return DurationHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        value = _is.read_ulonglong();
    }
    
    public void _write(OutputStream _os)
    {
        _os.write_ulonglong(value);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Duration:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Duration() );
    }

}
