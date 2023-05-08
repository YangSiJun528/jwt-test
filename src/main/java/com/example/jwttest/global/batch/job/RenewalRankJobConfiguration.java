package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.rank.domain.Rank;
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
import org.springframework.batch.item.database.JdbcPagingItemReader;
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
public class RenewalRankJobConfiguration {

    private int CHUNK_SIZE = 100;
    private final String JOB_NAME = "renewRankJob";
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

    // TODO 근데 이건 그냥 Order By 박아서 걍 저장하면 되는거 아님?
    //  아니면 ~ 별 랭킹 해가지고 스텝 여러 개 만들면 될 듯
    //  이러면 페이징 필요 없으니까 걍 JpaReader 써도 되고 - 그냥 JpaReader는 없음, 무조건 페이징기능 있는 구현체만 있더라
    //  다른 방식으로는 통계 전부 읽어서 하는 방법이 있는데,
    //  DB 부하(조회 많음)나 효율 면에서 별로임 - 스텝 여러개로 하는 방식이면 SQL 인덱스만 잘 타면 효율좋게 가능

    @Bean(BEAN_PREFIX + "step1")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<List<Summoner>, List<Rank>>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader1(entityManagerFactory))
                .processor(itemProcessor1())
                .writer(itemWriter1(entityManagerFactory))
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader1")
    @StepScope
    public JpaPagingItemReader<List<Summoner>> itemReader1(EntityManagerFactory entityManagerFactory) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("startDateTime", jobParameter.getDateTime().minusDays(1L));
        parameterValues.put("endDateTime", jobParameter.getDateTime());

        log.warn(BEAN_PREFIX + "itemReader1");
        return new JpaPagingItemReaderBuilder<List<Summoner>>()
                .name(BEAN_PREFIX + "itemReader1")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("")
                .parameterValues(parameterValues)
                .build();
    }

    @Bean(BEAN_PREFIX + "itemProcessor1")
    @StepScope
    public ItemProcessor<List<Summoner>, List<Rank>> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
        return summoners -> {
            // TODO 소환사 순서대로 랭킹 값 생성 반복문
            return null; // 랭킹 리스트
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter1")
    @StepScope
    public ItemWriter<List<Rank>> itemWriter1(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemWriter1");
        return new JpaItemWriterBuilder<List<Rank>>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }
}

