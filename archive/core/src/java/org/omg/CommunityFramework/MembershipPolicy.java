// Thu Nov 23 07:22:00 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
*The MembershipPolicy valuetype is contained within the CommunityModel 
* valuetype (and other valuetypes defined under the CollaborationFramework).  
* MembershipPolicy defines privacy and exclusivity policy of the containing 
* Membership.
*/

public class MembershipPolicy
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/MembershipPolicy:1.0"
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * Qualification of the extent of information to be made available to 
    * clients.
    * @see org.omg.CommunityFramework.PrivacyPolicyValue
    */
    public PrivacyPolicyValue privacy;

   /**
    * Restricts the number of Member instances associated to a Membership to 
    * 1 for a given principal identity.
    */
    public boolean exclusive;
    
    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default constructor.
    */
    public MembershipPolicy() {}
    
   /**
    * Creation of a new membership policy based on a supplied privacy and exclusivity
    * declaration.
    */
    public MembershipPolicy (PrivacyPolicyValue privacy, boolean exclusive ) 
    {
        this.privacy = privacy;
        this.exclusive = exclusive;
    }
    
   /**
    * Creation of a membership policy based on a supplied configuration.
    */
    public MembershipPolicy( Configuration conf )
    {
	  try
	  {
	      exclusive = conf.getAttributeAsBoolean("exclusivity",true);
	      String pp = conf.getAttribute("privacy","public").toLowerCase();
	      if( pp.equals("public") ) 
            {
                privacy = PrivacyPolicyValue.PUBLIC_DISCLOSURE;
            }
            else if( pp.equals("restricted") ) 
            {
                privacy = PrivacyPolicyValue.RESTRICTED_DISCLOSURE;
            }
            else 
            {
                privacy = PrivacyPolicyValue.PRIVATE_DISCLOSURE;
            }
	  }
	  catch( Exception e )
	  {
	      throw new RuntimeException("unable to create new configured membership policy", e );
	  }
    }

    //==========================================================
    // impementation
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return MembershipPolicyHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream _is)
    {
        privacy = PrivacyPolicyValueHelper.read(_is);
        exclusive = _is.read_boolean();
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(org.omg.CORBA.portable.OutputStream _os)
    {
        PrivacyPolicyValueHelper.write(_os, privacy);
        _os.write_boolean(exclusive);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * CollectedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new MembershipPolicy() );
    }

}
