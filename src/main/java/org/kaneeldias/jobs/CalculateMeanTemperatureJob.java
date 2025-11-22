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

public class CalculateMeanTemperatureJob extends WeatherDataJob {

    static final String OUTPUT_PATH = "mean_temperature_output";
    private final String inputPath;

    public CalculateMeanTemperatureJob(String jobTimestamp, String inputPath) {
        super(jobTimestamp, OUTPUT_PATH);
        this.inputPath = inputPath;
    }

    @Override
    public Job getJob() throws IOException {
        Configuration conf = new Configuration();

        Job calculateMeanTemperatureJob = Job.getInstance(conf, "Calculate Mean Temperature");
        calculateMeanTemperatureJob.setJarByClass(WeatherData.class);

        calculateMeanTemperatureJob.setMapperClass(MeanTemperatureMapper.class);
        calculateMeanTemperatureJob.setMapOutputKeyClass(Text.class);
        calculateMeanTemperatureJob.setMapOutputValueClass(FloatWritable.class);

        calculateMeanTemperatureJob.setReducerClass(MeanTemperatureReducer.class);
        calculateMeanTemperatureJob.setOutputKeyClass(Text.class);
        calculateMeanTemperatureJob.setOutputValueClass(FloatWritable.class);

        FileInputFormat.addInputPath(calculateMeanTemperatureJob, new Path(this.inputPath));
        FileOutputFormat.setOutputPath(calculateMeanTemperatureJob, new Path(this.getOutputPath()));

        return calculateMeanTemperatureJob;
    }

    public static class MeanTemperatureMapper extends Mapper<Object, Text, Text, FloatWritable> {
        private final Text locationMonth = new Text();
        private final FloatWritable temperatureValue = new FloatWritable();

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

            temperatureValue.set(Float.parseFloat(fields[5]));

            context.write(locationMonth, temperatureValue);
        }
    }

    public static class MeanTemperatureReducer extends Reducer<Text, FloatWritable, Text, FloatWritable> {
        private final FloatWritable result = new FloatWritable();

        public void reduce(Text key, Iterable<FloatWritable> values, Context context)
                throws IOException, InterruptedException {

            float sum = 0;
            int count = 0;
            for (FloatWritable val : values) {
                sum += val.get();
                count++;
            }
            result.set(sum / count);
            context.write(key, result);
        }
    }


}
