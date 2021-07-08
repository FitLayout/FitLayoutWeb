/**
 * Output.java
 *
 * Created on 8. 7. 2021, 12:24:19 by burgetr
 */
package cz.vutbr.fit.layout.web;

import java.io.IOException;
import java.io.OutputStream;

import cz.vutbr.fit.layout.io.ArtifactStreamOutput;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class StreamOutput
{

    public static void pageToStream(Page page, OutputStream os, String mimeType) throws IOException
    {
        switch (mimeType)
        {
            case "text/xml":
            case "application/xml":
                ArtifactStreamOutput.outputXML(page, os);
                break;
            case "text/html":
                ArtifactStreamOutput.outputHTML(page, os);
                break;
            case "image/png":
                ArtifactStreamOutput.outputPNG(page, os);
            case "image/pngi":
                ArtifactStreamOutput.outputPNGi(page, os);
        }
        
    }
    
    public static void areaTreeToStream(AreaTree atree, Page page, OutputStream os, String mimeType) throws IOException
    {
        switch (mimeType)
        {
            case "text/xml":
            case "application/xml":
                ArtifactStreamOutput.outputXML(atree, os);
                break;
            case "text/html":
                ArtifactStreamOutput.outputHTML(atree, page, os);
                break;
            case "image/png":
                ArtifactStreamOutput.outputPNG(atree, page, os);
                break;
            case "image/pngi":
                ArtifactStreamOutput.outputPNGi(atree, page, os);
                break;
        }
    }
    
}
