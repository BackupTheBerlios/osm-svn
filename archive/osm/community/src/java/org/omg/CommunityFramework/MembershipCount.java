// Mon Dec 18 13:17:32 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


/**
 * The MembershipCount valuetype  contains two values, the number of 
 * Member instances associated to the Membership, and the number of 
 * Member instances referencing connected Users at the time of 
 * invocation.
 * 
 * @see org.omg.Session.User#connectstate
 *
 */

public class MembershipCount
implements StreamableValue, ValueFactory
{

    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/MembershipCount:1.0"
    };

    //==========================================================
    // state
    //==========================================================

   /**
    * The number of users recognized by the Membership.
    */
    public int _static;

   /**
    * The number of recognized members that are associated uder a User instance 
    * under a connected (as opposed to disconnected) state.
    * 
    * @see org.omg.Session.User#connectstate
    *
    */
    public int active;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public MembershipCount()
    {
    }

   /**
    * Creation of a new MembershipCount instance based on a supplied Membership.
    * @param n the number of members
    * @param m ther number of members that are connected
    */
    public MembershipCount( int n, int m )
    {
	  if( m > n ) throw new RuntimeException(
		"Number of connected members cannot exceed the number of members.");
	  this._static = n;
        this.active = m;
    }

    //==========================================================
    // MembershipCount
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CommunityFramework.MembershipCountHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(org.omg.CORBA.portable.InputStream is)
    {
        _static = is.read_long();
        active = is.read_long();
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        os.write_long(_static);
        os.write_long(active);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * MembershipCount factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new MembershipCount() );
    }

}
