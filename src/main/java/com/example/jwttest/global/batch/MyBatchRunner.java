package com.example.jwttest.global.batch;

import com.example.jwttest.domain.statistics.repository.StatisticsRepository;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBatchRunner implements CommandLineRunner {
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;
    private final SummonerRepository summonerRepository;
    private final StatisticsRepository statisticsRepository;
    private final UserRepository userRepository;

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
        log.warn("job1 실행");
        /* batch 수행 */
        jobLauncher.run(job1, job1Parameters);

        Job job2 = jobLocator.getJob("renewLeagueJob");
        JobParameters job2Parameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("job2 실행");
        /* batch 수행 */
        jobLauncher.run(job2, job2Parameters);

        Job job3 = jobLocator.getJob("renewStatisticsJob");
        JobParameters job3Parameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("job3 실행");
        /* batch 수행 */
        jobLauncher.run(job3, job3Parameters);

        Job job4 = jobLocator.getJob("renewRankJob");
        JobParameters job4Parameters = new JobParametersBuilder()
                .addLocalDateTime("dateTime", LocalDateTime.now())
                .toJobParameters();
        log.warn("job4 실행");
        /* batch 수행 */
        jobLauncher.run(job4, job4Parameters);
    }

    @Override
    public void run(String... args) throws Exception {
        init();
        runJob();
    }

    @Transactional
    public void init() {
        User dummyUser1 = userRepository.save(RiotApiUtil.dummyUser1());
        User dummyUser2 = userRepository.save(RiotApiUtil.dummyUser2());
        List<Summoner> summoners = summonerRepository.saveAll(RiotApiUtil.dummySummoner(dummyUser1, dummyUser2));
        statisticsRepository.saveAll(RiotApiUtil.dummyStatistics(summoners));
    }
}

