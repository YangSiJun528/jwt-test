package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.match.dto.MatchSummonerDto;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.DelayItemReadListener;
import com.example.jwttest.global.batch.InMemCache;
import com.example.jwttest.global.batch.ResetCacheJobListener;
import com.example.jwttest.global.batch.dto.MatchSummonerBatchDto;
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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalMatchJobConfiguration {

    private int CHUNK_SIZE = 100;
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

    @Bean(JOB_NAME)
    public Job renewMatchJob(JobRepository jobRepository,
                             @Qualifier(BEAN_PREFIX + "step1") Step step1,
                             @Qualifier(BEAN_PREFIX + "step2") Step step2,
                             @Qualifier(BEAN_PREFIX + "step3") Step step3
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(new ResetCacheJobListener())
                .start(step1)
                .next(step2)
                .next(step3)
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
                .<Summoner, List<String>>chunk(CHUNK_SIZE, transactionManager)
                .listener(new DelayItemReadListener<>())
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

    //ItemReader만 직접 구현하는 방식으론 사용할 수 없음 - ItemStream도 구현해야 함
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
                .<String, Match>chunk(CHUNK_SIZE, transactionManager)
                .listener(new DelayItemReadListener<>())
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
            Map<String, Object> info = (Map<String, Object>) rs.get("info");
            List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");
            Long startTimestamp = (Long) info.get("gameStartTimestamp");
            LocalDateTime startDateTime =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp),
                            TimeZone.getDefault().toZoneId());
            List<String> summonerIds = participants.stream().map((participant) -> participant.get("summonerId").toString()).toList();
            log.warn(rs.toString());
            log.warn(summonerIds.toString());
            return new Match(matchId, summonerIds, rs, startDateTime, null);
        };
    }

    // 단순히 성능 면에서는 Jdbc Batch Insert가 더 좋음
    // https://jojoldu.tistory.com/507
    @Bean(BEAN_PREFIX + "itemWriter2")
    @StepScope
    public ItemWriter<Match> itemWriter2(EntityManagerFactory entityManagerFactory) {
        log.warn(BEAN_PREFIX + "itemWriter2");
        return new JpaItemWriterBuilder<Match>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }

    // TODO Step2 끝나고 나서 메모리 DB 초기화 하는 로직 추가

    @Bean(BEAN_PREFIX + "step3")
    @JobScope
    public Step step3(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "step3");
        return new StepBuilder(BEAN_PREFIX + "step3", jobRepository)
                .<MatchSummonerBatchDto, MatchSummonerBatchDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader3(dataSource))
                .processor(itemProcessor3())
                .writer(itemWriter3(dataSource))
                .build();
    }

    // 오늘 배치 돌린 Match의
    @Bean(BEAN_PREFIX + "itemReader3")
    @StepScope
    public JdbcPagingItemReader<MatchSummonerBatchDto> itemReader3(DataSource dataSource) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("startDateTime", jobParameter.getDateTime().minusDays(1L));
        parameterValues.put("endDateTime", jobParameter.getDateTime());
        return new JdbcPagingItemReaderBuilder<MatchSummonerBatchDto>()
                .name(BEAN_PREFIX + "itemReader3")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(matchSummonerDtoRowMapper)
                .queryProvider(createQueryProvider(dataSource))
                .parameterValues(parameterValues)
                .build();
    }
    // 그냥 이럴꺼면 JDBC 사용
    // summoner id + apiId(이 값으로 가져옴) - match id + summoner ids(join 필요)
    // 매치 중에 ids가 apiId랑 같은 것들
    // MATCH_SUMMONER 테이블에 매치 id랑 summoner id 연결한 테이블을 추가해서 관계 형성

    @Bean
    public PagingQueryProvider createQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
        queryProvider.setSelectClause("m.MATCH_ID as MATCH_ID, s.SUMMONER_API_ID as SUMMONER_API_ID, s.SUMMONER_ID as SUMMONER_ID");
        queryProvider.setFromClause("from `match` as m, `match_summoner_ids` msi, `summoner` as s");
        queryProvider.setWhereClause(
                "WHERE m.STARTED_AT BETWEEN :startDateTime AND :endDateTime " +
                        "AND msi.MATCH_ID = m.MATCH_ID " +
                        "AND s.SUMMONER_API_ID = msi.SUMMONER_IDS"
        );
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("MATCH_ID", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);
        try {
            return queryProvider.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final RowMapper<MatchSummonerBatchDto> matchSummonerDtoRowMapper =
            (rs, rowNum) -> {
                UUID uuid = UUID.randomUUID();
                byte[] uuidBytes = new byte[16];
                ByteBuffer.wrap(uuidBytes)
                        .order(ByteOrder.BIG_ENDIAN)
                        .putLong(uuid.getMostSignificantBits());
                return new MatchSummonerBatchDto(uuidBytes, rs.getString("MATCH_ID"), rs.getString("SUMMONER_ID"), rs.getString("SUMMONER_API_ID"));
            };
    @Bean(BEAN_PREFIX + "itemProcessor3")
    @StepScope
    public ItemProcessor<MatchSummonerBatchDto, MatchSummonerBatchDto> itemProcessor3() {
        log.warn(BEAN_PREFIX + "itemProcessor3");
        return item -> {
            log.warn("matchSummonerDto = {}", item);
            return item;
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter3")
    @StepScope
    public ItemWriter<MatchSummonerBatchDto> itemWriter3(DataSource dataSource) {
        log.warn(BEAN_PREFIX + "itemWriter3");
        return new JdbcBatchItemWriterBuilder<MatchSummonerBatchDto>()
                .dataSource(dataSource)
                .sql("insert into `match_summoner`(MATCH_SUMMONER_ID, MATCH_ID, SUMMONER_ID) values (:id, :matchId, :summonerId)")
                .beanMapped() // Match의 필드를 사용할 수 있게 해줌
                .build();
    }
}

