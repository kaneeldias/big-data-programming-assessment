package org.kaneeldias.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.kaneeldias.WeatherData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationNameJoinJob extends WeatherDataJob {

    static final String OUTPUT_PATH = "final";
    private final String weatherDataInputPath;
    private final String locationDataInputPath;


    public LocationNameJoinJob(String jobTimestamp, String weatherDataInputPath, String locationDataInputPath) {
        super(jobTimestamp, OUTPUT_PATH);
        this.weatherDataInputPath = weatherDataInputPath;
        this.locationDataInputPath = locationDataInputPath;
    }

    @Override
    public Job getJob() throws IOException {
        Configuration conf = new Configuration();

        Job locationNameJoinJob = Job.getInstance(conf, "Join Precipitation and Temperature Data");
        locationNameJoinJob.setJarByClass(WeatherData.class);

        locationNameJoinJob.setMapOutputKeyClass(IntWritable.class);
        locationNameJoinJob.setMapOutputValueClass(Text.class);

        MultipleInputs.addInputPath(locationNameJoinJob, new Path(this.weatherDataInputPath), TextInputFormat.class, weatherDataJoinMapper.class);
        MultipleInputs.addInputPath(locationNameJoinJob, new Path(this.locationDataInputPath), TextInputFormat.class, LocationNameJoinMapper.class);

        locationNameJoinJob.setReducerClass(LocationNameJoinReducer.class);
        locationNameJoinJob.setOutputKeyClass(NullWritable.class);
        locationNameJoinJob.setOutputValueClass(Text.class);
        FileOutputFormat.setOutputPath(locationNameJoinJob, new Path(this.getOutputPath()));

        return locationNameJoinJob;
    }

    public static class weatherDataJoinMapper extends Mapper<Object, Text, IntWritable, Text> {
        private final IntWritable locationIdKey = new IntWritable();
        private final Text weatherDataValue = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] fields = value.toString().split("\t");
            String locationMonth = fields[0];
            String precipitation = fields[1];
            String temperature = fields[2];

            int locationId = Integer.parseInt(locationMonth.split("_")[0]);
            String month = locationMonth.split("_")[1];

            locationIdKey.set(locationId);
            weatherDataValue.set("weather_data:" + month + "\t" + precipitation + "\t" + temperature);
            context.write(locationIdKey, weatherDataValue);
        }
    }

    public static class LocationNameJoinMapper extends Mapper<Object, Text, IntWritable, Text> {
        private final IntWritable locationIdKey = new IntWritable();
        private final Text locationNameValue = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            if (key.toString().equals("0")) {
                return;
            }

            String[] fields = value.toString().split(",");
            int locationId = Integer.parseInt(fields[0]);
            String locationName = fields[7];

            locationIdKey.set(locationId);
            locationNameValue.set("location_name:" + locationName);
            context.write(locationIdKey, locationNameValue);
        }
    }

    public static class LocationNameJoinReducer extends Reducer<IntWritable, Text, NullWritable, Text> {
        private static final Text HEADER =
                new Text("location_name,month,precipitation,temperature");
        private boolean headerWritten = false;

        public void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            if (!headerWritten) {
                context.write(NullWritable.get(), HEADER);
                headerWritten = true;
            }

            String locationName = "-";
            List<String> weatherData = new ArrayList<>();

            for (Text val : values) {
                String valueStr = val.toString();
                if (valueStr.startsWith("location_name:")) {
                    locationName = valueStr.substring("location_name:".length());
                } else {
                    weatherData.add(valueStr.substring("weather_data:".length()));
                }
            }

            for (String weatherDataEntry : weatherData) {
                context.write(NullWritable.get(), new Text(locationName + "," + weatherDataEntry.replace("\t", ",")));
            }
        }
    }


}
