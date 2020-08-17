package ir.ac.sbu.project.mapper;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class EdgeMapper extends Mapper<Object, Text, Text, Text> {
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] nodes = value.toString().split(" ");
        Text fromNodeText = new Text(nodes[0]);
        Text toNodeText = new Text(nodes[1]);
        context.write(fromNodeText, toNodeText);
    }
}
