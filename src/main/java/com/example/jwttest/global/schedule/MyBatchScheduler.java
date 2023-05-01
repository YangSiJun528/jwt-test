package com.example.jwttest.global.schedule;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBatchScheduler {
    private final Job job;
    private final JobLauncher jobLauncher;

    // 1시간 마다 실행
    @PostConstruct
    public void executeJob() {
        try {
            log.info("fetch gas station job start");
            jobLauncher.run(
                    job,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters()  // job parameter 설정
            );
            log.info("successfully complete job\n\n");
        } catch (JobExecutionException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

}
