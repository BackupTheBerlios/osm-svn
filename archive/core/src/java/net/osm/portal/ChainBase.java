/*
 * @(#)ChainBase.java
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
import java.util.Hashtable;
import java.util.Enumeration;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Entry;
import net.osm.discovery.Key;
import net.osm.discovery.Feature;
import net.osm.discovery.Content;
import net.osm.discovery.Chain;
import net.osm.discovery.Score;


/**
ValueFactory for net.osm.discovery.Chain 
*/

public class ChainBase extends Chain implements ValueFactory, CompositeValue
{

    private static final String delimiter = "/";
    protected Hashtable extent;

    // constructor

    public ChainBase( ){
        this.contents = new Entry[]{};
	  this.extent = new Hashtable();
    }

    public ChainBase( Entry[] contents ){
	  this.contents = contents;
	  this.extent = new Hashtable();
	  this.updateNVT();
    }

    public ChainBase( Entry nv ){
	  this.contents = new Entry[]{ nv };
	  this.extent = new Hashtable();
	  this.updateNVT();
    }


    // operations from value factory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new ChainBase() );
    }

    // from valuebase

    public void _read(org.omg.CORBA.portable.InputStream _is){
        super._read(_is);
	  this.extent = new Hashtable();
        this.updateNVT();
    }
    
    public void _write( org.omg.CORBA.portable.OutputStream _os){
        this.updateNVA();
        super._write(_os);
    }

    // from Chain

    public ChainBase value( ) {
	  return this;
    }

    public int size( ) {
	  return this.extent.size();
    }

    public Entry[] extent( ) {
	  return (Entry[]) this.extent.values().toArray( new Entry[0] );
    }

    public Entry lookup( String name ) throws KeywordNotFound{
	  Object t = this.extent.get( name.toLowerCase() );
	  if( t == null ) throw new KeywordNotFound( name );
	  return (Entry) t;
    }

    public KeyBase locateKey( String name ) throws KeywordNotFound {
	  try{ 
		return (KeyBase) this.lookup( name );
	  } catch (Exception e) {
		for( int i = 0; i<this.contents.length; i++ ){
		    try{
			  KeyBase k = (KeyBase) this.contents[i];
			  return k.locateKey( name );
		    } catch (Exception ex){
		    }
	      }
		throw new KeywordNotFound( name );
	  }
    }

    public KeyBase newKey( String name ) {
	  if( name.indexOf(delimiter) > 0 ) {
		// get the first name and add it, then use the result to add another
		int i = name.indexOf(delimiter);
		KeyBase k = this.newKey( name.substring(0,i) );
		return k.newKey( name.substring( i+1, name.length()));
	  } else {
		try{
		    return (KeyBase) this.lookup( name );
		} catch (Exception e) {
	          KeyBase k = new KeyBase( name );
	          this.addKey( k );
	          return k;
		}
	  }
    }


    public FeatureBase newFeature( String name, String value ) {
	  FeatureBase f = new FeatureBase( name, value );
	  this.addFeature( f );
	  return f;
    }

    public void addKey( Key key ) {
	  this.extent.put( key.name.toLowerCase(), key );
	  this.updateNVA();
    }

    public void addFeature( Feature f ) {
	  this.extent.put( f.name, f );
	  this.updateNVA();
    }

    // utilities

    public String toString() {
	  String s = "ChainBase[";
	  Enumeration enum = this.extent.elements();
	  while( enum.hasMoreElements() ) {
		s = s + enum.nextElement().toString();
	  }
        return s;
    }

    private void updateNVT(  ) {
        for( int i = 0; i < this.contents.length; i++ ) {
		Entry v = (Entry)this.contents[i];
		this.extent.put( v.name.toLowerCase(), v ); 
        }
    }

    protected void updateNVA( ) {
	  this.contents = (Entry[]) this.extent.values().toArray( new Entry[0] );
    };


} // ChainBase
