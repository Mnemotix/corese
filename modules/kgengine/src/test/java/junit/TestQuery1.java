package junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.storage.api.IStorage;
import fr.inria.acacia.corese.storage.api.Parameters;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.StatListener;
import fr.inria.edelweiss.kgraph.core.EdgeFactory;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryGraph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.JSONLDFormat;
import fr.inria.edelweiss.kgtool.transform.Loader;
import fr.inria.edelweiss.kgtool.util.GraphStoreInit;
import fr.inria.edelweiss.kgtool.util.QueryManager;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;


//import static junit.TestUnit.root;
import static org.junit.Assert.assertEquals;

/**
 *
 *
 */
public class TestQuery1 {

    static String data = TestQuery1.class.getClassLoader().getResource("data").getPath() + "/";
    static String QUERY = TestQuery1.class.getClassLoader().getResource("query").getPath() + "/";
    static String text = TestQuery1.class.getClassLoader().getResource("text").getPath() + "/";
    private static final String FOAF = "http://xmlns.com/foaf/0.1/";
    private static final String SPIN_PREF = "prefix sp: <" + NSManager.SPIN + ">\n";
    private static final String FOAF_PREF = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n";
    private static final String SQL_PREF = "prefix sql: <http://ns.inria.fr/ast/sql#>\n";
    static Graph graph;

    @BeforeClass
    static public void init() {
        //Query.STD_PLAN = Query.PLAN_RULE_BASED;
        
        Load.setDefaultGraphValue(true);
        //Graph.DEFAULT_GRAPH_MODE = Graph.DEFAULT_GRAPH;
        
        QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
        //QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        graph = Graph.create(true);
        //graph.setOptimize(true);

        Load ld = Load.create(graph);
        try {
            init(graph, ld);
        } catch (LoadException ex) {
            Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Option.isOption = false;
        //QueryProcess.setJoin(true);
        //fr.inria.edelweiss.kgenv.parser.Transformer.ISBGP = !true;
        //QueryProcess.setPlanDefault(Query.QP_HEURISTICS_BASED); 
    }
    
    @AfterClass
    static public void finish(){
       EdgeFactory.trace();
    }

    static void init(Graph g, Load ld) throws LoadException {
        ld.parse(data + "comma/comma.rdfs");
        ld.parse(data + "comma/model.rdf");
        ld.parseDir(data + "comma/data");
    }

    Graph getGraph() {
        return graph;
    }

    public static Graph graph() {
        Graph graph = Graph.create(true);
        graph.setOptimize(true);

        Load ld = Load.create(graph);
        try {
            ld.parse(data + "comma/comma.rdfs");
            ld.parse(data + "comma/model.rdf");
            ld.parseDir(data + "comma/data");
        } catch (LoadException ex) {
            Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
        }

        return graph;
    }
    
     @Test
    public void testJSON() throws EngineException, LoadException {
        String t =
                "template  {  st:apply-templates-with(st:json)}"
                + "where {}";

        Graph g = Graph.create(); //createGraph();
        Load ld = Load.create(g);
        ld.parse(data + "jsonld/test.jsonld");

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t);

        String json = map.getTemplateStringResult();
        assertEquals(true, (json.length() <= 1200 && json.length() >= 1000));

        Graph gg = Graph.create();
        Load ll = Load.create(gg);
        ll.loadString(json, Load.JSONLD_FORMAT);

        assertEquals(g.size(), gg.size());

    }
     
    @Test
       public void testDataStore() throws EngineException{
           Graph g = Graph.create(false);
           QueryProcess exec = QueryProcess.create(g);
           
           String init = "insert data {"
                   + "graph us:g1 {"
                   + "us:John foaf:knows us:Jack, us:Jules "
                   + "foaf:knows rdfs:domain foaf:Person"
                   + "} "
                   + "graph us:g2 {"
                   + "us:John foaf:knows us:Jack "
                   + "foaf:Person rdfs:subClassOf foaf:Human "
                   + "foaf:Human rdfs:subClassOf foaf:Humanoid "
                   + "}"                  
                   + "}";
                         
           exec.query(init);
                    
           ArrayList<Node> list = new ArrayList<Node>();
           ArrayList<Node> list2 = new ArrayList<Node>();
           
           Node g1 = g.getGraphNode(NSManager.USER+"g1");
           Node g2 = g.getGraphNode(NSManager.USER+"g2");
           list.add(g1);
           list2.add(g1);
           list2.add(g2);
           
           Node p = g.getPropertyNode(NSManager.FOAF+"knows");
           Node n = g.getNode(NSManager.USER+"John");
           Node n1 = g.getNode(NSManager.USER+"Jack");
           
           assertEquals(2, count(g.getDefault().iterate(p)));
           assertEquals(3, count(g.getNamed().iterate(p)));
           assertEquals(2, count(g.getDefault().iterate(n, 0)));
           assertEquals(3, count(g.getNamed().iterate(n, 0)));                               
           assertEquals(5, count(g.getDefault().iterate()));                           
           assertEquals(3, count(g.getDefault().from(list).iterate()));       
           assertEquals(5, count(g.getDefault().from(list2).iterate()));                 
           assertEquals(6, count(g.getNamed().iterate()));         
           assertEquals(3, count(g.getNamed().from(list).iterate()));                   
           assertEquals(6, count(g.getNamed().from(list2).iterate()));                           
           assertEquals(2, count(g.getDefault().from(list2).iterate(p, n, 0)));                           
           assertEquals(3, count(g.getNamed().from(list2).iterate(p, n, 0)));                
           assertEquals(1, count(g.getDefault().from(list2).iterate(p, n1, 1)));                  
           assertEquals(2, count(g.getNamed().from(list2).iterate(p, n1, 1)));
           assertEquals(3, count(g.getNamed().minus(g1).iterate()));
           assertEquals(3, count(g.getDefault().minus(g1).iterate()));  
           assertEquals(0, count(g.getDefault().minus(list2).iterate()));
           
           assertEquals(5, count(g.getDefault().iterate().filter(ExprType.ISURI)));          
           assertEquals(0, count(g.getDefault().iterate().filter(ExprType.ISBLANK)));           
           assertEquals(5, count(g.getDefault().iterate().filter(ExprType.ISBLANK).not()));
      }
    
    int count(Iterable<Entity> it){
          int c = 0;
          for (Entity ent : it){
              if (ent != null){
                  //System.out.println(ent);
                  c++;
              }
          }
          //System.out.println("");
          return c;
      }
    
   @Test  
    public void testrepl() throws EngineException{
        String q = "select ('o' as ?pat) ('oo' as ?rep) (replace('aobooc', ?pat, ?rep) as ?res) where {}";
        Graph g = Graph.create(); 
        QueryProcess exec = QueryProcess.create(g);
        
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals("aoobooooc", dt.getLabel());
    } 
   
     
  @Test   
    public void testrdfxml() throws LoadException, IOException{
         Graph g = Graph.create(); 
         Load ld = Load.create(g);
         ld.parse("/home/corby/AAServer/data/primer.owl");
         g.init();
         
         Transformer t = Transformer.create(g, Transformer.RDFXML);
         t.write("/home/corby/tmp.rdf");
         
         Graph g1 = Graph.create(); 
         Load ld1 = Load.create(g1);
         ld1.parse("/home/corby/tmp.rdf");
         g1.init();
         
         
         Transformer t2 = Transformer.create(g1, Transformer.TURTLE);
         t2.write("/home/corby/tmp.ttl");
         
         Graph g2 = Graph.create(); 
         Load ld2 = Load.create(g2);
         ld2.parse("/home/corby/tmp.ttl");
         g2.init();
         
         System.out.println(g.compare(g2));
         
         assertEquals(353, g.size());        
         assertEquals(g.size(), g1.size());
         // missing: _:b a rdf:List
         assertEquals(307, g2.size());
         
         //System.out.println(g.compare(g1, false, true, true));
        
    }
     
  
  
     @Test
     public void testIndex() throws EngineException{
      String q = "select * where {"
              + "bind (1 as ?x) "
              + "values (?x ?y) {"
              + "(2 2) (1 1)"
              + "}"
              + "}";
      
      Graph g = Graph.create();
      QueryProcess exec = QueryProcess.create(g);
      Mappings map = exec.query(q);
      assertEquals(1, map.size());
  }
  
    
    
     @Test
    public void testAGG22() throws EngineException {
        String q =  "select * (max(?c) as ?mc) where {"
                + "select (count(*) as ?c) (max(?y) as ?m) where {?x ?p ?y}"
                + "}";
              
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);       
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?c");
        IDatatype dt2 = (IDatatype) map.getValue("?mc");
        assertEquals(0, dt.intValue());
        assertEquals(0, dt2.intValue());
    }
    
    
     
              
    
    @Test
    public void testContext4() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "template {} "
                + "where {"
                + "bind (st:export(st:test, 'test') as ?t)"
                + "bind (st:atw('" + data + "junit/sttl/test') as ?tt)" 
                + "}";

        Mappings map = exec.query(q);
        Context c  = (Context) map.getContext();
        IDatatype dt = c.getName("test");
        assertEquals("test", dt.stringValue());
        Transformer t = (Transformer) map.getQuery().getTransformer();
        IDatatype res = t.getContext().getName("res");
        assertEquals(10, res.intValue());
    }
    
    
        @Test
    public void testContext33() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * "
                + "where {"
                + "bind (st:atw('" + data + "junit/sttl/test') as ?tt)" 
                + "}";

        Context c = new Context().exportName("test", DatatypeMap.newInstance("test"));
        Mappings map = exec.query(q, c);
        IDatatype dt = (IDatatype) map.getValue(("?tt"));
        assertEquals("test", dt.stringValue());
       
    }
    
    
      @Test
    public void testContext3() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * "
                + "where {"
                + "bind (st:export(st:test, 'test') as ?t)"
                + "bind (st:atw('" + data + "junit/sttl/test') as ?tt)" 
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue(("?tt"));
        assertEquals("test", dt.stringValue());
       
    }
    
    
    
    
    @Test
    public void testContext2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "template { st:get(st:test) }"
                + " where {"
                + "select * where {"
                + "bind (us:test() as ?t)"
                + "}"
                + "}"
                + "function us:test(){"
                + "st:set(st:test, 10)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals("10", map.getTemplateStringResult());
        Context c = (Context)  map.getContext();      
        IDatatype val = c.getName("test");
        assertEquals(10, val.intValue());
        assertEquals(true, map.getQuery().getTransformer() == null);    
    }
    
    @Test
    public void testContext() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * (st:get(st:test) as ?tt) where {"
                + "select * where {"
                + "bind (us:test() as ?t)"
                + "}"
                + "}"
                + "function us:test(){"
                + "st:set(st:test, 10)"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?tt");
        assertEquals(10, dt.intValue());
        Context c = (Context)  map.getContext();      
        IDatatype val = c.getName("test");
        assertEquals(10, val.intValue());    
    }
    
   
    

    
    
    // loop return concat of results of body of loop
     @Test
    public void testGLoop() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?t)"
                + "where {}"
                + ""
                + "function us:test(){"
                + "loop ((?s, ?p, ?o) in construct {us:John rdfs:label 'Jon', 'Jim' } where {}){"
                + "?o"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype)map.getValue("?t");
        assertEquals("JonJim", dt.stringValue());
    }
    
    
      @Test
     public void testMetaBind1() throws LoadException, EngineException {
        Graph g = createGraph();       
        QueryProcess exec = QueryProcess.create(g);
        String q = "@bind kg:values " +
                 "select  * where {"
                + "bind (<http://fr.dbpedia.org/resource/Auguste> as ?x) "
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?x ?p ?y"
                + "} limit 10}"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(10, map.size());
        ASTQuery ast = (ASTQuery) map.getAST();
        assertEquals(true, ast.toString().contains("values ("));
     }
     
     @Test
     public void testMetaBind2() throws LoadException, EngineException {
        Graph g = createGraph();       
        QueryProcess exec = QueryProcess.create(g);
        String q = "@bind kg:filter " +
                 "select  * where {"
                + "bind (<http://fr.dbpedia.org/resource/Auguste> as ?x) "
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?x ?p ?y"
                + "} limit 10}"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(10, map.size());
        ASTQuery ast = (ASTQuery) map.getAST();
        assertEquals(true, ast.toString().contains("filter ("));
     }
    

    @Test
    public void testIO() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        try {            
            ld.parse(data + "junit/data/test.ttl");
            ld.parse(data + "junit/data/test.ttl", "http://example.org/");
            
            ld.parse(data + "junit/data/test.rdf");
            ld.parse(data + "junit/data/test.rdf", "http://example.org/");
            
            ld.parse(data + "junit/data/test.xml", Load.RDFXML_FORMAT);

        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(5, g.size());
    }
     
     
      @Test
    public void testIO2() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        try {
            ld.parseDir(data + "junit/data");                      
            ld.parseDir(data + "junit/data", "http://example.org/");                      
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(4, g.size());
    }
      
    
    @Test
    public void testUnion5() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = 
                "select (us:main() as ?m) where {}"
                
                + "function us:main(){"
                + "let (?list = xt:list(us:test1, us:test2)){"
                + "apply(xt:union, maplist(rq:funcall, ?list))"
                + "}"
                + "}"
                
                
                + "function us:test1(){"
                + "let (?m = select *  where { ?x foaf:knows ?y}){"
                + "?m}"
                + "}"
                
                + "function us:test2(){"
                + "let (?m = select *  where { ?z foaf:knows ?t}){"
                + "?m}"
                + "}"
                
                ;

        exec1.query(i1);

        Mappings map  = exec1.query(q);
        IDatatype dt = (IDatatype) map.getValue("?m");
        Mappings m = dt.getPointerObject().getMappings();
        assertEquals(4, m.size());
    }
    
    @Test
    public void testUnnestSelectCons() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?x ?y ?path) {unnest(us:test())}"
                + "graph ?path { ?a ?p ?b }"
                + "}"
                
                + "function us:test(){"
                + "let (?m = select * where {"
                + "bind (us:graph() as ?g)"
                + "graph ?g {?x foaf:knows+ :: ?path  ?y}"
                + "})"
                + "{?m}"
                + "}"
                
                + "function us:graph(){"
                + "let (?g = construct  {"
                + "us:John foaf:knows us:Jack "
                + "us:Jack foaf:knows us:Jim} where {})"
                + "{?g}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(4, map.size());

    }

    @Test
    public void testUnnestGraph() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        //QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select ?s ?p ?o  where {"
                + "bind (us:test() as ?g)"
                + "values (?s ?p ?o) {unnest(?g)}"
                + "graph ?g {?s ?p ?o}"
                + "}"
                + "function us:test(){"
                + "let (?g = construct { us:John rdfs:label 'John', 'Johnny' } where {})"
                + "{?g}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(2, map.size());

    }

    @Test
    public void testUnnestSelect() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        //QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?x ?y) {unnest(us:test())}"
                + "}"
                + "function us:test(){"
                + "let (?m = select * where { "
                + "values ?x {unnest(xt:iota(2))}  "
                + "values ?z {unnest(xt:iota(1))}  "
                + "values ?y {unnest(xt:iota(2))}})"
                + "{?m}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(4, map.size());
        IDatatype dt = (IDatatype) map.getValue("?x");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testUnnestSelect2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        //QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?y ?x) {unnest(us:test())}"
                + "}"
                + "function us:test(){"
                + "let (?m = select * where { "
                + "values ?x {unnest(xt:iota(2))}  "
                + "values ?z {unnest(xt:iota(1))}  "
                + "values ?y {unnest(xt:iota(2))}})"
                + "{?m}"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(4, map.size());

    }

    @Test
    public void testUnnestContext() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * where {"
                + "bind (st:set(st:test, 'test') as ?test) "
                + "values (?key ?val) { unnest(xt:context()) } "
                + "}"
                + "function xt:start(?q){ st:set(st:count, 0)  } ";

        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testEventCall() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * where {"
                + "?x ?p ?y"
                + "}"
                + "function xt:produce(?t){xt:list(?t)} "
                + "function xt:start(?q){ st:set(st:count, 0) ; us:count() } "
                + "function xt:candidate(?q, ?t, ?b){us:count() ; ?b }"
                + "function xt:result(?q, ?m){us:count()}"
                + "function xt:solution(?q, ?m){us:count()}"
                + "function xt:finish(?q, ?m){us:count()}"
                + "function us:count(){st:set(st:count, 1 + st:get(st:count))}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        Transformer t = (Transformer) map.getQuery().getTransformer();
        Context c = (Context) map.getContext();
        IDatatype dt = c.getName("count");
        assertEquals(5, dt.intValue());
    }

    @Test
    public void testFunUpdate() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert { us:John foaf:name ?n } "
                + "where { bind (us:name(us:John) as ?n) }"
                + "function us:name(?n){ 'John' }";

        exec.query(i);
        Entity e = g.getEdges().iterator().next();
        IDatatype dt = (IDatatype) e.getNode(1).getValue();
        assertEquals("John", dt.stringValue());
    }

    @Test
    public void testNSMUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "prefix ex: <htp://example.org/>"
                + "insert data { "
                + "us:John rdfs:label 'John'"
                + "ex:John rdfs:label 'John'"
                + "}";

        String q = "prefix ex: <htp://example.org/>"
                + "select * "
                + "where {"
                + "?x ?p ?y "
                + "filter mapany(us:test, ?x, st:prefix())"
                + "}"
                + "function us:test(?uri, ?def){"
                + "let ((?p, ?n) = ?def){"
                + "strstarts(?uri, ?n)"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testQueryUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i = "insert data { us:John rdfs:label 'John'}";

        String q = "prefix ex: <htp://example.org/>"
                + "select * "
                + "where {"
                + "?x ?p ?y optional {?x ?p ?p }"
                + "values ?t { unnest(xt:query()) }"
                + "values ?e { unnest(?t) }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(6, map.size());
    }

    @Test
    public void testGraphUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i = "insert data { us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2' }";

        String q = "select * where {"
                + "values (?s ?p ?o) { unnest(us:define()) }"
                + "values (?s ?p ?o ?g) { unnest(us:define()) }"
                + "?s ?p ?o "
                + "}"
                + "function us:define(){"
                + "let (?g = construct {us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2'} where {})"
                + "{ "
                + "?g }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testUnnestNSM() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?p ?n) {unnest(st:prefix())}"
                + "}"
                + "function us:test(){"
                + "for ((?p, ?n) in st:prefix()){"
                + "xt:display(?p, ?n)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        System.out.println("*****************************************");
        System.out.println(map);
        assertEquals(2, map.size()); // there is also a global prefix c:
//        IDatatype p = (IDatatype) map.getValue("?p");
//        IDatatype n = (IDatatype) map.getValue("?n");
//        assertEquals("ex", p.stringValue());
//        assertEquals("htp://example.org/", n.stringValue());
    }

    @Test
    public void testExtNode() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i = "insert data { us:prop rdfs:label 'prop' }";

        String q = "select * where {"
                + "bind (us:define() as ?g)"
                + "?p rdfs:label ?l "
                + "graph ?g {"
                + "?p rdfs:label ?ll"
                + "}"
                + "}"
                + "function us:define(){"
                + "let (?g = construct {us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2'} where {})"
                + "{ ?g }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testUnion4() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let ("
                + "?m1 = select *  where { ?x foaf:knows ?y},"
                + "?m2 = select *  where { ?x foaf:knows ?y}){"
                + "xt:union(?m1, ?m2)"
                + "}"
                + "}";

        exec1.query(i1);



        IDatatype dt = exec1.eval(q);

        Mappings m = dt.getPointerObject().getMappings();

        assertEquals(4, m.size());

    }

    @Test
    public void testUnion3() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "select * where {"
                + "?x foaf:knows ?y"
                + "}";

        exec1.query(i1);

        Mappings m1 = exec1.query(q);
        Mappings m2 = exec1.query(q);
        Mappings m = m1.union(m2);


        assertEquals(4, m.size());

    }

    @Test
    public void testUnion2() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let (?g = us:union(),"
                + "?m = select * where { graph ?g {?x foaf:knows+ ?y}})"
                + "{ ?m }"
                + "}"
                + "function us:union(){"
                + "let ("
                + "?g1 = construct {us:John foaf:knows us:Jim}  where {},"
                + "?g2 = construct {us:Jim  foaf:knows us:Jack} where {}){"
                + "xt:union(?g1, ?g2)"
                + "}"
                + "}";

        IDatatype dt = exec.eval(q);
        Mappings map = dt.getPointerObject().getMappings();
        assertEquals(3, map.size());
    }

    @Test
    public void testUnion() throws EngineException {
        Graph g1 = Graph.create();
        Graph g2 = Graph.create();

        QueryProcess exec1 = QueryProcess.create(g1);
        QueryProcess exec2 = QueryProcess.create(g2);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "}";

        String i2 = "insert data {"
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "select * where {"
                + "?x foaf:knows+ ?y"
                + "}";

        exec1.query(i1);
        exec2.query(i2);

        Graph g = g1.union(g2);

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);

        assertEquals(3, map.size());

    }
    
     @Test
    public void testRelax6() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John'@en ; foaf:age 11 "
                + "}";
        exec.query(i);

        String q = "@relax "
                + "select * (sim() as ?s) where {"
                + "us:John foaf:name 'Jon' ; foaf:age 11 "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
       
    }
    
    
    @Test
    public void testRelax5() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:uri "
                + "select * where {"
                + "us:Jim foaf:name 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }
    
    @Test
    public void testRelax4() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax * "
                + "select * where {"
                + "?x rdfs:label 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }
    
      @Test
    public void testRelax3() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:property "
                + "select * where {"
                + "?x rdfs:label 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        
    }
    
     @Test
    public void testRelax2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John rdfs:label 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:literal "
                + "select * where {"
                + "?x rdfs:label 'Jon' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }
       
    
     @Test
    public void testRelax1() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John rdfs:label 'John' "
                + "}";
        exec.query(i);

        String q = "@relax "
                + "select * where {"
                + "?x rdfs:label 'Jon' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testApprox() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {"
                + "us:John us:name 'John' "
                + "us:Jack us:name 'Jack' "
                + "us:Jim  us:name 'Jim' "
                + "}";

        String q = "@relax * "
                + "select * (sim() as ?s) where {"
                + "?x xt:name 'Jon'"
                + "}"
                + "order by desc(?s)";

        exec.query(i);

        Mappings map = exec.query(q);
        assertEquals(3, map.size());
        IDatatype dt = (IDatatype) map.getValue("?x");
        assertEquals(NSManager.USER + "John", dt.stringValue());
    }

    @Test
    public void testSubqueryFun() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { us:John rdfs:label 'John' }"
                + "@export package {"
                + "function us:test(){"
                + "let ((?y) = select * where {?x ?p ?y}){?y}"
                + "}"
                + "}";

        String q = "select * (us:test() as ?r) (us:foo() as ?s) "
                + "where {"
                + "select * where {select * (us:test() as ?t) (us:foo() as ?f) "
                + "where {}}"
                + "}"
                + "function us:foo(){"
                + "let ((?b) = select * where {?a ?q ?b}){ us:test() }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);

        assertEquals("John", strValue(map, "?r"));
        assertEquals("John", strValue(map, "?s"));
        assertEquals("John", strValue(map, "?t"));
        assertEquals("John", strValue(map, "?f"));

    }

    String strValue(Mappings m, String v) {
        return ((IDatatype) m.get(0).getValue(v)).stringValue();
    }

    @Test
    public void testImport() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q =
                "prefix ex: <http://ns.inria.fr/sparql-extension/aggregate#>"
                + "@debug "
                + "select * "
                + "(ex:median(xt:iota(5)) as ?m)"
                + "(ex:sigma(xt:iota(5)) as ?s)"
                + ""
                + "where {}";
        exec.setLoadFunction(true);
        Mappings map = exec.query(q);
        IDatatype dm = (IDatatype) map.getValue("?m");
        IDatatype ds = (IDatatype) map.getValue("?s");
        assertEquals(3, dm.intValue());
        assertEquals(1.41421, ds.doubleValue(), 0.01);
        //1.41421
    }

    //@Test
    public void testServAnnot() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String qq = "select "
                + "(us:foo() as ?f)"
                + "(us:bar() as ?b)"
                + "where {}"
                + "@service <http://fr.dbpedia.org/sparql>"
                + "package {"
                + "@service <http://dbpedia.org/sparql>"
                + "function us:foo(){"
                + "let (?g = construct  where {?x rdfs:label ?l} limit 10)"
                + "{?g}}"
                + "function us:bar(){"
                + "let (?m = select *  where {?x rdfs:label ?l} limit 10)"
                + "{?m}}"
                + "}";

        Mappings map = exec.query(qq);
        IDatatype dg = (IDatatype) map.getValue("?f");
        IDatatype dm = (IDatatype) map.getValue("?b");
        Graph gg = (Graph) dg.getObject();
        Mappings mm = (Mappings) dm.getObject();
        assertEquals(10, gg.size());
        assertEquals(10, mm.size());
    }

    @Test
    public void testCustom() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "function xt:main(){"
                + "cs:test(10) "
                + "}";

        IDatatype dt = exec.eval(q);
        assertEquals(10, dt.intValue());
    }

    @Test
    public void testAnnot() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String i = "@public {"
                + "function us:foo(){"
                + "us:bar()"
                + "}"
                + "@debug function us:bar(){"
                + "10"
                + "}"
                + "}";

        String q = "function xt:main(){"
                + "us:foo()"
                + "}";

        exec.query(i);

        IDatatype dt = exec.eval(q);
        assertEquals(10, dt.intValue());
    }

    //@Test
    public void testService() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q1 = "@service <http://fr.dbpedia.org/sparql>"
                + "select * where {?x ?p ?y } limit 10";
        Mappings m1 = exec.query(q1);
        assertEquals(10, m1.size());

        String q2 = "@service <http://fr.dbpedia.org/sparql>"
                + "construct where {?x ?p ?y } limit 10";
        Mappings m2 = exec.query(q2);
        Graph g2 = (Graph) m2.getGraph();
        assertEquals(10, g2.size());
    }

    @Test
    public void testSPARQLfun() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(rq:isURI(us:test) as ?uri)"
                + "(rq:isBlank(us:test) as ?bn)"
                + "(mapany(rq:strstarts, us:test, xt:list(xt:, st:, us:)) as ?st)"
                + "where {}";
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?uri");
        IDatatype bn = (IDatatype) map.getValue("?bn");
        IDatatype st = (IDatatype) map.getValue("?st");
        assertEquals(true, dt.booleanValue());
        assertEquals(false, bn.booleanValue());
        assertEquals(true, st.booleanValue());
    }

    @Test
    public void testUnbound() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test() as ?res)"
                + "where {}"
                + "function us:test(){"
                + "let ((?x) = select * where { optional { ?x rdf:value ?y }}){"
                + "if (bound(?x), true, false)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(false, dt.booleanValue());
        //System.out.println(map);
    }

    //@Test 
    public void testLetService() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        String q = "prefix r: <http://fr.dbpedia.org/resource/>"
                + "select (us:dbpedia(?x, rdfs:label) as ?t)"
                + "where {"
                + "}"
                + "values ?x {r:Auguste}"
                + "function us:dbpedia(?x, ?p) {"
                + "  let((?l) ="
                + "    service <http://fr.dbpedia.org/sparql> {"
                + "	?x ?p ?l"
                + "      })"
                + "  {?l}"
                + "}";

        Mappings map = exec.query(q);
        for (Mapping m : map) {
            IDatatype dt = (IDatatype) m.getValue("?t");
            assertEquals("Auguste", dt.stringValue());
        }


    }

    @Test
    public void testFuncall() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String q = "function xt:main(){"
                + "let (?fun = us:test){"
                + "funcall(?fun, 'Hello')}"
                + "}"
                + "function us:test(?m){"
                + "?m"
                + "}";

        IDatatype dt = exec.eval(q);
        assertEquals("Hello", dt.stringValue());

    }

    @Test
    public void testMethod() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init =
                "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "us:John a foaf:Man "
                + "us:Boat a foaf:Thing "
                + "us:bar a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Person) ;"
                + "xt:output xsd:string ."
                + ""
                + "us:foo a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Thing) ;"
                + "xt:output xsd:string ."
                + "}";

        String q = "select (funcall(us:method(us:title, ?x), ?x) as ?t) where {"
                + "?x a foaf:Man"
                + "}"
                + "function us:method(?m, ?x){"
                + "let ((?fun) = select * where {"
                + "?x rdf:type/rdfs:subClassOf* ?t . "
                + "?fun a xt:Method ; xt:name ?m ; xt:input(?t)})"
                + "{ ?fun }"
                + "}"
                + "function us:bar(?x){"
                + "'bar'"
                + "}"
                + "function us:foo(?x){"
                + "'foo'"
                + "}";


        exec.query(init);
        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue(("?t"));
        assertEquals("bar", dt.stringValue());
    }

    @Test
    public void testMain() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String i = "insert data {"
                + "[] a us:Test ; us:width 2 ; us:length 3 "
                + "}";

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let (?m = select ?x (us:surface(?x) as ?s) where {?x a ?t}){"
                + "let ((?s, ?x) = ?m){"
                + "kg:display(?x); kg:display(?s);"
                + "?s"
                + "}"
                + "}}"
                + "function us:surface(?x){"
                + "let ((?l, ?w) =  select * where {?x us:length ?l ; us:width ?w }){"
                + "?l * ?w}"
                + "}";

        exec.query(i);
        gs.init();
//        Mappings map = exec.query(q);
//        //System.out.println(map);
//        Mappings m = (Mappings) map.getNodeObject(ASTQuery.MAIN_VAR);
//        IDatatype dt = (IDatatype) m.getValue("?s");

        IDatatype dt = exec.eval(q);
        assertEquals(6, dt.intValue());
    }

    @Test
    public void testConstruct() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "[] rdf:value 1, 2, 3 ."
                + "[] rdfs:label 'a', 'b', 'c' ."
                + "}";

        String qe = "select * where {"
                + "?x ?p ?y"
                + "}"
                + "function xt:produce(?q){"
                + "let (?g = construct where {?x rdfs:label ?y}){"
                + "?g"
                + "}}";

        exec.query(init);
        Mappings map = exec.query(qe);
        assertEquals(3, map.size());
    }

    @Test
    public void testexistsexport() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "[] rdf:value 1"
                + "}";

        String qe = "select where {}"
                + "export {"
                + "function us:test(){"
                + "if (exists {select * where {?x ?p ?y}}){"
                + "let ((?x, ?y) = select * where {?x ?p ?y}){"
                + "?y}"
                + "}"
                + "else {us:fun(false)}"
                + "}"
                + "function us:fun(?x){"
                + "?x}"
                + "}";

        String q = "select (us:test() as ?t)"
                + "where {}";

        exec.compile(qe);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testexists() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String i = "insert data {"
                + "[] us:width 2 ; us:length 3"
                + "[] us:width 3 ; us:length 4"
                + "}";

        String q = "select * (us:surface(?x) as ?s) where {"
                + "?x us:width ?w "
                + "}"
                + "function us:surface(?x){"
                + "let ((?l, ?w) = select * where { ?x us:length ?l ; us:width ?w }){"
                + "?l * ?w"
                + "}"
                + "}";

        String q2 =
                "select (us:test() as ?s) where {}"
                + "function us:test(){"
                + "let (?sum = 0){"
                + "for (?m in select * where { ?x us:width ?w }){"
                + "let ((?w, ?x) = ?m){  "
                + "set (?sum = ?sum + ?w)"
                + "}}; ?sum"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?s");
        assertEquals(6, dt.intValue());

        map = exec.query(q2);
        dt = (IDatatype) map.getValue("?s");
        assertEquals(5, dt.intValue());
    }

    @Test
    public void testBnode() throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);


        String i = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "[] ex:speed [ rdf:value 100 ; ex:unit 'km/h' ]"
                + "[] ex:speed [ rdf:value 90 ; ex:unit 'km/h' ]"
                + ""
                + ""
                + "}";

        String q =
                "prefix bn: <http://ns.inria.fr/sparql-extension/bnode#>"
                + "prefix ex: <http://example.org/>"
                + "select  ?x ?z where {"
                + "?x ex:speed ?y "
                + "?z ex:speed ?t "
                + "filter (?x != ?z) "
                + "filter (?y <= ?t) "
                + "}"
                + "function bn:lessEqual(?x, ?y){"
                + "let (?v1 = xt:value(?x, rdf:value), ?v2 = xt:value(?y, rdf:value)){"
                + "?v1 <= ?v2}"
                + "} ";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testCallback() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
                + "filter  exists {?x foaf:knows ?y} "
                + "}"
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}";

        String q2 = "select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
                + "filter not exists {?x foaf:knows ?y} "
                + "}"
                + "function xt:produce(?q){"
                + "xt:list(?q)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        map = exec.query(q2);
        assertEquals(0, map.size());
    }

    @Test
    public void testSet() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test() as ?t) where {}"
                + "function us:test(){"
                + "let (?sum = 0){ "
                + "for (?x in xt:iota(100)){"
                + "if (xt:prime(?x)){"
                + "set(?sum = ?sum + 1)"
                + "}};"
                + "?sum"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        //System.out.println(map);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(25, dt.intValue());

    }

    @Test
    public void testCandidate() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);


        String q = "select * where {"
                + "bind (unnest(xt:iota(3)) as ?n)"
                + "}"
                + "function xt:solution(?q, ?ms){"
                + "map (kg:display, ?ms)"
                + "}";

        Mappings map = exec.query(q);

        assertEquals(3, map.size());
    }

    @Test
    public void testSolution() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select * where {"
                + "bind (unnest(xt:iota(100)) as ?n)"
                + "}"
                + "function xt:solution(?q, ?ms){"
                + "for (?m in ?ms){"
                + "if (! us:check(?m)){"
                + "xt:reject(?m)}"
                + "}"
                + "}"
                + "function us:check(?m){"
                + "rand() <= 0.5"
                + "}";

        Mappings map = exec.query(q);
        //System.out.println(map.size());
        assertEquals(true, map.size() < 75);
    }

    @Test
    public void testCustomAgg() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "[] rdf:value 4, 5, 6, 7"
                + "[] rdf:value 1, 2, 3"
                + "}";

        String q = "select (aggregate(?v, us:mediane) as ?res)"
                + "where {"
                + "  ?x rdf:value ?v "
                + "}"
                + ""
                + "function us:mediane(?list){"
                + "  let (?l = xt:sort(?list)){"
                + "    xt:get(?l, xsd:integer((xt:size(?l) - 1) / 2))"
                + "  }"
                + "}"
                + "";

        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(4, dt.intValue());

    }

    @Test
    public void testVD13() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q =
                "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                + "select ?day  where {"
                + "bind (unnest(mapmerge(xt:span, mapfindlist(xt:prime, xt:iota(1901, 2000)))) as ?day)"
                + "}"
                + "function xt:span(?y) { "
                + "mapselect (xt:check, \"Friday\", maplist(cal:date, ?y, xt:iota(1, 12), 13)) "
                + "}"
                + "function xt:check(?d, ?x) { (xt:day(?x) = ?d) }";

        Mappings map = exec.query(q);
        assertEquals(23, map.size());

    }

    @Test
    public void testMapList() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * where {"
                + "bind (maplist (rq:plus, xt:iota(1, 10), xt:iota(1, 10)) as ?res)"
                + "}"
                + "";

        String q1 = "select * where {"
                + "bind (maplist (rq:plus, xt:iota(1, 10), 10) as ?res)"
                + "}"
                + "";

        String q2 = "select * where {"
                + "bind (maplist (rq:plus, 10, xt:iota(1, 10)) as ?res)"
                + "}"
                + "";

        String q3 = "select * where {"
                + "bind (maplist (rq:plus, xt:iota(1, 20), xt:iota(1, 10)) as ?res)"
                + "}"
                + "";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(10, dt.size());
        assertEquals(20, dt.get(dt.size() - 1).intValue());

        map = exec.query(q1);
        dt = (IDatatype) map.getValue("?res");
        assertEquals(10, dt.size());
        assertEquals(20, dt.get(dt.size() - 1).intValue());

        map = exec.query(q2);
        dt = (IDatatype) map.getValue("?res");
        assertEquals(10, dt.size());
        assertEquals(20, dt.get(dt.size() - 1).intValue());

        map = exec.query(q3);
        dt = (IDatatype) map.getValue("?res");
        assertEquals(20, dt.size());
        assertEquals(30, dt.get(dt.size() - 1).intValue());
    }

    @Test
    public void testExtList() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select ?n  "
                + "where {"
                + "bind (unnest(xt:iota(1, 100)) as ?n)"
                + "filter  xt:prime(?n)"
                + "}";


        String q2 = "select * where {"
                + "bind (mapfindlist (xt:prime, xt:iota(100)) as ?test)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(25, map.size());

        map = exec.query(q2);
        IDatatype dt = (IDatatype) map.getValue("?test");
        assertEquals(25, dt.size());


    }

    @Test
    public void testExtDT() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "prefix dt: <http://example.org/>"
                + "prefix ex: <http://example.org/test#>"
                + "select ?res where {"
                + "bind ( "
                + "'aa'^^dt:test <  'bb'^^dt:test &&"
                + "'bb'^^dt:test <=  'bb'^^dt:test &&"
                + "'cc'^^dt:test >  'bb'^^dt:test &&"
                + "'aa'^^dt:test != 'bb'^^dt:test &&"
                + "'aa'^^dt:test =  'aa'^^dt:test &&"
                + "'cc'^^dt:test >= 'bb'^^dt:test && "
                + " 'aa'^^dt:test not in ('bb'^^dt:test , 'cc'^^dt:test)"
                + "as ?res)"
                + "}"
                + "export {"
                + "function ex:equal(?x, ?y)   {  (str(?x) = str(?y))} "
                + "function ex:diff(?x, ?y)    {  (str(?x) != str(?y))}"
                + "function ex:less(?x, ?y)    {  (str(?x) < str(?y))}"
                + "function ex:lessEqual(?x, ?y)  {  (str(?x) <=  str(?y))}"
                + "function ex:greater(?x, ?y)   {  (str(?x) > str(?y))}"
                + "function ex:greaterEqual(?x, ?y) {  (str(?x) >= str(?y))}"
                + "}"
                + ""
                + "";

        String q2 =
                "prefix dt: <http://ns.inria.fr/sparql-datatype/>"
                + "prefix ex: <http://ns.inria.fr/sparql-datatype/romain#>"
                + "prefix rm: <http://ns.inria.fr/sparql-extension/spqr/>"
                + "select ?res ?val  (rm:digit(?val) as ?dig) "
                + ""
                + "where {"
                + ""
                + "bind (  'II'^^dt:romain * 'X'^^dt:romain + 'V'^^dt:romain as ?res) "
                + "bind (maplist(ex:romain,  xt:iota(7)) as ?list)"
                + "bind (apply (rq:mult, ?list) as ?val)"
                + " "
                + "}"
                + "export {"
                + "function ex:equal(?x, ?y)   { (rm:digit(?x) = rm:digit(?y))} "
                + "function ex:diff(?x, ?y)    { (rm:digit(?x) != rm:digit(?y))}"
                + "function ex:less(?x, ?y)    { (rm:digit(?x) < rm:digit(?y))}"
                + "function ex:lessEqual(?x, ?y)  { (rm:digit(?x) <= rm:digit(?y))}"
                + "function ex:greater(?x, ?y)   { (rm:digit(?x) > rm:digit(?y))}"
                + "function ex:greaterEqual(?x, ?y) { (rm:digit(?x) >= rm:digit(?y))} "
                + "function ex:plus(?x, ?y)  { ex:romain(rm:digit(?x) + rm:digit(?y))}"
                + "function ex:minus(?x, ?y) { ex:romain(rm:digit(?x) - rm:digit(?y))}"
                + "function ex:mult(?x, ?y)  { ex:romain(rm:digit(?x) * rm:digit(?y))}"
                + "function ex:divis(?x, ?y) { ex:romain(rm:digit(?x) / rm:digit(?y))} "
                + "function ex:romain(?x) { strdt(rm:romain(?x), dt:romain)}"
                + "}";

        String q3 = "prefix rm: <http://ns.inria.fr/sparql-extension/spqr/>"
                + "select * where {"
                + "bind (unnest(maplist(rm:romain, xt:reverse(xt:iota(5)))) as ?val)"
                + "}"
                + "order by ?val";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(dt.booleanValue(), true);

        map = exec.query(q2);
        dt = (IDatatype) map.getValue("?dig");
        assertEquals(5040, dt.intValue());

        map = exec.query(q3);
        //System.out.println(map);
    }

    public void testAgenda() throws EngineException {
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "/query/agenda.rq");
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(1637, map.getTemplateStringResult().length());

    }

    @Test
    public void testList() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select "
                + "(xt:list() as ?nil)"
                + "(xt:list(1, 2) as ?list)"
                + "(xt:cons(0, ?list) as ?res)"
                + "(xt:reverse(?res) as ?rev)"
                + "(xt:first(?res) as ?fst)"
                + "(xt:rest(?res) as ?rst)"
                + "(xt:copy(xt:list(1, 2)) as ?cp)"
                + "(xt:append(xt:list(1, 2), xt:list(3, 4)) as ?app)"
                + "where {}"
                + "function xt:copy(?list) { maplist(xt:self, ?list) }"
                + ""
                + "function xt:append(?l1, ?l2) {"
                + "if (xt:size(?l1) = 0, xt:copy(?l2),"
                + "xt:cons(xt:first(?l1), xt:append(xt:rest(?l1), ?l2)))}"
                + "";

        Mappings map = exec.query(q);
        assertEquals(true, true);
    }

    @Test
    public void testExtFun10() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2, 3 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select  * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter xt:test(?n)"
                + "filter xt:fun()"
                + "}"
                + "function xt:test(?n) { "
                + "if (?n = 1, "
                + "xt:test(?n + 1),"
                + "let (?m = ?n + 1){ exists { ?x rdf:value ?m }}"
                + ") "
                + "}"
                + "function xt:fun() { exists {?n rdf:value ?x} }";



        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testExtFun9() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select *"
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y rdf:value ?n "
                + "filter (let (?z = 3){ exists { ?t ?p ?z}}) } "
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
    }

    @Test
    public void testExtFun8() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { select { ?y ?p ?n  filter ( xt:fun(?n)) } } "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?x ?q ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //System.out.println(map);
    }

    @Test
    public void testExtFun7() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { "
                + "if (exists { select ?x where { ?x ?p ?n filter (?n < 10)} }, 1, 0) "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?sum");
        assertEquals(2, dt.intValue());
    }

    @Test
    public void testExtFun6() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { "
                + "if (exists { select ?n where { ?x ?p ?n filter (?n < 10)} }, 1, 0) "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?sum");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testExtFun3() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y ?p ?n filter (! xt:fun(?n)) } "
                + "}"
                + " function xt:fun(?n) { "
                + "exists { ?n ?q ?x } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //System.out.println(map);
    }

    @Test
    public void testExtFun2() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y ?p ?n filter xt:fun(?n) } "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?z ?q ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //System.out.println(map);
    }

    @Test
    public void testExtFun1() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter xt:fun(?n) "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?y ?p ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testExtFun5() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { if (exists { ?x ?p ?n }, 1, 0) } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?sum");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testExtFun4() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { if (exists { ?x ?p ?n }, 1, 0) } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?sum");
        assertEquals(2, dt.intValue());
    }

    @Test
    public void testExtAgg() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?x)  { ?x + ?x } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?sum");
        assertEquals(6, dt.intValue());
    }

    @Test
    /**
     * Test the occurrence of a recursive graph pattern that appears at least ?t
     * times
     */
    public void testExistFunRec() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:a a ex:Case "
                + "ex:a ex:p ex:b "
                + "ex:b ex:q ex:c "
                + "ex:c ex:r ex:a "
                + ""
                + "ex:c a ex:Case "
                + "ex:c ex:p ex:d "
                + "ex:d ex:q ex:e "
                + "ex:e ex:r ex:c "
                + "ex:e a ex:Case "
                + "ex:e ex:p ex:f "
                + "ex:f ex:q ex:g "
                + "ex:g ex:r ex:e "
                + "}";


        String q = "prefix ex: <http://example.org/>"
                + "select *"
                + "where {"
                + "?x a ex:Case "
                + "filter xt:test(?x, 2, 0)"
                + "}"
                + "function xt:test(?x, ?n, ?m) {"
                + "if (?m >= ?n, true,"
                + "exists { ?x ex:p ?y . ?y ex:q ?z . ?z ex:r ?x "
                + "filter xt:test(?z, ?n, ?m + 1) }"
                + ") }";

        exec.query(init);
        Mappings map = exec.query(q);
        //  assertEquals(2, map.size());

    }

   

    @Test
    public void testGeneralize() throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix ex: <http://example.org/>"
                + "insert data { "
                + "ex:John a ex:OldMan ;"
                + "ex:author [ a ex:Document ]"
                + "ex:Jack a ex:Person ;"
                + "ex:author [ a ex:Document ]"
                + "ex:Man       rdfs:subClassOf ex:Human "
                + "ex:YoungMan  rdfs:subClassOf ex:Man "
                + "ex:OldMan    rdfs:subClassOf ex:Man "
                + "ex:Report    rdfs:subClassOf ex:Document  "
                + "}";

        // target type more general than query
        // target type brother of query
        String qq = "prefix ex: <http://example.org/>"
                + "select * (kg:similarity() as ?sim) "
                + "where {"
                + "?x a ex:YoungMan, ?tt ;"
                + "ex:author [ a ex:Report ] "
                + ""
                + "}"
                + "function xt:candidate(?q, ?t, ?b) { "
                + "let ((?qs, ?qp, ?qo) = ?q, "
                + "     (?ts, ?tp, ?to) = ?t) {"
                + "if (?qp = rdf:type && isURI(?qo), "
                + "?b || exists { ?qo rdfs:subClassOf/(rdfs:subClassOf*|^rdfs:subClassOf) ?to } ,"
                + "?b)"
                + "}"
                + "}";

        exec.query(init);
        Mappings map = exec.query(qq);
        //System.out.println(map);
        //System.out.println(map.size());
        assertEquals(1, map.size());
    }

    @Test
    public void testNoGlobal() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { "
                + "<John> rdf:value 1, 2  ."
                + ""
                + "}";


        String qq =
                "select *"
                + "where {"
                + "bind (xt:test(?x) as ?z)"
                + "?x ?p ?y "
                + "}"
                + "function xt:test(?x) { ?y } ";


        exec.query(init);
        Mappings map = exec.query(qq);
        Node n = map.getNode("?z");
        assertEquals(null, n);
    }

    @Test
    public void testFunExist() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3 .}";
        String q =
                "select "
                + "(xt:test(?n) as ?r) "
                + "(xt:test(4) as ?b)"
                + "where {"
                + "?x rdf:value ?n "
                + "filter (xt:test(?n))}"
                + "function xt:test(?y) {  exists {?z rdf:value ?y} }";


        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());

        for (Mapping m : map) {
            IDatatype dt = (IDatatype) m.getValue("?r");
            assertEquals(true, dt.booleanValue());
            IDatatype dtf = (IDatatype) m.getValue("?b");
            assertEquals(false, dtf.booleanValue());
        }
    }

    @Test
    public void testSPQR() throws EngineException {
        Graph g = createGraph();
//        QueryLoad ql = QueryLoad.create();
//        String q = ql.read(data + "query/spqr.rq");
        QueryProcess exec = QueryProcess.create(g);
//        exec.query(q);

        String query =
                "prefix cal: <http://ns.inria.fr/sparql-extension/spqr/>\n"
                + "select \n"
                + "(99 as ?n)\n"
                + "(cal:romain(?n) as ?r)\n"
                + "(cal:digit(?r)  as ?d)"
                + "where {}";

        Mappings map = exec.query(query);

        IDatatype dtr = (IDatatype) map.getValue("?r");
        IDatatype dtn = (IDatatype) map.getValue("?d");

        assertEquals("XCIX", dtr.stringValue());
        assertEquals(99, dtn.intValue());

    }

    @Test
    public void testFunfdghf() throws EngineException {
        String q = "select \n"
                + "(xt:f(1) as ?x)\n"
                + "(xt:f(1, 2) as ?y)\n"
                + "\n"
                + "where {}"
                + "function xt:f(?x) { ?x }"
                + "function xt:f(?x, ?y) { ?x + ?y }"
                + "function xt:f(?x, ?y, ?z) { ?x + ?y + ?z }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt1 = (IDatatype) map.getValue("?x");
        IDatatype dt2 = (IDatatype) map.getValue("?y");
        assertEquals(1, dt1.intValue());
        assertEquals(3, dt2.intValue());
    }

    @Test
    public void testFuture() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 .}";
        String q =
                "template {"
                + "st:number() ' : ' ?y}"
                + "where {"
                + "?x rdf:value ?y "
                + "} order by desc(?y)";


        exec.query(init);
        Mappings map = exec.query(q);
        String str = map.getTemplateStringResult();
        assertEquals(true, str.contains("8 : 1"));

    }

    //TODO
    public void testCal2() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "query/cal.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?fr");
        assertEquals("Vendredi", dt.stringValue());
        //System.out.println(Interpreter.getExtension());
        String qq = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                + "select *"
                + "where {"
                + "?x ?p ?y "
                + "filter (cal:jour(?y) = 'Mardi' )"
                + "}";

        String init = "insert data { "
                + "<Day1> rdf:value '2015-06-16'^^xsd:date ."
                + "<Day2> rdf:value '2015-06-17'^^xsd:date ."
                + "<Day3> rdf:value '2015-06-23'^^xsd:date ."
                + "}";
        exec.query(init);


        Mappings m = exec.query(qq);
        assertEquals(2, m.size());

    }

    @Test
    public void testBBB() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { "
                + "<John> rdf:value 5 ."
                + "<Jim>  rdf:value 30}";
        String q =
                "select * "
                + "where {"
                + "?x rdf:value ?y "
                + "filter (xt:foo(?x, 10) || xt:foo(?x, 5))"
                + "} "
                + "function xt:foo(?x, ?n) { exists {?x rdf:value ?n} }";
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testapply() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select "
                + "(apply(rq:concat, xt:iota('a', 'c')) as ?con)"
                + "(apply(rq:plus,   xt:iota(5)) as ?sum)"
                + "(apply(rq:mult,   xt:iota(5)) as ?mul)"
                + "where {}";
        Mappings map = exec.query(q);
        IDatatype dt1 = (IDatatype) map.getValue("?con");
        IDatatype dt2 = (IDatatype) map.getValue("?sum");
        IDatatype dt3 = (IDatatype) map.getValue("?mul");
        assertEquals("abc", dt1.stringValue());
        assertEquals(15, dt2.intValue());
        assertEquals(120, dt3.intValue());
    }

    @Test
    public void testEEE2() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(apply  (rq:plus, maplist (xt:fun, xt:iota(0, 12))) as ?res)"
                + "where {}"
                + "function xt:fac(?n) { if (?n = 0, 1, ?n *  xt:fac(?n - 1)) }"
                + "function xt:fun(?x) { 1.0 / xt:fac(?x) }";

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(2.71828, dt.doubleValue(), 0.0001);

    }

    @Test
    public void testEEE() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 .}";
        String q =
                "select * "
                + "(1 + sum(xt:foo(xsd:long(?n))) as ?res)"
                + "where {"
                + "?x rdf:value ?n"
                + "}"
                + "function xt:fac(?n) { if (?n = 0, 1, ?n * xt:fac(?n - 1)) }"
                + "function xt:foo(?n) { 1 / xt:fac(?n) }";
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(2.71828, dt.doubleValue(), 0.0001);

    }

    @Test
    public void testExtFun() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);


        String query =
                "select "
                + "( st:test(st:fac(?x)) as ?r)"
                + "where {"
                + "bind (5 as ?x)"
                + "}"
                + "function st:fac(?x)  { if (?x = 1, 1, ?x * st:fac(?x - 1)) }"
                + "function st:test(?x) { let(?y = ?x * ?x){ ?y} }";

        String query2 =
                "select "
                + "(st:test(st:fac(?x)) as ?r)"
                + "where {"
                + "bind (5 as ?x)}"
                + "function st:fac(?x)   { if (?x = 1, 1, ?x * st:fac(?x - 1)) } "
                + "function st:test(?x) { let(?y = ?x * ?x){ ?y} } ";

        Mappings map = exec.query(query);

        IDatatype dt = (IDatatype) map.getValue("?r");

        assertEquals(14400, dt.intValue());

        map = exec.query(query2);

        dt = (IDatatype) map.getValue("?r");

        assertEquals(14400, dt.intValue());

    }

    @Test
    public void myastpp() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);


        String q = "select  * "
                + "where {"
                + "?x foaf:knows ?y "
                + "graph st:sys {"
                + "?y foaf:knows ?z, ?t "
                + "filter  exists { ?u rdfs:label 'toto' }"
                + "filter not exists { ?u rdfs:label 'tata' }"
                + "}"
                + "graph st:test { "
                + "?x rdfs:label ?n "
                + "filter exists { ?a rdfs:label 'John' }"
                + "filter not exists { ?u rdfs:label 'tata' }"
                + "}"
                + "}";
        Mappings map = exec.query(q);

        assertEquals(5, map.size());

    }

    @Test
    public void myastpp2() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);


        String q = "template {"
                + "str(?res)"
                + "}"
                + "where {"
                + "graph st:sys {"
                + "bind (st:atw(st:turtle) as ?res)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        //System.out.println(map.getTemplateStringResult());
        assertEquals(203, map.getTemplateStringResult().length());


    }

    @Test
    public void myastpp3() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);


        String q = "template {"
                + "str(?res)"
                + "}"
                + "where {"
                + "graph st:sys {"
                + "bind (st:atw('" + data + "template/test') as ?res)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        ////System.out.println(map);
        assertEquals(map.getTemplateStringResult().length(), 0);


    }

    Graph createGraph() {
        Graph g = Graph.create();
        Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 2);
        g.setStorage(IStorage.STORAGE_FILE, p);
        return g;
    }

    @Test
    public void testTCff() throws EngineException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.parse(data + "template/owl/data/primer.owl");
        Transformer t = Transformer.create(gs, Transformer.OWLRL);
        IDatatype dt = t.process();

        assertEquals(DatatypeMap.FALSE, dt);
    }

//      @Test 
//    public void testTCgg () throws EngineException{
//        GraphStore gs = GraphStore.create();
//        QueryProcess exec = QueryProcess.create(gs);
//        Load ld = Load.create(gs);
//        ld.load(data + "template/owl/data/success.ttl");       
//        Transformer t = Transformer.create(gs, Transformer.OWLRL);       
//        IDatatype dt = t.process();
//        
//        assertEquals(DatatypeMap.TRUE, dt);       
//     } 
    @Test
    public void testgraph() throws EngineException {
        Graph gs = createGraph();

        Node g = gs.addGraph(FOAF + "gg");
        Node s = gs.addResource(FOAF + "John");
        Node p = gs.addProperty(FOAF + "name");
        Node o = gs.addLiteral("John");

        Node b = gs.addBlank();
        Node q = gs.addProperty(FOAF + "knows");
        Node l = gs.addLiteral("Jack");

        Node gg = gs.createNode(DatatypeMap.newResource(FOAF, "gg"));
        Node ss = gs.createNode(DatatypeMap.newResource(FOAF, "Jim"));
        Node pp = gs.createNode(DatatypeMap.newResource(FOAF, "age"));
        Node oo = gs.createNode(DatatypeMap.newInstance(10));

        IDatatype g2 = DatatypeMap.newResource(FOAF, "gg");
        IDatatype s2 = DatatypeMap.newResource(FOAF, "James");
        IDatatype p2 = DatatypeMap.newResource(FOAF, "age");
        IDatatype o2 = DatatypeMap.newInstance(10);


        gs.addEdge(g, s, p, o);
        gs.addEdge(g, s, q, b);
        gs.addEdge(g, b, p, l);
        gs.add(gg, ss, pp, oo);
        gs.add(g2, s2, p2, o2);

        QueryProcess exec = QueryProcess.create(gs);

        String str = "select * where  { ?x ?p ?y ?y ?q ?z }";

        Mappings m1 = exec.query(str);
        assertEquals(1, m1.size());

        String q2 = FOAF_PREF
                + "select * where {"
                + "?x foaf:age ?y"
                + "}";

        Mappings m2 = exec.query(q2);
        assertEquals(2, m2.size());


        String q3 = FOAF_PREF
                + "select * where {"
                + "?x foaf:pp* ?y"
                + "}";

        Mappings m3 = exec.query(q3);
        //System.out.println(m3);
        assertEquals(7, m3.size());

    }

    @Test
    public void testeng() throws EngineException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.parse(data + "template/owl/data/primer.owl");

        String q = "select * where {"
                + "graph eng:describe {"
                + "[] kg:index 0 ; kg:item [ rdf:predicate ?p ; rdf:value ?v ] "
                + "}"
                + "filter exists { ?x ?p ?y }"
                + "}";

        Mappings map = exec.query(q);

        assertEquals(56, map.size());

        // query the SPIN graph of previous query
        q = "select * where {"
                + "graph eng:query {"
                + "[] sp:predicate ?p "
                + "values ?p { rdf:predicate rdf:value} "
                + "}"
                + "}";

        map = exec.query(q);

        assertEquals(2, map.size());
    }

    public void testTr() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:C4 owl:unionOf (ex:C5 ex:C6) "
                + "ex:C0 owl:unionOf (ex:C2 ex:C3) "
                + "ex:C1 owl:unionOf (ex:C2 ex:C3) "
                + "}";

        exec.query(init);

        String q = "select *"
                + "  where { "
                + "    ?x owl:unionOf (?c1 ?c2)  ;"
                + "       owl:unionOf ?l"
                + "  }"
                + "group by (st:apply-templates-with(st:hash2, ?l) as ?exp)";

        Mappings map = exec.query(q);

        assertEquals(2, map.size());

    }

   
    @Test
    public void testExists() throws EngineException {

        Graph g1 = createGraph();
        QueryProcess exec = QueryProcess.create(g1);
        Graph g2 = createGraph();
        QueryProcess exec2 = QueryProcess.create(g2);
        String init1 = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String init2 = "insert data { "
                + "<Jim> rdfs:label 'Jim' "
                + "}";


        String q = "select "
                + "(group_concat"
                + "(exists {"
                + "select "
                + "(group_concat(exists {"
                + "select "
                + "(group_concat(exists {"
                + "?x rdfs:label ?ll"
                + "}) as ?temp) "
                + "where {"
                + "?x rdfs:label ?l "
                + "}"
                + "}) as ?temp) "
                + "where {"
                + "?x rdfs:label ?l "
                + "}"
                + "}"
                + ") "
                + "as ?res) where {"
                + "?x rdfs:label ?l "
                + ""
                + "}";

        exec.query(init1);
        exec2.query(init2);

        exec.add(g2);

        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?res");
        //System.out.println(map);
        assertEquals("true true true", dt.stringValue());
    }

    @Test
    public void testQQS() throws EngineException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "{"
                + "?x rdfs:label 'John' "
                + "filter exists { select * where {filter(?l = 'John') ?y rdfs:label ?l}} "
                + "}"
                + "union {filter(?l = 'John') ?x rdfs:label ?l}"
                + "}"
                + ""
                + ""
                + "}";

        exec.query(init);

        Mappings map = exec.query(q);
        assertEquals(2, map.size());


    }

    @Test
    public void testGTT() throws LoadException, EngineException {

        Graph g = createGraph();
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        Transformer t = Transformer.create(g, Transformer.TURTLE, RDF.RDF);
        String str = t.transform();
        assertEquals(3934, str.length());

        t = Transformer.create(g, Transformer.TURTLE, RDFS.RDFS);
        str = t.transform();
        //System.out.println(str);
        assertEquals(3271, str.length());

        t = Transformer.create(g, Transformer.TURTLE);
        str = t.transform();
        //System.out.println(str);
        assertEquals(7047, str.length());
    }

    @Test
    public void testGT() throws LoadException, EngineException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        String t1 = "template { st:apply-templates-with-graph(st:turtle, rdf:)} where {}";
        String t2 = "template { st:apply-templates-with-graph(st:turtle, rdfs:)} where {}";
        String t3 = "template { st:apply-templates-with(st:turtle)} where {}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(t1);
        String str = map.getTemplateStringResult();
        //System.out.println(str);
        assertEquals(3934, str.length());

        map = exec.query(t2);
        str = map.getTemplateStringResult();
        //System.out.println(str);
        assertEquals(3271, str.length());

        map = exec.query(t3);
        str = map.getTemplateStringResult();
        //System.out.println(str);
        assertEquals(7047, str.length());
    }

    @Test
    public void testGCC() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<John> rdf:value 'test'@fr, 'titi'@fr . "
                + "<Jack> rdf:value 'test'@fr,'titi'@en . "
                + "<Jim>  rdf:value 'test'@fr, 'titi' . "
                + "}";

        String q = "select ?x (group_concat(?v) as ?g) (datatype(?g) as ?dt)where {"
                + "?x rdf:value ?v"
                + "}"
                + "group by ?x "
                + "order by ?x";

        exec.query(init);


        Mappings map = exec.query(q);
        //System.out.println(map);

        IDatatype dt0 = (IDatatype) map.get(0).getValue("?g");
        assertEquals(true, dt0.getDatatypeURI().equals(NSManager.XSD + "string"));

        IDatatype dt1 = (IDatatype) map.get(1).getValue("?g");
        assertEquals(true, dt1.getDatatypeURI().equals(NSManager.XSD + "string"));

        IDatatype dt2 = (IDatatype) map.get(2).getValue("?g");
        assertEquals(true, dt2.getLang() != null && dt2.getLang().equals("fr"));

    }

    @Test
    public void testTrig() throws LoadException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        Transformer pp = Transformer.create(g, Transformer.TRIG);
        String str = pp.transform();
        assertEquals(10314, str.length());


    }

    @Test
    public void testPPOWL() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.parse(data + "template/owl/data/primer.owl");
        QueryProcess exec = QueryProcess.create(g);

        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:owl)}"
                + "where {}";

        String t2 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:turtle)}"
                + "where {}";


        Mappings map = exec.query(t1);

        assertEquals(7574, map.getTemplateResult().getLabel().length());

        map = exec.query(t2);

        assertEquals(9245, map.getTemplateResult().getLabel().length());

    }

    @Test
    public void testPPSPIN() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.parseDir(data + "template/spinhtml/data/");
        QueryProcess exec = QueryProcess.create(g);

        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";


        Mappings map = exec.query(t1);
        assertEquals(3024, map.getTemplateResult().getLabel().length());

    }

    @Test
    public void testMove1() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "insert data {"
                + "graph <g1> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "graph rdf: {"
                + "<John> rdfs:label 'John', 'Jim' ."
                + "<James> rdfs:seeAlso <Jack>, <Jim> . "
                + "}"
                + "graph <g3> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "}";

        exec.query(init);

        String u1 = "move rdf: to default";

        exec.query(u1);

        String q1 = "select * from kg:default where  {?x ?p ?y}";
        String q2 = "select * from rdf: where  {?x ?p ?y}";

        Mappings m1 = exec.query(q1);
        Mappings m2 = exec.query(q2);
        assertEquals(4, m1.size());
        assertEquals(0, m2.size());

        String u2 = "move <g3> to <g0>";

        exec.query(u2);

        String q3 = "select * from <g0> where  {?x ?p ?y}";
        String q4 = "select * from <g3> where  {?x ?p ?y}";

        Mappings m3 = exec.query(q3);
        Mappings m4 = exec.query(q4);

        assertEquals(2, m3.size());
        assertEquals(0, m4.size());
    }

    @Test
    public void testJoinDistinct() {
        String init =
                "insert data {"
                + "<John> rdfs:label 'John', 'Jack' "
                + "}";

        String query =
                "select  distinct ?x where {"
                + "?x rdfs:label ?n "
                + "{?x rdfs:label ?a} "
                + "{?x rdfs:label ?b} "
                + "}";



        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void testBase() {
        NSManager nsm = NSManager.create();

        nsm.setBase("http://example.org/test.html");
        nsm.setBase("foo/");
        nsm.definePrefix(":", "bar#");

        String str = nsm.toNamespaceB(":Joe");

        assertEquals("http://example.org/foo/bar#Joe", str);


    }

    @Test
    public void testLoadJSONLD() throws LoadException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "jsonld/test.jsonld");



        String init =
                "select  "
                + "(count(*) as ?c)  "
                + " where {"
                + "?x ?p ?y"
                + "}";


        try {
            Mappings map = exec.query(init);
            IDatatype dt = (IDatatype) map.getValue("?c");
            assertEquals("Result", 18, dt.intValue());
            //System.out.println(g.display());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testJSONLD() throws LoadException, EngineException {
        Graph g = Graph.create(true);

        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person ; rdfs:range foaf:Person ."
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <James> "
                + "<Jack> foaf:knows <Jim> "
                + "<James> a foaf:Person"
                + "}";

        exec.query(init);

        JSONLDFormat jf = JSONLDFormat.create(g);
        String str = jf.toString();

        assertEquals(true, str.length() > 0);

    }

    @Test
    public void testDescr() {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "describe ?z  where {"
                + "?x ?p ?y filter exists { ?x ?p ?z}"
                + "}";


        try {
            Mappings map = exec.query(q);
            ASTQuery ast = exec.getAST(map);
            assertEquals(0, ast.getConstruct().size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testRDFa() throws LoadException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parseDir(data + "rdfa");



        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select  "
                + "(count(*) as ?c)  "
                + " where {"
                + "?x ?p ?y"
                + "}";


        try {
            Mappings map = exec.query(init);
            IDatatype dt = (IDatatype) map.getValue("?c");
            assertEquals("Result", 11, dt.intValue());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testClear() throws LoadException, EngineException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        QueryProcess exec = QueryProcess.create(g);

        String q1 = "select * where {graph ?g {?x a ?t}}";
        String qrem = "clear all";

        ld.parse(data + "math/data/fun.ttl");

        Mappings map = exec.query(q1);

        int res = map.size();

        exec.query(qrem);

        ld.parse(data + "math/data/fun.ttl");

        map = exec.query(q1);

        assertEquals(res, map.size());

    }

    @Test
    public void testDT() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init =
                "insert data { "
                + "[ rdf:value '2013-11-11'^^xsd:gYear "
                + ", '2013-11-11'^^xsd:gMonth "
                + ", '2013-11-11'^^xsd:gDay "
                + ", 'ar'^^xsd:int "
                + ", 'toto'^^xsd:double "
                + ""
                + "]}";

        String q = "select (datatype(?y) as ?res)  where {?x ?p ?y}";

        exec.query(init);

        Mappings map = exec.query(q);
        //System.out.println(map);

        assertEquals(5, map.size());
        assertEquals(false, gs.getProxy().typeCheck());


    }

    @Test
    public void testSystem() throws EngineException {
        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data { graph kg:system { "
                + "kg:kgram kg:listen true "
                + "kg:store sp:query true "
                + "}}";

        String q = "select * where  {?x ?p ?y}";

        String query = "select ?res where {"
                + "graph kg:query {"
                + "select (st:apply-templates-with(st:spin, ?q) as ?res) where {"
                + "?q a sp:Select"
                + "}"
                + "}"
                + "}";

        exec.query(init);
        exec.query(q);

        Mappings map = exec.query(query);

        IDatatype dt = (IDatatype) map.getValue("?res");
        assertEquals(true, dt.getLabel().contains("?x ?p ?y"));


    }

    @Test
    public void testLoc2() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { "
                + "[ foaf:knows <Jim> ] . "
                + "<Jim> foaf:knows <James> "
                + "<Jim> rdfs:label 'Jim' "
                + " "
                + "}";


        GraphStore gs = GraphStore.create();
        GraphStoreInit.create(gs).init();
        Graph gg = gs.getNamedGraph(Graph.SYSTEM);
        QueryProcess exec = QueryProcess.create(gs);

        String q = FOAF_PREF
                + "select  *"
                + "where {"
                + "graph kg:system { "
                + "?a kg:version+ ?b "
                + "filter ("
                + "?a != ?a || "
                + "if (! (exists { ?a kg:date+ ?d } = false),  true, false)"
                + ")"
                + "filter not exists { ?x foaf:knows ?y }"
                + "}"
                + "}";


        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals("result", 1, map.size());
    }

    @Test
    public void testLoc() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { "
                + "graph kg:system { "
                + "  kg:kgram kg:version '3.0.22' ;"
                + "    kg:date '2013-11-27'^^xsd:date ;"
                + "    kg:skolem true "
                + "}} ;"
                + "insert data { "
                + "[ foaf:knows <Jim> ]"
                + " "
                + "}";


        GraphStore g = GraphStore.create();
        Graph gg = g.createNamedGraph(Graph.SYSTEM);
        QueryProcess exec = QueryProcess.create(g);

        String q1 =
                "select *"
                + "where {"
                + "graph ?g { ?x ?p ?y  }"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q1);

        assertEquals("result", 1, map.size());

        String q2 =
                "select *"
                + "where {"
                + "graph kg:system { ?x ?p ?y  }"
                + "}";

        exec.query(init);
        map = exec.query(q2);

        assertEquals("result", 3, map.size());

    }

   

    @Test
    public void testTurtle() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init =
                "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data { "
                + "<John> foaf:name 'John' ; "
                + "foaf:knows [ foaf:name 'Jim' ]"
                + "}";

        String temp = "template {st:apply-templates-with(st:turtle)} where {}";
        exec.query(init);
        Mappings map = exec.query(temp);
        Node node = map.getTemplateResult();
        assertEquals("result", node == null, false);
        assertEquals("result", node.getLabel().contains("John"), true);
        assertEquals("result", node.getLabel().contains("Property"), false);
    }

    @Test
    public void testTurtle2() throws EngineException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        String init =
                "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data { "
                + "<John> foaf:name 'John' ; "
                + "foaf:knows [ foaf:name 'Jim' ]"
                + "}";

        String temp = "template {st:call-template-with(st:turtle, st:all)} where {}";
        exec.query(init);
        Mappings map = exec.query(temp);
        Node node = map.getTemplateResult();
        assertEquals("result", node == null, false);
        assertEquals("result", node.getLabel().contains("John"), true);
        assertEquals("result", node.getLabel().contains("Property"), true);
    }

    @Test
    public void testQV() {
        Graph g = createGraph();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:knows (<John> <Jim>)"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows (<Jim> ?x) "
                + "}"
                + "pragma {"
                + "kg:list kg:expand true"
                + "}"
                + "";
        try {
            exec.query(init);
            //exec.setVisitor(ExpandList.create());
            Mappings map = exec.query(query);
            assertEquals(1, map.size());

        } catch (EngineException ex) {
            assertEquals(ex, true);
        }

    }

    @Test
    public void testQM() {
        Graph g = createGraph();
        QueryManager man = QueryManager.create(g);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix sp: <http://spinrdf.org/sp#>"
                + "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:name ?n "
                + "?x foaf:knows ?p "
                + "minus { ?x foaf:age ?a } "
                + "<James> foaf:fake ?f "
                + "?f a foaf:Person "
                + "?f sp:elements ?e "
                + "?f sp:test ?t "
                + "filter(?b >= 20)"
                + "}";
        try {
            exec.query(init);
            Mappings map = man.query(query);
            assertEquals("result", 1, map.size());
            //System.out.println(map.getQuery().getAST());
            //System.out.println(map);
            //System.out.println("size: " + map.size());


        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
            assertEquals("result", true, ex);
        }

    }

    @Test
    public void testPPSPINwdfgdwfgd() throws EngineException, LoadException {
        String t1 =
                "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";

        File f = new File(data + "template/spinhtml/data/");

        for (File ff : f.listFiles()) {
            testSPPP(ff.getAbsolutePath());
        }


    }

    public void testSPPP(String path) throws EngineException, LoadException {
        String t1 =
                "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";
        Graph g = createGraph();
        Load ld = Load.create(g);
        //System.out.println("Load");
        ld.parseDir(path);

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t1);
        //System.out.println(map.getTemplateStringResult());
        try {
            Query q = exec.compile(map.getTemplateStringResult());
            assertEquals(true, true);
        } catch (EngineException e) {
            System.out.println(path);
            System.out.println(e);
            assertEquals(true, false);
        }
    }

    @Test
    public void testSPIN() {
        Graph g = Graph.create(true);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person ; foaf:knows <James> "
                + "<Jim> a foaf:Person "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select ?x (count(?y) as ?c) where {"
                + " { ?x a foaf:Test } union { ?x a foaf:Person ; foaf:pp* :: $path ?z }"
                + "optional { ?x foaf:knows ?y } "
                + "minus { ?x a foaf:Test } "
                + "filter(bound(?x) && ?x != 12)"
                + "}"
                + "group by ?x "
                + "having (?c >= 0)";
        try {

            SPINProcess sp = SPINProcess.create();
            QueryProcess exec = QueryProcess.create(g);
            Mappings m = exec.query(init);
            String str = sp.toSpinSparql(query);
            Mappings map = exec.query(str);
            //System.out.println(map);
            //System.out.println(map.getQuery().getAST());
            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testDistType() {
        Graph g1 = Graph.create(true);
        Graph g2 = createGraph();

        QueryProcess e1 = QueryProcess.create(g1);
        e1.add(g2);
        QueryProcess e2 = QueryProcess.create(g2);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + " ?x a foaf:Person "
                + "}";
        try {
            e2.query(init);
            Mappings map = e1.query(query);
            //System.out.println(map);
            assertEquals("result", 1, map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testPPLib() {
        assertEquals("result", true, test("owl.rul") != null);
        assertEquals("result", true, test("spin.rul") != null);
        assertEquals("result", true, test("sql.rul") != null);
        assertEquals("result", true, test("turtle.rul") != null);
    }

    InputStream test(String pp) {
        String lib = Loader.PPLIB;

        InputStream stream = getClass().getResourceAsStream(lib + pp);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return stream;
    }

    @Test
    public void testDataset() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        Dataset ds = Dataset.create();
        ds.setUpdate(true);
        ds.addFrom("http://inria.fr/g2");
        ds.addNamed("http://inria.fr/g1");

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "graph <http://inria.fr/g1> {"
                + "<John> foaf:name 'John' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/g2> {"
                + "<Jim> foaf:name 'Jim' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/o> {"
                + "foaf:Person rdfs:subClassOf foaf:Human"
                + "}"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from named <http://inria.fr/g2>"
                + "where {"
                + "    {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String query2 = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from <http://inria.fr/g2>"
                + "where {"
                + " graph ?g   {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "delete  where {?x ?p ?y}";

        try {
            QueryProcess sparql = QueryProcess.create(g);
            sparql.query(init);

            Mappings map = sparql.sparql(query, ds);
            assertEquals("result", 0, map.size());

            QueryProcess exec = QueryProcess.create(g);
            Mappings map2 = exec.query(query, ds);
            assertEquals("result", 2, map2.size());

            map = sparql.sparql(query2, ds);
            assertEquals("result", 0, map.size());

            map2 = exec.query(query2, ds);
            assertEquals("result", 2, map2.size());

        } catch (EngineException ex) {
            //System.out.println(ex);
        }



    }

    @Test
    public void TestOnto() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "c:Human rdfs:subClassOf c:Animal "
                + "c:Man   rdfs:subClassOf c:Human "
                + "c:Woman rdfs:subClassOf c:Human "
                + ""
                + "<John> a c:Man "
                + "<Tigrou> a c:Cat "
                + "<Mary> a c:Woman "
                + "<James> a c:Human "
                + "}";

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {"
                + "?x a c:Human, ?t"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            //System.out.println(map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * Test With valueOut
     */
    @Test
    public void testValues() {
        String init =
                "insert data {"
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 21.0 "
                + "}";

        String query =
                "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter(?a = ?b && ?x != ?y) "
                + "}";

        String query2 =
                "select  * where {"
                + "{select (21.0 as ?a) where {}}"
                + "?x foaf:age ?a "
                + "}";

        String query3 =
                "select  * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        //exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 2, map.size());

            map = exec.query(query2);
            //System.out.println(map);
            assertEquals("Result", 2, map.size());

            map = exec.query(query3);
            //System.out.println(map);
            assertEquals("Result", 4, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    @Test
    public void testValues2() {
        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    //@Test
    public void testMath() {
        Graph g = createGraph();
        Load ld = Load.create(g);

        try {
            ld.parseDir(data + "math/data");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q =
                "prefix m: <http://ns.inria.fr/2013/math#>"
                + "template  { st:apply-templates-with(?p) }"
                + "where { ?p a m:PrettyPrinter }";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            //System.out.println(node.getLabel());

            assertEquals("result", true, node.getLabel().length() > 10);

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testGC() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://example.org/> "
                + "insert data {"
                + "[ex:name 'John' , 'Jim']"
                + "[ex:name 'John' , 'Jim']"
                + "}"
                + "";



        String query2 = "prefix ex: <http://example.org/> "
                + "select (group_concat( concat(self(?n1), ?n2) ;  separator='; ') as ?t) where {"
                + "?x ex:name ?n1 "
                + "?y ex:name ?n2 "
                + "filter(?x != ?y)"
                + ""
                + "}";

        try {
            exec.query(init);



            Mappings map = exec.query(query2);
            IDatatype dt = (IDatatype) map.getValue("?t");
            assertEquals("Results", 70, dt.getLabel().length());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test1() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select check where {"
                + "?x rdf:type c:Person ;"
                + "c:FirstName 'John' ;"
                + "c:name ?n"
                + "}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            assertEquals("Result", true, true);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select more * (kg:similarity() as ?sim) where {"
                + "?x rdf:type c:Engineer "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type c:WebPage"
                + "}"
                + "order by desc(?sim)";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");
            assertEquals("Result", true, dt != null);
            double sim = dt.doubleValue();

            assertEquals("Result", .84, sim, 1e-2);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2b() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select more * (kg:similarity() as ?sim) where {"
                + "?x rdf:type ?c1 filter(kg:similarity(?c1, c:Engineer) > .5) "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type ?c2 filter(kg:similarity(?c2, c:WebPage) > .4)"
                + "}"
                + "order by desc(?sim)";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);

            assertEquals("Result", 9, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2c() {
        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  (kg:similarity(c:Person, c:Document) as ?sim) where {}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");
            double sim = dt.getDoubleValue();

            assertEquals("Result", sim, .16, 1e-2);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

  

    @Test
    public void test4() {
        Load ld = Load.create(Graph.create());
        try {
            ld.parse("gogo.rdf");
            assertEquals("Result", false, true);
        } catch (LoadException e) {
            //System.out.println(e);
            assertEquals("Result", e, e);
        }
        try {
            ld.parse(data + "comma/fail.rdf");
            assertEquals("Result", false, true);
        } catch (LoadException e) {
            //System.out.println(e);
            assertEquals("Result", e, e);
        }
    }

    @Test
    public void test5() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            Mappings map = exec.query(query);

            assertEquals("Result", 1, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test6() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop = "drop graph kg:entailment";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            exec.query(drop);
            Mappings map = exec.query(query);

            assertEquals("Result", 0, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test7() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop = "drop graph kg:entailment";
        String create = "create graph kg:entailment";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            exec.query(drop);
            exec.query(create);
            Mappings map = exec.query(query);

            assertEquals("Result", 1, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    public void test8() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x c:hasCreated ?doc"
                + "} "
                + "group by any "
                + "order by desc(count(?doc))"
                + "pragma {"
                + "kg:kgram kg:list true "
                + "kg:kgram kg:detail true}";

        try {
            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            assertEquals("Result", 3, map.size());
            Mapping m = map.get(0);
            assertEquals("Result", 2, m.getMappings().size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test9() {

        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type ?t; c:name ?n"
                + "} ";

        try {
            QueryProcess e1 = QueryProcess.create(g1);
            QueryProcess e2 = QueryProcess.create(g2);
            QueryProcess exec = QueryProcess.create(g1);
            exec.add(g2);

            e1.query("prefix c: <http://www.inria.fr/acacia/comma#>" +"insert data {<John> rdf:type c:Person}");
            e2.query("prefix c: <http://www.inria.fr/acacia/comma#>" +"insert data {<John> c:name 'John'}");

            Mappings map = exec.query(query);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

//    @Test
//    public void test10() {
//
//        String query = "select  *  where {"
//      + "bind (kg:unnest(kg:sparql('select * where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
//      + "as (?a, ?b))"
//        + "} ";
//
//        try {
//
//            QueryProcess exec = QueryProcess.create(graph);
//
//            Mappings map = exec.query(query);
//            assertEquals("Result", 3, map.size());
//            Node node = map.getNode("?a");
//            assertEquals("Result", true, node != null);
//
//        } catch (EngineException e) {
//            assertEquals("Result", true, e);
//        }
//    }
//     @Test
//    public void test10cons() {
//
//        String  query = 
//                "prefix c: <http://www.inria.fr/acacia/comma#>"
//                + "select  *  where {"
//      + "bind ((kg:sparql('"
//                + "prefix c: <http://www.inria.fr/acacia/comma#>"
//                + "construct  where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
//      + "as ?g)"
//                + "graph ?g { ?a ?p ?b }"
//        + "} ";
//
//        try {
//
//            QueryProcess exec = QueryProcess.create(graph);
//
//            Mappings map = exec.query(query);
//            assertEquals("Result", 5, map.size());
//            
//
//        } catch (EngineException e) {
//            assertEquals("Result", true, e);
//        }
//    }
    @Test
    public void test11() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * (count(?doc) as ?c)"
                + "(kg:setObject(?x, ?c) as ?t)"
                + "where {"
                + "?x c:hasCreated ?doc"
                + ""
                + "}"
                + "group by ?x";

        String query2 ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select distinct ?x"
                + "(kg:getObject(?x) as ?v)"
                + "where {"
                + "?x c:hasCreated ?doc filter(kg:getObject(?x) > 0)"
                + "}"
                + "order by desc(kg:getObject(?x))";


        try {

            QueryProcess exec = QueryProcess.create(graph);

            exec.query(query);
            Mappings map = exec.query(query2);

            assertEquals("Result", 3, map.size());

            IDatatype dt = getValue(map, "?v");

            assertEquals("Result", 2, dt.getIntegerValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test111() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * (count(?doc) as ?c)"
                + "(kg:setProperty(?x, 0, ?c) as ?t)"
                + "where {"
                + "?x c:hasCreated ?doc"
                + ""
                + "}"
                + "group by ?x";

        String query2 ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select distinct ?x"
                + "(kg:getProperty(?x, 0) as ?v)"
                + "where {"
                + "?x c:hasCreated ?doc filter(kg:getProperty(?x, 0) > 0)"
                + "}"
                + "order by desc(kg:getProperty(?x, 0))";


        try {

            QueryProcess exec = QueryProcess.create(graph);

            exec.query(query);
            Mappings map = exec.query(query2);

            assertEquals("Result", 3, map.size());

            IDatatype dt = getValue(map, "?v");

            assertEquals("Result", 2, dt.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test12() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +"select  *  where {"
                + "?x rdf:type ?class; c:hasCreated ?doc}";

        try {

            QueryProcess.setSort(true);
            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            QueryProcess.setSort(false);

            assertEquals("Result", 22, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test13() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +"select  *  where {"
                + "?x rdf:type ?class; c:hasCreated ?doc}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            StatListener el = StatListener.create();
            exec.addEventListener(el);
            Mappings map = exec.query(query);
            ////System.out.println(el);
            assertEquals("Result", 22, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test14() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +"select  *  where {"
                + "?x rdf:type c:Person; c:hasCreated ?doc "
                + "?doc rdf:type/rdfs:subClassOf* c:Document "
                + "c:Document rdfs:label ?l ;"
                + "rdfs:comment ?c"
                + "}";

        try {

            Graph g = Graph.create(true);
            Load ld = Load.create(g);
            //ld.setBuild(new MyBuild(g));
            try {
                init(g, ld);
            } catch (LoadException ex) {
                Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
            }

            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(query);
            assertEquals("Result", 68, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }





    }

    @Test
    public void test15() {




        String query = "select (kg:similarity() as ?sim) (max(kg:depth(?x)) as ?max)  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 13, dt.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test16() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * (kg:number() as ?num)  where {"
                + "?x c:hasCreated ?doc "
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            Mapping m = map.get(map.size() - 1);
            IDatatype dt = datatype(m.getNode("?num"));
            //System.out.println(map);
            assertEquals("Result", map.size(), dt.getIntegerValue() + 1);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test17() throws LoadException {

        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.parse(data + "comma/comma.rdfs");

        QueryProcess exec = QueryProcess.create(g);
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select (kg:similarity(c:Person, c:Document) as ?sim) {}";
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");

            assertEquals("Result", true, dt.getDoubleValue() < 0.5);

            String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                    "insert data {c:Human rdfs:subClassOf c:Person}";
            exec.query(update);

            //		assertEquals("Result", null, g.getClassDistance()); 	

            map = exec.query(query);
            IDatatype sim = getValue(map, "?sim");

            assertEquals("Result", dt, sim);


        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test18() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * where {"
                + "c:Person rdfs:subClassOf+ :: $path ?c "
                + "graph $path {?a ?p ?b}"
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 31, map.size());
        } catch (EngineException e) {
            assertEquals("Result", 31, e);
        }

    }

    @Test
    public void test19() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * "
                + "(pathLength($path) as ?l) (count(?a) as ?c) where {"
                + "?x c:isMemberOf+ :: $path ?org "
                + "graph $path {?a ?p ?b}"
                + "}"
                + "group by $path";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 99, map.size());

            for (Mapping mm : map) {
                IDatatype ldt = getValue(mm, "?l");
                IDatatype lc = getValue(mm, "?c");

                assertEquals("Result", ldt, lc);
            }

        } catch (EngineException e) {
            assertEquals("Result", 99, e);
        }

    }

    public IDatatype fun(Object o1, Object o2) {
        IDatatype dt1 = datatype(o1);
        IDatatype dt2 = datatype(o2);
        String str = concat(dt1, dt2);
        return DatatypeMap.createLiteral(str);
    }

    String concat(IDatatype dt1, IDatatype dt2) {
        return dt1.getLabel() + "." + dt2.getLabel();
    }

    @Test
    public void test20() {
        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "prefix ext: <function://junit.TestQuery1> "
                + "select (ext:fun(?fn, ?ln) as ?res) where {"
                + "?x c:FirstName ?fn ; c:FamilyName ?ln"
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 23, map.size());

            for (Mapping mm : map) {
                IDatatype dt1 = getValue(mm, "?fn");
                IDatatype dt2 = getValue(mm, "?ln");
                IDatatype dt3 = getValue(mm, "?res");

                assertEquals("Result", dt3.getLabel(), concat(dt1, dt2));
            }

        } catch (EngineException e) {
            assertEquals("Result", 23, e);
        }

    }

    @Test
    public void test21() {
        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * where {"
                + "?x c:FirstName 'Olivier' "
                + "filter(kg:contains('é', 'e')) "
                + "filter(kg:contains('e', 'é')) "
                + "filter(kg:equals('e', 'é')) "
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    @Test
    public void test22() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {?a ?p ?b}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 64, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test23() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {{c:Toto ?p ?b} union {c:Engineer ?p ?b}}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test24() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {?a ?p ?b filter(?a = c:Engineer)}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test25() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {optional{c:Engineer ?p ?b} filter(! bound(?b))}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 0, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test26() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {optional{c:Toto ?p ?b} filter(! bound(?b))}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test27() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {{c:Engineer ?p ?b} minus {?a ?p c:Engineer}}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test28() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {c:Engineer ?p ?b} "
                + "?x rdf:type c:Engineer "
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 119, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    
    @Test
    public void test30() {

        String query =
                "select  "
                + "(pathLength($path) as ?l) "
                + "(max(?l, groupBy(?x, ?y)) as ?m) "
                + "(max(?m) as ?max) "
                + "where {"
                + "?x rdfs:subClassOf+ :: $path ?y"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 13, dt.getIntegerValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test31() {
        String query = "select (count(?l) as ?c1) "
                + "(count(distinct ?l) as ?c2) "
                + "(count(distinct self(?l)) as ?c3) "
                + "where {"
                + "?x rdfs:label ?l"
                + "}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt1 = getValue(map, "?c1");
            IDatatype dt2 = getValue(map, "?c2");
            IDatatype dt3 = getValue(map, "?c3");

            assertEquals("Result", 1406, dt1.getIntegerValue());
            assertEquals("Result", 1367, dt2.getIntegerValue());
            assertEquals("Result", 1367, dt3.getIntegerValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test33() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x foaf:knows+ :: $path <Jim> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test34() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x ^ (foaf:knows+) :: $path <John> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test35() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x  (^foaf:knows)+ :: $path <John> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test36() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows (<a> <b> <c>) "
                + "}";

        String query = "select * where {"
                + "graph ?g {optional{?x rdf:rest*/rdf:first ?y} "
                + "filter(!bound(?y))  "
                + "}"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            ////System.out.println(map);
            assertEquals("Result", 0, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test37() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<John> <name> 'John'}";
        try {
            exec.query(init);

            g.init();

//			RDFFormat f = RDFFormat.create(g);
//			//System.out.println(f);

            assertEquals("Result", 3, g.size());

            String query = "select * where {?p rdf:type rdf:Property}";

            Mappings res = exec.query(query);
//			//System.out.println("** Res: " );
//			//System.out.println(res);
            assertEquals("Result", 2, res.size());


            String update = "delete {?x ?p ?y} where {?x ?p ?y}";
            exec.query(update);


            String qq = "select * where {?x ?p ?y}";
            res = exec.query(qq);
            assertEquals("Result", 0, res.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test38() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John>  <age> 20 "
                + "<John>  <age> 10 "
                + "<James> <age> 30 "
                + "}";

        String query =
                "select distinct (sum(?age) as ?s) where {"
                + "?x <age> ?age"
                + "}"
                + "group by ?x";


        try {
            exec.query(init);
            Mappings res = exec.query(query);
            assertEquals("Result", 1, res.size());
            assertEquals("Result", 30, getValue(res, "?s").getIntegerValue());


        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test39() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "insert data {"
                + "<a> foaf:knows <b> "
                + "<b> foaf:knows <a> "
                + "<b> foaf:knows <c> "
                + "<a> foaf:knows <c> "
                + "}";

        String query =
                "select * where {"
                + "<a> foaf:knows+ ?t "
                + "}";

        String query2 =
                "select * where {"
                + "<a> foaf:knows{1,10} ?t "
                + "}"
                + "pragma {kg:path kg:loop false}";


        try {
            exec.query(init);
            Mappings res = exec.query(query);
            assertEquals("Result", 2, res.size());

            exec.setPathLoop(false);
            res = exec.query(query);
            assertEquals("Result", 2, res.size());



        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    

    @Test
    public void test41() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a (  foaf:knows+ || (^rdfs:seeAlso) +) ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test42() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a (  foaf:knows || ^rdfs:seeAlso )+ ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test43() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:e "
                + "ex:e foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a ( foaf:knows+ || (^rdfs:seeAlso)+ ) ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

   
   

    @Test
    public void test47() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<person><name>John</name><lname>K</lname></person>"
                + "<person><name>James</name><lname>C</lname></person>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = ""
                + "prefix i: <http://www.inria.fr/test/> "
                + "construct {"
                + "[i:name ?name]"
                + "} where {"
                + "select (concat(?n, '.', ?l) as ?name) where {"
                + "?x i:contain ?xml "
                + "bind (xpath(?xml, '/doc/person') as ?p) "
                + "bind (xpath(?p, 'name/text()')  as ?n)  "
                + "bind (xpath(?p, 'lname/text()') as ?l) "
                + "}}";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test49() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<phrase><subject>Cat</subject><verb>on</verb><object>mat</object></phrase>"
                + "<phrase><subject>Cat</subject><verb>eat</verb><object>mouse</object></phrase>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = ""
                + "base      <http://www.example.org/schema/>"
                + "prefix s: <http://www.example.org/schema/>"
                + "prefix i: <http://www.inria.fr/test/> "
                + "construct {?su ?pr ?o} "
                + "where {"
                + "select  * where {"
                + "values ?xml {<file://"
                + text + "phrase.xml>"
                + "}"
                + "bind  (xpath(?xml, '/doc/phrase')   as ?st)"
                + "bind  (xpath(?st, 'subject/text()')  as ?s)"
                + "bind  (xpath(?st, 'verb/text()')     as ?p) "
                + "bind  (xpath(?st, 'object/text()')   as ?o) "
                + "bind  (uri(?s) as ?su) "
                + "bind  (uri(?p) as ?pr)   "
                + "}}";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //System.out.println(map);
            ResultFormat f = ResultFormat.create(map);
            //System.out.println(f);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("Result", 2, null);
        }

    }

    @Test
    public void test50() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":A0 :P :A1, :A2 . "
                + ":A1 :P :A0, :A2 . "
                + ":A2 :P :A0, :A1"
                + "}";

        String query =
                "prefix : <http://example.org/>"
                + "select * where { :A0 ((:P)*)* ?X }";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test51() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":A0 :P :A1, :A2 . "
                + ":A1 :P :A0, :A2 . "
                + ":A2 :P :A0, :A1"
                + "}";

        String query =
                "prefix : <http://example.org/>"
                + "select * where { ?X ((:P)*)* :A1 }";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test52() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String query =
                "prefix : <http://example.org/>"
                + "select * where { :a (:p/:q)+ ?y }";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test53() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * (kg:similarity() as ?sim) where {"
                + "?x rdf:type c:Engineer "
                + "}"
                + "order by desc(?sim)"
                + "pragma {kg:match kg:mode 'strict'}";

        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            //System.out.println(map);

            assertEquals("Result", 7, map.size());


        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test54() {
        String query = "select * where {"
                + "graph ?g {?s <name> ?o} "
                + "?s <age> ?a"
                + "}";
        Graph graph = createGraph();

        Node g = graph.addGraph("test");
        Node s = graph.addResource("URIJohn");
        Node p = graph.addProperty("age");
        Node o = graph.addLiteral(24);
        Node p2 = graph.addProperty("name");
        Node o2 = graph.addLiteral("John");

        Edge e = graph.addEdge(g, s, p, o);
        graph.addEdge(s, p2, o2);


        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            //System.out.println(map);

            assertEquals("Result", 1, map.size());


        } catch (EngineException ee) {
            assertEquals("Result", true, ee);
        }
    }

    /**
     * Two graphs with partial ontology each Each graph answer with its
     * viewpoint on the ontology
     */
    @Test
    public void test55() {


        String o1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Human rdfs:subClassOf foaf:Person "
                + "}";

        String o2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "}";

        String init1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<John> a foaf:Human"
                + "}";

        String init2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<Jack> a foaf:Man"
                + "}";




        String query = "prefix foaf: <http://foaf.org/>"
                + "select * where {"
                + "?x a foaf:Person"
                + "}";

        Graph o = Graph.create(true);
        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        QueryProcess exec1 = QueryProcess.create(g1);
        QueryProcess exec2 = QueryProcess.create(g2);

        QueryProcess exec = QueryProcess.create(g1, true);
        exec.add(g2);


        try {
            exec1.query(o1);
            exec1.query(init1);

            exec2.query(o2);
            exec2.query(init2);

//			exec.query(o1);
//			exec.query(o2);


            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());
            //System.out.println(map);


        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test56() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);
        //exec.setOptimize(true);

        String init = "insert data {"
                + "graph <g1> {<John> foaf:knows <Jim> }"
                + "graph <g2> {<Jim> foaf:knows <Jack>}"
                + "}";

        String query = "select  * where {"
                + "?x foaf:knows+ ?y "
                + "filter(?y = <Jack> || <John> = ?x)"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test57() {
        Graph graph = createGraph();
       // QueryProcess.definePrefix("e", "htp://example.org/");
        QueryProcess exec = QueryProcess.create(graph);

        RuleEngine re = RuleEngine.create(graph);

        String rule =
                "prefix e: <htp://example.org/>"
                + "construct {[a e:Parent; e:term(?x ?y)]}"
                + "where     {[a e:Father; e:term(?x ?y)]}";

        String rule2 = "prefix e: <htp://example.org/>" +
                "construct {[a e:Father;   e:term(?x ?y)]}"
                + "where     {[a e:Parent;   e:term(?x ?y)]}";


        String rule3 = "prefix e: <htp://example.org/>" +
                "construct {[a e:Parent]}"
                + "where     {[a e:Father]}";

        String rule4 = "prefix e: <htp://example.org/>" +
                "construct {[a e:Father]}"
                + "where     {[a e:Parent]}";


        try {
            re.defRule(rule);
            re.defRule(rule2);
        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String init = "prefix e: <htp://example.org/>" +"insert data {"
                + "[ a e:Father ; e:term(<John> <Jack>) ]"
                + "}";

        String query = "prefix e: <htp://example.org/>" +"select  * where {"
                + //"?x foaf:knows ?z " +
                "[a e:Parent; e:term(?x ?y)]"
                + "}";

        try {
            exec.query(init);
            // re.setDebug(true);
            re.process();
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 1, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test58() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <Jack> "
                + "}";

        String query = "select * (pathLength($path) as ?l) where {"
                + "?x short(foaf:knows|rdfs:seeAlso)+ :: $path ?y"
                + "}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            ResultFormat f = ResultFormat.create(map);
            //System.out.println(f);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test59() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:name 'Jack' ;"
                + "foaf:age 12 ;"
                + "foaf:date '2012-04-01'^^xsd:date ;"
                + "foaf:knows [] "
                + "}";

        String query = "select * where {?x ?p ?y}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            XMLFormat f = XMLFormat.create(map);

            XMLResult xml = XMLResult.create(exec.getProducer());
            Mappings m = xml.parseString(f.toString());
            //System.out.println(m);

            assertEquals("Result", 5, map.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test62() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:age 12 "
                + "<James> foaf:age 20"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:age ?age"
                + "}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            assertEquals("Result", 2, map.size());

            exec.filter(map, "?age > 15");
            assertEquals("Result", 1, map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test63() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<A> foaf:date '2012-05-10'^^xsd:dateTime "
                + "<B> foaf:date '2012-05-11'^^xsd:dateTime "
                + "<C> foaf:date '2012-05-10T10:20:30'^^xsd:dateTime "
                + "<D> foaf:date '2012-05-10T10:30:30.50'^^xsd:dateTime "
                + "<E> foaf:date '2012-05-10T10:30:30'^^xsd:dateTime "
                + "}";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:date ?date"
                + "}"
                + "order by desc(?date)";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 5, map.size());

            IDatatype dt0 = (IDatatype) map.get(0).getNode("?x").getValue();
            IDatatype dt1 = (IDatatype) map.get(1).getNode("?x").getValue();
            IDatatype dt2 = (IDatatype) map.get(2).getNode("?x").getValue();
            IDatatype dt3 = (IDatatype) map.get(3).getNode("?x").getValue();
            IDatatype dt4 = (IDatatype) map.get(4).getNode("?x").getValue();

            assertEquals("Result", "B", dt0.getLabel());
            assertEquals("Result", "D", dt1.getLabel());
            assertEquals("Result", "E", dt2.getLabel());
            assertEquals("Result", "C", dt3.getLabel());
            assertEquals("Result", "A", dt4.getLabel());

            // B D E C A


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test64() {

        QueryProcess exec = QueryProcess.create(graph);

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select (kg:ancestor(c:Event, c:Document) as ?a) where {"
                + ""
                + "}";

        try {
            Mappings map = exec.query(query);

            Node aa = graph.getResource("http://www.inria.fr/acacia/comma#Something");
            Node rr = map.getNode("?a");

            assertEquals("Result", aa.getLabel(), rr.getLabel());


            Node n1 = graph.getResource("http://www.inria.fr/acacia/comma#Person");
            Node n2 = graph.getResource("http://www.inria.fr/acacia/comma#Event");

            //System.out.println("ANC: " + n1);
            //System.out.println("ANC: " + n2);
            graph.setClassDistance();
            Node vv = graph.getClassDistance().ancestor(n1, n2);
            assertEquals("Result", aa.getLabel(), vv.getLabel());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test65() {
        Graph g = createGraph();

        Load ld = Load.create(g);

        try {
            ld.parse(data + "test/iso.ttl");
            ld.parse(data + "test/iso.rdf");

            ld.parse(data + "test/utf.ttl");
            ld.parse(data + "test/utf.rdf");
        } catch (LoadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String query = "select * where {"
                + "?x ?p ?y . ?z ?q ?y filter(?x != ?z)"
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 4, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testRelax() {
        Graph g = graph();

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "<John> foaf:type c:Researcher "
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:type c:Engineer "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:type c:Fireman "
                + "<e> foaf:type c:Event "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select   more * (kg:similarity() as ?s) where {"
                + "?x foaf:type c:Engineer "
                + "?x foaf:knows ?y "
                + "?y foaf:type c:Engineer"
                + "}"
                + "order by desc(?s) "
                + "pragma {kg:kgram kg:relax (foaf:type)}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create a Query graph from an RDF Graph Execute the query Use case: find
     * similar Graphs (cf Corentin)
     */
    
    public void testQueryGraph() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String cons =
                "prefix : <http://example.org/> "
                + ""
                + "construct {?x :p []}"
                + "where {?x :p ?y}";

        String init2 =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p [] ."
                + "}";


        try {
            // create a graph
            exec.query(init);

            // create a copy where triple objects (values) are Blank Nodes (aka Variables)
            // consider the copy as a Query Graph and execute it
            Mappings map = exec.queryGraph(cons);

            assertEquals("Results", 4, map.size());

            Graph g2 = createGraph();
            QueryProcess exec2 = QueryProcess.create(g2);
            exec2.query(init2);

            QueryGraph qg = QueryGraph.create(g2);
            QGVisitor vis = new QGVisitor();
            //qg.setVisitor(vis);
            qg.setConstruct(true);
            map = exec.query(qg);

            Graph res = exec.getGraph(map);
            assertEquals("Results", 2, res.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testOption() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :p :d, :a "
                + ":c :p :d "
                + ""
                + ":e :p :b, :c ."
                + ""
                + "} ";

        String query =
                "prefix : <http://example.org/> "
                + "select *  where  {"
                + "?x ((:p/:p) ?)  ?y "
                + "}";


        try {

            exec.query(init);
            Mappings map = exec.query(query);


            assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testWF() {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        QueryEngine qe = QueryEngine.create(g);
        g.addEngine(qe);

        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "[ c:hasParent [] ]"
                + "}";


        String update =
                "prefix c: <http://example.org/>"
                + "insert {?y c:hasChild ?x}"
                + "where { ?x c:hasParent ?y}";

        qe.addQuery(update);
//		qe.setDebug(true);
//		g.getWorkflow().setDebug(true);

        String query = "select * where {?x ?p ?y}";

        try {
            ////System.out.println("init");
            exec.query(init);
            ////System.out.println("query");

            Mappings map = exec.query(query);
//			//System.out.println(map);
//			//System.out.println(map.size());

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            assertEquals("Result", true, false);
        }



    }

    @Test
    public void testCompile() {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String query =
                "select * where {"
                + "graph ?g {?x ?p ?y "
                + "{select * where {"
                + "?a (rdf:type@[a rdfs:Resource]) ?b  "
                + "{values ?a {<John>}}"
                + "}"
                + "order by ?a "
                + "group by ?b "
                + "having (?a > ?b) "
                + "}"
                + "?a (rdf:type@[a rdfs:Resource]) ?b"
                + ""
                + "}"
                + "}";

        try {
            Mappings map = exec.query(query);
            Query q = map.getQuery();
            System.out.println("NB Procesor: " + Processor.count);
            assertEquals("Result", 20, q.nbNodes());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    IDatatype getValue(Mapping map, String name) {
        return datatype(map.getValue(name));
    }

    IDatatype getValue(Mappings map, String name) {
        Object value = map.getValue(name);
        if (value == null) {
            return null;
        }
        return datatype(value);
    }

    IDatatype datatype(Object n) {
        return (IDatatype) n;
    }

    IDatatype datatype(Node n) {
        return (IDatatype) n.getValue();
    }
}
