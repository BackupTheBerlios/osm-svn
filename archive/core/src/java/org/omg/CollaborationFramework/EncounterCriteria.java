
package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.MembershipModelHelper;


/**
EncounterCriteria is a Criteria valuetype used to construct a new Encounter instance using a ResourceFactory.  EncounterCriteria cointains a MembershipModel that defines the MembershipPolicy applicable to the created Encounter.
*/

public class EncounterCriteria
extends Criteria
{
    
    //
    //  state members
    //
    
    /**
    * The membership model instance to be associated to the created Encounter.
    */
    public MembershipModel model;
    
    // 
    // constructors
    //

    /**
    * Null argument constructor used during stream internalization.
    */
    public EncounterCriteria( ){}


   /**
    * Creates a EncounterCriteria based on a supplied Configuration instance.
    */
    public EncounterCriteria( Configuration config )
    {
	  super( config );
        try	
        {
      	// <!ELEMENT encounter (membership,context*) >
      	// <!ATTLIST encounter 
		//   %control;
		// >

		model = new MembershipModel( config.getChild("membership", false ) );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException(
              "unable to create new configured encounter criteria", e );
	  }
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return EncounterCriteriaHelper.type();
    }
    
    public void _read(InputStream is)
    {
        super._read(is);
        model = MembershipModelHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        super._write(os);
        MembershipModelHelper.write(os, model);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/EncounterCriteria:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new EncounterCriteria() );
    }


}
