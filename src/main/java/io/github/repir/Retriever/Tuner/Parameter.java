package io.github.repir.Retriever.Tuner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;

/**
 * Class used for a free parameters that needs to be tuned.
 * @author Jeroen Vuurens
 */
public abstract class Parameter implements Comparable<Parameter> {

   public static Log log = new Log(Parameter.class);
   public int index;
   public String parameter;
   final public BigDecimal upper, lower, width;
   protected BigDecimal targetstep;
   protected BigDecimal step;
   protected BigDecimal optimum;
   protected ArrayList<BigDecimal> points;
   protected TreeSet<BigDecimal> set;

   public Parameter(String parameter, String setting) {
      this.parameter = parameter;
      String[] spart = setting.split("[.][.]");
      if (spart.length == 1) {
         lower = upper = new BigDecimal(spart[0]);
      } else {
         log.info("param %s %s %s %s", parameter, setting, spart[0], spart[1]);
         lower = new BigDecimal(spart[0]);
         upper = new BigDecimal(spart[1]);
      }
      width = upper.subtract(lower);
      if (spart.length > 2) {
         targetstep = new BigDecimal(spart[2]);
      } else {
         double scale = Math.pow(10, -Math.max(lower.scale(), upper.scale()));
         targetstep = new BigDecimal(scale);
      }
      reset();
   }

   public void reset() {
      points = null;
      set = new TreeSet<BigDecimal>();
      generatePoints();
   }

   public abstract void generatePoints();

   public ArrayList<BigDecimal> getPoints() {
      if (points == null) {
         points = new ArrayList<BigDecimal>(set);
      }
      return points;
   }

   public void add(BigDecimal p) {
      if (p.compareTo(BigDecimal.ZERO) > 0 && !set.contains(p)) {
         //log.info("add %s", p);
         set.add(p);
         points = null;
      }
   }

   public int size() {
      return getPoints().size();
   }

   public BigDecimal get(int i) {
      return getPoints().get(i);
   }

   public String getStr(int i) {
      return parameter + "=" + getPoints().get(i);
   }

   public int compareTo(Parameter o) {
      return parameter.compareTo(o.parameter);
   }

   public boolean onEdge(BigDecimal v) {
      int i = points.indexOf(v);
      return i < 1 || i == points.size() - 1;
   }

   protected int pointIndex(BigDecimal value) {
      return getPoints().indexOf(value);
   }

   public BigDecimal below(BigDecimal v) {
      int i = pointIndex(v);
      return points.get(i - 1);
   }

   public BigDecimal above(BigDecimal v) {
      int i = pointIndex(v);
      return points.get(i + 1);
   }
}
