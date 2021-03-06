/*
 * Vault.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.vault;

import java.security.Principal;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import sun.security.x509.X509CertInfo;

/**
 * <p>A <code>Vault</code> provides support for the establishment 
 * of propergation of an X509 principal using an underlying JAAS 
 * authentication mechanism.  The <code>Vault</code> assumes that an 
 * authentication profile will be established by the client application using
 * a keystore loging configuration.</p>
 * @see DefaultVault
 */

public interface Vault
{
   /**
    * Return the security subject.
    * @return Subject the security subject.
    */
    public Subject getSubject();

   /**
    * Returns the public X509 certificate path.
    *
    * @return CertPath the certificate path for the principal 
    * @exception CertificateException if the certificate path could not be created
    * @see java.security.cert.CertPath
    */
    public CertPath getCertificatePath() throws CertificateException;

   /**
    * Return the current X500 principal.
    * @return X500Principal a possibly null value corresponding to an 
    *    X500 principal that has been authenticated by the vault.
    */
    public X500Principal getPrincipal() throws CertificateException;

   /**
    * Add a PrincipalListener to the Vault.
    */
    public void addPrincipalListener( PrincipalListener listener );

   /**
    * Removes a PrincipalListener from the Vault.
    */
    public void removePrincipalListener( PrincipalListener listener );

   /**
    * Creation of a new certification request using a self signed certificate 
    * packaged as a PKCS10 request.
    * @return PKSC10 the certificate request
    */
    //PKCS10 createPKCS10();

   /**
    * Login to the vault.
    */
    public void login() throws LoginException;

   /**
    * Logout from the vault.
    */
    public void logout() throws LoginException;

   /**
    * Creation of a new signed certificate.
    * @return X509Certificate the new certificate
    */
    public X509Certificate createCertificate( X509CertInfo info )
    throws VaultException;

   /**
    * Returns the signature algorith that will be used to sign certificates.
    */
    public String getSignatureAlgorithm()
    throws NoSuchAlgorithmException;

}
