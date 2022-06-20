/**
 * SelectQueryResult.java
 *
 * Created on 24. 3. 2021, 17:00:03 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A result value representing the result of a SPARQL query.
 * 
 * @author burgetr
 */
@Schema(name = "SelectQueryResult", description = "A result of a SPARQL SELECT query")
public class SelectQueryResult
{
    public ResultHeader head;
    public ResultBody results;
    
    public SelectQueryResult(List<BindingSet> bindings)
    {
        Set<String> names = new HashSet<>();
        results = new ResultBody();
        for (BindingSet bset : bindings)
        {
            names.addAll(bset.getBindingNames());
            results.add(bset);
        }
        head = new ResultHeader(names);
    }
    
    //================================================================================
    
    @Schema(name = "ResultHeader", description = "Query result header (column names)")
    public static class ResultHeader
    {
        public List<String> vars;

        public ResultHeader()
        {
            this.vars = null;
        }
        
        public ResultHeader(Collection<String> vars)
        {
            this.vars = new ArrayList<>(vars);
        }
    }
    
    @Schema(name = "ResultBody", description = "Query result body (bindings)")
    public static class ResultBody
    {
        public List<Map<String, ResultBinding>> bindings;
        
        public ResultBody()
        {
            bindings = new ArrayList<>();
        }
        
        public void add(BindingSet bset)
        {
            Map<String, ResultBinding> map = new HashMap<>();
            for (Binding b : bset)
            {
                if (b.getValue() instanceof IRI)
                {
                    map.put(b.getName(), new IriBinding((IRI) b.getValue()));
                }
                else if (b.getValue() instanceof BNode)
                {
                    map.put(b.getName(), new BNodeBinding((BNode) b.getValue()));
                }
                else if (b.getValue() instanceof Literal)
                {
                    map.put(b.getName(), new LiteralBinding(b.getValue().stringValue(), ((Literal) b.getValue()).getDatatype()));
                }
            }
            bindings.add(map);
        }
    }
    
    @Schema(name = "ResultBinding", description = "Assigned result value")
    public static abstract class ResultBinding
    {
        @Schema(description = "Value type {iri, literal}")
        public String type;
        @Schema(description = "The value")
        public String value; 
    }
    
    public static class IriBinding extends ResultBinding
    {
        public IriBinding(IRI iri)
        {
            type = "uri";
            value = String.valueOf(iri);
        }
    }
    
    public static class BNodeBinding extends ResultBinding
    {
        public BNodeBinding(BNode bnode)
        {
            type = "bnode";
            value = String.valueOf(bnode);
        }
    }
    
    public static class LiteralBinding extends ResultBinding
    {
        public String datatype;
        
        public LiteralBinding(String stringval, IRI dataTypeIri)
        {
            type = "literal";
            value = stringval;
            datatype = String.valueOf(dataTypeIri);
        }
    }
    
    public static class NullBinding extends ResultBinding
    {
        public NullBinding()
        {
            type = "unknown";
            value = null;
        }
    }
    
}
