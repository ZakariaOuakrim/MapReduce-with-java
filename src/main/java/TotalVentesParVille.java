import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TotalVentesParVille {

    // Mapper Class
    public static class VentesMapper
            extends Mapper<Object, Text, Text, FloatWritable> {

        private Text villeKey = new Text();
        private FloatWritable prixValue = new FloatWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            // Parse each line of input file
            String line = value.toString();
            String[] parts = line.split("\\s+");

            // Ensure the line has all required fields: date, ville, produit, prix
            if (parts.length >= 4) {
                // parts[0] = date
                // parts[1] = ville (city)
                // parts[2] = produit (product)
                // parts[3] = prix (price)

                try {
                    // Extract city and price
                    String ville = parts[1];
                    float prix = Float.parseFloat(parts[3]);

                    // Set mapper output
                    villeKey.set(ville);
                    prixValue.set(prix);

                    // Emit city as key and price as value
                    context.write(villeKey, prixValue);

                } catch (NumberFormatException e) {
                    // Handle case where price is not a valid number
                    System.err.println("Invalid price format in line: " + line);
                }
            }
        }
    }

    // Reducer Class
    public static class VentesReducer
            extends Reducer<Text, FloatWritable, Text, FloatWritable> {

        private FloatWritable result = new FloatWritable();

        public void reduce(Text key, Iterable<FloatWritable> values, Context context)
                throws IOException, InterruptedException {

            // Calculate the sum of all prices for each city
            float sum = 0;
            for (FloatWritable val : values) {
                sum += val.get();
            }

            // Set the result to the sum
            result.set(sum);

            // Emit the city and its total sales
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "total ventes par ville");

        // Set jar
        job.setJarByClass(TotalVentesParVille.class);

        // Set Mapper and Reducer classes
        job.setMapperClass(VentesMapper.class);
        job.setReducerClass(VentesReducer.class);

        // Set Combiner class (same as Reducer for better performance)
        job.setCombinerClass(VentesReducer.class);

        // Set output types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        // Validate arguments
        if (args.length != 2) {
            System.err.println("Usage: TotalVentesParVille <input path> <output path>");
            System.exit(2);
        }

        // Set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Submit job and wait for completion
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}