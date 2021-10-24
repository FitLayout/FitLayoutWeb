/**
 * SubjectDescriptionResult.java
 *
 * Created on 27. 3. 2021, 20:32:01 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import cz.vutbr.fit.layout.web.data.SelectQueryResult.IriBinding;
import cz.vutbr.fit.layout.web.data.SelectQueryResult.LiteralBinding;
import cz.vutbr.fit.layout.web.data.SelectQueryResult.ResultBinding;

/**
 * Represents the subject descripton as map of properties.
 * @author burgetr
 */
@Schema(name = "SubjectDescriptionResult", description = "Represents the subject descripton as map of properties.")
public class SubjectDescriptionResult
{
    public Map<String, List<ResultBinding>> description;
    
    public SubjectDescriptionResult(List<BindingSet> bindings)
    {
        super();
        description = new HashMap<>();
        for (BindingSet bset : bindings)
        {
            String name = null;
            ResultBinding value = null;
            for (Binding b : bset)
            {
                if ("p".equals(b.getName()))
                {
                    name = b.getValue().stringValue();
                }
                else
                {
                    if (b.getValue() instanceof IRI)
                    {
                        value = new IriBinding((IRI) b.getValue());
                    }
                    else if (b.getValue() instanceof Literal)
                    {
                        value = new LiteralBinding(b.getValue().stringValue(), ((Literal) b.getValue()).getDatatype());
                    }
                }
            }
            if (name != null && value != null)
            {
                List<ResultBinding> curVal = description.get(name);
                if (curVal == null)
                {
                    curVal = new ArrayList<>();
                    description.put(name, curVal);
                }
                curVal.add(value);
            }
        }
    }

}
