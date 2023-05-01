package com.example.jwttest.global.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MyBatchRunner implements CommandLineRunner {

    @Autowired
    private JobOperator jobOperator;

    public void runJob(String jobName) throws Exception {
        Properties parameters = new Properties();
        Long jobExecution = jobOperator.start(jobName, parameters);
        // Job 실행 결과 처리
    }

    @Override
    public void run(String... args) throws Exception {
        runJob("jobParameterJob");
    }
}

