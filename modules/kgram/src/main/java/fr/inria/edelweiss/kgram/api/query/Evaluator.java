package fr.inria.edelweiss.kgram.api.query;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.filter.Extension;
import fr.inria.edelweiss.kgram.filter.Proxy;

/**
 * Interface for the connector that evaluates filters
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 */
public interface Evaluator {

    int KGRAM_MODE = 0;
    int SPARQL_MODE = 1;

    int CACHE_MODE = 101;
    int NO_CACHE_MODE = 102;

    Proxy getProxy();

    void setMode(int mode);

    int getMode();

    void setDebug(boolean b);


    /**
     * Evaluate a filter
     *
     * @param f
     * @param e
     * @return
     */
    boolean test(Filter f, Environment e);

    boolean test(Filter f, Environment e, Producer p);

    /**
     * Evaluate a filter and return a Node
     * use case: select fun(?x) as ?y
     *
     * @param f
     * @param e
     * @return
     */
    Node eval(Filter f, Environment e, Producer p);

    Object eval(Expr f, Environment e, Producer p);

    Object eval(Expr f, Environment e, Producer p, Object[] values);

    Object eval(Expr f, Environment e, Producer p, Object[] values, String name);

    Object eval(Expr f, Environment e, Producer p, Object[] values, Extension ext);

    Object eval(Expr f, Environment e, Producer p, Object[] values, Expr ee);

    Expr getDefine(Expr exp, Environment env, Producer p, int n);

    Expr getDefine(Environment env, String name, int n);

    Expr getDefine(String name);

    int compare(Environment env, Producer p, Node n1, Node n2);

    // cast Java object into IDatatype
    Node cast(Object obj, Environment e, Producer p);


    /**
     * Evaluate a filter and return a list of Node
     * use case: ?doc xpath('/book/title') ?title
     *
     * @param f
     * @param e
     * @return
     */
    List<Node> evalList(Filter f, Environment e);

    /**
     * Evaluate an extension function filter and return Mappings
     * use case: select sql('select from where') as (?x ?y) where {}
     * TODO: should be an interface instead of Mappings
     *
     * @param f
     * @param e
     * @param nodes
     * @return
     */
    Mappings eval(Filter f, Environment e, List<Node> nodes);

    void setProducer(Producer p);

    void setKGRAM(Object o);

    Object getEval();

    void addResultListener(ResultListener rl);

    void start(Environment env);

    void finish(Environment env);

}
