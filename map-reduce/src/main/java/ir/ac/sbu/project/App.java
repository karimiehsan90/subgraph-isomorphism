package ir.ac.sbu.project;

import ir.ac.sbu.project.mapper.EdgeMapper;
import ir.ac.sbu.project.mapper.ReportMapper;
import ir.ac.sbu.project.reducer.EdgeReducer;
import ir.ac.sbu.project.reducer.NodeMapReducer;
import ir.ac.sbu.project.reducer.ReportReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class App {
    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job preProcessJob = Job.getInstance(conf, "subgraph-isomorphism-pre-process");
        preProcessJob.setMapperClass(EdgeMapper.class);
        preProcessJob.setReducerClass(EdgeReducer.class);
        preProcessJob.setOutputKeyClass(Text.class);
        preProcessJob.setOutputValueClass(Text.class);
        preProcessJob.setOutputFormatClass(SequenceFileOutputFormat.class);
        preProcessJob.setJar("subgraph-isomorphism.jar");
        preProcessJob.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
        FileInputFormat.addInputPath(preProcessJob, new Path("/subgraph-isomorphism/input"));
        FileOutputFormat.setOutputPath(preProcessJob, new Path("/subgraph-isomorphism/tmp/0"));
        boolean completedSuccessfully = preProcessJob.waitForCompletion(true);
        if (! completedSuccessfully) {
            System.exit(1);
        }
        int numberOfRuns = 10;
        for (int i = 0; i < numberOfRuns; i++) {
            Job algorithmJob = Job.getInstance(conf, "subgraph-isomorphism-" + i);
            algorithmJob.setInputFormatClass(SequenceFileInputFormat.class);
            algorithmJob.setReducerClass(NodeMapReducer.class);
            algorithmJob.setOutputKeyClass(Text.class);
            algorithmJob.setOutputValueClass(Text.class);
            if (i != numberOfRuns - 1) {
                algorithmJob.setOutputFormatClass(SequenceFileOutputFormat.class);
            }
            algorithmJob.setJar("subgraph-isomorphism.jar");
            algorithmJob.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
            FileInputFormat.addInputPath(algorithmJob, new Path("/subgraph-isomorphism/tmp/" + i));
            FileOutputFormat.setOutputPath(algorithmJob, new Path("/subgraph-isomorphism/tmp/" + (i + 1)));
            completedSuccessfully = algorithmJob.waitForCompletion(true);
            if (! completedSuccessfully) {
                System.exit(1);
            }
        }

        Job reporterJob = Job.getInstance(conf, "subgraph-isomorphism-reporter");
        reporterJob.setMapperClass(ReportMapper.class);
        reporterJob.setReducerClass(ReportReducer.class);
        reporterJob.setMapOutputKeyClass(Text.class);
        reporterJob.setMapOutputValueClass(Text.class);
        reporterJob.setOutputKeyClass(NullWritable.class);
        reporterJob.setOutputValueClass(Text.class);
        reporterJob.setJar("subgraph-isomorphism.jar");
        reporterJob.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
        FileInputFormat.addInputPath(reporterJob, new Path("/subgraph-isomorphism/tmp/" + numberOfRuns));
        FileOutputFormat.setOutputPath(reporterJob, new Path("/subgraph-isomorphism/output"));
        System.exit(reporterJob.waitForCompletion(true) ? 0 : 1);
    }
}
