package org.kaneeldias;

import org.kaneeldias.jobs.*;

public class WeatherData {

    public static void main(String[] args) throws Exception {
        String weatherDataInputPath = args[0];
        String locationDataInputPath = args[1];
        String jobTimestamp = args[2];

        WeatherDataJob calculateTotalPrecipitationJob = new CalculateTotalPrecipitationJob(jobTimestamp, weatherDataInputPath);
        WeatherDataJob calculateMeanTemperatureJob = new CalculateMeanTemperatureJob(jobTimestamp, weatherDataInputPath);
        WeatherDataJob precipitationTemperatureJoinJob = new PrecipitationTemperatureJoinJob(jobTimestamp, calculateTotalPrecipitationJob.getOutputPath(), calculateMeanTemperatureJob.getOutputPath());
        WeatherDataJob locationNameJoinJob = new LocationNameJoinJob(jobTimestamp, precipitationTemperatureJoinJob.getOutputPath(), locationDataInputPath);

        boolean precipitationJobCompleted = calculateTotalPrecipitationJob.getJob().waitForCompletion(true);
        boolean temperatureJobCompleted = calculateMeanTemperatureJob.getJob().waitForCompletion(true);
        if (!precipitationJobCompleted || !temperatureJobCompleted) {
            System.exit(1);
        }

        boolean joinJobCompleted = precipitationTemperatureJoinJob.getJob().waitForCompletion(true);
        if (!joinJobCompleted) {
            System.exit(1);
        }

        boolean locationJoinJobCompleted = locationNameJoinJob.getJob().waitForCompletion(true);
        if (!locationJoinJobCompleted) {
            System.exit(1);
        }

        System.exit(0);
    }

}