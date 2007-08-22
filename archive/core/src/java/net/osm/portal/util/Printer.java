
package net.osm.portal.util;

import java.text.DateFormat;
import java.util.Vector;
import java.util.Enumeration;
import net.osm.discovery.Key;
import net.osm.discovery.Feature;
import net.osm.discovery.Description;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.DisclosurePolicyValue;
import net.osm.discovery.URI;
import net.osm.discovery.Identifier;
import net.osm.discovery.UtcT;
import net.osm.discovery.Entry;

import net.osm.portal.UtcTBase;


/**
* Print to System.out different discovery types.
*/

public class Printer {

    private static final String delimiter = ".";
    private static final String featureDelimitor = ": ";

    private Printer(){}

    public static void print( String prefix, Description r ) {
	  System.out.println( "" );
	  print( prefix, r.id );
	  print( prefix + "title: ", r.title );
	  print( prefix + "description: ", r.description );
	  print( prefix + "timestamp: ", r.timestamp );
	  print( prefix, r.policy );
	  print( prefix + "resource: ", r.resource );
	  Entry[] children = r.value.contents;
	  for( int i = 0 ; i < children.length ; i ++ ){
		  Object child = (Object) children[i];
		  if( child instanceof Feature) {
		  	print( prefix, (Feature) child );
		  }else if( child instanceof Key ) {
		  	print( prefix, (Key) child );
		  }else{
			print( prefix, child );
		  }
        }
    }

    public static void print( String prefix, Key k ) {
	  print( prefix, null, k );
    }

    private static void print( String prefix, String last, Feature f ) {
	  System.out.print( prefix + last + delimiter + f.name + featureDelimitor + f.value + "\n");
    }

    private static void print( String prefix, String last, Key key ) {

	   // append the next named value

	   // If this is an instance of a Key, then just add Key.name to
	   // "last" string and invoke the operation on all of the key's
         // children.

	   if( last != null ) {
	       last = last + delimiter + key.name;
	   } else {
	       last = key.name;
	   }

	   Entry[] children = key.value.contents;
	   if( children.length == 0 ) {
		  System.out.print( prefix + last + "\n");
	   } else {
	        for( int i = 0; i < children.length; i++ ) {
	           	try{
		         print( prefix, last, children[i] );
		      } catch (Exception e) {
                  }
	        }
         }
    }

    private static void print( String prefix, String last, Entry nv ) {
	  if( nv instanceof Key ) {
		print( prefix, last, (Key) nv);
	  } else if ( nv instanceof Feature ) {
		print( prefix, last, (Feature) nv );
        }
    }

    public static void print( String prefix, Feature f ) {
	  System.out.println( prefix + f.name + ": " + f.value );
    }

    public static void print( String prefix, UtcT s ) {
	  System.out.println( prefix + DateFormat.getDateInstance().format( ((UtcTBase)s).date() ) );
    }

    public static void print( String prefix, URI uri ) {
	  System.out.println( prefix + uri.value );
    }

    private static void print( String prefix, DisclosurePolicy p ) {
	if( p.value == DisclosurePolicyValue.REPLICATION ) {
        System.out.println( prefix + "policy: REPLICATION");
	  } else if( p.value == DisclosurePolicyValue.REFERRAL ) {
            System.out.println( prefix + "policy: REFERRAL");
	  } else {
            System.out.println( prefix + "policy: UNDEFINED");
        }
    }

    private static void print( String prefix, Identifier i ) {
        print( prefix + "id: ", i.value );
        print( prefix + "dns: ", i.dns );
    }

    public static void print( String prefix, String s ) {
	  System.out.println( prefix + s );
    }

    public static void print( String prefix, Object obj ) {
	  if( obj instanceof Description ){
		print( prefix, (Description) obj );
	  } else {
	      System.out.println( prefix + "- " + obj );
        }
    }

} // JarDescription
