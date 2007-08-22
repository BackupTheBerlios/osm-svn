
package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.Session.BaseBusinessObject;
import org.omg.Session.BaseBusinessObjectKeyHelper;

/**
 * Declaration of an identity key valuetype used to optimise the collection 
 * of identity infomation with a single invocation.  
 * This type is a non-standard OSM extension.
 */
public class BaseBusinessObjectKey 
implements StreamableValue, ValueFactory
{

    	private AuthorityId id;

	/**
	 * RegistrationAuthority value that qualifies the domain address
	 * type as one of DNS, ISO, IDL, DCE or OTHER.
	 */
	public int authority;

	/**
	 * The address of the domain relative to the authority class.
	 */
	public java.lang.String address;

	/**
	 * Random identifier corresponding to the value returned from
	 * the IdentifiableObject constant_random_id operation.
	 */
	public int random;

	/**
	 * Value returned from the BaseBusienssObject creation operation.
	 */
	public long creation;

    //
    // constructors
    //
   
   /**
    * Default constructor for stream internalization.
    */
    public BaseBusinessObjectKey(){}

   /**
    * Creation of a new BaseBusinessObjectKey link based on a 
    * supplied BaseBusinessObject reference.
    */
    public BaseBusinessObjectKey( BaseBusinessObject object ) {
	  try
	  {
		id = object.domain();
	      this.random = object.constant_random_id();
	      this.authority = id.authority.value();
	      this.address = id.naming_entity;
	      this.creation = object.creation().time;
	  } catch(Exception e)
	  {
		throw new RuntimeException("failed to resolve business object identity");
	  }
    }

   /**
    * Creation of a new BaseBusinessObjectKeyImpl link based on
    * supplied values.
    */

    public BaseBusinessObjectKey( AuthorityId id, int random, long creation ) {
	  this.id = id;
	  this.authority = id.authority.value();
	  this.address = id.naming_entity;
	  this.random = random;
	  this.creation = creation;
    }

    //
    // implementation of BaseBusinessObjectKey operations
    //

   /**
    * Returns true if the supplied key is equal to this key. Equality 
    * is defined as having equal values for all state members.
    */

    public boolean equal( BaseBusinessObjectKey key )
    {
	  try
	  {
	      if( this.authority != key.authority ) return false;
	      if( !this.address.equals( key.address )) return false;
	      if( this.random != key.random ) return false;
	      if( this.creation != key.creation ) return false;
	      return true;
	  } catch (Exception e)
        {
		return false;
	  }
    }

   /**
    * Returns the AuthorityId value for this domain based on the 
    * authority and address values.
    */

    public AuthorityId authority_id()
    {
	  if( id == null ) id = new AuthorityId( RegistrationAuthority.from_int( this.authority ), this.address );
	  return id;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new BaseBusinessObjectKey() );
    }

	//
	// Return the truncatable ids
	//
	static final String[] _ids_list =
	{
		"IDL:omg.org/Session/BaseBusinessObjectKey:1.0"
	};

	public String [] _truncatable_ids()
	{
		return _ids_list;
	}

	//
	// Unmarshal the value into an InputStream
	//
	public void _read( org.omg.CORBA.portable.InputStream is )
	{
		authority = is.read_long();
		address = is.read_string();
		random = is.read_ulong();
		creation = is.read_ulonglong();
	}

	//
	// Marshal the value into an OutputStream
	//
	public void _write( org.omg.CORBA.portable.OutputStream os )
	{
		os.write_long(authority);
		os.write_string(address);
		os.write_ulong(random);
		os.write_ulonglong(creation);
	}

	//
	// Return the value TypeCode
	//
	public org.omg.CORBA.TypeCode _type()
	{
		return org.omg.Session.BaseBusinessObjectKeyHelper.type();
	}

}

