grammar Query;

@header {
    package io.github.repir.QueryParser;
    import java.util.ArrayList;
    import io.github.repir.Repository.*;
    import io.github.repir.Retriever.*;
    import io.github.repir.Strategy.*;
    import io.github.repir.tools.Lib.Log;
}

@members {
   public static Log log = new Log( QueryParser.class );
   public GraphRoot root;
   public int channels;
   public int bodychannel;
   public int titlechannel;
}

/** 
This grammar file compiles to a parser, that converts a query string into a Document Processing Graph.
The simple syntax rules:
termA -> FeatureTerm( Extract( termA ) ) (i.e. termA is converted into a FeatureTerm object that uses the Extracted TF feature of termA)
termA termB ...-> FeatureTerm( Extract( termA ) ) + FeatureTerm( Extract( termB) ... ) (i.e. scores are added, as in a logical OR)
{termA termB ...} -> FeatureProximityUnordered( Exract(termA) Extract(termB) ... ) i.e. an unordered phrase, unlimited distance
(termA termB ...) -> ordered phrase (FeatureProximityOrdered)
termA-termB-... -> ordered phrase with span limitation (also uses FeatureProximityOrdered, but limits distance span=n)
[termA termB ...] -> FeatureSynonym( Extract(termA) Extract(termB) ) i.e. termA and termB are regarded as the same symbol.
termA|termB|... -> same as [termA termB ...]
Feature:(termA termB ...) -> constructs a feature of class 'Feature', and passes an arraylist of features contained within the brackets

Some constructions allow optional parameters:
termA#weight  to set the weight of a term (must be a float or scientific notation), this works for any type of feature (e.g. {termA termB}#0.2)
{termA termB ... [span=?] [tf=?] [df=?]} to limit the span, set the tf/df fr proper processing of phrase occurrences
(termA termB ... [span=?] [tf=?] [df=?]) same as {} but for ordered phrases
[termA termB ... [tf=?] [df=?]] to set the tf/df fr proper processing of synonym occurrences
*/

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

endterm returns [ GraphNode feature ]
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
        log.info("aap");
      }
   )? 
;

phrase returns [ GraphNode feature ]
   @init { $feature = new FeatureProximityOrdered( root ); }
   @after { if ($feature.containedfeatures.size() == 1)
                $feature = $feature.containedfeatures.get(0);
          }
   :  ( BRACKOPEN (WS?)
      ( ( term       { ((FeatureProximity)$feature).add( $term.feature ); }
         |VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        $feature.setGenericD( $VARIABLE.text );
                     else 
                        $feature.setGenericL( $VARIABLE.text ); }
        )(WS)?
      )+ 
      BRACKCLOSE )
;

phrase2 returns [ FeatureProximity feature ]
   @init { $feature = new FeatureProximityOrdered( root ); }
   @after { 
             $feature.setspan( new Long( $feature.containedfeatures.size() ));
          }
   : 
      ( PHRASETERM 
          { $feature.add( root.getTerm( $PHRASETERM.text.substring(0, $PHRASETERM.text.length()-1 )));  }
      )+
      TERM
          { $feature.add( root.getTerm( $TERM.text ) ); }
;

syn returns [ FeatureSynonym feature ]
   @init { $feature = new FeatureSynonym( root ); }
   : 
      (ALTTERM  
          { $feature.add( root.getTerm( $ALTTERM.text.substring(0, $ALTTERM.text.length() - 1 ) ) ); 
          }
          (WS)?
      )+
      TERM
          { $feature.add( root.getTerm( $TERM.text ) ); }
;

syn2 returns [ FeatureSynonym feature ]
   @init { $feature = new FeatureSynonym( root ); }
   : 
      BLOCKOPEN (WS)?
      (( term { $feature.add( $term.feature );  }
         |VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        $feature.setGenericD( $VARIABLE.text );
                     else 
                        $feature.setGenericL( $VARIABLE.text ); }
       )(WS)?
      )+
     BLOCKCLOSE
;

set returns [ FeatureProximity feature ]
   @init { $feature = new FeatureProximityUnordered( root ); }
   :   BRACEOPEN (WS?)
      ( (term        { $feature.add( $term.feature ); }
         |VARIABLE  { if ($VARIABLE.text.indexOf(".") >= 0)
                        $feature.setGenericD( $VARIABLE.text );
                     else 
                        $feature.setGenericL( $VARIABLE.text ); }
      )(WS)?
      )+ 
      BRACECLOSE 
;

function returns [ GraphNode feature ]
   @init { ArrayList<GraphNode> terms = new ArrayList<GraphNode>(); 
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

term returns [ GraphNode feature ]
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




