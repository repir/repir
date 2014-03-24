grammar Query;

@header {
package io.github.repir.QueryParser;

import java.util.ArrayList;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.*;
import io.github.repir.Strategy.*;
import io.github.repir.Strategy.Operator.*;
import io.github.repir.tools.Lib.Log;

/** 
 * !!!Do not edit this code!!! 
 * It was generated with Antlr using the Query.g4 source code. 
 * <p/>
 * This parser converts a query string into a Graph that is processed for every
 * entity retrieved by a {@link RetrievalModel}. Prasing uses the following syntax rules:
 * <ul>
 * <li>termA -> Term( termA ) (i.e. termA is converted into a Term object with termA as parameter)
 * <li>termA termB" -> Term( termA ) Term( termsB ) (i.e. whitespace separates terms)
 * <li>{termA termB ...} -> FeatureProximityUnordered( termA, termB, ... ) by default maximum span = unlimited
 * <li>(termA termB ...) -> FeatureProximityOrdered( termA, termB, ...), by default maximum span = #terms
 * <li>termA-termB-... -> (termA termB ...)
 * <li>[termA termB ...] -> FeatureSynonym( Term(termA) Term(termB) )
 * <li>termA|termB|... -> same as [termA termB ...]
 * <li>ClassName:(termA termB ...) -> constructs a instance of class 'ClassName', and passes an arraylist of features contained within the brackets
 * </ul>
 * Some constructions allow optional parameters to be passed. requirements are that 
 * parameternames must be lowercase, the Feature MUST have methods set<parametername>() implemented,
 * (which is not checked, so use at you own risk), and only long (ints) and double values
 * are supported. For example:
 * <ul>
 * <li>termA#weight  weight must be a float or scientific notation, this uses setweight(value) on the feature
 * <li>termA-termB#weight sets the weight for the entire ProximityOperator, not for termB
 * <li>{termA termB ... [span=?] [cf=?] [df=?]} uses setspan(long) setcf(long) and setdf(long) on the feature
 * <li> (termA termB ... [span=?] [cf=?] [df=?]) same as {} but for ordered phrases
 * <li> [termA termB ... [cf=?] [df=?]] to set the cf/df fr proper processing of synonym occurrences
 * <li> ClassName:( termA [parama=?] [parama=?] )#weight uses setparama(?) and setparamb(?)
 * <li> termA~channel uses setchannel(channel) to set channel (e.g. title), may not be implemented 
 * </ul>
*/
}

@members {
   public static Log log = new Log( QueryParser.class );
   public GraphRoot root;
   public int channels;
   public int bodychannel;
   public int titlechannel;
}

prog 
   @init { 
   }
   :
   (endterm 
      { 
          root.add( $endterm.feature );
      }
   )+
;

endterm returns [ Operator feature ]
   //options{greedy=true;}
   :
   term
      { $feature = $term.feature;
      }
   (WS)? ( weight 
      { $feature.setweight( $weight.value );
      }
   )? 
   (WS)? ( channel 
      { $feature.setchannel( $channel.value );
      }
   )? 
;

phrase returns [ Operator feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>(); }
   @after { if (terms.size() == 1)
               $feature = terms.get(0);
            else {
               ProximityOperatorOrdered f = new ProximityOperatorOrdered( root, terms );
               f.setspan( new Long( f.containednodes.size() ));
               for (String v : varl)
                  f.setGenericL( v );
               for (String v : vard)
                  f.setGenericD( v );
               $feature = f;
            }
          }
   :  ( BRACKOPEN (WS?)
      ( ( term  { terms.add( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
        )(WS)?
      )+ 
      BRACKCLOSE )
;

phrase2 returns [ ProximityOperatorOrdered feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>();  }
   @after { $feature = new ProximityOperatorOrdered( root, terms );
            $feature.setspan( new Long( $feature.containednodes.size() ));
          }
   : 
      ( PHRASETERM 
          { terms.add( root.getTerm( $PHRASETERM.text.substring(0, $PHRASETERM.text.length()-1 )));  }
      )+
      TERM
          { terms.add( root.getTerm( $TERM.text ) ); }
;

syn returns [ SynonymOperator feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>(); }
   @after { $feature = new SynonymOperator( root, terms ); }
   : 
      (ALTTERM  
          { terms.add( root.getTerm( $ALTTERM.text.substring(0, $ALTTERM.text.length() - 1 ) ) ); 
          }
          (WS)?
      )+
      TERM
          { terms.add( root.getTerm( $TERM.text ) ); }
;

syn2 returns [ SynonymOperator feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>();  }
   @after { $feature = new SynonymOperator( root, terms );
            for (String v : varl)
               $feature.setGenericL( v );
            for (String v : vard)
               $feature.setGenericD( v );
          }
   : 
      BLOCKOPEN (WS)?
      (( term  { terms.add( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
       )(WS)?
      )+
     BLOCKCLOSE
;

set returns [ ProximityOperatorUnordered feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>(); }
   @after { $feature = new ProximityOperatorUnordered( root, terms );
            for (String v : varl)
               $feature.setGenericL( v );
            for (String v : vard)
               $feature.setGenericD( v );
          }
   :   BRACEOPEN (WS?)
      ( (term  { terms.add( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
      )(WS)?
      )+ 
      BRACECLOSE 
;

function returns [ Operator feature ]
   @init { ArrayList<Operator> terms = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>(); 
           String functionclass;
         }
   @after { $feature = root.construct( functionclass, terms );
            for (String v : varl)
               $feature.setGenericL( v );
            for (String v : vard)
               $feature.setGenericD( v );
          }
   : FUNCTION { functionclass = $FUNCTION.text; 
                functionclass = functionclass.substring(0, functionclass.length()-1);
              }
     BRACKOPEN WS?
     ( ( term  { terms.add( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
       )(WS)*
     )+
     BRACKCLOSE
;

term returns [ Operator feature ]
   : TERM 
       { $feature = root.getTerm( $TERM.text ); }
   | phrase
       { $feature = $phrase.feature; }
   | function
       { $feature = $function.feature; }
   | phrase2
       { $feature = $phrase2.feature; }
   | syn
       { $feature = $syn.feature; }
   | syn2
       { $feature = $syn2.feature; }
   | set
       { $feature = $set.feature; }
;



weight returns [ Double value ]
   :
   WEIGHT 
      { $value = Double.valueOf( $WEIGHT.text.substring(1) ); }
;

channel returns [ String value ]
   :
   CHANNEL 
      { $value = $CHANNEL.text.substring(1); }
;


TERM      : CHAR+ ;
PHRASETERM: CHAR+ '-';
VARIABLE  : CHAR+ '=' FLOAT;
ALTTERM   : CHAR+ '|';
FUNCTION  : CHAR+ ':';
CHANNEL   : '~' CHAR+;

WS        : (' '|'\t'|'\n'|'\r')+;
WEIGHT    : '#' FLOAT;
FLOAT     : DIGIT+ ('.' DIGIT+ (('e'|'E') ('-'|'+')? DIGIT+)?)?;
fragment DIGIT     : ('0'..'9');
fragment CHAR      : ('a'..'z'|'A'..'Z'|'0'..'9'|'\''|'.'|'&') ;
BRACKOPEN  : '(';
BRACKCLOSE : ')';
BRACEOPEN  : '{';
BRACECLOSE : '}';
BLOCKOPEN  : '[';
BLOCKCLOSE : ']';




