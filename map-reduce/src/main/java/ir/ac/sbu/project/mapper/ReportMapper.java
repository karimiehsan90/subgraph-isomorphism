package ir.ac.sbu.project.mapper;

import ir.ac.sbu.project.entity.MappedNodes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class ReportMapper extends Mapper<Object, Text, Text, Text> {
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] keyValue = value.toString().split("\t");
        MappedNodes mappedNodes = MappedNodes.fromString(keyValue[1]);
        if (mappedNodes.isValidated()) {
            context.write(mappedNodes.toKeyText(), mappedNodes.toText());
        }
    }
}
