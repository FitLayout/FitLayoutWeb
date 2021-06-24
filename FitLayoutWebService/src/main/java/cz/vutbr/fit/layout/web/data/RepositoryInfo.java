/**
 * RepositoryInfo.java
 *
 * Created on 15. 4. 2021, 20:41:52 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.Date;

/**
 * Basic repository information transferred via the API.
 * 
 * @author burgetr
 */
public class RepositoryInfo
{
    public String id;
    public String description;
    public String version;
    public String email;
    public String owner;
    public Date createdOn;
    public Date accessedOn;
    public Date expires;
    
    
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

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Date getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn)
    {
        this.createdOn = createdOn;
    }

    public Date getAccessedOn()
    {
        return accessedOn;
    }

    public void setAccessedOn(Date accessedOn)
    {
        this.accessedOn = accessedOn;
    }

    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    
}
