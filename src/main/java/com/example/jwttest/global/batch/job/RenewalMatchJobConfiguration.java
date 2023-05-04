package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCache;
import com.example.jwttest.global.riot.service.MatchRiotApiService;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalMatchJobConfiguration {

    private int CHUNK_SIZE = 1;
    private final String JOB_NAME = "renewMatchJob";
    private final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobParameter jobParameter;

    private final MatchRiotApiService matchRiotApiService;

    private List<Summoner> dummySummoner() {
        Summoner s1 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("DIiLDPb8BjQewHIbqm1adVUIAObCRiA-wHgAU7mKaGjRNgI")
                .accountId("Pkh25cyxBN_6RQF3qD9WZZ1azpFJj-cqWtsqpYEVe2zMz_g")
                .puuid("JRv9GZ1NllHPUY1DXqQZ66yWwbDNIdi8UDeOtW-4pFxPQMhr17Vc5x1yrhWFehSvyeP2sU3rWiSO2g")
                .name("메추리 알빠노")
                .profileIconId(5389)
                .revisionDate(1683041620434L)
                .summonerLevel(2448)
                .build();
        Summoner s2 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("vkqcHZK6RT4NX4UfwIe3zWcuZsBRTHhVR4lnKD-JAUWVlOY")
                .accountId("vz3-jvm9gb1SODe3jRshCg1xzkVOXTkt6YbIuy44DlIVSqE")
                .puuid("ad951W1ExSx9ho7R4eYMZvAMB8wEMLvh-Z-azO1zh3gFkGqFg3ESYZySp9ed-O-IbY0N7mMWT813Fg")
                .name("잠은 뒤져서 잔다")
                .profileIconId(5464)
                .revisionDate(1683034046273L)
                .summonerLevel(2422)
                .build();
        Summoner s3 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("-YWoVxmeUI-MpR8YiM0UtRUcAwbYfG_ZwDjTrf5O1hR5re84DZ5u9uAxBA")
                .accountId("eXGT3kV7bOxG3j0_kfl8WY7l9_sAJp18e-fe9ZzrsIFtcznkqeV9uiuv")
                .puuid("pX1roodpuAb1soUN394FlIpYxPmXJyrsdWUYhQEEpM9SjT5sW-pKWhVXW09_3BusJyxAUQy7Z2n7-A")
                .name("Faker")
                .profileIconId(6)
                .revisionDate(1683082584000L)
                .summonerLevel(45)
                .build();
        return List.of(s1, s2, s3);
    }

    @Getter
    @AllArgsConstructor
    public class JobParameter {
        @NonNull
        private final LocalDateTime dateTime;
    }

    @Bean
    public Job RenewJob(JobRepository jobRepository, @Qualifier(BEAN_PREFIX + "step") Step step) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step)
                .build();
    }
    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[dateTime]}") LocalDateTime dateTime
    ) {
        return new JobParameter(dateTime);
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step step(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory
    ) {
        log.warn(BEAN_PREFIX + "step");
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Summoner, List<String>>chunk(100, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader")
    @StepScope
    public ItemReader<Summoner> itemReader() {
        log.warn(BEAN_PREFIX + "itemReader");
        return new ListItemReader<>(dummySummoner());

    }

    @Bean(BEAN_PREFIX + "itemProcessor")
    @StepScope
    public ItemProcessor<Summoner, List<String>> itemProcessor() {
        log.warn(BEAN_PREFIX + "itemProcessor");
        return summoner -> {
                List<String> matchIds = new ArrayList<>();
                int startIndex = 0;
                int fetchedCount;
                do {
                    List<String> newMatchIds = matchRiotApiService.getMatchIdsByPuuid(
                            summoner.getPuuid(),
                            startIndex,
                            100,
                            jobParameter.getDateTime().minusDays(1L),
                            jobParameter.getDateTime()
                    );
                    fetchedCount = newMatchIds.size();
                    matchIds.addAll(newMatchIds);
                    startIndex += 100;
                } while (fetchedCount == 100);
                log.warn(matchIds.toString());
                return matchIds;
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter")
    @StepScope
    public ItemWriter<List<String>> itemWriter() {
        log.warn(BEAN_PREFIX + "itemWriter");
        return new ItemWriter<List<String>>() {
            @Override
            public void write(Chunk<? extends List<String>> chunk) throws Exception {
                List<? extends List<String>> chunkItems = chunk.getItems();
                log.warn(chunkItems.toString());
                for (List<String> matchIds : chunkItems) {
                    InMemCache.getInstance().addAll(matchIds);
                }
            }
        };
    }

}
