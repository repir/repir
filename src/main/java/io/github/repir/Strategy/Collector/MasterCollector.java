package io.github.repir.Strategy.Collector;

import java.util.ArrayList;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.Repository.Repository;

/**
 * {@link GraphRoot} internally uses a MasterCollector to manage and control all
 * {@link Collector}s.
 * <p/>
 * @author jeroen
 */
public class MasterCollector extends ArrayList<Collector> {

    public static Log log = new Log(MasterCollector.class);
    private Repository repository;
    private ArrayList<String> reducers;

    public MasterCollector() {
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public ArrayList<String> getReducers() {
        if (reducers == null) {
            reducers = new ArrayList<String>();
            for (String r : repository.configuredStrings("retriever.reducers")) {
                reducers.add(r);
            }
        }
        return reducers;
    }

    public int getReducerID(Collector c) {
        //log.info("%s %s %s %d", c.getCanonicalName(), getReducers(), c.getReducerIDs(), getReducers().indexOf(c.getReducerIDs()));
        if (getReducers().size() == 0)
            return 0;
        else
            return getReducers().indexOf(c.getReducerName());
    }

    /**
     * Should be called once, after the GraphRoot is initialized and prepared
     * for retrieval. During this phase, the collectors can prepare themselves
     * for retrieval.
     */
    public void prepareAggregation() {
        getReducers();
        for (Collector c : this) {
            c.prepareAggregation();
        }
    }

    public void prepareRetrieval() {
        getReducers();
        for (Collector c : this) {
            c.doPrepareRetrieval();
        }
    }

    public void finishSegmentRetrieval() {
        for (int i = size() - 1; i >= 0; i--) {
            get(i).finishSegmentRetrieval();
        }
    }

    /**
     * Process a document, having all SubCollectors call their Feature nodes to
     * collect the data required.
     * <p/>
     * @param doc the document to be processed
     */
    public void collect(Document doc) {
        for (Collector s : this) {
            s.processRetrievedDocument(doc);
        }
    }

    /**
     * provides a hook for SubCollectors to do a post retrieval processing step
     * before the collected results are pulled.
     */
    public void postLoadFeatures(int partition) {
        log.s("postLoadFeatures");
        for (Collector s : this) {
            s.postLoadFeatures(partition);
        }
        log.e("postLoadFeatures");
    }

    public void reuse() {
        for (Collector s : this) {
            s.reuse();
        }
    }

    public void finishReduce() {
        HashMap<String, ArrayList<CollectorCachable>> list
                = new HashMap<String, ArrayList<CollectorCachable>>();
        for (Collector c : this) {
            if (c instanceof CollectorCachable) {
                ArrayList<CollectorCachable> a = list.get(c.getCanonicalName());
                if (a == null) {
                    a = new ArrayList<CollectorCachable>();
                    list.put(c.getCanonicalName(), a);
                }
                a.add((CollectorCachable) c);
            }
        }
        for (Map.Entry<String, ArrayList<CollectorCachable>> entry : list.entrySet()) {
            CollectorCachable writer = null;
            for (CollectorCachable c : entry.getValue()) {
                if (writer == null) {
                    log.info("new writer %s", entry.getKey());
                    writer = c;
                    writer.startAppend();
                }
                log.info("%s", c);
                c.finishReduce();
                c.streamappend(writer);
            }
            writer.finishAppend();
        }
    }

    public Collector getCollector(String canonicalname) {
        for (Collector c : this) {
            if (c.getCanonicalName().equals(canonicalname)) {
                return c;
            }
        }
        return null;
    }
}
