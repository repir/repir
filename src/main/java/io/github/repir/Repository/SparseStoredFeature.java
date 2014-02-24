package io.github.repir.Repository;

import io.github.repir.tools.Content.RecordIdentity;

public abstract class SparseStoredFeature<F extends RecordIdentity, C> extends StoredReportableFeature<F, C> implements ReducableFeature {

   public SparseStoredFeature(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public String getLabel() {
      return this.getClass().getSimpleName();
   }

   public abstract C getValueFromFile();

   public abstract void write(C value);

   public C readValue(int id) {
      read(id);
      return getValueFromFile();
   }
}
