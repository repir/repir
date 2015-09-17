package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.Qrel.QRel;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes statAP, as described by Aslam & Pavlu in "A Practical Sampling
 * Strategy for Efficient Retrieval Evaluation", following the code in
 * http://trec.nist.gov/data/web/09/statAP_MQ_eval_v3.pl
 * <p/>
 * For evaluation of ClueWeb StatAP should be used instead of MAP, because
 * evaluation was done by collection a small non-uniform sampling over the
 * retrieved queries. StatAP estimates MAP, compensating for the likelihood that
 * a document at rank r was evaluated.
 * <p/>
 * The original report and code also use the variance of StatAP, which can be
 * used to estimate a 95% confidence interval for the estimated AP. Te reduce
 * the error of the estimations these can be used to trade off a smaller
 * confidence interval against the costs of judging a larger sample, which is
 * computed in {@link #calculateVar(io.github.repir.TestSet.TestSet, io.github.repir.Retriever.Query)
 * }.
 *
 * @author Jeroen
 */
public class QueryMetricStatAP extends QueryMetric {

    public static Log log = new Log(QueryMetricStatAP.class);
    public double[] curve;

    public QueryMetricStatAP() {
        super();
    }

    @Override
    public double calculate(TestSet testset, Query query) throws IOException {

        int qrelid = testset.getQRelId(query);
        if (qrelid < 1) {
            return -1; // invalid topic
        }
        QRel qrel = testset.getQrels().get(qrelid);
        int sum_prec = 0;
        double sumfinalup = 0;
        double sumfinaldown = Math.round(estimateRQ(qrel, query));
        double statAP = 0;

        ArrayList<String> doc_inlist = new ArrayList();
        if (sumfinaldown > 0) {
            int r = 0;
            for (Document d : query.getQueryResults()) {
                r++;
                Integer relevance = qrel.relevance.get(d.getCollectionID());
                if (relevance != null && relevance > 0) {
                    doc_inlist.add(d.getCollectionID());
                    double iprob = qrel.iprob.get(d.getCollectionID());
                    double prec = (1.0 + sum_prec) / r;
                    sumfinalup += prec / iprob;
                    sum_prec += 1 / iprob;
                }
            }
            statAP = sumfinalup / sumfinaldown;
        }
        return statAP;
    }

    /**
     * See http://trec.nist.gov/pubs/trec17/papers/MQ.OVERVIEW.pdf The estimated
     * variance over StatAP can be calculated from the sample. Assuming normally
     * distributed StatMAP values, a 95% confidence interval is given by 2SD. To
     * compute the variance, the sum of calculateVar over all queries must me
     * divided by SQR(num valid queries).
     *
     * @return estimated variance of StatAP
     */
    public double calculateVar(TestSet testset, Query query) throws IOException {

        int qrelid = testset.getQRelId(query);
        if (qrelid < 1) {
            return -1; // invalid topic
        }
        QRel qrel = testset.getQrels().get(qrelid);
        double estimated_RQ = Math.round(estimateRQ(qrel, query));
        int sum_prec = 0;
        double sumfinalup = 0;
        double sumfinaldown = estimated_RQ;
        double statAP = 0;
        double var_statAP = -1;
        int no_sampled_inlist = 0;
        int no_sampled_relevant_inlist = 0;
        ArrayList<Double> iarray_iprob = new ArrayList();
        ArrayList<Double> iarray_prec = new ArrayList();
        ArrayList<Double> iarray_rank = new ArrayList();
        ArrayList<String> doc_inlist = new ArrayList();
        if (sumfinaldown > 0) {
            int r = 0;
            for (Document d : query.getQueryResults()) {
                r++;
                Integer relevance = qrel.relevance.get(d.getCollectionID());
                if (relevance != null) {
                    no_sampled_inlist++;
                    if (relevance > 0) {
                        no_sampled_relevant_inlist++;
                        doc_inlist.add(d.getCollectionID());
                        double iprob = qrel.iprob.get(d.getCollectionID());
                        double prec = (1.0 + sum_prec) / r;
                        sumfinalup += prec / iprob;
                        iarray_iprob.add(iprob);
                        iarray_prec.add(prec);
                        iarray_rank.add(1.0 / r);
                        sum_prec += 1 / iprob;
                    }
                }
            }
            statAP = sumfinalup / sumfinaldown;

            // for the relevant ones in the ist shift the prec by statAP
            for (int c = 0; c < no_sampled_relevant_inlist; c++) {
                iarray_prec.set(c, (iarray_prec.get(c) - statAP) / iarray_iprob.get(c));
            }
            // add the relevant document that are not retrieved
            int count_rel = no_sampled_relevant_inlist;
            for (Map.Entry<String, Integer> entry : qrel.relevance.entrySet()) {
                if (entry.getValue() > 0) {
                    if (!doc_inlist.contains(entry.getKey())) {
                        count_rel++;
                        double iprob = qrel.iprob.get(entry.getKey());
                        iarray_iprob.add(iprob);
                        iarray_prec.add(-statAP / iprob);
                        iarray_rank.add(0.0);
                    }
                }
            }
            // estimate variance
            double sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0;

            for (int i = count_rel; i > no_sampled_relevant_inlist; i--) {
                double ip = iarray_iprob.get(i - 1);
                double yy = iarray_prec.get(i - 1);
                sum1 += (1 - ip) * yy * yy;
                for (int j = i - 1; j > 0; j--) {
                    sum2 += 2 * yy * iarray_prec.get(j - 1);
                }
            }

            for (int i = no_sampled_relevant_inlist; i > 0; i--) {
                double ip = iarray_iprob.get(i - 1);
                double yy = iarray_prec.get(i - 1);
                sum1 += (1 - ip) * yy * yy;
                for (int j = i - 1; j > 0; j--) {
                    sum2 += 2 * yy * iarray_prec.get(j - 1);
                }
                sum3 += sum4 * sum4 * (1 - ip) / ip / ip;
                sum4 += iarray_rank.get(i - 1) / ip;
            }

            var_statAP = (sum1 - sum2 / (qrel.no_sampled - 1) + sum3) / estimated_RQ / estimated_RQ
                    / (no_sampled_inlist + 0.1) * qrel.no_sampled;
            if (var_statAP > 1) {
                var_statAP = 1;
            }

        }
        return var_statAP;
    }

    public HashMap<String, Integer> getDocInList(Query query) {
        HashMap<String, Integer> docinlist = new HashMap();
        int rank = 0;
        for (Document doc : query.getQueryResults()) {
            docinlist.put(doc.getCollectionID(), rank++);
        }
        return docinlist;
    }

    public double estimateRQ(QRel qrel, Query query) {
        double rq = 0;
        for (Map.Entry<String, Integer> entry : qrel.relevance.entrySet()) {
            double iprob = qrel.iprob.get(entry.getKey());
            if (entry.getValue() > 0) {
                rq += 1 / iprob;
            }
        }
        return rq;
    }
}
