package org.kaneeldias.jobs;

import org.apache.hadoop.mapreduce.Job;
import org.kaneeldias.utils.PathUtils;

import java.io.IOException;

public abstract class WeatherDataJob {

    protected String jobTimestamp;
    protected String outputPath;

    public WeatherDataJob(String jobTimestamp, String outputPath) {
        this.jobTimestamp = jobTimestamp;
        this.outputPath = outputPath;
    }

    public abstract Job getJob() throws IOException;

    public String getOutputPath() {
        return PathUtils.getOutputPath(jobTimestamp, this.outputPath);
    }
}
