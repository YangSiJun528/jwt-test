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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public Job RenewJob(JobRepository jobRepository, Step step) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step)
                .build();
    }

    public class MatchIdItemWriter extends ListItemWriter<List<String>> {
        @Override
        public void write(Chunk<? extends List<String>> chunk) throws Exception {
            List<? extends List<String>> chunkItems = chunk.getItems();
            log.warn(chunkItems.toString());
            for (List<String> matchIds : chunkItems) {
                log.warn(matchIds.toString());
                InMemCache.getInstance().addAll(matchIds);
            }
        }
    }

    public class MatchIdItemProcessor implements ItemProcessor<Summoner, List<String>> {

        private final MatchRiotApiService matchRiotApiService;
        private final LocalDateTime dateTime;

        public MatchIdItemProcessor(MatchRiotApiService matchRiotApiService, LocalDateTime dateTime) {
            this.matchRiotApiService = matchRiotApiService;
            this.dateTime = dateTime;
        }

        @Override
        public List<String> process(Summoner summoner) throws Exception {
            List<String> matchIds = new ArrayList<>();
            int startIndex = 0;
            int fetchedCount;
            do {
                List<String> newMatchIds = matchRiotApiService.getMatchIdsByPuuid(
                        summoner.getPuuid(),
                        startIndex,
                        100,
                        Timestamp.valueOf(dateTime.minusDays(1L)),
                        Timestamp.valueOf(dateTime)
                );
                fetchedCount = newMatchIds.size();
                matchIds.addAll(newMatchIds);
                startIndex += 100;
            } while (fetchedCount == 100);
            return matchIds;
        }

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
                .<List<String>, List<String>>chunk(100, transactionManager)
                .reader(itemReader())
//                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader")
    @StepScope
    public ItemReader itemReader() {
        log.warn(BEAN_PREFIX + "itemReader");

        ListItemReader write = new ListItemReader<>(List.of("pX1roodpuAb1soUN394FlIpYxPmXJyrsdWUYhQEEpM9SjT5sW-pKWhVXW09_3BusJyxAUQy7Z2n7-A", "hVBdIdq9d-W-vSxm__JIqAnM0jIp8Htr9BEMRSOiya45gep4wXhKWjjIgg1R8Qsf23SbO6HN2iUCXQ"));

        return write;

    }

    @Bean(BEAN_PREFIX + "itemProcessor")
    @StepScope
    public MatchIdItemProcessor itemProcessor() {
        log.warn(BEAN_PREFIX + "itemProcessor");
        return new MatchIdItemProcessor(matchRiotApiService, jobParameter.getDateTime());
    }

    @Bean(BEAN_PREFIX + "itemWriter")
    @StepScope
    public MatchIdItemWriter itemWriter() {
        log.warn(BEAN_PREFIX + "itemWriter");
        return new MatchIdItemWriter();
    }

}
