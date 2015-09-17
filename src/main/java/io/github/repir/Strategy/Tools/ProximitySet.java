package io.github.repir.Strategy.Tools;

import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.PositionalOperator;
import io.github.repir.Strategy.Operator.QTerm;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A general multiple position lists iterator, that iterates through the
 * co-occurrences of operators in a {@link Document}.
 * <p/>
 * To use, instantiate with a list of operators that are used to create a list
 * of co-occurrences. Optionally {@link #setMaxSpan(int)} can be used to limit
 * occurrences returned to a maximum span. Use by calling
 * {@link #hasProximityMatches(io.github.repir.Retriever.Document)}, which will
 * recursively process the operators used, traverse to the first occurrence that
 * meets all requirements (e.g. span, dependencies, all required operators,
 * sequential requirements) and return true if there is such a first occurrence
 * in the document. The occurrence can be inspected using {@link #getFirst()} to
 * return the operator in the left most position, {@link #otherTermList()} to
 * retrieve a list of the other operators in order of their position (the first
 * is not included, the last is), and {@link #getLast()} to get the operator in
 * last position.
 * <p/>
 * ProximitySet will properly handle duplicate operators, e.g. "to be or not to
 * be".
 * <p/>
 * Internally occurrences and dependencies are represented as a long bit-pattern
 * over the contained operators. Therefore, Proximity sets are currently limited
 * to a maximum of 64 operators. In case partial matches are used, on a set of
 * operators that contain duplicates, {@link #convertDuplicatesInPattern(long)}
 * should be used to replace used Operators of Duplicate sets by the first of
 * those Duplicates.
 *
 * @author Jeroen Vuurens
 */
public abstract class ProximitySet {

    public static Log log = new Log(ProximitySet.class);
    public boolean dontoverlap = true;
    public ProximityTerm[] tpi;
    public long[] dependency;
    public int maximumspan = Integer.MAX_VALUE;
    ArrayList<PositionalOperator> elements;
    public ProximityTermDupl duplicateof[];
    final protected ProximityTermList ZEROLIST = new ProximityTermList();
    public ProximityTermList proximitytermlist;
    public ProximityTerm first, last;
    public long presentterms;

    public ProximitySet(ArrayList<PositionalOperator> containedfeatures) {
        elements = new ArrayList<PositionalOperator>();
        elements.addAll(containedfeatures);
        tpi = new ProximityTerm[elements.size()];
        duplicateof = new ProximityTermDupl[elements.size()];
        dependency = getDependence();
        for (int i = 0; i < elements.size() - 1; i++) {
            if (tpi[i] == null) {
                PositionalOperator ti = elements.get(i);
                for (int j = i + 1; j < elements.size(); j++) {
                    if (elements.get(j).equals(ti)) {
                        if (tpi[i] == null) {
                            tpi[i] = new ProximityTermDupl(ti, i, ti.getSpan());
                            duplicateof[i] = (ProximityTermDupl) tpi[i];
                            duplicateof[i].dupl = new ArrayList<ProximityTermDupl>();
                            duplicateof[i].dupl.add(duplicateof[i]);
                        }
                        PositionalOperator tj = elements.get(j);
                        tpi[j] = new ProximityTermDupl(tj, j, tj.getSpan());
                        duplicateof[j] = (ProximityTermDupl) tpi[i];
                        duplicateof[i].dupl.add((ProximityTermDupl) tpi[j]);
                    }
                }
                if (tpi[i] != null) {
                    for (ProximityTermDupl d : duplicateof[i].dupl) {
                        d.setDuplicates(duplicateof[i]);
                    }
                }
            }
        }
        for (int i = 0; i < elements.size(); i++) {
            if (tpi[i] == null) {
                PositionalOperator ti = elements.get(i);
                tpi[i] = new ProximityTerm(ti, i, ti.getSpan());
            }
        }
        for (ProximityTerm t : tpi) {
            if (t instanceof ProximityTermDupl) {
                ProximityTermDupl d = (ProximityTermDupl) t;
                d.setDuplicateDependency();
                for (int i = 0; i < d.dependency.length; i++) {
                    d.dependency[i] = this.convertDuplicatesInPattern(d.dependency[i], d.sequence);
                }
            }
        }
        for (ProximityTerm t : tpi) {
            t.setDependency(dependency[t.sequence]);
        }
    }

    /**
     * @param span maximum span of co-occurrences matched
     */
    public void setMaxSpan(int span) {
        this.maximumspan = span;
    }

    /**
     * transforms a dependency pattern, shifting ids of duplicates so that each
     * number of n contained duplicates remains the same, but is converted to
     * the first n of that set of duplicates. This is necessary because of two
     * duplicates, the second begins at the second position in the document.
     *
     * @param id
     * @return
     */
    public long convertDuplicatesInPattern(long id) {
        return convertDuplicatesInPattern(id, -1);
    }

    private long convertDuplicatesInPattern(long id, int sequence) {
        long modifiedid = id;
        HashMap<Integer, Integer> duplicates = new HashMap<Integer, Integer>();
        long bit = 1;
        for (int p = 0; p < elements.size(); p++, bit <<= 1) {
            if ((id & bit) != 0 && tpi[p] instanceof ProximityTermDupl) {
                // for duplicate terms we have to make sure to use the first values in the list
                // i.e. transform the id mask so that the n occurences of term x are
                // replaced with the first n occurrences of term x
                int firstid = ((ProximityTermDupl) tpi[p]).first.sequence;
                Integer count = duplicates.get(firstid);
                if (count == null) {
                    duplicates.put(firstid, 1);
                } else {
                    duplicates.put(firstid, count + 1);
                }
                modifiedid -= bit;
            }
        }
        for (Map.Entry<Integer, Integer> e : duplicates.entrySet()) {
            boolean foundmyself = false;
            ProximityTermDupl d = (ProximityTermDupl) tpi[e.getKey()];
            if (e.getKey() != sequence) {
                modifiedid |= d.bitsequence;
            } else {
                foundmyself = true;
            }
            for (int i = 1; i < e.getValue(); i++) {
                if (d.sequence != sequence) {
                    modifiedid |= d.dupl.get(i).bitsequence;
                } else {
                    foundmyself = true;
                }
            }
            if (foundmyself && e.getValue() < d.dupl.size()) {
                modifiedid |= d.dupl.get(e.getValue()).bitsequence;
            }
        }
        return modifiedid;
    }

    /**
     * By default no dependencies are used. When overridden, bits set indicate
     * that these operators can only be scored in combinations with the
     * dependent operators, and can otherwise be ignored. e.g. suppose "Joan of
     * Arc" has three corresponding for the three terms. The sequence ids will
     * the be Joan:0, of:1, Arc:2. If "of" is dependent on "Joan" and "Arc",
     * dependency[1] = (2^0 + 2^2) = 5.
     *
     * @return
     */
    protected long[] getDependence() {
        return new long[elements.size()];
    }

    /**
     *
     * @param doc
     * @return true if the ProximitySet has at least 1 ProximityOccurrence for
     * this {@link Document}. If the Operators do not satisfy the requirements
     * (more than 1 operator for partial matches, all operators for full
     * matches, meeting all other requirements (e.g. in correct order). Note
     * that ProximitySet has no knowledge of additional requirements such as
     * maximum span.
     *
     */
    public abstract boolean hasProximityMatches(Document doc);

    public abstract boolean next();

    protected void pollFirstLast() {
        first = proximitytermlist.pollFirst();
        last = proximitytermlist.last();
    }

    /**
     * @return The positional first Operator of the currently matched
     * co-occurrence.
     */
    public ProximityTerm getFirst() {
        return first;
    }

    /**
     * @return a list of all {@link Operator}s, in positional order of the
     * current occurrence in the document. The Operator in first position is not
     * in this list, that can be obtained using {@link #getFirst() }.
     *
     */
    public ProximityTermList otherTermList() {
        return proximitytermlist;
    }

    /**
     * @return the positional last Operator of the currently matched
     * co-occurrence.
     */
    public ProximityTerm getLast() {
        return last;
    }

    /**
     *
     * @return For debug purposes.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            sb.append(elements.get(i)).append(" ").append(tpi[i]);
        }
        return sb.toString();
    }

    /**
     * Contains the positional information of an Operator.
     */
    public class ProximityTerm implements Comparable<ProximityTerm> {

        PositionalOperator operator;
        int[] position;
        public int sequence;
        public long bitsequence;
        public long alldependency;
        public int span;
        int previous;
        public int current;
        public int next;
        public long dependency;
        int p;

        protected ProximityTerm(PositionalOperator operator, int sequence, int span) {
            this.operator = operator;
            this.sequence = sequence;
            this.bitsequence = (1l << sequence);
            this.span = span;
        }

        public void setDependency(long dependency) {
            this.dependency = dependency;
            this.alldependency = dependency | bitsequence;
        }

        protected void reset() {
            this.position = operator.getPos();
            p = 0;
            previous = Integer.MIN_VALUE;
            if (position.length == 0) {
                current = Integer.MAX_VALUE;
                next = Integer.MAX_VALUE;
            } else if (position.length == 1) {
                current = position[p++];
                next = Integer.MAX_VALUE;
            } else {
                current = position[p++];
                next = position[p++];
            }
        }

        public boolean satisfiesDependency(long pattern) {
            return (dependency & pattern) == dependency;
        }

        public int peek() {
            return next;
        }

        protected int next() {
            previous = current;
            current = next;
            next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
            return current;
        }

        final protected int move() {
            previous = current;
            current = next;
            next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
            return current;
        }

        protected void moveFirstBelowNext() {
            if (dontoverlap) {
                int nextpos = proximitytermlist.first().current;
                while (next < nextpos) {
                    next();
                }
            }
        }

        @Override
        public int compareTo(ProximityTerm o) {
            //log.info("compareTo %d %d", current, o.current);
            if (current == Integer.MAX_VALUE) {
                if (o.current == Integer.MAX_VALUE) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if (o.current == Integer.MAX_VALUE) {
                    return -1;
                } else {
                    return current - o.current;
                }
            }
        }

        @Override
        public String toString() {
            return Integer.toString(current);
        }
    }

    /**
     * ProximityTerm variant for duplicate operators, e.g. "to be or not to be",
     * connecting N duplicate ProximityTermDupl, initially positioning them on
     * the first N positions of the Operator, and when the first is moved to the
     * next, the other duplicates are too. For proper handling of duplicates,
     * any unordered partial bit pattern must be converted using
     * {@link #convertDuplicatesInPattern(long)}, e.g. "not to be" will be
     * converted from "111000" to "001011", using the first "to" and "be"
     * operators.
     */
    public class ProximityTermDupl extends ProximityTerm {

        public ArrayList<ProximityTermDupl> dupl;
        protected long[] dependency;
        ProximityTermDupl first, previousdupl, nextdupl, last;
        private int initshift = 0;

        protected ProximityTermDupl(PositionalOperator operator, int sequence, int span) {
            super(operator, sequence, span);
        }

        protected void setDuplicates(ProximityTermDupl first) {
            this.first = first;
            this.last = first.dupl.get(first.dupl.size() - 1);
            int pos = 0;
            for (; pos < first.dupl.size() && first.dupl.get(pos) != this; pos++) {
            }
            if (pos > 0) {
                previousdupl = first.dupl.get(pos - 1);
            }
            initshift = pos;
            if (++pos < first.dupl.size()) {
                nextdupl = first.dupl.get(pos);
            }
        }

        public int peek() {
            return last.next;
        }

        protected void setDuplicateDependency() {
            ArrayList<ProximityTermDupl> list = first.dupl;
            HashSet<Long> dep = new HashSet<Long>();
            for (int i = (1 << first.dupl.size()) - 1; i > 0; i--) {
                if (MathTools.numberOfSetBits(i) == (initshift + 1)) {
                    long pattern = 0;
                    int b = 1;
                    for (int j = 0; b <= i; j++, b <<= 1) {
                        if ((i & b) != 0) {
                            pattern |= ProximitySet.this.dependency[first.dupl.get(j).sequence];
                        }
                    }
                    dep.add(convertDuplicatesInPattern(pattern, sequence));
                }
            }
            HashSet<Long> dep1 = new HashSet<Long>();
            SKIP:
            for (long d : dep) {
                for (long m : dep) {
                    if (m != d && (d & m) == m) {
                        continue SKIP;
                    }
                }
                dep1.add(d);
            }
            dependency = ArrayTools.toLongArray(dep1);
        }

        public boolean satisfiesDependency(long pattern) {
            for (long d : dependency) {
                if ((pattern & d) == d) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void reset() {
            position = operator.getPos();
            p = initshift;
            previous = (p < position.length && p > 0) ? position[p - 1] : Integer.MIN_VALUE;
            current = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
            next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
        }

        @Override
        protected int next() {
            if (dupl != null) {
                for (ProximityTermDupl t : dupl) {
                    if (t != this && t.current < Integer.MAX_VALUE) {
                        proximitytermlist.remove(t);
                        t.next();
                        if (t.current < Integer.MAX_VALUE) {
                            proximitytermlist.add(t);
                        } else if (proximitytermlist.size() == 0) {
                            return Integer.MAX_VALUE;
                        }
                    }
                }
            }
            return super.next();
        }

        @Override
        protected void moveFirstBelowNext() {
            if (dontoverlap) {
                int nextpos = proximitytermlist.first().current;
                if (next == this.nextdupl.current) {
                    int count = 0;
                    ProximityTerm target = null;
                    for (ProximityTerm t : proximitytermlist) {
                        if (++count == dupl.size()) {
                            target = t;
                            break;
                        }
                        if (duplicateof[t.sequence] != this) {
                            break;
                        }
                    }
                    if (target != null) {
                        while (last.next < target.current) {
                            next();
                        }
                    }
                }
            }
        }
    }
}
