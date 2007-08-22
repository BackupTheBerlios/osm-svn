// Tue Dec 19 00:44:42 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Control;
import org.omg.TimeBase.UtcT;

/**
 * VoteCount is a valutype that contains the summation of a vote process.
 */
public class VoteCount
implements Proof, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
    public UtcT timestamp;
    
    public int yes;
    
    public int no;
    
    public int abstain;

    public VoteCount(){}

    public VoteCount( int yes, int no, int abstain, UtcT time )
    {
        this.yes = yes;
        this.no = no;
        this.abstain = abstain;
        this.timestamp = time;
    }
    
    //
    // implementation of Streamable
    //
    
    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CollaborationFramework.VoteCountHelper.type();
    }
    
    public void _read(org.omg.CORBA.portable.InputStream is)
    {
        timestamp = org.omg.Session.TimestampHelper.read(is);
        yes = is.read_long();
        no = is.read_long();
        abstain = is.read_long();
    }
    
    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        org.omg.Session.TimestampHelper.write(os, timestamp);
        os.write_long(yes);
        os.write_long(no);
        os.write_long(abstain);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/VoteCount:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new VoteCount() );
    }

}
