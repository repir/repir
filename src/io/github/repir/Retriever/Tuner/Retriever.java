package io.github.repir.Retriever.Tuner;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Retriever.PostingIteratorReusable;
import java.io.IOException;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Retriever that supports running the same model with several parameter
 * settings by reusing the loaded data.
 * <p/>
 * The result of the retrieval process is a combination of parameter settings
 * and the resulting mean average precision. These are stored in the
 * {@link ModelParameters} feature.
 * <p/>
 * Two modes of operation are currently supported. (1) if
 * "testset.crossevaluate=fold", 10-fold cross evaluation is performed, by
 * dividing the set sequentially into 10 folds, and adding "fold=<foldnr>" to
 * the parameters that are stored with the results. (2) leave-a-testset-out if
 * "testset.crossevaluate" contains a (comma separated list of) other testsets.
 * Then the results for the entire set are scored per parameter setting, e.g.
 * "testset.crossevaluate=trec2kld,trec3kld".
 * <p/>
 */
public class Retriever extends io.github.repir.Retriever.Reusable.Retriever {

   public static Log log = new Log(Retriever.class);
   protected PostingIteratorReusable postingiterator;

   public Retriever(Repository repository) {
      super(repository);
   }

   public Retriever(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository, mappercontext);
   }

   public Retriever(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository, reducercontext);
   }

   @Override
   public Job createJob(String path) throws IOException {
      return new Job(this, path);
   }

   /**
    * @return a list of Variants to be tuned. These {@link Query.Variant} can be
    * added to the Query to tune. By default, only combinations of parameter
    * settings in configured parameter's range, that are not in ModelParameters,
    * are returned. By setting "tuner.overwrite=true", all variants within
    * parameter range are tried.
    */
   public ArrayList<Query.Variant> getVariants() {
      ArrayList<Parameter> parameters = getParameters();
      ArrayList<Query.Variant> variants = new ArrayList<Query.Variant>();
      ArrayList<String> settings = generatePoints(parameters);
      if (!repository.configuredBoolean("tuner.overwrite", false)) {
         settings = removeKnownSettings(repository, settings);
      }
      for (String conf : settings) {
         Query.Variant v = new Query.Variant();
         v.configuration = conf;
         v.retrievalmodelclass = repository.configuredString("retriever.strategy");
         v.scorefunctionclass = repository.configuredString("retriever.scorefunction");
         variants.add(v);
      }
      return variants;
   }

   /**
    * @return a list of Parameters that have been configured as
    * "strategy.freeparameter". This implementation supports a grid search, thus
    * parameters will be configured with a range and step, e.g.
    * "+strategy.freeparameter=kld.mu=100..2500..100" will try settings 100,
    * 200, ..., 2500 for "kld.mu".
    */
   public ArrayList<Parameter> getParameters() {
      ArrayList<Parameter> parameters = new ArrayList<Parameter>();
      for (Map.Entry<String, String> p : repository.getFreeParameters().entrySet()) {
         parameters.add(new ParameterGrid(p.getKey(), p.getValue()));
      }
      Collections.sort(parameters);
      for (int i = 0; i < parameters.size(); i++) {
         Parameter p = parameters.get(i);
         p.index = i;
         p.generatePoints();
      }
      return parameters;
   }

   /**
    * returns a list of Strings with all possible combinations of parameter
    * settings within range.
    */
   public ArrayList<String> generatePoints(ArrayList<Parameter> parameters) {
      ArrayList<String> settings = new ArrayList<String>();
      int parami[] = new int[parameters.size()];
      for (int i = 0; i < parameters.size(); i++) {
         parami[i] = parameters.get(i).getPoints().size() - 1;
      }
      while (parami[0] >= 0) {
         settings.add(getSettings(parameters, parami));
         for (int i = parameters.size() - 1; i >= 0; i--) {
            if (i < parameters.size() - 1) {
               parami[i + 1] = parameters.get(i + 1).getPoints().size() - 1;
            }
            if (--parami[i] >= 0) {
               break;
            }
         }
      }
      return settings;
   }

   private String getSettings(ArrayList<Parameter> parameters, int settings[]) {
      ArrayList<String> list = new ArrayList<String>();
      for (Parameter p : parameters) {
         String pstr = p.parameter + "=" + p.getPoints().get(settings[p.index]).toString();
         list.add(pstr);
      }
      return ArrayTools.concatStr(list, ",");
   }

   private ArrayList<String> removeKnownSettings(Repository repository, ArrayList<String> settings) {
      String[] storedparameters = repository.getStoredFreeParameters();
      repository.getConfiguration().setInt("fold", 0); // for if n-fold is used
      ModelParameters modelparameters = (ModelParameters) repository.getFeature(ModelParameters.class, repository.configurationName());
      modelparameters.setDataBufferSize(1000000);
      modelparameters.openRead();
      Iterator<String> iter = settings.iterator();
      while (iter.hasNext()) {
         String s = iter.next();
         repository.addConfiguration(s);
         ModelParameters.Record newRecord = modelparameters.newRecord(storedparameters);
         ModelParameters.Record found = modelparameters.read(newRecord);
         if (found != newRecord) {
            iter.remove();
         }
      }
      return settings;
   }

   /**
    * Checks if the settings in ModelParameters are complete, i.e. are recorded for
    * all folds. You should not need this, it is only for testing.s
    */
   public ArrayList<String> removeKnownSettingsFold(Repository repository, ArrayList<String> settings) {
      String[] storedparameters = repository.getStoredFreeParameters();
      ModelParameters modelparameters = (ModelParameters) repository.getFeature(ModelParameters.class, repository.configurationName());
      modelparameters.setDataBufferSize(1000000);
      modelparameters.openRead();
      Iterator<String> iter = settings.iterator();
      while (iter.hasNext()) {
         String s = iter.next();
         repository.addConfiguration(s);
         boolean allthere = true;
         for (int i = 0; i < 10; i++) {
            repository.getConfiguration().setInt("fold", i);
            ModelParameters.Record newRecord = modelparameters.newRecord(storedparameters);
            ModelParameters.Record found = modelparameters.read(newRecord);
            if (found == newRecord) {
               allthere = false;
               break;
            }
         }
         if (allthere) {
            iter.remove();
         }
      }
      return settings;
   }
}
