package ir.ac.sbu.project.mapper;

import ir.ac.sbu.project.entity.MappedNodes;
import ir.ac.sbu.project.entity.QueryGraph;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class ReportMapper extends Mapper<Object, Text, Text, Text> {
    private QueryGraph queryGraph;
    @Override
    protected void setup(Context context) throws IOException {
        queryGraph = new QueryGraph();
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null) {
            FileSystem hdfs = FileSystem.get(context.getConfiguration());
            for (URI cacheFile : cacheFiles) {
                Path cacheFilePath = new Path(cacheFile.toString());
                BufferedReader reader = new BufferedReader(new InputStreamReader(hdfs.open(cacheFilePath)));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] nodes = line.split(" ");
                    queryGraph.addEdge(nodes[0], nodes[1]);
                }
            }
        }
    }

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] keyValue = value.toString().split("\t");
        MappedNodes mappedNodes = MappedNodes.fromString(keyValue[1]);
        if (mappedNodes.isCompleteResult(queryGraph)) {
            context.write(mappedNodes.toKeyText(), mappedNodes.toText());
        }
    }
}
