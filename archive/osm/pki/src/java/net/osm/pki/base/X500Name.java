

package net.osm.pki.base;

import java.security.Principal;
import java.io.IOException;

/**
 * X.500 names are used to identify entities, such as those which are identified by X.509 certificates. 
 */

public interface X500Name extends Principal
{

   /**
    * Returns a "Country" component.
    * @return String - Country component
    * @exception IOException
    */
    public String getCountry() throws IOException;

   /**
    * Returns an "Organization" name component.
    * @return String - Organization component
    * @exception IOException
    */
    public String getOrganization() throws IOException;

   /**
    * Returns an "Organizational Unit" name component.
    * @return String - Organizational Unit component
    * @exception IOException
    */
    public String getOrganizationalUnit() throws IOException;

   /**
    * Returns a "Common Name" component.
    * @return String - Common Name component
    * @exception IOException
    */
    public String getCommonName() throws IOException;

   /**
    * Returns a "Locality" name component.
    * @return String - Locality component
    * @exception IOException
    */
    public String getLocality() throws IOException;

   /**
    * Returns a "State" name component.
    * @return String - State component
    * @exception IOException
    */
    public String getState() throws IOException;

   /**
    * Returns a string representation of the X.500 distinguished name using the format defined in RFC 1779.
    * @return String - RFC 1779 name
    */
    public String getName();

   /**
    * Gets the name in DER-encoded form.
    * @exception IOException
    */
    public byte[] getEncoded() throws IOException;

   /**
    * Return the provider instance of the X500 name.
    */
    public Object getNative();
}
