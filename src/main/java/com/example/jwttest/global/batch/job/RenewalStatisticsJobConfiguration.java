package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
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
                        @Qualifier(BEAN_PREFIX + "step1") Step step1
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(step1)
                .build();
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
                .queryString("SELECT new com.example.jwttest.global.batch.dto.MatchStatisticsDto(m, st) " +
                        "FROM Match m, Summoner sm, Statistics st " +
                        "WHERE m.createAt > :startDateTime AND m.createAt <= :endDateTime" +
                        "AND sm.summonerApiId IN m.summonerIds" +
                        "AND st.summoner = sm "
                )
                .parameterValues(parameterValues)
                .build();

    }


    @Bean(BEAN_PREFIX + "itemProcessor1")
    @StepScope
    public ItemProcessor<MatchStatisticsDto, Statistics> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
        return matchStatisticsDto -> {
            Statistics statistics = matchStatisticsDto.statistics();
            boolean isWin = isWinMatch(matchStatisticsDto);
            int curWinStreak = isWin ? statistics.getCurWinStreak() + 1 : 0;
            int curLoseStreak = !isWin ? statistics.getCurLoseStreak() + 1 : 0;
            int maxWinStreak = curWinStreak > statistics.getMaxWinStreak() ? curWinStreak : statistics.getMaxWinStreak();
            int maxLoseStreak = curLoseStreak > statistics.getMaxLoseStreak() ? curLoseStreak : statistics.getMaxLoseStreak();
            long winCount = isWin ? statistics.getWinCount() + 1 : statistics.getWinCount();
            long loseCount = !isWin ? statistics.getLoseCount() + 1 : statistics.getLoseCount();
            return new Statistics(
                    statistics.getId(),
                    statistics.getSummoner(),
                    curWinStreak,
                    curLoseStreak,
                    maxWinStreak,
                    maxLoseStreak,
                    winCount,
                    loseCount
            );
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

