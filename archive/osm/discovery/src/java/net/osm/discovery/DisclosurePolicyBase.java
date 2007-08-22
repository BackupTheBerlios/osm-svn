/*
 * @(#)DisclosurePolicyBase.java
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
import net.osm.discovery.DisclosurePolicy;
import net.osm.discovery.DisclosurePolicyValue;
import net.osm.discovery.Score;
import net.osm.discovery.URI;

/**
ValueFactory for DisclosurePolicy 
*/

public class DisclosurePolicyBase extends DisclosurePolicy implements ValueFactory 
{

    // constructors

    public DisclosurePolicyBase( ){}

    public DisclosurePolicyBase(DisclosurePolicyValue value ){
	  this.value = value;
    }

    // operations from ValueFactory

    public Serializable read_value( InputStream is ) {
	  return is.read_value( new DisclosurePolicyBase() );
    }

    // utilities

    public boolean equals(Object value){
	  try{
		DisclosurePolicy p = (DisclosurePolicy) value;
            if( this.value.value() != p.value.value() ) return false;
	  } catch (Exception e) {
	      return false;
	  }
	  return true;
    }

    public String toString() {
	  if( this.value == DisclosurePolicyValue.REPLICATION ) {
	      return "REPLICATION";
	  } else if( this.value == DisclosurePolicyValue.REFERRAL ) {
            return "REFERRAL";
	  } else {
            return "UNDEFINED";
        }
    }

} // DisclosurePolicyBase
