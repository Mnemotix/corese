package fr.inria.edelweiss.kgram.api.core;

import java.util.List;


/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */

public interface Expr {
	
	Filter getFilter();
	
	// Exp as Object for modularity
	Object getPattern();
        boolean isSystem();
        boolean isPublic();
        void setPublic(boolean b);
        boolean isTrace();
        boolean isDebug();
        boolean isTester();

	String getLabel();
        String getShortName();
	
	String getModality();

	List<Expr> getExpList();

	Expr getExp(int i);
	void setExp(int i, Expr e);
	void addExp(int i, Expr e);
        
	Expr getArg();
	void setArg(Expr exp);

	Object getValue();
        DatatypeValue getDatatypeValue();
	
	int type();
        
        int subtype();

	int oper();
        
        void setOper(int n);

	boolean isAggregate();
	
	boolean isRecAggregate();
	
	boolean isExist();

	boolean isRecExist();

        boolean isVariable();
        
        boolean isFuncall();
	
	boolean isBound();
	
	boolean isDistinct();

	int arity();
        
        int place();

	int getIndex();

	void setIndex(int index);
                
        Expr getDefine();
        
        void setDefine(Expr exp);
        
        Expr getFunction();
        
        Expr getBody();
        
        Expr getVariable();
        
        Expr getDefinition();
        	
}
