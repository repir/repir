package io.github.repir.Repository;

import io.github.repir.tools.extract.Content;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredFile;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Generic class for Features that are stored in the repository. Implementations
 * must declare a StructuredFileIntID file, (usually an extension of
 * RecordBinary that ensures records have a unique ID (int)). For performance,
 * the features that are merged with other features should be stored physically
 * sorted on ID. The second declaration is a data type, which can be complex.
 *
 * @author jeroen
 * @param <F> FileType that extends StructuredFileIntID
 * @param <C> Data type of the feature
 */
public abstract class StringLookupFeature<F extends StructuredFile, C> extends StoredUnreportableFeature<F> implements ReducibleFeature {

    public static Log log = new Log(StringLookupFeature.class);
    public String key;

    public StringLookupFeature(Repository repository, String field, String key) {
        super(repository, field);
        this.key = key;
        //log.info("StringLookupfeature %s %s %s", getCanonicalName(), field, key);
    }

    @Override
    public String getCanonicalName() {
        return canonicalName(getClass(), this.getField(), key);
    }

    public String extract(Content entity) {
        return entity.get(key).getContentStr();
    }

    @Override
    public TermEntityKey createMapOutputKey(int feature, String docname, Content entity) {
        String keyname = extract(entity);
        TermEntityKey t = TermEntityKey.createTermDocKey(0, feature, 0, keyname);
        t.type = TermEntityKey.Type.LOOKUPFEATURE;
        return t;
    }

    TermEntityKey outkey;
    TermEntityValue outvalue = new TermEntityValue();

    @Override
    public void writeMap(Mapper.Context context, int feature, String docname, Content entity) throws IOException, InterruptedException {
        outkey = createMapOutputKey(feature, docname, entity);
        setMapOutputValue(outvalue, entity);
        context.write(outkey, outvalue);
    }

    abstract public void setMapOutputValue(TermEntityValue writer, Content doc);

    public abstract C get(String term);

    @Override
    public abstract F createFile(Datafile datafile);

    @Override
    public void finishReduce() {
        if (file != null) {
            closeWrite();
            file = null;
        }
    }

    @Override
    public void startReduce(int buffersize) {
        getFile().setBufferSize(buffersize);
        openWrite();
    }

    @Override
    public void openRead() {
        getFile().openRead();
    }

    @Override
    public void closeRead() {
        getFile().closeRead();
        file = null;
    }

    public boolean hasNext() {
        return file.hasNext();
    }

    public boolean next() {
        return file.nextRecord();
    }

    public void skip() {
        file.skipRecord();
    }

    public void openWrite() {
        getFile().openWrite();
    }

    public void closeWrite() {
        getFile().closeWrite();
        file = null;
    }
}
