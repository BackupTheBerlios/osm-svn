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

public class DefaultContentFilter extends ContentFilter implements ValueFactory, FilterOperations
{

    public static final String orDelimiter = "|";

    public DefaultContentFilter( ){}

    public DefaultContentFilter( String name, String value ){
	  this.logical = true;
	  this.binary = true;
	  this.name = name;
	  this.value = value;
	  this.exact = false;
	  this.sensitive = false;
    }

    // from ValueFactory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new DefaultContentFilter() );
    }

    // FilterOperations

    public String name() {
	  return this.name;
    }

    public boolean equivilent( int n, FeatureBase source ) {
	  return this.equivilent( n, source.value );
    }

    public boolean equivilent( int n, String source ) {
	  if( this.value == null ) return true;
	  if( source == null ) return false;
	  int i = 0;
	  int j = this.value.indexOf('|');
        boolean b = false;
	  if( j < 0 ) {
		b = this.verifyString( this.value, source );
	  } else {
		while( j > 0 ) {
		    if( this.verifyString( this.value.substring( i, j), source ) ) {
			  return true;
		    } else {
			  i = j+1;
			  j = this.value.indexOf('|',i);
		    }
		}
		b = this.verifyString( this.value.substring( i, this.value.length()), source );
	  }
        return b;
    }

    public boolean equivilent( int n, Object source ) {
	  if( this.value == null ) return true;
	  if( source instanceof FeatureBase ) return equivilent( n, ((FeatureBase)source).value );
	  if( source instanceof String ) return equivilent( n, (String) source );
	  return false;
    }

    private boolean verifyString( String datum, String source ) {
	  String s = source;
	  String v = datum;
	  boolean match = false;
	  if( !this.sensitive ) {
	 	s = s.toLowerCase();
	      v = v.toLowerCase();
	  }
	  if( exact ) {
		match = v.equals(s);
	  } else {
	      match = (s.indexOf(v)>-1);
	  }
	  if( logical ) {
	      return match;
	  } else {
	      return !match;
	  }
    }

    public ScoreBase measure( Object source ) {
	  System.err.println("\tExecuting measure ??");
	  return null;
    }

    // Utilities

    public String toString() {
	  return this.getClass().getName() + "[" + this.name + ";" + this.value + "]";
    }

} // Filter
