package org.kaneeldias.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.kaneeldias.WeatherData;

import java.io.IOException;

public class CalculateTotalPrecipitationJob extends WeatherDataJob {

    static final String OUTPUT_PATH = "total_precipitation_output";
    private final String inputPath;

    public CalculateTotalPrecipitationJob(String jobTimestamp, String inputPath) {
        super(jobTimestamp, OUTPUT_PATH);
        this.inputPath = inputPath;
    }

    @Override
    public Job getJob() throws IOException {
        Configuration conf = new Configuration();

        Job calculateTotalPrecipitationJob = Job.getInstance(conf, "Calculate Total Precipitation");
        calculateTotalPrecipitationJob.setJarByClass(WeatherData.class);

        calculateTotalPrecipitationJob.setMapperClass(TotalPrecipitationMapper.class);
        calculateTotalPrecipitationJob.setMapOutputKeyClass(Text.class);
        calculateTotalPrecipitationJob.setMapOutputValueClass(FloatWritable.class);

        calculateTotalPrecipitationJob.setReducerClass(TotalPrecipitationReducer.class);
        calculateTotalPrecipitationJob.setOutputKeyClass(Text.class);
        calculateTotalPrecipitationJob.setOutputValueClass(FloatWritable.class);

        FileInputFormat.addInputPath(calculateTotalPrecipitationJob, new Path(this.inputPath));
        FileOutputFormat.setOutputPath(calculateTotalPrecipitationJob, new Path(this.getOutputPath()));

        return calculateTotalPrecipitationJob;
    }

    public static class TotalPrecipitationMapper extends Mapper<Object, Text, Text, FloatWritable> {
        private final Text locationMonth = new Text();
        private final FloatWritable precipitationValue = new FloatWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            if (key.toString().equals("0")) {
                return;
            }

            String[] fields = value.toString().split(",");

            String locationId = fields[0];

            String date = fields[1];
            String[] dateParts = date.split("/");
            String month = dateParts[0];

            locationMonth.set(locationId + "_" + month);

            precipitationValue.set(Float.parseFloat(fields[11]));

            context.write(locationMonth, precipitationValue);
        }
    }

    public static class TotalPrecipitationReducer extends Reducer<Text, FloatWritable, Text, FloatWritable> {
        private final FloatWritable result = new FloatWritable();

        public void reduce(Text key, Iterable<FloatWritable> values, Context context)
                throws IOException, InterruptedException {

            float sum = 0;
            for (FloatWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

}
