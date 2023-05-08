package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.league.domain.League;
import com.example.jwttest.domain.match.dto.MatchSummonerDto;
import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCache;
import com.example.jwttest.global.batch.InMemCacheLeague;
import com.example.jwttest.global.riot.service.LeagueRiotApiService;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalLeagueJobConfiguration {

    private int CHUNK_SIZE = 100;
    private final String JOB_NAME = "renewLeagueJob";
    private final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobParameter jobParameter;

    private final LeagueRiotApiService leagueRiotApiService;

    @Getter
    @AllArgsConstructor
    public class JobParameter {
        @NonNull
        private final LocalDateTime dateTime;
    }

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[dateTime]}") LocalDateTime dateTime
    ) {
        return new JobParameter(dateTime);
    }

    @Bean(JOB_NAME)
    public Job renewStatisticsJob(JobRepository jobRepository,
                                  @Qualifier(BEAN_PREFIX + "step1") Step step1,
                                  @Qualifier(BEAN_PREFIX + "step2") Step step2
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean(BEAN_PREFIX + "step1")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<Summoner, List<League>>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader1(entityManagerFactory))
                .processor(itemProcessor1())
                .writer(itemWriter1())
                .build();
    }

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
    public ItemProcessor<Summoner, List<League>> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
        return summoner -> {
            Set<Map<String, Object>> apiLeagues = leagueRiotApiService.getLeagueBySummonerId(summoner.getSummonerApiId());
            List<League> leagues = summoner.getLeagues();
            List<League> newLeagues = new ArrayList<>(Collections.emptyList());
            for(Map<String, Object> league : apiLeagues) {
                String summonerId = (String) league.get("summonerId");
                String queueType = (String) league.get("queueType");
                String tier = (String) league.get("tier");
                String rank = (String) league.get("rank");
                int leaguePoints = (int) league.get("leaguePoints");
                int wins = (int) league.get("wins");
                int losses = (int) league.get("losses");
                League renewleague = leagues.stream()
                        .filter(l -> l.getSummoner().getSummonerApiId().equals(summonerId))
                        .map(l -> new League(l.getId(), summoner,
                                queueType,
                                tier,
                                rank,
                                leaguePoints,
                                wins,
                                losses,
                                jobParameter.getDateTime())
                        )
                        .findAny()
                        .orElse(new League(UUID.randomUUID(), summoner,
                                queueType,
                                tier,
                                rank,
                                leaguePoints,
                                wins,
                                losses,
                                jobParameter.getDateTime())
                        );
                newLeagues.add(renewleague);
            }
            return newLeagues;
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter1")
    @StepScope
    public ItemWriter<List<League>> itemWriter1() {
        log.warn(BEAN_PREFIX + "itemWriter1");
        return new ItemWriter<List<League>>() {
            @Override
            public void write(Chunk<? extends List<League>> chunk) throws Exception {
                List<? extends List<League>> chunkItems = chunk.getItems();
                for (List<League> leagues : chunkItems) {
                    InMemCacheLeague.getInstance().addAll(leagues);
                }
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
                .<League, League>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader2())
                .writer(itemWriter2(entityManagerFactory))
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader2")
    @StepScope
    public ItemReader<League> itemReader2() {
        log.warn(BEAN_PREFIX + "itemReader2");
        List<League> leagues = List.copyOf(InMemCacheLeague.getInstance());
        return new ListItemReader<>(leagues);

    }

    @Bean(BEAN_PREFIX + "itemWriter2")
    @StepScope
    public ItemWriter<League> itemWriter2(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemWriter2");
        return new JpaItemWriterBuilder<League>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }
}

