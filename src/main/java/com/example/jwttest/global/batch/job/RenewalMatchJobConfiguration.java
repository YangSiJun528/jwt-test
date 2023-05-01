package com.example.jwttest.global.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class RenewalMatchJobConfiguration {

    private int chunkSize = 1;


    @Bean
    public Job simpleJob(JobRepository jobRepository, Step testStep1) {
        return new JobBuilder("jobParameterJob", jobRepository)
                .start(testStep1)
                .build();
    }

    @Bean
    public Step testStep1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, Tasklet testTasklet1) {
        return new StepBuilder("testStep1", jobRepository)
                .tasklet(testTasklet1)
                .transactionManager(platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet testTasklet1() {
        return (stepContribution, chunkContext) -> {
            log.warn(">>>>>>tasklet");
            return RepeatStatus.FINISHED;
        };
    }
}
