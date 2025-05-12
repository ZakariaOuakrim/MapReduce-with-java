import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

    // Mapper Class
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            // Convert the line to string and tokenize
            StringTokenizer itr = new StringTokenizer(value.toString());

            // Emit each word with count of 1
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    // Reducer Class
    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            // Sum up the counts for each word
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }

            // Write the word and its total count
            result.set(sum);
            context.write(key, result);
        }
    }

    // Main Method to Configure and Run the Job
    public static void main(String[] args) throws Exception {
        // Check for correct number of arguments
        if (args.length != 2) {
            System.err.println("Usage: wordcount <input path> <output path>");
            System.exit(2);
        }

        // Create Configuration
        Configuration conf = new Configuration();

        // Create Job Instance
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);

        // Set Mapper and Reducer
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        // Set Output Key and Value Types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Set Input and Output Paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Submit the job and wait for completion
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}