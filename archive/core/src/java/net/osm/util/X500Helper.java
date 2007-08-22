
package net.osm.util;



/**
 * Utility class supporting X500 Name components access.
 */

public class X500Helper
{

    private String CN, OU, O, L, ST, C;
    private String name;
    private char nameChar[];

    //============================================================
    // constructor
    //============================================================

    public X500Helper(String s) 
    {
        if (s == null) throw new IllegalArgumentException("Name can’t be null");
        name = s;
    }

    //============================================================
    // constructor
    //============================================================
    
   /**
    * Returns the common name from the X500 name structure.
    * @return  String the common name
    */
    public String getCommonName()
    {
        if (CN == null) CN = parse("CN=");
        return CN;
    }

   /**
    * Returns the organization name from the X500 name structure.
    * @return  String - the organization name
    */
    public String getOrganization()
    {
        if (O == null) O = parse("O=");
        return O;
    }

   /**
    * Returns the organization unit name from the X500 name structure.
    * @return  String - the organization unit name
    */
    public String getOrganizationalUnit()
    {
        if (OU == null) OU = parse("OU=");
        return OU;
    }


   /**
    * Returns a "Locality" name component.
    * @return String - Locality component
    */
    public String getLocality()
    {
        if (L == null) L = parse("L=");
        return L;
    }

   /**
    * Returns the region name from the X500 name structure.
    * @return  String the region name
    */
    public String getState()
    {
        if (ST == null) ST = parse("ST=");
        return ST;
    }

   /**
    * Returns the country code from the X500 name structure.
    * @return  String - the country code
    */
    public String getCountry()
    {
        if (C == null) C = parse("C=");
        return C;
    }

    //============================================================
    // internal
    //============================================================

    // Parse the name for the given target
    private String parse(String target) 
    {
        if (nameChar == null) nameChar = name.toCharArray();
        char targetChar[] = target.toCharArray();

        for (int i = 0; i < nameChar.length; i++) 
        {
            if (nameChar[i] == targetChar[0]) 
	      {
                // Possible match, check further
                boolean found = true;   // At least so far...
                for (int j = 0; j < targetChar.length; j++) 
                {
                    try 
                    {
                        if (nameChar[i + j] != targetChar[j]) 
                        {
                            // No match, continue on...
                            found = false;
                            break;
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe) 
                    {
                        // No match, and nothing left in nameChar
                        return null;
                    }
                }
                if (found) 
                {
                    int firstPos = i + targetChar.length;
                    int lastPos;
                    int endChar;
                    if (nameChar[firstPos] == '\"') endChar = '\"'; else endChar = ',';

                    // The substring will be terminated by a quote if
                    // the substring is quoted (CN="My Name",OU=...)
                    // or by a comma otherwise (L=New York,ST=...)
                    // or by the end of the string
                    // A badly formed substring will throw an
                    // ArrayIndexOutOfBoundsException

                    for (lastPos = firstPos + 1; lastPos < nameChar.length; lastPos++) 
                    {
                        if (nameChar[lastPos] == endChar) break;
                    }

                    // If the lastPos is a quote, then we need to
                    // include it in the string; if it’s a comma then
                    // we don’t

                    return new String(nameChar, firstPos,
                            (endChar == ',' ?
                                lastPos - firstPos :
                                lastPos - firstPos + 1)
                    );
                }
                // else try the next index
            }
        }
        return null;
    }

    public String toString() 
    {
        //
	  // make sure everything is initialized
	  //

        getCommonName();
        getOrganization();
        getOrganizationalUnit();
        getLocality();
        getState();
        getCountry();

        // 
        // return the string
	  //

        return "CN=" + CN + ", " +
               "OU=" + OU + ", " +
               "O=" + O + ", " +
               "L=" + L + ", " +
               "ST=" + ST + ", " +
               "C=" + C;
    }
}
