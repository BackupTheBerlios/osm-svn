/*
 * @(#)ReceiptBase.java
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

import java.util.Date;
import java.util.Random;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Artifact;
import net.osm.discovery.Receipt;
import net.osm.discovery.ReceiptClass;
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.UtcT;
import net.osm.discovery.Key;
import net.osm.discovery.URI;
import net.osm.discovery.Chain;
import net.osm.discovery.VerificationFailure;
import net.osm.discovery.Identifier;


/** 
ValueFactory for Receipt 
*/

public class ReceiptBase extends Receipt implements ValueFactory {
    

    private static final Random random = new Random();


    public ReceiptBase( ){}

    public java.io.Serializable read_value(InputStream is)
    {
        return is.read_value(new ReceiptBase());
    }

    public ReceiptBase( ReceiptClass type, Identifier artifact ){
	  this.id = new IdentifierBase( random ); 
	  this.classification = type;
	  this.artifact = artifact;
	  this.timestamp = new UtcTBase( new Date() );

	  // manifest pending
    }

    // from Verifiable
    
    public boolean verify() throws VerificationFailure {
	  throw new VerificationFailure("method not implemented", this );
    }

    // utilities

    public boolean equals( Object obj ) {
	  try{
		ReceiptBase r = (ReceiptBase) obj;
	      if( !this.id.equals( r.id )) return false;
	      if( !this.classification.equals( r.classification )) return false;
	      if( !this.timestamp.equals( r.timestamp )) return false;
	      if( !this.artifact.equals( r.artifact )) return false;
	      if( !this.manifest.equals( r.manifest )) return false;
	  }catch (Exception e) {
		return false;
	  }
	  return true;
    }


} // ReceiptBase
