/*
 * @(#)DescriptionBase.java
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
import java.util.Enumeration;
import java.util.Date;
import java.util.Random;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Description;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.DisclosurePolicyValue;
import net.osm.discovery.Key;
import net.osm.discovery.Feature;
import net.osm.discovery.Filter;
import net.osm.discovery.Score;
import net.osm.discovery.URI;
import net.osm.discovery.Identifier;
import net.osm.discovery.Entry;
import net.osm.discovery.Chain;
import net.osm.discovery.Content;
import net.osm.discovery.VerificationFailure;


/**
Value implementation for net.osm.discovery.Description 
*/

public class DescriptionBase extends Description implements ValueFactory, CompositeValue{
    
    private static final Random random = new Random();

    public DescriptionBase( ){}
    public DescriptionBase(
		String title, String description, String resource )
        {

	  this.id = new IdentifierBase( random ); 
	  this.value = new ChainBase();
	  this.resource = new URIBase( resource );
	  this.timestamp = new UtcTBase( new Date());
	  this.policy = new DisclosurePolicyBase( DisclosurePolicyValue.UNDEFINED );
	  this.title = title;
	  this.description = description;

	  // manifest pending
    }

    // operations from value factory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new DescriptionBase() );
    }

    // from Verifiable
   
    public boolean verify() throws VerificationFailure {
	  throw new VerificationFailure("method not implemented", this );
    }
 
    // from CompositeValue

    public ChainBase value( ) {
	  return (ChainBase) this.value;
    }

    public int size( ) {
	  return ((ChainBase)this.value).size();
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
	  return "DescriptionBase[" + 
		this.id + ";" +
		this.resource + ";" +
		this.timestamp + ";" +
		this.policy + ";" +
		this.title + ";" +
		this.description + ";" +
		this.manifest + ";" + 
		this.value + "]";
    }


} // DescriptionBase
