
package net.osm.discovery.util;

import java.io.File;

/**
 * @author  Stephen McConnell
 * @version 1.01 29/05/1999
 */

public class BadManifestContentException extends Exception {

    File file;

    public BadManifestContentException(File file, String message) {
	super(message);
	this.file = file;
    }

    public File getSource() {
	  return this.file;
    }

}
