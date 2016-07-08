package junit;



import java.io.FileInputStream;
import java.io.FileNotFoundException;


import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.exceptions.*;
import fr.inria.acacia.corese.storage.api.Parameters;

import fr.inria.edelweiss.engine.core.Engine;
import fr.inria.edelweiss.engine.model.api.LBind;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgpipe.Pipe;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.EdgeFactory;
import fr.inria.edelweiss.kgraph.core.EdgeIndexer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.IOException;
import java.util.Date;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;


/**
 * Test rule engines and pipeline
 *
 */
public class TestRuleEngine {
	
//	static String data = "/user/corby/home/workspace/coreseV2/src/test/resources/data/";
        static String data = TestRuleEngine.class.getClassLoader().getResource("data").getPath()+"/";
//	static String root = "/user/corby/home/workspace/kgengine/src/test/resources/data/";
        static String root = TestRuleEngine.class.getClassLoader().getResource("data").getPath()+"/";

	static Graph graph;
	static Engine rengine;
	static RuleEngine fengine;

        	
	@BeforeClass
	public static void init() throws EngineException {	
		//Graph.setCompareIndex(true);
		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");	
                //Load.setDefaultGraphValue(true);
                //EdgeIndexer.test = false;

		graph = createGraph(true);
		Load load = Load.create(graph);
                //QueryProcess.setPlanDefault(Query.QP_HEURISTICS_BASED);
            try {
                load.parse(data + "engine/ontology/test.rdfs");
                load.parse(data + "engine/data/test.rdf");

                load.parse(data + "engine/rule/test2.brul");
                load.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		
		fengine = load.getRuleEngine();
                fengine.setSpeedUp(true);

		QueryProcess exec = QueryProcess.create(graph);
		rengine = Engine.create(exec);

		rengine.load(data + "engine/rule/test2.brul");
		rengine.load(data + "engine/rule/meta.brul");
	}
        
     @AfterClass
    static public void finish(){
        EdgeFactory.trace();
    }   
	
     static  GraphStore createGraph() {
          return createGraph(false);
      }
  
        
     static GraphStore createGraph(boolean b) {
        GraphStore g = GraphStore.create(b);
        Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 2);
        //g.setStorage(IStorage.STORAGE_FILE, p);
        return g;
    }
       
     
      @Test
    public void testOWLRL() throws EngineException, IOException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        try {
            ld.parse(data + "template/owl/data/primer.owl");
            ld.parse(data + "owlrule/owlrllite.rul");
        } catch (LoadException ex) {
            System.out.println(ex);
        }
        RuleEngine re = ld.getRuleEngine();
        Date d1 = new Date();
        re.setProfile(re.OWL_RL_FULL);
        re.process();

        String q = "prefix f: <http://example.com/owl/families/>"
                + "select * "
                + "where {"
                + "graph kg:rule {"
                + "?x ?p ?y "
                + "filter (isURI(?x) && strstarts(?x, f:) "
                + "    && isURI(?y) && strstarts(?y, f:))"
                + "}"
                + "filter not exists {graph ?g {?x ?p ?y filter(?g != kg:rule)}}"
                + "}"
                + "order by ?x ?p ?y";
        Mappings map = exec.query(q);
        assertEquals(103, map.size());

    }

    @Test
    public void testOWLRL2() throws EngineException, IOException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        try {
            ld.parse(data + "template/owl/data/primer.owl");
            ld.parse(data + "owlrule/owlrllite.rul");
        } catch (LoadException ex) {
            System.out.println(ex);
        }
        RuleEngine re = ld.getRuleEngine();
        Date d1 = new Date();
        //re.setProfile(re.OWL_RL);
        re.process();

        String q = "prefix f: <http://example.com/owl/families/>"
                + "select * "
                + "where {"
                + "graph kg:rule {"
                + "?x ?p ?y "
                + "filter (isURI(?x) && strstarts(?x, f:) "
                + "    && isURI(?y) && strstarts(?y, f:))"
                + "}"
                + "filter not exists {graph ?g {?x ?p ?y filter(?g != kg:rule)}}"
                + "}"
                + "order by ?x ?p ?y";

        Mappings map = exec.query(q);
        assertEquals(103, map.size());

    }

     
        
         @Test
     public void testOWLRL3() throws LoadException, EngineException{
        GraphStore g = GraphStore.create();
        Load ld = Load.create(g);
        ld.parse(data + "template/owl/data/primer.owl");
        RuleEngine re = RuleEngine.create(g);
        re.setProfile(RuleEngine.OWL_RL_LITE);
        re.process();
        
        String q = "select * "
                + "from kg:rule "
                + "where { ?x ?p ?y }";
        
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        
        assertEquals(611, map.size());
        
        String qq = "select distinct ?p ?pr "
                + "from kg:rule "
                + "where { ?x ?p ?y bind (kg:provenance(?p) as ?pr) }";
        
        map = exec.query(qq);
        
        //assertEquals(31, map.size());
        
        String qqq = "select distinct ?q  "
                + "from kg:rule "
                + "where { "
                + "?x ?p ?y bind (kg:provenance(?p) as ?pr) "
                + "graph ?pr { [] sp:predicate ?q }"
                + "} order by ?q";
        
    
        map = exec.query(qqq);
        
        //assertEquals(19, map.size());
        
        String q4 = "select ?q  "
                + "where { "
                + "graph eng:engine { ?q a sp:Construct }"
                + "} ";
        
        map = exec.query(q4);
        
        assertEquals(64, map.size());
        
        String q5 = "select ?q  "
                + "where { "
                + "graph eng:record { ?r a kg:Index }"
                + "} ";
        
        map = exec.query(q5);
        
        assertEquals(159, map.size());
        
        String q6 = "select ?r  "
                + "where { "
                + "graph kg:re2 {  ?r a kg:Index  }"
                + "} ";
        
        map = exec.query(q6);
        assertEquals(3, map.size());
        
         String q7 = "select ?r  "
                + "where { "
                + "graph eng:queries {  ?r a sp:Construct  }"
                + "} ";
        
        map = exec.query(q7);
        assertEquals(4, map.size());
     }
        
   
         @Test 
    public void testOWLRL4() throws LoadException, EngineException {
        GraphStore g = GraphStore.create();
        Load ld = Load.create(g);
        ld.parse(data + "template/owl/data/primer.owl");
        RuleEngine re = RuleEngine.create(g);
        re.setProfile(RuleEngine.OWL_RL_LITE);
        //re.process();
        g.addEngine(re);
        String q = "select * "
                + "from kg:rule "
                + "where { ?x ?p ?y }";
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);

        assertEquals(611, map.size());
    }

        @Test
        
        public void testRuleOptimization() throws LoadException, EngineException { 
            Graph g1 = testRuleOpt();
            Graph g2 = testRuleNotOpt();
            
            QueryProcess e1 = QueryProcess.create(g1, true);
            QueryProcess e2 = QueryProcess.create(g2, true);
            
            String q = "prefix c: <http://www.inria.fr/acacia/comma#>"
                    + "select distinct ?x where {"
                    + "?x a c:Person ; "
                    + " c:hasCreated ?doc "
                    + "?doc a c:Document"
                    + "}";
            
            Mappings m1 = e1.query(q);
            Mappings m2 = e2.query(q);
            assertEquals(m1.size(), m2.size());
        }      
    
    public Graph testRuleOpt() throws LoadException, EngineException {       
        RuleEngine re = testRules();
        Graph g = re.getRDFGraph();
        
        re.setSpeedUp(true);
        System.out.println("Graph: " + g.size());
        Date d1 = new Date();
        re.process();
        Date d2 = new Date();
        System.out.println("** Time opt: " + (d2.getTime() - d1.getTime()) / ( 1000.0));
         validate(g, 37735);     
                 
        assertEquals(54028, g.size());
        return g;
    }
        
 
        
    public Graph testRuleNotOpt() throws LoadException, EngineException {
        RuleEngine re = testRules();
        Graph g = re.getRDFGraph();
        
        System.out.println("Graph: " + g.size());
        Date d1 = new Date();
        re.process();
        Date d2 = new Date();
        System.out.println("** Time std: " + (d2.getTime() - d1.getTime()) / ( 1000.0));

        validate(g, 41109);                
        assertEquals(57402, g.size());
        return g;
            
    }
  
          
       
     RuleEngine testRules() throws LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        ld.parse(data + "comma/comma.rdfs");
        ld.parseDir(data + "comma/data");
        ld.parseDir(data + "comma/data2");
        try {
            ld.parse(data + "owlrule/owlrllite-junit.rul");
        } catch (LoadException e) {
            e.printStackTrace();
        }
        RuleEngine re = ld.getRuleEngine();
        return re;
               
    } 
        
     void validate(Graph g, int n) throws EngineException{
         QueryProcess exec = QueryProcess.create(g);
         String q = "select * "
                 + "from kg:rule "
                 + "where {?x ?p ?y}";
         
         Mappings map = exec.query(q);
         assertEquals(n, map.size());
     }
     
    
     
	
	@Test
	public void test1(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select ?x ?y where { " +
			"?y c:hasSister ?z" +
			"?x c:hasBrother ?y " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 13, bind.size());
	}
	
	
	@Test
	public void test2(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 4, bind.size());
	}
	
	
	@Test
	public void test3(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre ?x c:hasID ?id " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 0, bind.size());
	}
	
	
	@Test
	public void test4(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";

		fengine.process();
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
	}
	
	@Test
	public void test44(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		String ent = "select * where {graph kg:entailment {?x ?p ?y}}";

		graph.process(fengine);
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
			
			map = exec.query(ent);
			//System.out.println(map);
			
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
	}
	
	
	public void test5(){

		Graph g = createGraph();
		Pipe pipe = Pipe.create(g);
		pipe.load(root + "pipe/pipe.rdf");
		pipe.process();
		
		QueryProcess exec = QueryProcess.create(g);
		String query = "select * where {graph ?g {?x ?p ?y}}";
		try {
			Mappings map = exec.query(query);
			//System.out.println(map);
			assertEquals("Result", 9, map.size());
		} catch (EngineException e) {
			assertEquals("Result", 9, e);
		}
		
	}
	
	
	
	/**
	 * Rule engine with QueryExec on two graphs
	 */
	@Test
	public void test6() throws LoadException{
		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");	

		Graph g1 = createGraph(true);
		Graph g2 = createGraph(true);

		Load load1 = Load.create(g1);
		Load load2 = Load.create(g2);
		
		load1.parse(data + "engine/ontology/test.rdfs");
		load2.parse(data + "engine/data/test.rdf");

		QueryProcess exec = QueryProcess.create(g1);
		exec.add(g2);
		RuleEngine re = RuleEngine.create(g2, exec);
		//re.setOptimize(true);
		
		load2.setEngine(re);
		
		try {
			load2.parse(data + "engine/rule/test2.brul");
			load2.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Engine rengine = Engine.create(exec);

		rengine.load(data + "engine/rule/test2.brul");
		rengine.load(data + "engine/rule/meta.brul");
		
		
		
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		
		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 4, bind.size());
		//System.out.println(bind);
		

		re.process();
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 4, map.size());
			//System.out.println(map);
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
		
	}
	
	
	
	
	
	public void test7() throws LoadException{
		Graph g1 = createGraph(true);
		Load load1 = Load.create(g1);
		load1.parse(root + "sdk/sdk.rdf");
		
		String init = "load <" + root + "rule/server.rul> into graph kg:rule";
		String query = "select * where {?x a ?class}";
		QueryProcess exec = QueryProcess.create(g1);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			//System.out.println(map);
			assertEquals("Result", 6, map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Test
	public void test8(){
		Graph g = createGraph();
		QueryProcess exec = QueryProcess.create(g);
		
		QueryEngine qe = QueryEngine.create(g);
		String query = "insert data { <John> rdfs:label 'John' }";
		qe.addQuery(query);
		
		qe.process();
		
		assertEquals("Result", 1, g.size());


	}

	
	
	
	
	@Test
	public void testWF(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		String ent = "select * where {graph kg:entailment {?x ?p ?y}}";

		graph.addEngine(fengine);
		
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
			
			map = exec.query(ent);
			//System.out.println(map);
			
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
		
		
		
		
		
		
		
