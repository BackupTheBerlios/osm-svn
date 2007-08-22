// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CollaborationFramework.Guard;
import org.omg.CollaborationFramework.DurationHelper;
import org.omg.CollaborationFramework.ClockHelper;

/**
A Clock, representing a timeout condition that is automatically armed by a
Collaboration implementation whenever the containing trigger is a candidate (within the active
state path). A Trigger containing a Clock is managed by a Collaboration implementation.
*/

public class Clock
implements Guard, StreamableValue, ValueFactory
{

    //
    //  static
    //
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list =
    {
	"IDL:omg.org/CollaborationFramework/Clock:1.0",
    };

    //
    //  state members
    //

   /**
    * Declaration of the delay between establishment of the containing trigger
    * as a candidate (the moment the Trigger’s containing state enters the
    * active state path) and the automatic invocation of the action contained by
    * the containing Trigger by a Collaboration implementation.
    */
    public Duration timeout;
    

   /**
    * Null argument constructor used during stream internalization.
    */
    public Clock( ){}

   /**
    * Configuration of a clock based on a supplied Configuration instance.
    */
    public Clock( Configuration conf )
    {
	  try
	  {
            timeout = new Duration( conf.getAttributeAsLong( "timeout", 0 ) );
	  }
	  catch( Exception e )
	  {
	      throw new RuntimeException("unable to create new configured Clock", e );
        }
    }

    //
    // implementation of Streamable
    //

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ClockHelper.type();
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _read(InputStream _is)
    {
        timeout = DurationHelper.read(_is);
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _write(OutputStream _os)
    {
        DurationHelper.write(_os, timeout);
    }
    
    //
    // implementation of ValueBase
    //
        
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
   /**
    * Creation of a new instance from a input stream.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Clock() );
    }

}
