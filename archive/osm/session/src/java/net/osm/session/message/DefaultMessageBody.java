
package net.osm.session.message;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 * Valuetype implementation for a typed message body.
 */
public class DefaultMessageBody extends MessageBody 
implements ValueFactory
{

    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public DefaultMessageBody() 
    {
    }

   /**
    * Creation of a new SystemMessageBase based on a 
    * type identifier and message content.
    */
    public DefaultMessageBody( String type, String message ) 
    {
	  super.type = type;
	  super.message = message;
    }

    //==========================================================
    // ValueFactory
    //==========================================================
    
    public Serializable read_value( InputStream is ) 
    {
        return is.read_value( new DefaultMessageBody( ) );
    }

}

