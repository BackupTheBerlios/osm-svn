
package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 */
public class SystemMessageBase extends SystemMessage implements ValueFactory
{

    //
    // constructors
    //
   
   /**
    * Default constructor for stream internalization.
    */
    public SystemMessageBase() 
    {
    }

   /**
    * Creation of a new SystemMessageBase  based on a 
    * supplied header and body value.
    */
    public SystemMessageBase( MessageHeader header, MessageBody body ) {
	  this.header = header;
	  this.body = body;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new SystemMessageBase( ) );
    }

}

