package org.kaneeldias.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.kaneeldias.WeatherData;

import java.io.IOException;

public class FindMaximumPrecipitationByYearMonth extends WeatherDataJob {

    static final String OUTPUT_PATH = "maximum_precipitation_by_month_output";
    private final String inputPath;

    public FindMaximumPrecipitationByYearMonth(String jobTimestamp, String inputPath) {
        super(jobTimestamp, OUTPUT_PATH);
        this.inputPath = inputPath;
    }

    @Override
    public Job getJob() throws IOException {
        Configuration conf = new Configuration();

        Job findMaximumPrecipitationJob = Job.getInstance(conf, "Find Maximum Total Precipitation by Month");
        findMaximumPrecipitationJob.setJarByClass(WeatherData.class);

        findMaximumPrecipitationJob.setMapperClass(MaximumPrecipitationMapper.class);
        findMaximumPrecipitationJob.setMapOutputKeyClass(NullWritable.class);
        findMaximumPrecipitationJob.setMapOutputValueClass(Text.class);

        findMaximumPrecipitationJob.setReducerClass(MaximumPrecipitationReducer.class);
        findMaximumPrecipitationJob.setOutputKeyClass(NullWritable.class);
        findMaximumPrecipitationJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(findMaximumPrecipitationJob, new Path(this.inputPath));
        FileOutputFormat.setOutputPath(findMaximumPrecipitationJob, new Path(this.getOutputPath()));

        return findMaximumPrecipitationJob;
    }

    public static class MaximumPrecipitationMapper extends Mapper<Object, Text, NullWritable, Text> {
        private final Text weatherData = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] fields = value.toString().split("\t");

            String yearMonth = fields[0];
            String totalPrecipitation = fields[1];

            weatherData.set(yearMonth + "," + totalPrecipitation);
            context.write(NullWritable.get(), weatherData);
        }
    }

    public static class MaximumPrecipitationReducer extends Reducer<NullWritable, Text, NullWritable, Text> {
        private static final Text HEADER =
                new Text("year,month,maximum_total_precipitation (mm)");
        private static float maximumPrecipitation = -1;
        private static int maximumPrecipitationYear = -1;
        private static int maximumPrecipitationMonth = -1;
        private boolean headerWritten = false;

        public void reduce(NullWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            if (!headerWritten) {
                context.write(NullWritable.get(), HEADER);
                headerWritten = true;
            }

            for (Text val : values) {
                String[] fields = val.toString().split(",");
                String yearMonth = fields[0];
                float totalPrecipitation = Float.parseFloat(fields[1]);

                if (totalPrecipitation > maximumPrecipitation) {
                    maximumPrecipitation = totalPrecipitation;
                    String[] yearMonthParts = yearMonth.split("-");
                    maximumPrecipitationYear = Integer.parseInt(yearMonthParts[0]);
                    maximumPrecipitationMonth = Integer.parseInt(yearMonthParts[1]);
                }
            }

            context.write(key, new Text(
                    maximumPrecipitationYear + "," +
                            maximumPrecipitationMonth + "," +
                            maximumPrecipitation
            ));
        }
    }

}
