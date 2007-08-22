/*
 * RequestCertificateService.java
 *
 * Copyright 2001 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/12/2001
 */
package net.osm.pki.authority;

import org.apache.avalon.framework.component.Component;

import org.omg.PKI.CertificateRequest;
import org.omg.PKIAuthority.RequestCertificateManager;
import org.omg.PKIAuthority.UnsupportedTypeException;
import org.omg.PKIAuthority.UnsupportedEncodingException;
import org.omg.PKIAuthority.MalformedDataException;

/**
 * Manager that provides support for the creation of certificate 
 * request managers that handle client request for new certificates.
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface RequestCertificateService extends Component
{
   /**
    * Creation of a CORBA reference to a RequestCertificateManager 
    * that will handle a supplied certificate request.
    * @param request the incomming certificate request
    * @return RequestCertificateManager manager to handle the certificate request
    * @exception UnsupportedTypeException
    * @exception UnsupportedEncodingException
    * @exception MalformedDataException
    */
    public RequestCertificateManager createRequestCertificateManager( CertificateRequest request )
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException;

   /**
    * Notification to the service that a manager is no longer required and 
    * can be disposed of.
    * @param manager the manager to be disposed
    * @return boolean true if the manager was sucessfully disposed
    */
    public boolean disposeRequestCertificateManager( RequestCertificateManager manager );
    
}



