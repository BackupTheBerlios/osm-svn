/*
 * @(#)CompositeFilter.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;

import java.io.Serializable;
import java.io.File;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Filter;
import net.osm.discovery.Portal;
import net.osm.discovery.PortalHelper;
import net.osm.discovery.DisclosurePolicyValue;
import net.osm.portal.util.*;

/**
ValueFactory for net.osm.portal.CompositeFilter 
*/

public class CompositeFilter extends net.osm.discovery.CompositeFilter implements ValueFactory, FilterOperations
{

    private static final String service = "DiscoveryService";
    public static final String featureDelimiter = ":";
    public static final String keyDelimiter = "/";
    private static SelectionSetBase selectionSet;
    protected Hashtable extent;

    public CompositeFilter( ){
	  this.logical = true;
	  this.binary = true;
	  this.extent = new Hashtable();
	  this.name = "";
    }

    public CompositeFilter( String name ){
	  this( name, new Filter[0] );
    }

    public CompositeFilter( String name, Filter[] filters ){
	  this.logical = true;
	  this.binary = true;
	  this.name = name;
	  this.extent = createExtent( filters );
    }

    // from ValueFactory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new CompositeFilter() );
    }

    public void _read(org.omg.CORBA.portable.InputStream _is){
        super._read(_is);
	  this.extent = createExtent( this.filters );
    }
    
    public void _write( org.omg.CORBA.portable.OutputStream _os){
	  this.filters = (Filter[]) this.extent.values().toArray(new Filter[0]);
        super._write(_os);
    }

    // internal

    public int size( ) {
	  return this.extent.size();
    }

    public Filter lookup( String name ) throws KeywordNotFound{
	  if( this.extent == null ) throw new KeywordNotFound( name );
	  Object f = this.extent.get( name.toLowerCase() );
	  if( f == null ) throw new KeywordNotFound( name );
	  return (Filter) f;
    }

    public Filter newFilter( String name ) {
	  return newFilter( name, new Filter[0] );
    }

    public Filter newFilter( String name, Filter filter ) {
	  return newFilter( name, new Filter[]{ filter } );
    }

    public Filter newFilter( String name, Filter[] filters ) {
	  name = trimKeyword( name );
	  if( name.indexOf( keyDelimiter ) > 0 ) {
		// get the first name and add it, then use the result to add another
		int i = name.indexOf( keyDelimiter );
		String thisName = name.substring( 0, i );
		Filter f = this.newFilter( thisName );
		return ((CompositeFilter)f).newFilter( name.substring( i+1, name.length()), filters );
	  } else {
		try{
		    return (Filter) this.lookup( name );
		} catch (Exception e) {
	          Filter f = new CompositeFilter( name, filters );
	          this.add( f );
	          return f;
		}
	  }
    }

    public Filter newFeatureFilter( String arg ) {
	  if( arg.indexOf(":" ) > -1 ) {
		String name = arg.substring(0,arg.indexOf(":"));
		String remainder = arg.substring(arg.indexOf(":")+1, arg.length());
		return newFeatureFilter( name, remainder );
	  } else {
	      return newFeatureFilter( arg, null );
	  }
    }

    public Filter newFeatureFilter( String name, String value ) {
	  Filter v = null;
	  try{
		v = this.lookup( "value" );
	  } catch (Exception e) {
		v = new CompositeFilter( "value" );
		this.add( v );
        }
	  Filter f = new ContentFilter( name, value) ;
	  ((CompositeFilter)v).add( (Filter) f );
	  return f;
    };

    public Filter newKeyFilter( String path ) {
	  Filter v = null;
	  try{
		v = this.lookup( "value" );
	  } catch (Exception e) {
		v = new CompositeFilter( "value" );
		this.add( v );
        }
	  Filter f = ((CompositeFilter)v).newFilter( path );
	  return f;
    };

    public void add( Filter filter ) {
	  if( this.extent == null ) this.extent = new Hashtable();
	  this.extent.put( filter.name, filter );
    }

    public static String trimKeyword( String keyword ) {
	  String s = keyword.trim();
	  if( s.endsWith(keyDelimiter)) s = s.substring(0,s.length()-1);
	  return s;
    }

    // 

    public String name() {
	  return this.name;
    }

    public boolean equivilent( int n, KeyBase source ) {
	  return this.equivilent( n, source.value );
    }

    public boolean equivilent( int n, ChainBase source ) {
	  if( this.size() == 0 ) return true;
	  boolean b = false;
	  Iterator i = this.extent.values().iterator();
	  while( i.hasNext() ) {
		Filter f = (Filter) i.next();
		if( !(f.name.equals("*")) && !(f.name.equals("**"))) { 

		     // normal match (no wildcard)
		     try{
		         b = ((FilterOperations)f).equivilent( n + 1, source.lookup( f.name ));
		         if( !b ) return false;
		     } catch (KeywordNotFound e) {
		         //return false;
		     }
			  
		 } else {

		    // all members of source are candadates
		    // so, invoke process on 'nvf' with every key of 
		    // source as an argument
 
		    for( int j = 0; j<source.contents.length; j++ ) {
                    Object obj = source.contents[j];
			  b = ((FilterOperations)f).equivilent( n + 1, obj );
			  if( b ) return true;
		    }

		    if( f.name.equals("*") ) {
		  	  return false;
		    } else {

			  // do a light jump from this source content base
			  // to a content base one level deeper in the key tree
 
		        for( int j = 0; j<source.contents.length; j++ ) {
		            try{
                            KeyBase key = (KeyBase) source.contents[j];
				    b = this.equivilent( n + 1, (ChainBase) key.value());
				    if( b ) return true;
		            } catch (Exception e) {
				}
		        } // end for

		    } // end if

            } // end if

        } // end while

	  return b;
    }

    public boolean equivilent( int n, Object source ) {

	  // This filter is simply a container of filters.  For each filter
        // contained in this filter, get its name, and locate an equivilent
	  // field of the source object, then apply the 'equivilent' operation
	  // of the located filter with the value of the extracted field.

	  if( source instanceof ChainBase ) return this.equivilent( n, (ChainBase) source );
	  if( source instanceof KeyBase ) return this.equivilent( n, (KeyBase) source );
	  if( source instanceof FeatureBase ) return false;

	  Iterator i = this.extent.values().iterator();
	  while( i.hasNext() ) {
		Filter filter = (Filter) i.next();
		try{
		    Object object = source.getClass().getField( filter.name ).get( source );
		    boolean b = ((FilterOperations)filter).equivilent( n + 1, object );
		    if( !b ) {
			  return false;
		    }
		} catch (IllegalAccessException e) {
		    return false;
		} catch (NoSuchFieldException e) {
		    return false;
		}
	  }
	  return true;
    }

    public ScoreBase measure( Object source ) {
	  System.err.println("\tExecuting measure ??");
	  return null;
    }

    // Utilities

    public String toString() {
	  String s = this.getClass().getName() + "[" + this.name + ";[";
	  Enumeration e = this.extent.elements();
	  while( e.hasMoreElements() ) {
		s = s + e.nextElement().toString();
        }
	  s = s + "]]";
	  return s;
    }

    private static Hashtable createExtent( Filter[] filters ) {
	  Hashtable extent = new Hashtable();
	  for( int i = 0; i < filters.length; i++ ) {
		Filter f = (Filter) filters[i];
		extent.put( f.name, f );
        }
	  return extent;
    }

} // Filter
