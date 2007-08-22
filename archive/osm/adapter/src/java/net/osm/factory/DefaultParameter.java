
package net.osm.factory;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 * Valuetype implementation of a system message.
 */
public class DefaultParameter extends Parameter 
implements ValueFactory
{

    //=============================================================
    // constructors
    //=============================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public DefaultParameter() 
    {
    }

   /**
    * Creation of a new DefaultParameter  based on a 
    * supplied header and body value.
    */
    public DefaultParameter( String key, String name, String description, boolean required, Any type ) 
    {
	  super.m_key = key;
	  super.m_name = name;
	  super.m_description = description;
	  super.m_required = required;
	  super.m_type = type;
    }

   /**
    * Return the parameter key.
    * @return String the key
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Return the parameter name.
    * @return String the name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the parameter description.
    * @return String the parameter description.
    */
    public String getDescription()
    {
        return m_description;
    }


   /**
    * Return the required parameter flag.
    * @return boolean TRUE if this parameter is required else FALSE
    */
    public boolean getRequired()
    {
        return m_required;
    }

   /**
    * Return the parameter type.
    * @return Any type holder.
    */
    public Any getType()
    {
        return m_type;
    }

    //=============================================================
    // ValueFactory
    //=============================================================
    
    public Serializable read_value( InputStream is ) 
    {
        return is.read_value( new DefaultParameter( ) );
    }

}

