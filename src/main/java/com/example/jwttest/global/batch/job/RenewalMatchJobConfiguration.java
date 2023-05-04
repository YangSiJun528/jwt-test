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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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
                      PlatformTransactionManager transactionManager,
                      @Qualifier(BEAN_PREFIX + "itemReader1") ItemReader<Summoner> itemReader
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<Summoner, List<String>>chunk(100, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor1())
                .writer(itemWriter1())
                .build();
    }

//    @Bean(BEAN_PREFIX + "itemReader1")
//    @StepScope
//    public ItemReader<Summoner> itemReader1() {
//        log.warn(BEAN_PREFIX + "itemReader1");
//        return new ListItemReader<>(RiotApiUtil.dummySummoner());
//
//    }

    //ItemReader를 사용하면 DI가 안됨
    //https://www.inflearn.com/questions/482396/stepscope-jpaitemreader%EC%97%90%EC%84%9C-entitymanager-null-pointer-exception-%EB%B0%9C%EC%83%9D-%EB%AC%B8%EC%A0%9C-%EB%8F%84%EC%99%80%EC%A3%BC%EC%84%B8%EC%9A%94
    @Bean(BEAN_PREFIX + "itemReader1")
    @StepScope
    public JpaPagingItemReader<Summoner> itemReader1(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemReader1");
        return new JpaPagingItemReaderBuilder<Summoner>()
                .name(BEAN_PREFIX + "itemReader1")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT s FROM Summoner s")
                .build();

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

