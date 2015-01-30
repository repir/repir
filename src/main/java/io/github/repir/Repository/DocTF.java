package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredFile;
import io.github.repir.tools.io.struct.StructuredFileIntID;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.lib.Log;
import io.github.repir.Repository.DocTF.File;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.io.EOCException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the number of tokens in a Document as an Integer.
 *
 * @see EntityStoredFeature
 * @author jer
 */
public class DocTF
        extends EntityStoredFeature<File, Integer>
        implements ReduciblePartitionedFeature, ReportableFeature<Integer>, ResidentFeature {

    public static Log log = new Log(DocTF.class);

    private DocTF(Repository repository, String field) {
        super(repository, field);
    }

    public static DocTF get(Repository repository, String field) {
        String label = canonicalName(DocTF.class, field);
        DocTF termid = (DocTF) repository.getStoredFeature(label);
        if (termid == null) {
            termid = new DocTF(repository, field);
            repository.storeFeature(label, termid);
        }
        return termid;
    }

    @Override
    public void setMapOutputValue(TermEntityValue value, Content doc) {
        value.writer.write(doc.get(entityAttribute()).size());
    }

    @Override
    public void writeReduce(TermEntityKey key, Iterable<TermEntityValue> values) {
        try {
            file.dtf.write(values.iterator().next().reader.readInt());
        } catch (EOCException ex) {
            log.fatal(ex);
        }
    }

    @Override
    public void encode(Document d, int reportid) {
        bdw.write((Integer) d.getReportedFeature(reportid));
        d.setReportedFeature(reportid, bdw.getBytes());
    }

    @Override
    public void decode(Document d, int reportid) {
        reader.setBuffer((byte[]) d.getReportedFeature(reportid));
        try {
            d.setReportedFeature(reportid, reader.readInt());
        } catch (EOCException ex) {
            log.fatalexception(ex, "decode( %s ) reader %s", d, reader);
        }
    }

    @Override
    public void report(Document doc, int reportid) {
        //log.info("report %s doc %d reportid %d value %s", this.getCanonicalName(), doc.docid, reportid, getValue());
        doc.setReportedFeature(reportid, getValue());
    }

    @Override
    public Integer valueReported(Document doc, int docid) {
        return (Integer) doc.getReportedFeature(docid);
    }

    @Override
    public File createFile(Datafile datafile) {
        return new File(datafile);
    }

    @Override
    public Integer getValue() {
        return file.dtf.value;
    }

    @Override
    public void write(Integer value) {
        file.dtf.write(value);
    }

    @Override
    public void setValue(Integer value) {
        getFile().dtf.value = value;
    }

    public void readResident() {
        try {
            getFile().readResident(0);
        } catch (EOCException ex) {
            log.fatalexception(ex, "readResident()");
        }
    }

    public boolean isReadResident() {
        return getFile().isresident;
    }

    public static class File extends StructuredFile implements StructuredFileIntID {

        public Int3Field dtf = this.addInt3("dtf");
        public boolean isresident = false;

        public File(Datafile df) {
            super(df);
        }

        @Override
        public void read(int id) {
            reader.setOffset(id * 3);
            try {
                dtf.readNoReturn();
            } catch (EOCException ex) {
                log.exception(ex, "read( %d ) dtf %s", id, dtf);
            }
        }

        @Override
        public void find(int id) {
            this.setOffset(id * 3);
        }

        @Override
        public void readResident(int id) throws EOCException {
            readResident();
        }

        @Override
        public void readResident() throws EOCException {
            openRead();
            BufferReaderWriter w = new BufferReaderWriter(getDatafile().readFully());
            reader = w;
            isresident = true;
        }

        public boolean isReadResident() {
            return isresident;
        }

        public void reuseBuffer() {
            reader.reuseBuffer();
        }
    }
}
