/**
 * ArtifactServiceDescr.java
 *
 * Created on 28. 2. 2021, 18:32:13 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.api.ArtifactService;

/**
 * Artifact service description.
 * 
 * @author burgetr
 */
public class ArtifactServiceDescr extends ParametrizedServiceDescr
{
    public String consumes;
    public String produces;
    
    public ArtifactServiceDescr(ArtifactService service)
    {
        super(service);
        if (service.getConsumes() != null)
            consumes = String.valueOf(service.getConsumes());
        if (service.getProduces() != null)
            produces = String.valueOf(service.getProduces());
    }
}
