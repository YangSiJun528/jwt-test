package com.example.jwttest.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronBatch {
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")// every day 03:00
    public void run() throws Exception {
        runJob();
    }

    //@Scheduled(cron = "5 * * * * *", zone = "Asia/Seoul")// every day 03:00
    public void test() {
        log.warn("DATE TIME : {}", ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    public void runJob() throws Exception {
        ZonedDateTime batchStartDateTime = ZonedDateTime.of(LocalDate.now(), LocalTime.of(3, 0), ZoneId.of("Asia/Seoul"));
        log.warn("Run Batches - Date : {}", batchStartDateTime);

        Job renewMatchJob = jobLocator.getJob("renewMatchJob");
        JobParameters renewMatchJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .toJobParameters();
        log.warn("Run Batch :{} At : {}", renewMatchJob.getName(), LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        jobLauncher.run(renewMatchJob, renewMatchJobParameters);

        Job renewLeagueJob = jobLocator.getJob("renewLeagueJob");
        JobParameters renewLeagueJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .toJobParameters();
        log.warn("Run Batch :{} At : {}", renewLeagueJob.getName(), LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        jobLauncher.run(renewLeagueJob, renewLeagueJobParameters);

        Job renewStatisticsJob = jobLocator.getJob("renewStatisticsJob");
        JobParameters renewStatisticsJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .toJobParameters();
        log.warn("Run Batch :{} At : {}", renewStatisticsJob.getName(), LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        jobLauncher.run(renewStatisticsJob, renewStatisticsJobParameters);

        Job renewRankJob = jobLocator.getJob("renewRankJob");
        JobParameters renewRankJobParameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .toJobParameters();
        log.warn("Run Batch :{} At : {}", renewRankJob.getName(), LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        jobLauncher.run(renewRankJob, renewRankJobParameters);
    }
}

