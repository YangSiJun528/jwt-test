package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCache;
import com.example.jwttest.global.riot.RiotApiUtil;
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
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                .profileIconIdUri(RiotApiUtil.getImgUri(5389))
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
                .profileIconIdUri(RiotApiUtil.getImgUri(5464))
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
                .profileIconIdUri(RiotApiUtil.getImgUri(6))
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
    public Job RenewJob(JobRepository jobRepository,
                        @Qualifier(BEAN_PREFIX + "step1") Step step1,
                        @Qualifier(BEAN_PREFIX + "step2") Step step2
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[dateTime]}") LocalDateTime dateTime
    ) {
        return new JobParameter(dateTime);
    }

    @Bean(BEAN_PREFIX + "step1")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<Summoner, List<String>>chunk(100, transactionManager)
                .reader(itemReader1())
                .processor(itemProcessor1())
                .writer(itemWriter1())
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader1")
    @StepScope
    public ItemReader<Summoner> itemReader1() {
        log.warn(BEAN_PREFIX + "itemReader1");
        return new ListItemReader<>(dummySummoner());

    }

    @Bean(BEAN_PREFIX + "itemProcessor1")
    @StepScope
    public ItemProcessor<Summoner, List<String>> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
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

    @Bean(BEAN_PREFIX + "itemWriter1")
    @StepScope
    public ItemWriter<List<String>> itemWriter1() {
        log.warn(BEAN_PREFIX + "itemWriter1");
        return new ItemWriter<List<String>>() {
            @Override
            public void write(Chunk<? extends List<String>> chunk) throws Exception {
                List<? extends List<String>> chunkItems = chunk.getItems();
                log.warn(chunkItems.toString());
                for (List<String> matchIds : chunkItems) {
                    InMemCache.getInstance().addAll(matchIds);
                }
                log.warn(InMemCache.getInstance().toString());
            }
        };
    }

    @Bean(BEAN_PREFIX + "step2")
    @JobScope
    public Step step2(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory
    ) {
        log.warn(BEAN_PREFIX + "step2");
        return new StepBuilder(BEAN_PREFIX + "step2", jobRepository)
                .<String, Match>chunk(100, transactionManager)
                .reader(itemReader2())
                .processor(itemProcessor2())
                .writer(itemWriter2(entityManagerFactory))
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader2")
    @StepScope
    public ItemReader<String> itemReader2() {
        log.warn(BEAN_PREFIX + "itemReader2");
        List<String> matchIds = List.copyOf(InMemCache.getInstance());
        return new ListItemReader<>(matchIds);

    }

    @Bean(BEAN_PREFIX + "itemProcessor2")
    @StepScope
    public ItemProcessor<String, Match> itemProcessor2() {
        log.warn(BEAN_PREFIX + "itemProcessor2");
        return matchId -> {
            Map<String, Object> rs = matchRiotApiService.getMatchByMatchId(matchId);
            log.warn(rs.toString());
            return new Match(matchId, rs);
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter2")
    @StepScope
    public ItemWriter<Match> itemWriter2(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemWriter2");
        return new JpaItemWriterBuilder<Match>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
}

