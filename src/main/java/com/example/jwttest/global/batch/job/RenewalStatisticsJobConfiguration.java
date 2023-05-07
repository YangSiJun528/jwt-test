package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCacheStatistics;
import com.example.jwttest.global.batch.dto.MatchStatisticsDto;
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
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalStatisticsJobConfiguration {

    private int CHUNK_SIZE = 100;
    private final String JOB_NAME = "renewStatisticsJob";
    private final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobParameter jobParameter;

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
                                  @Qualifier(BEAN_PREFIX + "step0") Step step0,
                                  @Qualifier(BEAN_PREFIX + "step1") Step step1
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step0)
                .next(step1)
                .build();
    }

    @Bean(BEAN_PREFIX + "step0")
    @JobScope
    public Step step0(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory
                      ) {
        log.warn(BEAN_PREFIX + "step0");
        return new StepBuilder(BEAN_PREFIX + "step0", jobRepository)
                .<Statistics, Statistics>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader0(entityManagerFactory))
                .writer(itemWriter0())
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader0")
    @StepScope
    public JpaPagingItemReader<Statistics> itemReader0(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemReader0");
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("startDateTime", jobParameter.getDateTime().minusDays(1L));
        parameterValues.put("endDateTime", jobParameter.getDateTime());

        return new JpaPagingItemReaderBuilder<Statistics>()
                .name(BEAN_PREFIX + "itemReader0")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT s.statistics " +
                        "FROM Match m " +
                        "JOIN MatchSummoner sm ON sm.match = m " +
                        "JOIN Summoner s ON s = sm.summoner " +
                        "WHERE m.createAt > :startDateTime AND m.createAt <= :endDateTime"
                )
                .parameterValues(parameterValues)
                .build();

    }

    @Bean(BEAN_PREFIX + "itemWriter0")
    @StepScope
    public ItemWriter<Statistics> itemWriter0() {
        log.warn(BEAN_PREFIX + "itemWriter0");
        return new ItemWriter<Statistics>() {
            @Override
            public void write(Chunk<? extends Statistics> chunk) throws Exception {
                List<? extends Statistics> chunkItems = chunk.getItems();
                InMemCacheStatistics.getInstance().addAll(chunkItems);
            }
        };
    }

    @Bean(BEAN_PREFIX + "step1")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory,
                      @Qualifier(BEAN_PREFIX + "itemReader1") ItemReader<MatchStatisticsDto> itemReader
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<MatchStatisticsDto, Statistics>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor1())
                .writer(itemWriter1(entityManagerFactory))
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader1")
    @StepScope
    public JpaPagingItemReader<MatchStatisticsDto> itemReader1(EntityManagerFactory entityManagerFactory) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("startDateTime", jobParameter.getDateTime().minusDays(1L));
        parameterValues.put("endDateTime", jobParameter.getDateTime());

        log.warn(BEAN_PREFIX + "itemReader1");
        return new JpaPagingItemReaderBuilder<MatchStatisticsDto>()
                .name(BEAN_PREFIX + "itemReader1")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT new com.example.jwttest.global.batch.dto.MatchStatisticsDto(m, s.statistics) " +
                        "FROM Match m " +
                        "JOIN MatchSummoner sm ON sm.match = m " +
                        "JOIN Summoner s ON s = sm.summoner " +
                        "WHERE m.createAt > :startDateTime AND m.createAt <= :endDateTime"
                )
                .parameterValues(parameterValues)
                .build();
    }


    // 쿼리의 `통계`로 캐싱된 `소환사`를 찾아서 `캐싱된 소환사`의 정보를 업데이트하고 저장한다.
    // 이런 식으로 하는 이유는 읽는 시점에 `통계` 값들이 고정되어서 DB에 저장할 때, 최근의 ItemProcessor 결과 하나만 저장하게 됨
    @Bean(BEAN_PREFIX + "itemProcessor1")
    @StepScope
    public ItemProcessor<MatchStatisticsDto, Statistics> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
        return matchStatisticsDto -> {
            Statistics statistics = matchStatisticsDto.statistics();
            Statistics cachedStatistics = InMemCacheStatistics.getInstance().stream()
                    .filter(s -> s.getId().equals(statistics.getId())).findAny()
                    .orElseThrow(() -> new RuntimeException("캐싱된 소환사에 해당되는 소환사가 아닙니다."));
            boolean isWin = isWinMatch(matchStatisticsDto);
            int curWinStreak = isWin ? cachedStatistics.getCurWinStreak() + 1 : 0;
            int curLoseStreak = !isWin ? cachedStatistics.getCurLoseStreak() + 1 : 0;
            int maxWinStreak = curWinStreak > cachedStatistics.getMaxWinStreak() ? curWinStreak : cachedStatistics.getMaxWinStreak();
            int maxLoseStreak = curLoseStreak > cachedStatistics.getMaxLoseStreak() ? curLoseStreak : cachedStatistics.getMaxLoseStreak();
            long winCount = isWin ? cachedStatistics.getWinCount() + 1 : cachedStatistics.getWinCount();
            long loseCount = !isWin ? cachedStatistics.getLoseCount() + 1 : cachedStatistics.getLoseCount();
            Statistics newStatistics = new Statistics(
                    statistics.getId(),
                    statistics.getSummoner(),
                    curWinStreak,
                    curLoseStreak,
                    maxWinStreak,
                    maxLoseStreak,
                    winCount,
                    loseCount
            );
            InMemCacheStatistics.getInstance().remove(cachedStatistics);
            InMemCacheStatistics.getInstance().add(newStatistics);
            log.warn("newStatistics = {}", newStatistics);
            return newStatistics;
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter1")
    @StepScope
    public ItemWriter<Statistics> itemWriter1(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemWriter1");
        return new JpaItemWriterBuilder<Statistics>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }

    private boolean isWinMatch(MatchStatisticsDto matchStatisticsDto) {
        return (boolean) getParticipant(matchStatisticsDto).get("win");
    }

    private Map<String, Object> getParticipant(MatchStatisticsDto matchStatisticsDto) {
        List<Map<String, Object>> participants = (List<Map<String, Object>>) ((Map<String, Object>) matchStatisticsDto.match().getResponse().get("info")).get("participants");
        Summoner summoner = matchStatisticsDto.statistics().getSummoner();
        Map<String, Object> participant = participants.stream().filter(p -> p.get("puuid").equals(summoner.getPuuid())).findAny()
                .orElseThrow(() -> new RuntimeException("Match에서 유효한 summoner를 찾을 수 없습니다"));
        return participant;
    }
}

