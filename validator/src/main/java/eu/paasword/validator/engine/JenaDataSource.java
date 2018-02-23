/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.validator.engine;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by Chris Petsos
 */
public class JenaDataSource {

    private static String[] neededPrefixesForQueries;

    OntModel model;

    public JenaDataSource(OntModel model)
    {
        this.populatePrefixMappings(model);
        this.model = model;
    }

    public JenaDataSource(String filePath)
    {
        OntModel model = null;

        try {
            InputStream is = new FileInputStream(new File(filePath));
            model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
            model.read(is, null, "TTL");
            this.populatePrefixMappings(model);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.model = model;
    }

    public JenaDataSource(InputStream stream)
    {
        OntModel model = null;

        model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        model.read(stream, null, "TTL");
        this.populatePrefixMappings(model);

        this.model = model;
    }

    private void populatePrefixMappings(OntModel model) {
        Map<String, String> pm = model.getNsPrefixMap();
        neededPrefixesForQueries = new String[pm.keySet().size()];
        int i=0;
        for(String key:pm.keySet())
        {
            neededPrefixesForQueries[i] = key + ": <" + pm.get(key) + ">";
            i++;
        }
    }

    public List<RDFNode> executeQuery(String wherePart) {
        QueryExecution qexec = returnQueryExecObject("SELECT ?var WHERE " + wherePart);
        List<RDFNode> result = new ArrayList<RDFNode>();
        try {
            ResultSet rs = qexec.execSelect();

            while (rs.hasNext()) {
                QuerySolution soln = rs.nextSolution();
                result.add(soln.get("?var"));
            }
        } finally {
            qexec.close();
        }
        return result;
    }

    private QueryExecution returnQueryExecObject(String coreQuery) {
        StringBuffer queryStr = new StringBuffer();
        // Establish Prefixes
        for(String prefix:neededPrefixesForQueries)
        {
            queryStr.append("prefix " + prefix);
        }

        queryStr.append(coreQuery);

        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.create(query, this.model);

        return qexec;
    }

    public Boolean isNodeType(RDFNode node, String type)
    {
        if(!node.isResource())
        {
            return false;
        }

        StmtIterator si =  node.asResource().listProperties();
        while(si.hasNext())
        {
            Statement nextStatement = si.next();
            if(nextStatement.getPredicate().equals(this.createFromNsAndLocalName("rdf", "type")) &&
                    nextStatement.getObject().toString().equals(type))
            {
                return true;
            }
        }

		/*List<RDFNode> nodeTypes = this.executeQuery("{<" + node.toString() + "> a ?var}");
		for(RDFNode nodeType:nodeTypes)
		{
			if(nodeType.toString().equals(type))
			{
				return true;
			}
		}*/

        return false;
    }

    public Resource createFromNsAndLocalName(String nameSpace, String localName)
    {
        return new ResourceImpl(model.getNsPrefixMap().get(nameSpace), localName);
    }

}
