/*
 * @(#)IdentifierBase.java
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
import java.lang.Character;
import java.math.BigInteger;
import java.util.Random;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Identifier;
import net.osm.discovery.URI;
import net.osm.discovery.Score;

/**
ValueFactory for Identifier 
*/

public class IdentifierBase extends Identifier implements ValueFactory
{

    private static final int DEFAULT_SIZE = 4;
    private static final int DEFAULT_PACKETS = 4;
    private static final String DEFAULT_DELIMITER = "-";
    private static final int MIN_INDEX = 11;
    private static final int MAX_INDEX = 35;

    private static final String authority = null;
    private static final String defaultDomain = "unknown";


    // constructors

    public IdentifierBase( ){}

    public IdentifierBase( String s ){
	  this.dns = getAuthority();
	  this.value = s;
    }

    public IdentifierBase( String dns, String s ){
	  this.dns = dns;
	  this.value = s;
    }

    public IdentifierBase(Random r ){
	  this( r, DEFAULT_SIZE , DEFAULT_PACKETS, DEFAULT_DELIMITER );
    }

    public IdentifierBase(Random r, int n, int m, String s ){
	  this.dns = getAuthority();
	  this.value = random( r, n , m, s );
    }

    // operations from ValueFactory

    public Serializable read_value( InputStream is ){
	  return is.read_value( new IdentifierBase() );
    }

    // utilities

    public String toString() {
	  return "net.osm.IdentifierBase[" + this.dns + ";" + this.value + "]";
    }

    /**
    * Creates a random string identifier in the form ABCD-EFGH-IJKL. This example
    * string identitifer is a single string constructed with 3 packets, 4 characters
    * per packet, and the character '-' as a packet delimiter.
    * 
    * @param k Random seed.
    * @param n defines the number of packets per identifier
    * @param m defines the number of characters per packet
    * @param sep defines the seperator character used between packets
    */

    public static String random( Random k, int n, int m, String sep ){
	  if( n < 1) n = 1;
	  if( m < 1) m = 1;
	  String s = getPacket(k,m);
        for( int i=0; i<n-1; i++) {
		s = s + sep + getPacket(k,m);
        }
	  return s;
    };

    private static int getInt( Random k ) {
	  BigInteger n = new BigInteger( 6, k );
	  int m = n.intValue();
	  if( m > MAX_INDEX || m  < MIN_INDEX ) return getInt( k );
	  return m;
    }

    private static String getPacket( Random k, int count ){
	  String s = "";
        for( int i=0; i<count; i++) {
		s = s + Character.toUpperCase(Character.forDigit(getInt(k),MAX_INDEX + 1));
        }
	  return s;
    }

    private static String getAuthority() {
	  if( authority != null ) return authority;
	  String dns = System.getProperty("OSM.DOMAIN");
	  if( dns == null ) dns = defaultDomain;
	  return dns;
    }

} // URIBase
