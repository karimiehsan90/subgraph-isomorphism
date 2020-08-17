package ir.ac.sbu.project.reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ReportReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        boolean isFirst = true;
        for (Text value : values) {
            if (isFirst) {
                context.write(key, value);
            }
            isFirst = false;
        }
    }
}
