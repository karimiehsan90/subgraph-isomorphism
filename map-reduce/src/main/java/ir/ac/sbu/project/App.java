package ir.ac.sbu.project;

import ir.ac.sbu.project.mapper.EdgeMapper;
import ir.ac.sbu.project.mapper.ReportMapper;
import ir.ac.sbu.project.reducer.EdgeReducer;
import ir.ac.sbu.project.reducer.NodeMapReducer;
import ir.ac.sbu.project.reducer.ReportReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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
        // TODO complete map reduce
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "subgraph-isomorphism-pre-process");
        job.setMapperClass(EdgeMapper.class);
        job.setReducerClass(EdgeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJar("subgraph-isomorphism.jar");
        job.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
        FileInputFormat.addInputPath(job, new Path("/subgraph-isomorphism/input"));
        FileOutputFormat.setOutputPath(job, new Path("/subgraph-isomorphism/tmp/0"));
        boolean completedSuccessfully = job.waitForCompletion(true);
        if (! completedSuccessfully) {
            System.exit(1);
        }
        int numberOfRuns = 10;
        for (int i = 0; i < numberOfRuns; i++) {
            job = Job.getInstance(conf, "subgraph-isomorphism-" + i);
            job.setInputFormatClass(SequenceFileInputFormat.class);
            job.setReducerClass(NodeMapReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            if (i != numberOfRuns - 1) {
                job.setOutputFormatClass(SequenceFileOutputFormat.class);
            }
            job.setJar("subgraph-isomorphism.jar");
            job.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
            FileInputFormat.addInputPath(job, new Path("/subgraph-isomorphism/tmp/" + i));
            FileOutputFormat.setOutputPath(job, new Path("/subgraph-isomorphism/tmp/" + (i + 1)));
            completedSuccessfully = job.waitForCompletion(true);
            if (! completedSuccessfully) {
                System.exit(1);
            }
        }

        Job reporterJob = Job.getInstance(conf, "subgraph-isomorphism-reporter");
        reporterJob.setMapperClass(ReportMapper.class);
        reporterJob.setReducerClass(ReportReducer.class);
        reporterJob.setOutputKeyClass(Text.class);
        reporterJob.setMapOutputValueClass(Text.class);
        reporterJob.setJar("subgraph-isomorphism.jar");
        reporterJob.addCacheFile(new URI("hdfs://master:9000/subgraph-isomorphism/query"));
        FileInputFormat.addInputPath(reporterJob, new Path("/subgraph-isomorphism/tmp/" + numberOfRuns));
        FileOutputFormat.setOutputPath(reporterJob, new Path("/subgraph-isomorphism/output"));
        System.exit(reporterJob.waitForCompletion(true) ? 0 : 1);
    }
}
