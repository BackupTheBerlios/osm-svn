// Tue Dec 19 00:44:42 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.TimeBase.UtcT;
import org.omg.Session.TimestampHelper;

public class VoteReceipt
implements Proof, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    public UtcT timestamp;
    
    public VoteStatement statement;

    //
    //  constructors
    //

    public VoteReceipt(){}

    public VoteReceipt( UtcT timestamp, VoteStatement statement )
    {
        this.timestamp = timestamp;
        this.statement = statement;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return VoteReceiptHelper.type();
    }
    
    public void _read(InputStream is)
    {
        timestamp = TimestampHelper.read(is);
        statement = VoteStatementHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        TimestampHelper.write(os, timestamp);
        VoteStatementHelper.write(os, statement);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/VoteReceipt:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new VoteReceipt() );
    }

}
