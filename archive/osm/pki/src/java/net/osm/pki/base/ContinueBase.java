package net.osm.pki.base;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.Continue;
import org.omg.PKI.ContinueHelper;
import org.omg.PKI.EncodedData;

/**
 * Valuetype used to represent a continuation statement.
 */
public class ContinueBase extends Continue
implements ValueFactory
{
   /** 
    *  Default null constructor
    */ 
    public ContinueBase(){}

   /**
    * ContinueBase constructor using the supplied initialization values.
    * <p>
    * @param continue_type = the type of continue message
    * @param data = encoded Continue datastructure
    */
    public ContinueBase ( int continue_type, EncodedData data )
    {
        this.continue_type = continue_type;
        this.data = data;
    }

    //===========================================================
    // Continue implementation
    //===========================================================

    public int getContinueType()
    {
	  return continue_type;
    }

    public int getEncodingType()
    {
        return data.encoding_type;
    }

    public byte[] getEncoded()
    {
        return data.data;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "Continue:" + " type: " + continue_type
		+ ", data: " + data;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new ContinueBase( ) );
    }
}

