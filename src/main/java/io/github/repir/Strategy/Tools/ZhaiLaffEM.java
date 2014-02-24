package io.github.repir.Strategy.Tools;

import io.github.repir.tools.Lib.Log;
import java.util.Collection;
import java.util.Set;

public class ZhaiLaffEM {

   public static Log log = new Log(ZhaiLaffEM.class);

   public static void score(Collection<element> elements, double lambda, double threshold) {
      double diff = threshold;
      while (diff >= threshold) {
         diff = 0;
         for (element t : elements) {
            double w = (1 - lambda) * t.getP() / ((1 - lambda) * t.getP() + lambda * t.getCMLE());
            diff += Math.abs(w - t.getW());
            t.setW(w);
         }

         double sum = 0;
         for (element t : elements) {
            t.setP(t.getTF() * t.getW());
            sum += t.getP();
         }
         for (element t : elements) {
            t.setP(t.getP() / sum);
         }
      }
   }

   public static void score2(Collection<element> elements, double lambda, double threshold) {
      double diff = threshold;
      int ttf = 0;
      while (diff >= threshold) {
         diff = 0;
         for (element t : elements) {
            double w = (1 - lambda) * t.getP() / ((1 - lambda) * t.getP() + lambda * t.getCMLE());
            diff += Math.abs(w - t.getW());
            t.setW(w);
            ttf += t.getTF();
         }

         double sum = 0;
         for (element t : elements) {
            double utf = t.getTF() - t.getCMLE() * ttf;
            if (utf < 0) {
               utf = 0;
            }
            t.setP(utf * t.getW());
            sum += t.getP();
         }
         for (element t : elements) {
            t.setP(t.getP() / sum);
         }
      }
   }

   public static interface element {

      double getP();

      double getW();

      double getCMLE();

      int getTF();

      void setP(double p);

      void setW(double w);
   }
}
