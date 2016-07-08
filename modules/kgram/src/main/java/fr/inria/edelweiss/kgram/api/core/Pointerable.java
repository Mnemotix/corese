package fr.inria.edelweiss.kgram.api.core;

import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable extends Loopable {

    int UNDEF_POINTER = -1;
    int MAPPINGS_POINTER = 1;
    int MAPPING_POINTER = 2;
    int ENTITY_POINTER = 3;
    int GRAPH_POINTER = 4;
    int NSMANAGER_POINTER = 5;
    int CONTEXT_POINTER = 6;
    int QUERY_POINTER = 7;
    int METADATA_POINTER = 8;
    int DATASET_POINTER = 9;

    int pointerType();

    Mappings getMappings();

    Mapping getMapping();

    Entity getEntity();

    Query getQuery();

    TripleStore getTripleStore();

    // let ((?x, ?y) = ?m)
    // ->
    // let (?x = xt:gget(?m, "?x", 0))
    Object getValue(String var, int n);

    int size();

}
