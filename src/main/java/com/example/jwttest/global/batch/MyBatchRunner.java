package com.example.jwttest.global.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBatchRunner implements CommandLineRunner {

    private final Job job;
    private final JobLauncher jobLauncher;

    public void runJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        /* batch 수행 */
        jobLauncher.run(job, jobParameters);
    }


    @Override
    public void run(String... args) throws Exception {
        runJob();
    }
}

