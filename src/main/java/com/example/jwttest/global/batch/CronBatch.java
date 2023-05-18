package com.example.jwttest.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronBatch {
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 0 3 * * *")// every day 03:00
    public void run() throws Exception {
        runJob();
    }

    public void runJob() throws Exception {
        LocalDateTime batchStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0));
        log.warn("Run Batches - Date : {}", batchStartDateTime);

        Job renewMatchJob = jobLocator.getJob("renewMatchJob");
        JobParameters renewMatchJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("Run Batch :{} At : {}",renewMatchJob.getName(), LocalDateTime.now());
        jobLauncher.run(renewMatchJob, renewMatchJobParameters);

        Job renewLeagueJob = jobLocator.getJob("renewLeagueJob");
        JobParameters renewLeagueJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("Run Batch :{} At : {}",renewLeagueJob.getName(), LocalDateTime.now());
        jobLauncher.run(renewLeagueJob, renewLeagueJobParameters);

        Job renewStatisticsJob = jobLocator.getJob("renewStatisticsJob");
        JobParameters renewStatisticsJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("Run Batch :{} At : {}",renewStatisticsJob.getName(), LocalDateTime.now());
        jobLauncher.run(renewStatisticsJob, renewStatisticsJobParameters);

        Job renewRankJob = jobLocator.getJob("renewRankJob");
        JobParameters renewRankJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("Run Batch :{} At : {}",renewRankJob.getName(), LocalDateTime.now());
        jobLauncher.run(renewRankJob, renewRankJobParameters);
    }
}

