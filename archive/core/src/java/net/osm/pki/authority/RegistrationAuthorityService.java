/*
 * RegistrationAuthorityService.java
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

import org.omg.PKIAuthority.RegistrationAuthority;


/**
 * The AuthorityService is an interface facilitating access to a PKI registration authority.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface RegistrationAuthorityService extends Component
{

   /**
    * Returns a CORBA object reference to a Registration Authority
    * @return RegistrationAuthority the PKI Registration Authority
    */
    public RegistrationAuthority getRegistrationAuthority( );

   /**
    * Returns a reference to a RequestCertificateService.
    * @return RequestCertificateService the certification request service
    */
    public RequestCertificateService getRequestCertificateService();

    
}

