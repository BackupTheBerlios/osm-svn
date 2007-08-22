
package org.omg.CommunityFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
* CommunityCriteria is a valuetype derived from Criteria and is 
* used as an argument to a ResourceFactory in the creation of a new Community
* business object.
*/

public class CommunityCriteria extends Criteria
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/CommunityCriteria:1.0",
    };

    //==========================================================
    // state
    //==========================================================

    /**
    * The MembershipModel defining the policy and constraints
    * applicable to a Community business object.
    */
    public MembershipModel model;
 
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public CommunityCriteria( ){}

   /**
    * Creation of a CommunityCriteria based on a supplied name, note and membership model.
    */
    public CommunityCriteria( String label, String note, MembershipModel model ){
        super( label, note );
	  this.model = model;
    }

   /**
    * Creation of a new CommunityCriteria based on a supplied Configuration instance.
    */
    public CommunityCriteria( Configuration conf )
    {
	  super( conf );
	  try
	  {
            model = new MembershipModel( conf.getChild( "membership" ));
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("unable to create configured community criteria", e );
	  }
    }

    //==========================================================
    // CommunityCriteria
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return CommunityCriteriaHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        model = MembershipModelHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        MembershipModelHelper.write(os, model);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * CommunityCriteria factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CommunityCriteria() );
    }

}
