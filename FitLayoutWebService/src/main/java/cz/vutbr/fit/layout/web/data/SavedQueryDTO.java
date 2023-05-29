/**
 * SavedQueryDTO.java
 *
 * Created on 29. 5. 2023, 13:09:22 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.rdf.SavedQuery;

/**
 * 
 * @author burgetr
 */
public class SavedQueryDTO
{
    long id;
    String title;
    String queryString;
    
    
    public SavedQueryDTO()
    {
    }

    public SavedQueryDTO(SavedQuery src)
    {
        title = src.getTitle();
        queryString = src.getQueryString();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

}
