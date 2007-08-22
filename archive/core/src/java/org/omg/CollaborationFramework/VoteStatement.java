// Tue Dec 19 00:44:42 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class VoteStatement
implements Evidence, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    public VoteDescriptor vote;
    
    //
    //  constructor
    //

    public VoteStatement(){}

    public VoteStatement( VoteDescriptor vote )
    {
	  this.vote = vote;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return VoteStatementHelper.type();
    }
    
    public void _read(InputStream is)
    {
        vote = VoteDescriptorHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        VoteDescriptorHelper.write(os, vote);
    }
    
    //
    // implementation of ValueBase
    //
    
    private static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/VoteStatement:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new VoteStatement() );
    }

}
