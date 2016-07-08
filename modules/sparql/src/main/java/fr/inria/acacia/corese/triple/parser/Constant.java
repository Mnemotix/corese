package fr.inria.acacia.corese.triple.parser;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Constant extends Atom {
	private static Logger logger = Logger.getLogger(Constant.class);
	private static NSManager nsm;
	static DatatypeMap dm;
	boolean isQName = false;
	IDatatype dt;
	String datatype = null;
	
	int weight = 1;
	private Variable var;
	// draft regexp
	private Expression exp;
        
        static {        
            dm = DatatypeMap.create();
            nsm = NSManager.create();
	}

	public Constant() {}

	 private Constant(String name) {
		super(name);
		// by safety:
		setLongName(name);
		datatype = RDFS.RDFSRESOURCE;
	}

	 private Constant(String name, String datatype, String lg) {
		super(name);
                this.datatype = datatype;
                setDatatypeValue(DatatypeMap.createLiteral(name, nsm.toNamespace(datatype), lg));
	}

	 private Constant(String name, String dt) {
		this(name, dt, null);
	}
		  	
	public static Constant create(String str){
		Constant cst = new Constant(str);
                cst.setDatatypeValue(DatatypeMap.createResource(cst.getLongName()));
                return cst;
	}
        
        public static Constant createResource(String str, String longName){
		Constant cst = new Constant(str);
                cst.setLongName(longName);
                cst.setDatatypeValue(DatatypeMap.createResource(longName));
                return cst;
	}
	
	public static Constant createBlank(String label){
		Constant cst = new Constant(label);
                cst.setDatatypeValue(DatatypeMap.createBlank(label));
		return cst;
	}
	
	public static Constant createResource(String str){
		Constant cst =  new Constant(str);		
		cst.setLongName(nsm.toNamespace(str));
                cst.setDatatypeValue(DatatypeMap.createResource(cst.getLongName()));
		return cst;
	}
        
        public static Constant createString(String str){
              return create(str, RDFS.qxsdString);      
        }

	
	public static Constant create(int n){
		return new Constant(Integer.toString(n), RDFS.xsdinteger);
	}
        
        public static Constant create(double d){
		return new Constant(Double.toString(d), RDFS.xsddouble);
	}
	
	public static Constant create(boolean b){
		return new Constant((b)?"true":"false", RDFS.xsdboolean);
	}
	
	public static Constant create(String name, String dt) {
		return new Constant(name, dt, null);		
	}
	
	public static Constant create(String name, String dt, String lg) {
		Constant cst = new Constant(name, dt, lg);
		return cst;
	}
        
        public String getDatatype() {
		return datatype;
	}
	
	void setDatatype(String str) {
		datatype = str;
	}
        
        boolean hasRealDatatype() {
		if (datatype == null) return false;
		for (String str : RDFS.FAKEDT){
			if (datatype.equals(str)){
				return false;
			}
		}
		return true;				
	}

//	// only xsd/rdf datatype (no rdfs:Literal no rdfs:Resource)
//	public String getRealDatatype() {
//		if (! hasRealDatatype()){
//			return null;
//                }
//                else {
//			return datatype;
//                }
//	}

	public String getLang() {
		return dt.getLang();
	}

	public boolean hasLang() {
            String lg = dt.getLang();
            return lg != null && lg != "";
	}


       public StringBuffer toString(StringBuffer sb) { 
		if (isLiteral()){						
			if (hasLang()) {
				//return name + "@" + lang;
				toString(name, sb);
				sb.append(KeywordPP.LANG + getLang());
			} 
			else if (hasRealDatatype()) {
				if (datatype.equals(RDF.qxsdInteger) || datatype.equals(RDF.xsdinteger)){
					sb.append(name);
				}
				else if (datatype.startsWith("http://")){
					toString(name, sb);
					sb.append(KeywordPP.SDT + "<"+ datatype +">");
				}
				else {
					toString(name, sb);
					if (! datatype.equals(RDF.qxsdString)){
						sb.append(KeywordPP.SDT + datatype);
					}
				}
			} 
			else {
				toString(name, sb);
				return sb;
			}
		}
		else if (isBlank()) {
			sb.append(name);
		} 
		else if (isQName) {
			sb.append(name);
		} 
		else {
			sb.append(KeywordPP.OPEN + getLongName() + KeywordPP.CLOSE);
		}
		return sb;
	}
	
	public StringBuffer toString2(StringBuffer sb) {
		String str = getDatatypeValue().toString();
		sb.append(str);
		return sb;
	}
	
	/**
	 * Escape special chars
	 * Add surrounding quotes to a string literal
	 */
	public static void toString(String str, StringBuffer sb){
		String s = addEscapes(str);
		String sep = KeywordPP.QUOTE;
		
		if (s.contains(KeywordPP.QUOTE)){
			if (s.contains(KeywordPP.DQUOTE)){
				sep = KeywordPP.TQUOTE;
			}
			else {
				sep = KeywordPP.DQUOTE;
			}
		}
		
		sb.append(sep);
		sb.append(s);
		sb.append(sep);				
	}
	
	
	
	/**
	 *  source: javacc
	 *  replace special char by escape char for pprint
	 */
         public static String addEscapes(String str) {
             return addEscapes(str, true);
         }
        
	 public static String addEscapes(String str, boolean quote) {
	      StringBuilder retval = new StringBuilder();
	      char ch;
	      for (int i = 0; i < str.length(); i++) {
	        switch (str.charAt(i))
	        {
	           case 0 :
	              continue;
	           case '\b':
	              retval.append("\\b");
	              continue;
	           case '\t':
	              retval.append("\\t");
	              continue;
	           case '\n':
	              retval.append("\\n");
	              continue;
	           case '\f':
	              retval.append("\\f");
	              continue;
	           case '\r':
	              retval.append("\\r");
	              continue;
	           case '\"':
	              retval.append("\\\"");
	              continue;
	           case '\'':
                      if (quote) {retval.append("\\\'");}
                      else {retval.append(str.charAt(i));}	              
                      continue;
	           case '\\':
	              retval.append("\\\\");
	              continue;
	           default:
	        	   retval.append(str.charAt(i));
	        	   
//	              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
//	                 String s = "0000" + Integer.toString(ch, 16);
//	                 retval.append("\\u" + s.substring(s.length() - 4, s.length()));
//	              } else {
//	                 retval.append(ch);
//	              }
	              	              
	              continue;
	        }
	      }
	      return retval.toString();
	   }

	
	public Constant getConstant() {
		return this;
	}
	
	public boolean equals(Object c){
		if (c instanceof Constant){
			Constant cc = (Constant) c;
			return getDatatypeValue().sameTerm(cc.getDatatypeValue());
		}
		return false;
	}
	
	static String getJavaType(String datatypeURI){		
		return dm.getJType(nsm.toNamespace(datatypeURI));
	}
	
        @Override
	public IDatatype getDatatypeValue(){
		//if (dt == null) dt = createDatatype();
		return dt;
	}
	
	public Expression prepare(ASTQuery ast){
		getDatatypeValue();
		return this;
	}
	
	/**
	 * Create Constant from IDatatype
	 */
	public static Constant create(IDatatype dt){
		Constant cst;
		if (dt.isLiteral()){
			cst = create(dt.getLabel(), dt.getDatatype().getLabel(), dt.getLang());
		}
		else if (dt.isURI()){
			// URI & Blank
			cst = create(dt.getLabel());			
		}
                else {
                    cst = createBlank(dt.getLabel());
                }
		cst.setDatatypeValue(dt);
		return cst;
	}
		
	public void setDatatypeValue(IDatatype dd){
		dt = dd;
	}

	public int regLength(){
		return 1;
	}
	
	public int length(){
		return 1;
	}
		

	public boolean isConstant() {
		return true;
	}

	public boolean isLiteral() {
           return dt.isLiteral();
    }
	
	public boolean isBlank(){
            return dt.isBlank();
	}
		
	public void setWeight(String w){
		try {
			setWeight(Integer.parseInt(w));
		}
		catch (Exception e){
		}
	}
	
	public void setWeight(int w){
		weight = w;
	}

	
	public int getWeight(){
		return weight;
	}
	
	public boolean isNumber(){
		return false;
	}
	
	public boolean isResource() {
            return dt.isURI();
	}

	// use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    public void setVar(Variable s) {
        var = s;
    }
    
    public void setExpression(Expression e) {
        exp = e;
    }
    
    public Expression getExpression() {
        return exp;
    }

    // use when "get:name::?x" or "c:SomeRelation::?p" because we have both a variable and a constant
    public Variable getIntVariable() {
        return var;
    }

        public boolean isQName() {
		return isQName;
	}
	
	public void setQName(boolean isQName) {
		this.isQName = isQName;
	}
    
	/**
	 * KGRAM
	 */
	
        @Override
	public int type(){
		return ExprType.CONSTANT;
	}
	
        @Override
	public IDatatype getValue(){
		return dt;
	}
	
	
	public Constant copy(){
		Constant cst;
		if (isLiteral()){
			cst = create(getName(), getDatatype(), getLang());                       
		}
		else if (isResource()){
			cst = createResource(getName(), getLongName());
			cst.setQName(isQName);
		}
                else {
                    cst = createBlank(getName());
                }
                cst.setDatatypeValue(getDatatypeValue());
		return cst;
	}
        
//        public Constant copySave(){
//		Constant cst;
//		if (isLiteral()){
//			cst = new Constant(getLabel(), getDatatype(), getLang());                       
//		}
//		else {
//			cst = new Constant(getLabel());
//			cst.setQName(isQName);
//		}
//		cst.setLongName(getLongName());
//		return cst;
//	}
        
         void visit(ExpressionVisitor v){
            v.visit(this);
        }
	
	public Expression transform(boolean isReverse){
		Constant cst = this;
		if (isReverse){
			cst = copy();
			cst.setReverse(isReverse);
			cst.setWeight(getWeight());
		}
		cst.setretype(cst.getretype());
		return cst;
	}
        
        
}