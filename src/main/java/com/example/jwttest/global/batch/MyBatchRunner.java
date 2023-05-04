package com.example.jwttest.global.batch;

import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.global.riot.RiotApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBatchRunner implements CommandLineRunner {
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;
    private final SummonerRepository summonerRepository;

    public void runJob() throws Exception {
        Job testJob = jobLocator.getJob("simpleJob");
        JobParameters testJobParameters = new JobParametersBuilder()
                .addLocalDateTime("date", LocalDateTime.now())
                .addString("status", "test_status_1234")
                .toJobParameters();
        log.warn("testJob 실행");

        jobLauncher.run(testJob, testJobParameters);

        Job job1 = jobLocator.getJob("renewMatchJob");
        JobParameters job1Parameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("job 실행");
        /* batch 수행 */
        jobLauncher.run(job1, job1Parameters);
    }

    @Override
    public void run(String... args) throws Exception {
        init();
        runJob();
    }

    private void init() {
        summonerRepository.saveAll(RiotApiUtil.dummySummoner());
    }
}

