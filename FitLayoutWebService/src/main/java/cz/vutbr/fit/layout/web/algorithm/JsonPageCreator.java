/**
 * JsonPageCreator.java
 *
 * Created on 31. 10. 2023, 9:41:53 by burgetr
 */
package cz.vutbr.fit.layout.web.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import cz.vutbr.fit.layout.json.impl.JSONBoxTreeBuilder;
import cz.vutbr.fit.layout.json.impl.PageImpl;
import cz.vutbr.fit.layout.json.parser.InputFile;
import cz.vutbr.fit.layout.model.Page;

/**
 * The implementation of creating and storing a new page from its JSON description (e.g. obtained
 * from a puppeteer backend or a client browser extension).
 * 
 * @author burgetr
 */
public class JsonPageCreator extends JSONBoxTreeBuilder
{
    private static final String CREATOR_ID = "FitLayoutWeb.JsonPageCreator";

    public JsonPageCreator()
    {
        super(false, true);
    }

    @Override
    protected InputFile invokeRenderer(URL url)
            throws IOException, InterruptedException
    {
        // This builder is never used for parsing URLs
        return null;
    }
    
    /**
     * Reads JSON page description from an input stream and creates a page instance.
     * 
     * @param is the input stream to parse
     * @param charsetName charset used in the input stream
     * @return A page instance
     * @throws IOException
     */
    public Page renderInputStream(InputStream is, String charsetName) throws IOException
    {
        InputFile inputFile = loadJSON(is, charsetName);
        if (inputFile == null || inputFile.getPage() == null)
            throw new RuntimeException("Parse error: not a page description");
        
        URL url;
        try {
            if (inputFile.page != null && inputFile.page.url != null)
                url = new URL(inputFile.page.url);
            else
                url = new URL("http://url.not.available");
        } catch (MalformedURLException e) {
            url = new URL("http://url.not.available");
        }
        parseInputFile(inputFile, url);
        PageImpl page = (PageImpl) getPage();
        page.setCreator(CREATOR_ID);
        page.setCreatorParams("");
        return page;
    }

}
