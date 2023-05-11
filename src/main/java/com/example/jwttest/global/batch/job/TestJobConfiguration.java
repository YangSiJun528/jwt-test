package com.example.jwttest.global.batch.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TestJobConfiguration {

    /**
     * <h3>실행 예시 파라미터</h3>
     * --spring.batch.job.enabled=true --spring.batch.job.name=simpleJob date=2023-05-10T02:00:00 status=test_status_1234
     * 웹 어플리케션을 함께 사용하느라 spring.batch.job.enabled false로 설정해서 true로 바꿔줘야 함
     * 실행할 job이 여러개면 job.name=job1,job2 처럼 선언해서 실행 가능함
     */

    private int CHUNK_SIZE = 1;
    private final String JOB_NAME = "simpleJob";

    private final JobParameter jobParameter;

    // 파라미터 정의한 클래스 - test용이라 inner로 사용
    @Getter
    @AllArgsConstructor
    public class JobParameter {
        @NonNull
        private final LocalDateTime standardDateTime;
        @NonNull
        private final String status;

        // @NonNull을 필드에 지정하면 롬복이 생성한 메서드에만 null check 들어감
        // 내가 직접 allArg 생성자를 만든다고 해도, 그거까지 null check를 해주지 않는다는 소리임
    }

    @Bean
    public Job simpleJob(JobRepository jobRepository, @Qualifier(JOB_NAME + "_step1") Step testStep1) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(testStep1)
                .build();
    }

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[date]}") LocalDateTime date,
            @Value("#{jobParameters[status]}") String status
    ) {
        log.warn("jobParameter : {}, {}", date, status);
        return new JobParameter(date, status);
    }


    @Bean(JOB_NAME + "_step1")
    @JobScope
    public Step testStep1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, Tasklet testTasklet1) {
        return new StepBuilder("testStep1", jobRepository)
                .tasklet(testTasklet1, platformTransactionManager)
                .transactionManager(platformTransactionManager)
                .build();
    }

    @Bean(JOB_NAME + "_tasklet1")
    @StepScope
    public Tasklet testTasklet1() {
        return (stepContribution, chunkContext) -> {
            LocalDateTime dateTime = jobParameter.getStandardDateTime();
            String status = jobParameter.getStatus();
            log.warn(">>>>>>tasklet {} - {}", dateTime, status);
            return RepeatStatus.FINISHED;
        };
    }
}
