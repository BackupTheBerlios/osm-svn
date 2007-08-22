/*
 * X500NameWrapper.java
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

import java.io.IOException;

/**
 * X500NameWrapper provides implementation support for an X500Name
 * conformant with the net.osm.pki.base.X500Name interface.
 */
public class X500NameWrapper implements net.osm.pki.base.X500Name
{

    private sun.security.x509.X500Name name;

    public X500NameWrapper( sun.security.x509.X500Name name )
    {
       this.name = name;
    }

   /**
    * Returns a "Country" component.
    * @return String - Country component
    * @exception IOException
    */
    public String getCountry() throws IOException
    {
        return name.getCountry();
    }

   /**
    * Returns an "Organization" name component.
    * @return String - Organization component
    * @exception IOException
    */
    public String getOrganization() throws IOException
    {
        return name.getOrganization();
    }

   /**
    * Returns an "Organizational Unit" name component.
    * @return String - Organizational Unit component
    * @exception IOException
    */
    public String getOrganizationalUnit() throws IOException
    {
        return name.getOrganizationalUnit();
    }

   /**
    * Returns a "Common Name" component.
    * @return String - Common Name component
    * @exception IOException
    */
    public String getCommonName() throws IOException
    {
        return name.getCommonName();
    }

   /**
    * Returns a "Locality" name component.
    * @return String - Locality component
    * @exception IOException
    */
    public String getLocality() throws IOException
    {
        return name.getLocality();
    }

   /**
    * Returns a "State" name component.
    * @return String - State component
    * @exception IOException
    */
    public String getState() throws IOException
    {
        return name.getState();
    }

   /**
    * Returns a string representation of the X.500 distinguished name using the format defined in RFC 1779.
    * @return String - RFC 1779 name
    */
    public String getName()
    {
        return name.getName();
    }

   /**
    * Gets the name in DER-encoded form.
    * @exception IOException
    */
    public byte[] getEncoded() throws IOException
    {
        return name.getEncoded();
    }

   /**
    * Retuirns the name as a string.
    * @return String - the name
    */
    public String toString()
    {
        return name.toString();
    }

   /**
    * Return the provider instance of the X500 name.
    */
    public Object getNative()
    {
        return name;
    }

}

