grammar Query;

@header {
package io.github.repir.QueryParser;

import java.util.ArrayList;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.*;
import io.github.repir.Strategy.*;
import io.github.repir.Strategy.Operator.*;
import io.github.repir.tools.lib.Log;

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
   @init { }
   : (endterm 
      { 
          root.add( $endterm.feature );
      } )+
;

endterm returns [ ArrayList<Operator> feature ]
   //options{greedy=true;}
   :
   term
      { $feature = $term.feature;
      }
   (WS)? ( weight 
      { 
        $feature.get( $feature.size() - 1).setweight( $weight.value );
      }
   )? 
   (WS)? ( channel 
      { $feature.get( $feature.size() - 1).setchannel( $channel.value );
      }
   )? 
;

term returns [ ArrayList<Operator> feature ]
   : phrase { $feature = $phrase.feature; }
;

phrase returns [ ArrayList<Operator> feature ]
   @init { $feature = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>(); }
   @after { if ($feature.size() > 1) {
               ProximityOperatorOrdered o = new ProximityOperatorOrdered( root, $feature );
               o.setMinimalSpan();
               for (String v : varl)
                  o.setGenericL( v );
               for (String v : vard)
                  o.setGenericD( v );
               $feature = new ArrayList<Operator>();
               $feature.add(o);
            }
          }
   : syn2 { $feature = $syn2.feature; } 
     | ( BRACKOPEN (WS?)
       ( ( term  { $feature.addAll( $term.feature ); } 
         | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
          )(WS)?
        )+ 
      BRACKCLOSE )
;

syn2 returns [ ArrayList<Operator> feature ]
   @init { $feature = new ArrayList<Operator>();
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>();  }
   @after { if ( $feature.size() > 1) {
               Operator o = new SynonymOperator( root, $feature );
               for (String v : varl)
                  o.setGenericL( v );
               for (String v : vard)
                  o.setGenericD( v );
               $feature = new ArrayList<Operator>();
               $feature.add(o);
            }
          }
   : set { $feature = $set.feature; }
     | BLOCKOPEN (WS)?
      (( term  { $feature.addAll( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
       )(WS)?
      )+
     BLOCKCLOSE
;

set returns [ ArrayList<Operator> feature ]
   @init { $feature = new ArrayList<Operator>(); 
           ArrayList<String> varl = new ArrayList<String>(); 
           ArrayList<String> vard = new ArrayList<String>(); }
   @after { if ($feature.size() > 1) {
               Operator o = new ProximityOperatorUnordered( root, $feature );
               for (String v : varl)
                  o.setGenericL( v );
               for (String v : vard)
                  o.setGenericD( v );
               $feature = new ArrayList<Operator>();
               $feature.add( o );
            }
          }
   : phrase2 { $feature = $phrase2.feature; }
   | BRACEOPEN (WS?) ((term { $feature.addAll( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
      )(WS)?
      )+ 
      BRACECLOSE 
;

phrase2 returns [ ArrayList<Operator> feature ]
   @init { $feature = new ArrayList<Operator>();  }
   @after { if ($feature.size() > 1) {
               ProximityOperatorOrdered o = new ProximityOperatorOrdered( root, $feature );
               o.setMinimalSpan();
               $feature = new ArrayList<Operator>();
               $feature.add(o);
            }
         }
   : syn { $feature.addAll( $syn.feature ); }
     ('-' endterm { $feature.addAll( $endterm.feature ); } )*
;

syn returns [ ArrayList<Operator> feature ]
   @init { $feature = new ArrayList<Operator>(); }
   @after { if ($feature.size() > 1) {
               Operator o = new SynonymOperator( root, $feature ); 
               $feature = new ArrayList<Operator>();
               $feature.add(o);
            }
          }
   : func { $feature.add( $func.feature ); }
     ((WS)? '|' (WS)? term { $feature.addAll( $term.feature ); } )* 
;

func returns [ Operator feature ]
   : TERM { $feature = root.getTerm( $TERM.text ); }
   | FUNCTION { ArrayList<String> varl = new ArrayList<String>(); 
                ArrayList<String> vard = new ArrayList<String>(); 
                String functionclass = $FUNCTION.text; 
                functionclass = functionclass.substring(0, functionclass.length()-1);
              }
     BRACKOPEN WS? { ArrayList<Operator> list = new ArrayList<Operator>(); }
     ( ( term  { list.addAll( $term.feature ); } 
       | VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        vard.add( $VARIABLE.text );
                     else 
                        varl.add( $VARIABLE.text ); }
       )(WS)*
     )+
     BRACKCLOSE { 
               $feature = root.construct( functionclass, list );
               for (String v : varl)
                  $feature.setGenericL( v );
               for (String v : vard)
                  $feature.setGenericD( v );
     }
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

TERM      : (('@')? CHAR+)|('@' '#' DIGIT+) ;
VARIABLE  : CHAR+ '=' FLOAT;
ALTTERM   : CHAR+ '|';
FUNCTION  : CHAR+ ':';
CHANNEL   : '~' CHAR+;

WS        : (' '|'\t'|'\n'|'\r')+;
WEIGHT    : '#' FLOAT;
FLOAT     : DIGIT+ ('.' DIGIT+ (('e'|'E') ('-'|'+')? DIGIT+)?)?;
fragment DIGIT     : ('0'..'9');
fragment CHAR      : ('a'..'z'|'A'..'Z'|'0'..'9'|'\''|'.') ;
BRACKOPEN  : '(';
BRACKCLOSE : ')';
BRACEOPEN  : '{';
BRACECLOSE : '}';
BLOCKOPEN  : '[';
BLOCKCLOSE : ']';




