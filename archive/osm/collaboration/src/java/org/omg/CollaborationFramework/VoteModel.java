
package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class VoteModel
extends ProcessorModel
{
    
    //
    //  state members
    //
    
    public VoteCeiling ceiling;
    public VotePolicy policy;
    public boolean single;
    public Duration lifetime;
    
    //
    // constructor
    //
    
    public VoteModel( ){}

    public VoteModel( Configuration config )
    {
	  super( config );
        try
	  {
		single = config.getAttributeAsBoolean("single", true );
		Short numerator = new Short( config.getAttribute("numerator","1"));
		Short denominator = new Short( config.getAttribute("denominator","2"));
		ceiling = new VoteCeiling( numerator.shortValue(), denominator.shortValue() );

		String policyString = config.getAttribute("policy", "AFFERMATIVE" );
		if( policyString.equals("AFFERMATIVE") )
		{
		    policy = VotePolicy.AFFIRMATIVE_MAJORITY;
		}
	      else
	      {
		    policy = VotePolicy.NON_ABSTAINING_MAJORITY;
	      }

		lifetime = new Duration( config.getAttributeAsLong("lifetime", (20*60)) );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("unable to create new configured VoteModel", e );
	  }
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return VoteModelHelper.type();
    }
    
    public void _read(InputStream is)
    {
        super._read(is);
        ceiling = VoteCeilingHelper.read(is);
        policy = VotePolicyHelper.read(is);
        single = is.read_boolean();
        lifetime = DurationHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        super._write(os);
        VoteCeilingHelper.write(os, ceiling);
        VotePolicyHelper.write(os, policy);
        os.write_boolean(single);
        DurationHelper.write(os, lifetime);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/VoteModel:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new VoteModel() );
    }


}
