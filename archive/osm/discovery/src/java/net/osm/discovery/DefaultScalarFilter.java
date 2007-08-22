/*
 * @(#)ContentFilter.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.discovery;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
ValueFactory for net.osm.discovery.Filter 
*/

public class DefaultScalarFilter extends ScalarFilter implements ValueFactory, FilterOperations
{

    public DefaultScalarFilter( ){}

    public DefaultScalarFilter( String name, float value, LogicalOperator operator ){
	  this.name = name; 
	  this.binary = true; 
	  this.value = value;
	  this.operator = operator;
    }

    public DefaultScalarFilter( String name, float value ){
	  this( name, value, LogicalOperator.EQUAL_TO );
    }

    public DefaultScalarFilter( String name, UtcTBase utct ){
	  this( name, utct.value.time, LogicalOperator.EQUAL_TO );
    }

    // from ValueFactory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new DefaultScalarFilter() );
    }

    // FilterOperations

    public String name() {
        return this.name;
    }

    public boolean equivilent( int n, Object source ) {
	  if ( source instanceof Number ) {
		return equivilent( n, ((Number)source).floatValue() );
	  } else if ( source instanceof UtcTBase ) {
		return equivilent( n, ((UtcTBase)source).value.time );
	  } else if ( source instanceof DisclosurePolicyBase ) {
		return equivilent( n, ((DisclosurePolicyBase)source).value.value() );
	  } else if( source instanceof String ) {
		return equivilent( n, ((Number)source).floatValue() );
	  } else {
		System.err.println(
			this.getClass().getName() + 
			": attempt to apply equivilent on an unknown type, '" + 
			source.getClass().getName() +
			"'."
		);
	  }
	  return false;
    }

    public boolean equivilent( int n, String source ) {
	  try{
		Float f = new Float( (String) source );
	  	return equivilent( n, f );
	  } catch (NumberFormatException e ) {
		return false;
	  }
    }

    public boolean equivilent( int n, float source ) {
	  Float f = new Float( this.value );
	  Float s = new Float( source );
	  int logic = f.compareTo( s );
	  if( logic == 0 ) {
		if(
			( this.operator == LogicalOperator.EQUAL_TO ) ||
			( this.operator == LogicalOperator.GREATER_THAN_OR_EQUAL ) ||
			( this.operator == LogicalOperator.LESS_THAN_OR_EQUAL )
		){
		    return true;
		} else {
		    return false;
		}
	  } else if( logic == -1 ) {
		if(
			( this.operator == LogicalOperator.LESS_THAN ) ||
			( this.operator == LogicalOperator.LESS_THAN_OR_EQUAL )
		){
		    return true;
		} else {
		    return false;
		}
	  } else if ( logic == 1 ) {
		if(
			( this.operator == LogicalOperator.GREATER_THAN ) || 
			( this.operator == LogicalOperator.GREATER_THAN_OR_EQUAL )
		){
		    return true;
		} else {
		    return false;
		}
	  }
	  return false;
    }

    public ScoreBase measure( Object source ) {
	  System.err.println("\tSCALAR - Executing measure ??");
	  return null;
    }

    // Utilities

    public String toString() {
	  return this.getClass().getName() + "[" + this.name + ";" + this.value + "]";
    }

} // Filter
