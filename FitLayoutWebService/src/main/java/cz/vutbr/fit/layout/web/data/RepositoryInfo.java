/**
 * RepositoryInfo.java
 *
 * Created on 15. 4. 2021, 20:41:52 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

/**
 * Basic repository information transferred via the API.
 * 
 * @author burgetr
 */
public class RepositoryInfo
{
    public String id;
    public String description;
    
    public RepositoryInfo()
    {
    }

    public RepositoryInfo(String id, String description)
    {
        this.id = id;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
}
