/**
 * ServiceParams.java
 *
 * Created on 2. 9. 2020, 13:52:29 by burgetr
 */

package cz.vutbr.fit.layout.web.data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Definition of service parametres used for its invocation.
 * 
 * @author burgetr
 */
@Schema(name="ServiceParams", description="Service input parametres")
public class ServiceParams
{
    @Schema(description = "ID of the service to invoke", required = true)
    private String serviceId;
    @Schema(description = "IRI of the input artifact if required by the given service", required = false)
    private String parentIri;
    @Schema(description = "Service invocation parametres", type = SchemaType.OBJECT, required = false)
    private Map<String, Object> params;

    public ServiceParams()
    {
        this.serviceId = "";
        this.params = new HashMap<>();
    }
    
    public ServiceParams(String serviceId, Map<String, Object> params)
    {
        this.serviceId = serviceId;
        this.params = params;
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(String serviceId)
    {
        this.serviceId = serviceId;
    }

    public String getParentIri()
    {
        return parentIri;
    }

    public void setParentIri(String parentIri)
    {
        this.parentIri = parentIri;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

}
