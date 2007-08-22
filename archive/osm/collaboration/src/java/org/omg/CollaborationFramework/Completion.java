
package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
Completion is a valuetype contained within StateDescriptor. When a processor completes
(signalled by the establishment of the closed processor state), the completion field contains a
Completion instance that qualifies the closed state as either a logical business level success or
failure. For example, a processor supporting vote aggregation can declare a distinction between
a successful and unsuccessful result towards a client. In this example, failure could arise as a
result of an insufficient number of affirmative votes, or through failure of the group to establish
quorum. In both cases, the failure is a business level failure and should not be confused with
technical or transaction failure. An implementation dependant identifier may be attributed to a
Completion instance to further classify a success or fail result. Prior to a processor reaching a
closed state the completion field shall return a null value.
*/

public class Completion
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // state
    //==========================================================
    
   /**
    * An implementation specific identifier of a completion state.
    */
    public ResultClass result;

   /**
    * A boolean value indicating a business level notion of success or failure of a process.
    */
    public ResultID code;
    
    //==========================================================
    // constructors
    //==========================================================
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public Completion() {}

   /**
    * Creates a Completion based on a supplied Configuration.
    */
    public Completion( Configuration config ) 
    {
        try
	  {
		result = new ResultClass( config.getAttributeAsBoolean("class"));
            code = new ResultID( config.getAttributeAsInteger("code",0) );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException("unable to create new Completion", e );
	  }
    }

    //==========================================================
    // implementation
    //==========================================================
    
    public TypeCode _type()
    {
        return CompletionHelper.type();
    }
    
    public void _read(InputStream is)
    {
        result = ResultClassHelper.read(is);
        code = ResultIDHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        ResultClassHelper.write(os, result);
        ResultIDHelper.write(os, code);
    }
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Completion:1.0"
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Completion() );
    }

}
