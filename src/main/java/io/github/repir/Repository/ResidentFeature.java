/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.repir.Repository;

/**
 * Features that can be fully read into memory, which is mostly used for fast
 * iteration over the same data for tuning, but can also be used to speed up retrieval
 * in general.
 * @author jer
 */
public interface ResidentFeature {
   public abstract void readResident();

   public boolean isReadResident();
}
