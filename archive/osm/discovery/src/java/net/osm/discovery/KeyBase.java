/*
 * @(#)KeyBase.java
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
import java.util.Hashtable;
import java.util.Enumeration;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Key;
import net.osm.discovery.Feature;
import net.osm.discovery.Entry;
import net.osm.discovery.Content;
import net.osm.discovery.Score;

/**
ValueFactory for net.osm.discovery.Key 
*/

public class KeyBase extends Key implements ValueFactory
{

    private static final String delimiter = "/";

    // constructors

    public KeyBase( ){}
    
    public KeyBase(String name ){
	  String s = trimKeyword(name);
	  this.value = new ChainBase( );
	  if( s.indexOf(delimiter) > 0 ) {
		this.name = s.substring(0,s.indexOf(delimiter));
		String s2 = s.substring(s.indexOf(delimiter)+1, s.length());
		this.value().addKey( new KeyBase( s.substring(s.indexOf(delimiter)+1, s.length())));
	  } else {
	      this.name = s;
	  }
    }

    public KeyBase(String[] keywords ){
	  this.name = trimKeyword(keywords[0]);
	  this.value = new ChainBase( );
	  KeyBase last = this;
        for( int i = 1; i < keywords.length; i++) {
		last = last.value().newKey( keywords[i] );
	  }
    }

    public KeyBase(String name, Entry[] keys ){
	  this.name = trimKeyword( name );
	  this.value = new ChainBase( );
	  for( int i = 0; i < keys.length; i++ ) {
		if( keys[i] instanceof Feature ) {
		    this.value().addFeature( (Feature) keys[i] );
		} else if ( keys[i] instanceof Key ) {
		    this.value().addKey( (Key) keys[i] );
		} else {
		    System.err.println(
			"KeyBase/constructor(String, Entry[]) - unknown type argument.");
		}
        }
    }

    // operations from value factory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new KeyBase() );
    }

    // operations from Entry

    public String name() {
	  return this.name;
    }

    // operations from CompositeValue

    public ChainBase value( ) {
	  return (ChainBase) this.value;
    }

    public int size( ) {
	  return this.value().size();
    }

    public Entry lookup( String name ) throws KeywordNotFound{
	  return ((ChainBase)this.value).lookup( name );
    }

    public KeyBase locateKey( String name ) throws KeywordNotFound {
	  return this.value().locateKey( name );
    }

    public KeyBase newKey( String name ){
	  return ((ChainBase)this.value).newKey( name );
    }

    public FeatureBase newFeature( String name, String value ){
	  return ((ChainBase)this.value).newFeature( name, value );
    }

    public void addKey( Key key ){
	   ((ChainBase)this.value).addKey( key );
    }

    public void addFeature( Feature f ){
	  ((ChainBase)this.value).addFeature( f );
    }

    // utilities

    public String toString() {
	  return "KeyBase[" + this.name + ";" + this.value + "]";
    }

    public static String trimKeyword( String keyword ) {
	  String s = keyword.trim();
	  if( s.endsWith(delimiter)) s = s.substring(0,s.length()-1);
	  return s;
    }


} // KeyBase
