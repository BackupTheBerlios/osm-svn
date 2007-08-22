
package net.osm.portal.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import net.osm.discovery.Key;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.Feature;
import net.osm.discovery.URI;
import net.osm.discovery.UtcT;
import net.osm.discovery.Entry;
import net.osm.discovery.Chain;

import net.osm.portal.DescriptionBase;
import net.osm.portal.KeyBase;
import net.osm.portal.URIBase;
import net.osm.portal.ChainBase;
import net.osm.portal.SelectionSetBase;
import net.osm.portal.SelectionBase;

/**
* Create a resource description using a jar file as the source.
*/

public class JarDescription extends DescriptionBase {

    public JarDescription( File f ) throws 
			FileNotFoundException, IOException, BadManifestContentException {
	  super( "", "", "" );
	  if( !f.exists() ) throw new FileNotFoundException();
	  if( !f.canRead() ) throw new IOException();
        JarFile file = new JarFile( f );
	  this.setRequiredAttributes( f );
	  this.setOptionalAttributes( file );
	  this.setRemainingAttributes( file );
    }

    private static String getProperty( JarFile file, String property ) throws IOException {
        Attributes a = file.getManifest().getMainAttributes();
	  return a.getValue( property );
    }

    private void setRequiredAttributes( File f ) throws BadManifestContentException, IOException {
        JarFile file = new JarFile( f );
	  String t = getProperty( file, "resource" );
	  if( t != null ) {
		this.resource = new URIBase(t);
	  } else {
	      throw new BadManifestContentException( 
			f, "The required 'resource' attribute is undefined." );
	  }
    }

    private void setOptionalAttributes( JarFile file ) throws IOException {

	  String t = getProperty( file, "title" );
	  if( t != null ) {
		this.title = t;
	  }
	  t = getProperty( file, "description" );
	  if( t != null ) {
		this.description = t;
	  }
	  t = getProperty( file, "key" );
	  if( t != null ) {
		this.value().addKey( (Key) new KeyBase(t) );
	  }
    }

    private void setRemainingAttributes( JarFile file ) throws IOException {

	  // iterate through the default attributes in the manifest and add
	  // any additional attribuites as features of this description

	  Attributes a = file.getManifest().getMainAttributes();
	  Iterator iterator = a.keySet().iterator();

        while( iterator.hasNext() ) {
		Object key = iterator.next();
		String k = key.toString();
		if( 
		    !(
		      	k.equals("Manifest-Version") || 
		      	k.equals("Main-Class") || 
		      	k.equals("title") || 
				k.equals("description") || 
				k.equals("key") ||
				k.equals("resource")
		    ) 
            )
		{
		    this.value().newFeature( (String) key.toString(), 
				a.getValue( (java.util.jar.Attributes.Name) key )); 
		}
	  }

	  // Get any additional Attribiute sets and add these as features.
	  // The convention for converting suppliemnmetaty attribute sets to features is based 
	  // on the supplimentary attribute name.  The character '/' is considered to be a key
	  // name divider.  A attribute name "A/B" will result in the creation of a Key names "A" 
	  // and a sub-Key named "B".

        Manifest m = file.getManifest();
	  Map map = m.getEntries();
 	  Iterator names = map.keySet().iterator(); // names
	  Collection collection = map.values(); // values
 	  Iterator values = collection.iterator();

	  // for all of the named supplimentary attribute sets 

        while( names.hasNext() ) {
		Object name = names.next();
		mapAttributeSet( this.value(), name.toString(), (Attributes) map.get(name));
	  }
    }

    private void mapAttributeSet( ChainBase nvs, String s, Attributes a ) throws IOException {
	  KeyBase k = nvs.newKey( s );
	  Iterator iterator = a.keySet().iterator(); // iterator of names
        while( iterator.hasNext() ) {
		Name name = (Name) iterator.next();
		k.newFeature( name.toString(), a.getValue( name )); 
        }
    }

    /**
    * Create a vector containing a set of resource descriptions based on jar files contained 
    * within a directory passed in under the d argument.  Current implementation assumes that 
    * all files in the directory are jar files and that every jar file has manifest and the 
    * each manifest contains at least an attribute named "title".
    */

    public static Vector createDescriptionsFromJarFiles( File d ) throws 
			IOException, FileNotFoundException, BadManifestContentException{

        if( !d.isDirectory() ) throw new IOException("Path '" + d + "' does not refer to a directory");
	  if( !d.exists() ) throw new IOException("Path '" + d + "' does not exist");
	  Vector v = new Vector();

	  File[] files = d.listFiles();
	  for( int i = 0; i < files.length; i++ ) {
		v.add( new JarDescription( files[i] ));
	  }
        return v;
    }

    /**
    * Create a vector containing a set of resource descriptions based on jar files contained 
    * within a directory passed in under the d argument.  Current implementation assumes that 
    * all files in the directory are jar files and that every jar file has manifest and the 
    * each manifest contains at least an attribute named "title".
    */

    public static SelectionSetBase createSelectionSet( File d ) throws 
			IOException, FileNotFoundException, BadManifestContentException{
        if( !d.isDirectory() ) throw new IOException("Path '" + d + "' does not refer to a directory");
	  if( !d.exists() ) throw new IOException("Path '" + d + "' does not exist");
	  Vector v = new Vector();
	  File[] files = d.listFiles();
	  for( int i = 0; i < files.length; i++ ) {
		v.add( new SelectionBase( new JarDescription( files[i] ), null ));
	  }
        return new SelectionSetBase( (SelectionBase[]) v.toArray( new SelectionBase[0] ));
    }



} // JarDescription
