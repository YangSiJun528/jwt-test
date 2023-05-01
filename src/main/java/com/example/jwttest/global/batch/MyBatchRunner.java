package com.example.jwttest.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBatchRunner implements CommandLineRunner {
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;

    public void runJob() throws Exception {
        Job job = jobLocator.getJob("simpleJob");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("date", LocalDateTime.now())
                .toJobParameters();
        log.warn("job 실행");

        /* batch 수행 */
        jobLauncher.run(job, jobParameters);
    }

    @Override
    public void run(String... args) throws Exception {
        runJob();
    }
}

