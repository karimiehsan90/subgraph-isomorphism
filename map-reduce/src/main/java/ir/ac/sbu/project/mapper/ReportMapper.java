package ir.ac.sbu.project.mapper;

import ir.ac.sbu.project.entity.MappedNodes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class ReportMapper extends Mapper<Text, Text, Text, Text> {
    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        MappedNodes mappedNodes = MappedNodes.fromString(value.toString());
        if (mappedNodes.isValidated()) {
            context.write(mappedNodes.toKeyText(), mappedNodes.toResultText());
        }
    }
}
