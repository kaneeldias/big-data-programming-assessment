package org.kaneeldias.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.kaneeldias.WeatherData;

import java.io.IOException;

public class PrecipitationTemperatureJoinJob extends WeatherDataJob {

    static final String OUTPUT_PATH = "joined_output";
    private final String precipitationInputPath;
    private final String temperatureInputPath;

    public PrecipitationTemperatureJoinJob(String jobTimestamp, String precipitationInputPath, String temperatureInputPath) {
        super(jobTimestamp, OUTPUT_PATH);
        this.precipitationInputPath = precipitationInputPath;
        this.temperatureInputPath = temperatureInputPath;
    }

    @Override
    public Job getJob() throws IOException {
        Configuration conf = new Configuration();

        Job joinPrecipitationTemperatureJob = Job.getInstance(conf, "Join Precipitation and Temperature Data");
        joinPrecipitationTemperatureJob.setJarByClass(WeatherData.class);

        joinPrecipitationTemperatureJob.setMapOutputKeyClass(Text.class);
        joinPrecipitationTemperatureJob.setMapOutputValueClass(Text.class);

        MultipleInputs.addInputPath(joinPrecipitationTemperatureJob, new Path(this.precipitationInputPath), TextInputFormat.class, PrecipitationJoinMapper.class);
        MultipleInputs.addInputPath(joinPrecipitationTemperatureJob, new Path(this.temperatureInputPath), TextInputFormat.class, TemperatureJoinMapper.class);

        joinPrecipitationTemperatureJob.setReducerClass(PrecipitationTemperatureJoinReducer.class);
        joinPrecipitationTemperatureJob.setOutputKeyClass(Text.class);
        joinPrecipitationTemperatureJob.setOutputValueClass(Text.class);
        FileOutputFormat.setOutputPath(joinPrecipitationTemperatureJob, new Path(this.getOutputPath()));

        return joinPrecipitationTemperatureJob;
    }

    public static class PrecipitationJoinMapper extends Mapper<Object, Text, Text, Text> {
        private final Text locationMonth = new Text();
        private final Text precipitation = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] fields = value.toString().split("\t");
            locationMonth.set(fields[0]);
            precipitation.set("precipitation:" + fields[1]);
            context.write(locationMonth, precipitation);
        }
    }

    public static class TemperatureJoinMapper extends Mapper<Object, Text, Text, Text> {
        private final Text locationMonth = new Text();
        private final Text temperature = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] fields = value.toString().split("\t");
            locationMonth.set(fields[0]);
            temperature.set("temperature:" + fields[1]);
            context.write(locationMonth, temperature);
        }
    }

    public static class PrecipitationTemperatureJoinReducer extends Reducer<Text, Text, Text, Text> {

        private final Text outValue = new Text();

        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            String precipitation = "-";
            String temperature = "-";

            for (Text text : values) {
                String value = text.toString();
                int separator = value.indexOf(":");

                String tag = value.substring(0, separator);
                String data = value.substring(separator + 1);

                if ("precipitation".equals(tag)) {
                    precipitation = data;
                } else if ("temperature".equals(tag)) {
                    temperature = data;
                }
            }

            outValue.set(precipitation + "\t" + temperature);
            context.write(key, outValue);
        }
    }

}
