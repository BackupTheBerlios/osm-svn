// Tue Dec 19 00:44:42 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Control;
import org.omg.Session.TimestampHelper;
import org.omg.TimeBase.UtcT;


public class Timeout
implements StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    public String identifier;
    
    public org.omg.TimeBase.UtcT timestamp;
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public Timeout(){}

   /**
    * Creation of a new Timeout instance based on a supplied identifier and 
    * timout UTC value.
    * @param id timeout identifier
    * @param time UtcT time interval
    */
    public Timeout( String id, UtcT time ) 
    {
        identifier = id;
        timestamp = time;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return TimeoutHelper.type();
    }
    
    public void _read(InputStream is)
    {
        identifier = LabelHelper.read(is);
        timestamp = TimestampHelper.read(is);
    }
    
    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        LabelHelper.write(os, identifier);
        TimestampHelper.write(os, timestamp);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Timeout:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Timeout() );
    }

}
