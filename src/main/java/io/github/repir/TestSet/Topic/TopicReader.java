package io.github.repir.TestSet.Topic;

import io.github.repir.TestSet.Metric.TestSetTopic;
import java.util.HashMap;

/**
 * An abstract for Readers of topic files for {@link TestSet}s.
 * @author jeroen
 */
public interface TopicReader {

   HashMap<Integer, TestSetTopic> getTopics();
}
