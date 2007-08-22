/*
 * CRLInfoBase.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 31 JUL 2001
 */

package net.osm.pki.base;

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.PKI.CRLInfo;
import org.omg.PKI.CRLInfoHelper;

/**
 * Valuetype used to contain the information about a repository provider.
 */
public class CRLInfoBase extends CRLInfo
implements ValueFactory
{
   /** 
    *  Default null constructor
    */ 
    public CRLInfoBase(){}

   /**
    * CRLInfoBase constructor using the supplied initialization values.
    * <p>
    * @param crl_type = the type of CRL
    * @param encoding_type = encoding of the CDL data
    */
    public CRLInfoBase ( int crl_type, int encoding_type )
    {
        this.crl_type = crl_type;
        this.encoding_type = encoding_type;
    }

    //===========================================================
    // internals
    //===========================================================

    public String toString()
    {
        return "CRL Type:" + " type: " + crl_type 
		+ ", encoding: " + encoding_type;
    }

    //===========================================================
    // ValueFactory
    //===========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new CRLInfoBase( ) );
    }
}

