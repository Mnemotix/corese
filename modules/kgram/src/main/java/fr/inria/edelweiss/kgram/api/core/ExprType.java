package fr.inria.edelweiss.kgram.api.core;

public interface ExprType {

    int UNDEF = -1;
    int UNBOUND = -2;
    int LOCAL = -3;
    int GLOBAL = -4;

    // abstract type
    int JOKER = 1;
    int ALTER = 2;

    // type
    int CONSTANT = 6;
    int VARIABLE = 7;
    int BOOLEAN = 8;
    int TERM = 9;
    int FUNCTION = 10;


    // boolean
    int AND = 11;
    int OR = 12;
    int NOT = 13;

    // function
    int BOUND = 14;
    int COUNT = 15;
    // ?x in (1, 2)
    int INLIST = 16;

    int SAMETERM = 17;
    int CUSTOM = 18;
    int ISNUMERIC = 19;
    int MIN = 20;
    int MAX = 21;
    int AVG = 22;
    int SUM = 23;
    int ISURI = 24;
    int ISBLANK = 25;
    int ISLITERAL = 26;
    int LANG = 27;
    int LANGMATCH = 28;
    int REGEX = 29;

    int DATATYPE = 30;
    int CAST = 31;
    int SELF = 32;
    int DEBUG = 33;
    int EXTERNAL = 34;
    int EXTERN = 35;
    int KGRAM = 36;
    int SQL = 37;
    int XPATH = 38;
    int SKIP = 39;

    int LENGTH = 40;
    int UNNEST = 41;
    int EXIST = 42;
    int STRDT = 43;
    int STRLANG = 44;
    int BNODE = 45;
    int COALESCE = 46;
    int IF = 47;
    int SYSTEM = 48;
    int GROUPCONCAT = 49;
    int SAMPLE = 50;

    int STRLEN = 51;
    int SUBSTR = 52;
    int UCASE = 53;
    int LCASE = 54;
    int ENDS = 55;
    int STARTS = 56;
    int CONTAINS = 57;
    int ENCODE = 58;
    int CONCAT = 59;

    int YEAR = 60;
    int MONTH = 61;
    int DAY = 62;
    int HOURS = 63;
    int MINUTES = 64;
    int SECONDS = 65;
    int TIMEZONE = 66;
    int NOW = 67;

    int ABS = 68;
    int FLOOR = 69;
    int ROUND = 70;
    int CEILING = 71;
    int RANDOM = 72;

    int HASH = 73;
    int URI = 74;
    int TZ = 75;
    int STR = 76;

    int STRBEFORE = 77;
    int STRAFTER = 78;
    int STRREPLACE = 79;
    int FUUID = 80;
    int STRUUID = 81;
    int XSDSTRING = 82;
    int APPROXIMATE = 83;
    int APP_SIM = 84;


    // term
    int TEQ = 101;
    int TNEQ = 102;
    int TLE = 103;
    int TGE = 104;
    int TLT = 105;
    int TGT = 106;

    int EQNE = 109;

    int EQ = 110;
    int NE = 111;
    int NEQ = 111;

    int GL = 112;
    int LE = 113;
    int GE = 114;
    int LT = 115;
    int GT = 116;

    int PLUS = 117;
    int MINUS = 118;
    int MULT = 119;

    int DIV = 120;

    int CONT = 121; // ~
    int START = 122; // ^
    int IN = 123;
    int POWER = 124;


    // extension

    int DISPLAY = 200;
    int NUMBER = 201;
    int SIM = 202;
    int EXTEQUAL = 203;
    int EXTCONT = 204;
    int PROCESS = 205;
    int ENV = 206;
    int DEPTH = 207;
    int KG_GRAPH = 208;
    int NODE = 209;
    int GET_OBJECT = 210;
    int SET_OBJECT = 211;
    int LOAD = 212;
    int PATHNODE = 213;
    int GROUPBY = 214;
    int PSIM = 215;
    int GETP = 216;
    int SETP = 217;
    int PWEIGHT = 218;
    int ANCESTOR = 219;
    int PROVENANCE = 220;
    int INDEX = 221;
    int TIMESTAMP = 222;
    int ID = 223;
    int TEST = 224;
    int DESCRIBE = 225;
    int STORE = 226;


    int TURTLE = 227;
    int LEVEL = 228;
    int INDENT = 229;
    int PPURI = 230;
    int URILITERAL = 231;
    int VISITED = 232;
    int AGGAND = 233;
    int PROLOG = 234;
    int WRITE = 235;
    int FOCUS_NODE = 236;
    int XSDLITERAL = 237;
    int QNAME = 238;

    int STL_DEFAULT = 239;
    int STL_DEFINE = 240;
    int STL_NL = 241;
    int STL_PREFIX = 242;
    int STL_AGGREGATE = 243;
    int STL_CONCAT = 244;
    int STL_GROUPCONCAT = 245;
    int STL_AND = 246;
    int STL_NUMBER = 247;
    int STL_LOAD = 248;
    int STL_IMPORT = 249;
    int STL_PROCESS = 250;


    int APPLY_TEMPLATES = 251;
    int APPLY_TEMPLATES_WITH = 252;
    int APPLY_TEMPLATES_ALL = 253;
    int APPLY_TEMPLATES_WITH_ALL = 254;
    int APPLY_TEMPLATES_GRAPH = 255;
    int APPLY_TEMPLATES_WITH_GRAPH = 256;
    int APPLY_TEMPLATES_NOGRAPH = 257;
    int APPLY_TEMPLATES_WITH_NOGRAPH = 258;
    int CALL_TEMPLATE = 259;
    int CALL_TEMPLATE_WITH = 260;
    int STL_TEMPLATE = 261;

    int STL_SET = 262;
    int STL_GET = 263;
    int STL_BOOLEAN = 264;
    int STL_VISIT = 265;
    int STL_VISITED = 266;
    int STL_FUTURE = 267;
    int STL_INDEX = 268;
    int STL_VSET = 269;
    int STL_VGET = 270;
    int STL_PROCESS_URI = 271;
    int STL_EXPORT = 272;
    int STL_ERRORS = 273;
    int STL_ISSTART = 274;
    int AGGLIST = 275;
    int AGGREGATE = 276;
    int STL_FORMAT = 277;
    int STL_VISITED_GRAPH = 278;


    int ISSKOLEM = 300;
    int SKOLEM = 301;

    int QUERY = 302;
    int EXTENSION = 303;
    int EVEN = 304;
    int ODD = 305;
    int READ = 306;
    int PACKAGE = 307;

    int IOTA = 308;
    int LIST = 309;
    int MAP = 310;
    int MAPLIST = 311;
    int APPLY = 312;
    int LET = 313;
    int LAMBDA = 314;
    int ERROR = 315;
    int MAPEVERY = 316;
    int MAPANY = 317;
    int MAPFIND = 318;
    int MAPFINDLIST = 319;
    int MAPMERGE = 320;
    int MAPFUN = 321;
    int SET = 322;
    int SEQUENCE = 323;
    int RETURN = 324;
    int EVAL = 325;
    int FUNCALL = 326;
    int FOR = 327;

    int XT_SORT = 330;

    int XT_MAPPING = 400;
    int XT_ADD = 401;
    int XT_CONCAT = 402;
    int XT_COUNT = 403;
    int XT_CONS = 404;
    int XT_FIRST = 405;
    int XT_REST = 406;
    int XT_GET = 407;
    int XT_SET = 408;
    int XT_REVERSE = 409;
    int XT_APPEND = 410;

    int XT_SUBJECT = 411;
    int XT_OBJECT = 412;
    int XT_PROPERTY = 413;
    int XT_VALUE = 414;
    int XT_INDEX = 415;
    int XT_GRAPH = 416;
    int XT_REJECT = 417;
    int XT_VARIABLES = 418;
    int XT_VALUES = 419;
    int XT_EDGE = 420;
    int XT_TRIPLE = 421;
    int XT_GEN_GET = 422;
    int XT_DISPLAY = 423;
    int XT_TUNE = 424;

    int XT_UNION = 425;
    int XT_MINUS = 426;
    int XT_OPTIONAL = 427;
    int XT_JOIN = 428;
    int XT_QUERY = 429;
    int XT_AST = 430;
    int XT_CONTEXT = 431;
    int XT_METADATA = 432;
    int XT_FROM = 433;
    int XT_NAMED = 434;

    int SLICE = 450;
    int EDGE_LEVEL = 451;


}
