/*
 * @(#)SelectionBase.java
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

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Selection;
import net.osm.discovery.Artifact;
import net.osm.discovery.Score;


public class SelectionBase extends Selection implements ValueFactory {
    
    public SelectionBase( ){}

    public SelectionBase(Artifact artifact, Score score ){
	  this.artifact = artifact;
	  this.ranking = score;
    }

    public java.io.Serializable read_value( InputStream is )
    {
	  return is.read_value( new SelectionBase() );
    }

    public String toString() {
        return "SelectionBase[" + this.artifact + ";" + this.ranking + "]";
    }

} // SelectionBase
