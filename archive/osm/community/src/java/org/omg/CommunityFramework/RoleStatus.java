// Mon Dec 18 15:40:18 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * RoleStatus is a valuetype returned from the Membership get_quorum_status
 * operation that contains a role label as an identifier, the count of 
 * members asscoaited under the identified role, and the quorum status for
 * the role.
 *
 * @see org.omg.CommunityFramework.Membership#get_quorum_status
 */

public class RoleStatus
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/RoleStatus:1.0"
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * String identifier corresponding to the label of a busienss role.
    * @see org.omg.CommunityFramework.Role
    */
    public String identifier;
 
   /**
    * The number of Users recognized by the Membership under the role
    * with a label corresponding to the identifier value.
    * @see org.omg.CommunityFramework.Role#label
    */
    public MembershipCount count;
    
   /**
    * The quorum status of a role with a label corresponding to the 
    * identifier value under a particular Membership supplying the 
    * RoleStatus value.
    * @see org.omg.CommunityFramework.Role#label
    * @see org.omg.CommunityFramework.Membership#get_quorum_status
    */
    public QuorumStatus status;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public RoleStatus()
    {
    }

   /**
    * Creation of a new RoleStatus based on a supplied Membership.
    */
    public RoleStatus( Membership membership )
    {
	  throw new UnsupportedOperationException("not implemented");
    }

    //==========================================================
    // RoleStatus
    //==========================================================

   /**
    * Return the value TypeCode
    */    
    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CommunityFramework.RoleStatusHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream _is)
    {
        identifier = LabelHelper.read(_is);
        count = MembershipCountHelper.read(_is);
        status = QuorumStatusHelper.read(_is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream _os)
    {
        LabelHelper.write(_os, identifier);
        MembershipCountHelper.write(_os, count);
        QuorumStatusHelper.write(_os, status);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * RoleStatus factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new RoleStatus() );
    }

}
