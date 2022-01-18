/**
 * TagInfo.java
 *
 * Created on 4. 1. 2022, 18:17:23 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import cz.vutbr.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
@Schema(name="TagInfo", description="Tag information")
public class TagInfo
{
    private String iri;
    private String type;
    private String name;
    
    public TagInfo()
    {
    }
    
    public TagInfo(Tag src)
    {
        this.iri = String.valueOf(src.getIri());
        this.type = src.getType();
        this.name = src.getName();
    }

    public String getIri()
    {
        return iri;
    }

    public void setIri(String iri)
    {
        this.iri = iri;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
