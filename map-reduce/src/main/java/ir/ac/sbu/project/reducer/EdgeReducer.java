package ir.ac.sbu.project.reducer;

import ir.ac.sbu.project.entity.MappedNodes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class EdgeReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        MappedNodes mappedNodes = new MappedNodes(true);
        for (Text value : values) {
            mappedNodes.addChild(value.toString());
        }
        context.write(key, mappedNodes.toText());
    }
}
