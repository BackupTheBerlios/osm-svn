/*
 * @(#)PropertySetDefDelegate.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/04/2001
 */

package net.osm.properties;

import java.util.Vector;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CosPropertyService.PropertySetDefPOA;
import org.omg.CosPropertyService.PropertySetDef;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.ConstraintNotSupported;
import org.omg.CosPropertyService.MultipleExceptions;
import org.omg.CosPropertyService.ConflictingProperty;
import org.omg.CosPropertyService.UnsupportedTypeCode;
import org.omg.CosPropertyService.UnsupportedProperty;
import org.omg.CosPropertyService.UnsupportedMode;
import org.omg.CosPropertyService.ReadOnlyProperty;
import org.omg.CosPropertyService.InvalidPropertyName;
import org.omg.CosPropertyService.PropertyNotFound;
import org.omg.CosPropertyService.FixedProperty;
import org.omg.CosPropertyService.PropertyException;
import org.omg.CosPropertyService.ExceptionReason;
import org.omg.CosPropertyService.Property;
import org.omg.CosPropertyService.PropertyMode;
import org.omg.CosPropertyService.PropertyModeType;
import org.omg.CosPropertyService.PropertyModesHolder;
import org.omg.CosPropertyService.PropertyTypesHolder;
import org.omg.CosPropertyService.PropertyNamesHolder;
import org.omg.CosPropertyService.PropertiesIterator;
import org.omg.CosPropertyService.PropertiesIteratorHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertyNamesIteratorHolder;
import org.omg.CosPropertyService.PropertyNamesIterator;
import org.omg.CosPropertyService.PropertyDefsHolder;
import org.omg.CosPropertyService.PropertyDefHolder;
import org.omg.CosPersistentState.NotFound;

import net.osm.properties.pss.PropertySetDefStorage;
import net.osm.properties.pss.PropertySetDefStorageBase;

/**
 * The PropertySetDefDelegate is an implementation of the PropertySetDef interface.
 * The PropertySetDef interface is a specialization (subclass) of the PropertySet interface.
 * The PropertySetDef interface provides operations to retrieve PropertySet constraints,
 * define and modify properties with modes, and to get or set property modes.
 * It should be noted that a PropertySetDef is still considered a PropertySet. The
 * specialization operations are simply to provide more client access and control of the
 * characteristics (metadata) of a PropertySet.<p>
 * The PropertySetDef interface also provides "batch" operations, such as
 * define_properties_with_modes, to deal with sets of property definitions as a
 * whole. The execution of the "batch" operations is considered best effort (i.e., not an
 * atomic set) in that not all suboperations need to succeed for any suboperation to
 * succeed.<p>
 * For define_properties_with_modes and set_property_modes, if any
 * suboperation fails, a MultipleExceptions exception is returned to identify which
 * property name had which exception.<p>
 * For example, a client may invoke define_properties_with_modes using four
 * property definition structures. The first property could be accepted (added or
 * modified), the second could fail due to an UnsupportedMode, the third could fail due
 * to a ConflictingProperty, and the fourth could fail due to ReadOnlyProperty. In this
 * case a property is either added or modified in the PropertySetDef and a
 * MultipleExceptions exception is raised with three items in the PropertyExceptions
 * sequence.<p>
 * The get_property_modes "batch" operation utilizes a boolean flag to signal that
 * mixed results occurred and additional processing may be required to fully analyze the
 * exceptions.<p>
 * Making "batch" operations behave in an atomic manner is considered an
 * implementation issue that could be accomplished via specialization of this property
 * service.<p>
 *
 * The PropertySet interface provides operations to define and modify properties, list and
 * get properties, and delete properties.<p>
 * The PropertySet interface also provides "batch" operations, such as
 * define_properties, to deal with sets of properties as a whole. The execution of
 * the "batch" operations is considered best effort (i.e., not an atomic set) in that not all
 * suboperations need succeed for any suboperation to succeed.
 * For define_properties and delete_properties, if any suboperation fails, a
 * MultipleExceptions exception is returned to identify which property name had which
 * exception.<p>
 * For example, a client may invoke define_properties using three property
 * structures. The first property could be accepted (added or modified), the second could
 * fail due to an InvalidPropertyName, and the third could fail due to a
 * ConflictingProperty. In this case a property is either added or modified in the
 * PropertySet, and a MultipleExceptions is raised with two items in the
 * PropertyExceptions sequence.<p>
 * The get_properties and delete_all_properties "batch" operations utilize
 * a boolean flag to identify that mixed results occurred and additional processing may be
 * required to fully analyze the exceptions.<P>
 * Making "batch" operations behave in an atomic manner is considered an
 * implementation issue that could be accomplished via specialization of this property
 * service.
 */


public class PropertySetDefDelegate extends PropertySetDefPOA implements LogEnabled
{

    public Logger log;

    private ORB orb;

    private PropertySetDefStorage store;

    private byte[] scope;
        
    // ==========================================================================
    // Constructors
    // ==========================================================================

   /**
    * Creation of a new PropertySetDefDelegate for properties defined within an 
    * enclosing scope qualified by a byte[] value, help within a PSS storage object,
    * on the supplied ORB.
    */

    public PropertySetDefDelegate ( ORB orb, PropertySetDefStorage store, byte[] scope )
    {
        this.orb = orb;
	  this.store = store;
	  this.scope = scope;
    }
    
    //============================================
    // Loggable implementation
    //============================================

   /**
    * Set the log channel for this instance.
    */

    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

   /**
    * Returns the log channel for this instance.
    */

    protected final Logger getLogger()
    {
        return log;
    }

    //============================================
    // PropertySetDef implementation
    //============================================
 
   /**
    * Indicates which types of properties are supported by this PropertySet. If the output
    * sequence is empty, then there is no restrictions on the any TypeCode portion of the
    * property_value field of a Property in this PropertySet, unless the
    * get_allowed_properties output sequence is not empty.
    * For example, a PropertySet implementation could decide to only accept properties that
    * had any TypeCodes of tk_string and tk_ushort to simplify storage processing and
    * retrieval.
    */

    public void get_allowed_property_types( PropertyTypesHolder property_types )
    {
	  property_types.value = new TypeCode[ this.store.types().length ];
		
	  for ( int i=0;i< this.store.types().length;i++)
	  {
	  	property_types.value[i]= (TypeCode) this.store.types()[i];
	  }
    }

    
   /** 
    * Indicates which properties are supported by this PropertySet. If the output sequence is
    * empty, then there is no restrictions on the properties that can be in this PropertySet,
    * unless the get_allowed_property_types output sequence is not empty.
    */

    public void get_allowed_types( PropertyTypesHolder property_types )
    {
        property_types.value = new TypeCode[ this.store.types().length ];
        for ( int i=0;i< this.store.types().length;i++)
        {
            property_types.value[i]= (TypeCode) this.store.types()[i];
        }
    }

   /**
    * Indicates which properties are supported by this PropertySet. If the output sequence is
    * empty, then there is no restrictions on the properties that can be in this PropertySet, 
    * subject to any allowable type constraints.
    */
    public void get_allowed_properties(PropertyDefsHolder property_defs)
    {
        property_defs.value = new PropertyDef[ store.definitions().length ];
        for ( int i=0; i < store.definitions().length; i++ )
        {
            property_defs.value[i]= (PropertyDef) store.definitions()[i];
        }
    }

   /**
    * This operation will modify or add a property to the PropertySet. If the property already
    * exists, then the property type is checked before the value is overwritten. The property
    * mode is also checked to be sure a new value may be written. If the property does not exist, 
    * then the property is added to the PropertySet. To change the any TypeCode portion of the 
    * property_value of a property, a client must first delete_property, then invoke the 
    * define_property_with_mode.
    */

    public void define_property_with_mode( String property_name, Any property_value, PropertyModeType property_mode )
    throws InvalidPropertyName, ConflictingProperty, UnsupportedTypeCode,
    UnsupportedProperty, UnsupportedMode, ReadOnlyProperty
    {
        validate(  property_name, property_value, property_mode );
	  PropertyDef tmpValue = null;
	  try
	  {
            tmpValue = (PropertyDef) store.get( this.scope, property_name);
	  }
	  catch( NotFound nf )
	  {
	  }
        if(( tmpValue != null ) && ( tmpValue.property_mode.value() == PropertyModeType._read_only )) 
	    throw new ReadOnlyProperty();
        if( ( tmpValue != null ) && ( 
	    tmpValue.property_value.type().kind().value() != property_value.type().kind().value() 
	    || tmpValue.property_mode.value() != property_mode.value() ) 
        ) throw  new ConflictingProperty();

        store.put( this.scope, property_name, property_value, property_mode );
    }

   /**
    * This operation will modify or add each of the properties in the Properties parameter to
    * the PropertySet. For each property in the list, if the property already exists, then the
    * property type is checked before overwriting the value. The property mode is also
    * checked to be sure a new value may be written. If the property does not exist, then the
    * property is added to the PropertySet.
    * This is a batch operation that returns the MultipleExceptions exception if any define
    * operation failed.    
    */
    public void define_properties_with_modes(PropertyDef[] property_defs)
    throws MultipleExceptions
    {
        Vector vector =new Vector();
        
        for (int i=0;i< property_defs.length;i++)
        {
            try
            {
                define_property_with_mode(
			property_defs[i].property_name,property_defs[i].property_value,property_defs[i].property_mode
		    );
            }
            catch(InvalidPropertyName ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.invalid_property_name,property_defs[i].property_name)
		    );
            }
            catch(ConflictingProperty ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.conflicting_property,property_defs[i].property_name)
		    );                
            }
            catch(UnsupportedTypeCode ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.unsupported_type_code ,property_defs[i].property_name)
		    );
            }
            catch(UnsupportedProperty ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.unsupported_property,property_defs[i].property_name)
		    );                
            }                                                                                                                                                       
            catch( UnsupportedMode ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.unsupported_mode ,property_defs[i].property_name)
		    );
            }
            catch(ReadOnlyProperty ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.read_only_property ,property_defs[i].property_name)
		    );
            }
        }
        
        if ( !vector.isEmpty() )
        {
            PropertyException tt[]= new PropertyException[vector.size() ];
            vector.copyInto(tt);
            throw new MultipleExceptions(tt);
        }
    }

   /**
    * Returns the mode of the property in the PropertySet.    
    */

    public PropertyModeType get_property_mode( String property_name )
    throws PropertyNotFound, InvalidPropertyName
    {
	  if( property_name == null ) throw new InvalidPropertyName();
	  if( property_name.length() == 0 ) throw new InvalidPropertyName();
	  try
	  {
            return store.get( this.scope, property_name ).property_mode;
	  }
	  catch( NotFound nf )
	  {
		throw new PropertyNotFound();
        }
    }
    
   /**
    * Returns the modes of the properties listed in property_names.
    * when the boolean flag is true, the property_modes parameter contains valid values for
    * all requested property names. If false, then all properties with a property_mode_type of
    * undefined failed due to PropertyNotFound or InvalidPropertyName. A separate
    * invocation of get_property_mode for each such property name is necessary to
    * determine the specific exception for that property name.
    * This approach was taken to avoid a complex, hard to program structure to carry mixed
    * results.
    */

    public boolean get_property_modes(String[] property_names, PropertyModesHolder property_modes)
    {
        boolean flag= true;
        
        property_modes.value = new PropertyMode[property_names.length];
        
        for ( int i=0; i < property_names.length;i++)
        {
            try
            {
	  	    if( property_names[i] == null ) throw new InvalidPropertyName();
	  	    if( property_names[i].length() == 0 ) throw new InvalidPropertyName();
                PropertyDef tmpProp = store.get( this.scope, property_names[i] );
                property_modes.value[i] = new PropertyMode(property_names[i] ,tmpProp.property_mode);
            }
            catch(NotFound ex )
            {
                property_modes.value[i] = new PropertyMode(property_names[i] ,PropertyModeType.undefined);
                flag= false;
            }
            catch( InvalidPropertyName ex)
            {
                property_modes.value[i] = new PropertyMode(property_names[i] ,PropertyModeType.undefined);
                flag= false;
            }
        }
        return flag;    
    }

   /**
    * Sets the mode of a property in the PropertySet.
    * Protection of the mode of a property is considered an implementation issue. For
    * example, an implementation could raise the UnsupportedMode when a client attempts
    * to change a fixed_normal property to normal.
    */

    public void set_property_mode(String property_name, PropertyModeType property_mode)
    throws InvalidPropertyName, PropertyNotFound, UnsupportedMode
    {
	  if( property_name == null ) throw new InvalidPropertyName();
	  if( property_name.length() == 0 ) throw new InvalidPropertyName();
	  try
	  {
		// WARNING: should be verifying current mode
            store.get( this.scope, property_name ).property_mode = property_mode;
	  }
	  catch( NotFound nf )
        {
            throw new PropertyNotFound();
	  }
    }

   /**
    * Sets the mode for each property in the property_modes parameter. This is a batch
    * operation that returns the MultipleExceptions exception if any set failed.
    */

    public void set_property_modes(PropertyMode[] property_modes)
    throws MultipleExceptions
    {
        Vector vector = new Vector();
        
        for (int i=0;i< property_modes.length;i++)
        {
            try
            {
                set_property_mode(property_modes[i].property_name,property_modes[i].property_mode);
            }
            catch( PropertyNotFound ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.property_not_found,property_modes[i].property_name)
		    );
            }
            catch( InvalidPropertyName ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.invalid_property_name,property_modes[i].property_name)
		    );
            }
            catch( UnsupportedMode ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.unsupported_mode ,property_modes[i].property_name)
		    );
            }
        }
        
        if ( !vector.isEmpty() )
        {
            PropertyException tt[]= new PropertyException[ vector.size() ];
            vector.copyInto(tt);
            throw new MultipleExceptions(tt);
        }
    }

    //===================================
    // PropertySet implementation
    //===================================
    
   /**
    * Define_property will modify or add a property to the PropertySet. If the property 
    * already exists, then the property type is checked before the value is overwritten. 
    * If the property does not exist, then the property is added to the PropertySet.
    * To change the any TypeCode portion of the property_value of a property, a client must
    * first delete_property, then invoke the define_property.
    */

    public void define_property(String property_name, Any property_value)
    throws InvalidPropertyName, ConflictingProperty, UnsupportedTypeCode, 
    UnsupportedProperty, ReadOnlyProperty
    {
	  if( getLogger().isDebugEnabled()  ) getLogger().debug("defining property '" + property_name + "'");
	  if( property_name == null ) throw new InvalidPropertyName();
	  if( property_name.length() == 0 ) throw new InvalidPropertyName();
        try
        {
            validate( property_name, property_value, null );
            define_property_with_mode( property_name, property_value, PropertyModeType.undefined );
        }
        catch(UnsupportedMode e)
        {
	      if( getLogger().isWarnEnabled() ) getLogger().warn("unsupported property '" + property_name + "'");
	      throw new UnsupportedProperty();
        }
    }

   /**
    * define_properties will modify or add each of the properties in Properties parameter to 
    * the PropertySet.      
    * For each property in the list, if the property already exists, then the property type is 
    * checked before overwriting the value. If the property does not exist, then the property  
    * is added to the PropertySet.                                                             
    * This is a batch operation that returns the MultipleExceptions exception if any define    
    * operation failed.  
    */

    public void define_properties(Property[] nproperties)        
    throws MultipleExceptions
    {        
        Vector vector =new Vector();
        for (int i=0;i< nproperties.length;i++)
        {
            try
            {
                define_property_with_mode(
			nproperties[i].property_name,nproperties[i].property_value, PropertyModeType.undefined );
            }
            catch(InvalidPropertyName ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.invalid_property_name,nproperties[i].property_name)
		    );
            }
            catch(ConflictingProperty ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.conflicting_property,nproperties[i].property_name)
		    );                
            }
            catch(UnsupportedTypeCode ex)
            {
                vector.addElement( 
			new PropertyException( ExceptionReason.unsupported_type_code , nproperties[i].property_name)
		    );
            }
            catch(UnsupportedProperty ex)
            {
                vector.addElement( 
			new PropertyException( ExceptionReason.unsupported_property,nproperties[i].property_name)
		    );                
            }                                                                                                                                                       
            catch( UnsupportedMode ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.unsupported_mode ,nproperties[i].property_name)
		    );
            }
            catch(ReadOnlyProperty ex)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.read_only_property ,nproperties[i].property_name)
		    );
            }
        }
        
        if ( !vector.isEmpty() )
        {
            PropertyException tt[]= new PropertyException[vector.size() ];
            vector.copyInto(tt);
            throw new MultipleExceptions(tt);
        }
    }

   /**
    * get_number_of_properties Returns the current number of properties associated with this PropertySet.
    */
    public int get_number_of_properties()
    {
        return store.size();
    }

   /**
    * get_all_property_names returns all of the property names currently defined in the 
    * PropertySet. If the PropertySet contains more than how_many property names, then the 
    * remaining property names are put into the PropertyNamesIterator. 
    */
    public void get_all_property_names( 
	int how_many, PropertyNamesHolder property_names, PropertyNamesIteratorHolder rest )
    {
        int i=0;
        String[] names = store.names();
        
        int max = store.size();
        if( how_many < max ) max = how_many;
        
        property_names.value = new String[ max ];

        for( i=0; i < max ;i++ )
        {
            property_names.value[i] = new String( names[i] );
        }
        
        if( store.names().length > how_many )
        {
            Vector vector= new Vector();
		for( i = how_many; i < store.names().length; i++ )
            {
                vector.addElement( store.names()[i] );
            }
            PropertyNamesIteratorDelegate tmp = new PropertyNamesIteratorDelegate( vector );
            rest.value =( PropertyNamesIterator) tmp._this(this.orb);
        }
    }
    
   /**
    * Returns the value of a property in the PropertySet.
    */        
    public Any get_property_value(String property_name)
    throws PropertyNotFound, InvalidPropertyName
    {
	  try
	  {
            return store.get( this.scope, property_name ).property_value;
	  }
	  catch( NotFound nf )
	  {
	      throw new PropertyNotFound();
	  }
    }
    
   /**
    * get_properties Returns the values of the properties listed in property_names.
    * When the boolean flag is true, the Properties parameter contains valid values for all
    * requested property names. If false, then all properties with a value of type tk_void may
    * have failed due to PropertyNotFound or InvalidPropertyName.
    * A separate invocation of get_property for each such property name is necessary to
    * determine the specific exception or to verify that tk_void is the correct any TypeCode
    * for that property name.
    * This approach was taken to avoid a complex, hard to program structure to carry mixed
    * results.
    */    
    public boolean get_properties(String[] property_names, PropertiesHolder nproperties)
    {
        boolean flag= true;
        
        nproperties.value = new Property[ property_names.length ];
        
        for ( int i=0; i < property_names.length;i++)
        {
            try
            {
	  	    if( property_names[i] == null ) throw new InvalidPropertyName();
	  	    if( property_names[i].length() == 0 ) throw new InvalidPropertyName();
		    PropertyDef p = store.get( this.scope, property_names[i] );
	          Property tmpProp = new Property( p.property_name, p.property_value );
                nproperties.value[i] = tmpProp;
            }
            catch( NotFound ex )
            {
                Property tmpProp = new   Property();
                tmpProp.property_name = property_names[i];
                Any myAny = this.orb.create_any();
                myAny.type(this.orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void) );
                tmpProp.property_value= myAny;
                nproperties.value[i] = tmpProp;
                flag= false;
            }
            catch( InvalidPropertyName ex )
            {
                Property tmpProp = new Property();
                tmpProp.property_name = property_names[i];
                Any myAny = this.orb.create_any();
                myAny.type(this.orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void) );
                tmpProp.property_value= myAny;
                nproperties.value[i] = tmpProp;
                flag= false;
            }
        }
        return flag;
    }
    
    
   /**
    * get_all_properties Returns all of the properties defined in the PropertySet. 
    * If more than how_many properties are found, then the remaining properties are 
    * returned in rest
    */

    public void get_all_properties(
	int how_many, PropertiesHolder nproperties, PropertiesIteratorHolder rest)
    {

        String[] names = store.names();

        int max = store.size();
        if ( how_many < max ) max= how_many;
        
        nproperties.value = new Property[ max ];
        for ( int i=0; i < max ;i++ )
        {
            try
            {
		    PropertyDef p = store.get( this.scope, names[i] );
	          nproperties.value[i] = new Property( p.property_name, p.property_value );
            }
            catch(Throwable ex)
            {
            }   
        }
        
        Vector vector= new Vector();
        for( int i=how_many; i<names.length; i++ )
        {
            try
            {
		    PropertyDef p = store.get( this.scope, names[i] );
                vector.addElement( new Property( p.property_name, p.property_value ));
            }
            catch(Throwable ex)
            {
            }
        }
        PropertiesIteratorDelegate tmp = new PropertiesIteratorDelegate( vector );
        rest.value = (PropertiesIterator) tmp._this( this.orb );
    }

   /**
    * delete_property Deletes the specified property if it exists from a PropertySet.     
    */

    public void delete_property(String property_name)        
    throws PropertyNotFound, InvalidPropertyName, FixedProperty
    {
	  if( property_name == null ) throw new InvalidPropertyName();
	  if( property_name.length() == 0 ) throw new InvalidPropertyName();
	  try
	  {
            PropertyDef p = (PropertyDef) store.get( this.scope, property_name );
            if( 
		    p.property_mode.value() == PropertyModeType._fixed_normal  
		    || p.property_mode.value() == PropertyModeType._fixed_readonly 
	      ) throw new FixedProperty();             
            store.delete( this.scope, property_name );
	  }
	  catch( NotFound nf )
	  {
            throw new PropertyNotFound();     
	  }   
    }

   /**
    * delete_properties     
    * Deletes the properties defined in the property_names parameter. This is a batch
    * operation that returns the MultipleExceptions exception if any delete failed.      
    */        
    public void delete_properties( String[] property_names )        
    throws MultipleExceptions
    {
        
        Vector vector = new Vector();
        for (int i=0;i< property_names.length; i++ )
        {
            try
            {
                delete_property( property_names[i] );
            }
            catch( InvalidPropertyName e)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.invalid_property_name, property_names[i])
		    );
            }
            catch( PropertyNotFound e)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.property_not_found, property_names[i])
		    );
            }
            catch( FixedProperty e)
            {
                vector.addElement( 
			new PropertyException(ExceptionReason.fixed_property, property_names[i])
		    );
            }
        }
        if ( !vector.isEmpty() )
        {
            PropertyException exceptions[]= new PropertyException[ vector.size() ];
            vector.copyInto( exceptions );
            throw new MultipleExceptions( exceptions );
        }
    }

   /**
    * Variation of delete_properties. Applies to all properties.
    * Since some properties may be defined as fixed property types, it may be that not all
    * properties are deleted. The boolean flag is set to false to indicate that not all properties
    * were deleted.
    * 
    * A client could invoke get_number_of_properties to determine how many properties
    * remain. Then invoke get_all_property_names to extract the property names remaining. A
    * separate invocation of delete_property for each such property name is necessary to
    * determine the specific exception.
    */
    public boolean delete_all_properties()
    {
        String[] names = store.names();
        boolean flag=true;
        for (int i=0; i<names.length; i++)
        {
            try
            {
                delete_property( (String)  names[i] );
            }
            catch(PropertyNotFound ex)
            {
                flag=false;
            }
            catch(InvalidPropertyName ex)
            {
                flag=false;
            }
            catch(FixedProperty ex)
            {
                flag=false;
            }
        }
        return flag;
    }

   /**
    * The is_property_defined operation returns true if the property is defined in the
    * PropertySet, and returns false otherwise.     
    */
    public boolean is_property_defined( String name )        
    throws InvalidPropertyName
    {
        return store.available( this.scope, name );
    }
    
   /**
    * Internal method validating a property_name or a property_value
    * It look in all allowed table and search if name or type are contained
    * Method returns true is all thing are good, false otherwise
    */
    private boolean validate(  String property_name, Any property_value, PropertyModeType property_mode )
    throws InvalidPropertyName, UnsupportedTypeCode, UnsupportedProperty, UnsupportedMode, ReadOnlyProperty
    {
	  if( property_name == null ) throw new InvalidPropertyName();
	  if( property_name.length() == 0 ) throw new InvalidPropertyName();
        if ( !isAllowedType( store.types(), property_value.type()) )
        {
            throw new UnsupportedTypeCode();
        }    
        if ( !isAllowedProperty( store.definitions(), property_name) )
        {
            throw new UnsupportedProperty();
        }
        return true;
    }

    private boolean isAllowedType( TypeCode[] types, TypeCode tc)
    {
        if ( types.length == 0 ) return true;
        for (int i=0;i< types.length ; i++)
        {
            if ( ((TypeCode)types[i]).equal( tc ) ) return true;
        }
        return false;
    }
    
    private boolean isAllowedProperty( PropertyDef[] definitions, String name )
    {
        if ( definitions.length == 0 ) return true;
        for (int i=0; i< definitions.length ; i++)
        {
            if (((String)(( PropertyDef )definitions[i]).property_name).equals(name) ) return true;
        }
        return false;
    }
}
