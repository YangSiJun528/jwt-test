package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.match.dto.MatchSummonerDto;
import com.example.jwttest.domain.rank.domain.Rank;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCacheStatistics;
import com.example.jwttest.global.batch.ResetCacheJobListener;
import com.example.jwttest.global.batch.dto.MatchStatisticsDto;
import com.example.jwttest.global.batch.dto.RankForJdbcDto;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final RowMapper<Map<String, Object>> columnMapRowMapper =
            (rs, rowNum) -> {
                // Get the ResultSetMetaData
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Create a new Map to hold the column values
                Map<String, Object> map = new HashMap<>(columnCount);

                // Loop through the columns and add the column values to the map
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object columnValue = rs.getObject(i);
                    map.put(columnName, columnValue);
                }

                // Return the map
                return map;
            };

    @Bean(JOB_NAME)
    public Job renewStatisticsJob(JobRepository jobRepository,
                                  @Qualifier(BEAN_PREFIX + "tier_" + "step") Step rankStep,
                                  @Qualifier(BEAN_PREFIX + "curLoseStreak_" + "step") Step curLoseStreakStep,
                                  @Qualifier(BEAN_PREFIX + "curWinStreak_" + "step") Step curWinStreakStep,
                                  @Qualifier(BEAN_PREFIX + "matchCount_" + "step") Step matchCountStep,
                                  @Qualifier(BEAN_PREFIX + "summonerLevel_" + "step") Step summonerLevelStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(new ResetCacheJobListener())
                .start(rankStep)
                .next(curLoseStreakStep)
                .next(curWinStreakStep)
                .next(matchCountStep)
                .next(summonerLevelStep)
                .build();
    }

    // TODO 근데 이건 그냥 Order By 박아서 걍 저장하면 되는거 아님?
    //  아니면 ~ 별 랭킹 해가지고 스텝 여러 개 만들면 될 듯
    //  이러면 페이징 필요 없으니까 걍 JpaReader 써도 되고 - 그냥 JpaReader는 없음, 무조건 페이징기능 있는 구현체만 있더라
    //  다른 방식으로는 통계 전부 읽어서 하는 방법이 있는데,
    //  DB 부하(조회 많음)나 효율 면에서 별로임 - 스텝 여러개로 하는 방식이면 SQL 인덱스만 잘 타면 효율좋게 가능

    @Bean(BEAN_PREFIX + "tier_" + "step")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "tier_" + "step");
        return new StepBuilder(BEAN_PREFIX + "tier_" + "step", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(tierItemReader(dataSource))
                .processor(tierItemProcessor())
                .writer(commonItemWriter(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "tier_" + "itemReader")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> tierItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        String rankCase = "CASE l.RANK_NUM " +
                "WHEN 'I' THEN 0 " +
                "WHEN 'II' THEN 1 " +
                "WHEN 'III' THEN 2 " +
                "WHEN 'VI' THEN 3 " +
                "END ";
        String tierCase = "CASE l.TIER " +
                "WHEN 'CHALLENGER' THEN 0 " +
                "WHEN 'GRANDMASTER' THEN 1 " +
                "WHEN 'MASTER' THEN 2 " +
                "WHEN 'DIAMOND' THEN 3 " +
                "WHEN 'PLATINUM' THEN 4 " +
                "WHEN 'GOLD' THEN 5 " +
                "WHEN 'SILVER' THEN 6 " +
                "WHEN 'BRONZE' THEN 7 " +
                "WHEN 'IRON' THEN 8 " +
                "ELSE -1 END";
        queryProviderFactoryBean.setSelectClause("s.SUMMONER_ID as SUMMONER_ID, " +
                "ROW_NUMBER() OVER (PARTITION BY QUEUE_TYPE ORDER BY "+ rankCase +" ASC, "+ tierCase +" ASC, LEAGUE_POINTS DESC) as RANKING_NUMBER, " +
                "l.RANK_NUM as RANK_NUM, " +
                "l.TIER as TIER_TYPE, " +
                "l.LEAGUE_POINTS as LEAGUE_POINTS, " +
                "l.QUEUE_TYPE as QUEUE_TYPE "
        );
        queryProviderFactoryBean.setFromClause("from summoner as s, statistics as st, league as l ");
        queryProviderFactoryBean.setWhereClause("WHERE st.SUMMONER_SUMMONER_ID = s.SUMMONER_ID AND l.SUMMONER_SUMMONER_ID = s.SUMMONER_ID ");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("SUMMONER_ID", Order.ASCENDING);
        // h2는 select 절에서 case 사용해도 order By절 에서 사용 불가

        queryProviderFactoryBean.setSortKeys(sortKeys);

        PagingQueryProvider queryProvider;

        try {
            queryProvider = queryProviderFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.warn(BEAN_PREFIX + "tier_" + "itemReader");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "tier_" + "itemReader")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "tier_" + "itemProcessor")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> tierItemProcessor() {
        log.warn(BEAN_PREFIX + "tier_" + "itemProcessor");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            String queueType = (String) rankInfo.get("QUEUE_TYPE"); // 솔랭, 자랭
            String tierType = (String) rankInfo.get("TIER_TYPE");
            String rankNum = (String) rankInfo.get("RANK_NUM");
            Integer leaguePoints = (Integer) rankInfo.get("LEAGUE_POINTS");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            String strRankType;
            if(queueType.equals("RANKED_SOLO_5x5")) strRankType = RankType.TIER_RANKED_SOLO_5x5.name();
            else if(queueType.equals("RANKED_FLEX_SR")) strRankType = RankType.TIER_RANKED_FLEX_SR.name();
            else strRankType = "error";

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    strRankType, // String으로 변환해서 저장
                    queueType + "_" +tierType + "_" + rankNum + "_" + leaguePoints,
                    jobParameter.getDateTime()
            );
        };
    }

    @Bean(BEAN_PREFIX + "curLoseStreak_" + "step")
    @JobScope
    public Step curLoseStreakStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "curLoseStreak_" + "step");
        return new StepBuilder(BEAN_PREFIX + "curLoseStreak_" + "step", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(curLoseStreakItemReader(dataSource))
                .processor(curLoseStreakItemProcessor())
                .writer(commonItemWriter(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "curLoseStreak_" + "itemReader")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> curLoseStreakItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("s.SUMMONER_ID as SUMMONER_ID, " +
                "ROW_NUMBER() OVER (ORDER BY CUR_LOSE_STREAK DESC) as RANKING_NUMBER, " +
                "st.CUR_LOSE_STREAK as CUR_LOSE_STREAK "
        );
        queryProviderFactoryBean.setFromClause("from SUMMONER as s, STATISTICS as st ");
        queryProviderFactoryBean.setWhereClause("WHERE st.SUMMONER_SUMMONER_ID = s.SUMMONER_ID ");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("SUMMONER_ID", Order.ASCENDING);

        queryProviderFactoryBean.setSortKeys(sortKeys);

        PagingQueryProvider queryProvider;

        try {
            queryProvider = queryProviderFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.warn(BEAN_PREFIX + "curLoseStreak_" + "itemReader");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "curLoseStreak_" + "itemReader")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "curLoseStreak_" + "itemProcessor")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> curLoseStreakItemProcessor() {
        log.warn(BEAN_PREFIX + "curLoseStreak_" + "itemProcessor");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            Integer curLoseStreak = (Integer) rankInfo.get("CUR_LOSE_STREAK");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    RankType.CUR_LOSE_STREAK.name(), // String으로 변환해서 저장
                    curLoseStreak.toString(),
                    jobParameter.getDateTime()
            );
        };
    }

    @Bean(BEAN_PREFIX + "curWinStreak_" + "step")
    @JobScope
    public Step curWinStreakStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "curWinStreak_" + "step");
        return new StepBuilder(BEAN_PREFIX + "curWinStreak_" + "step", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(curWinStreakItemReader(dataSource))
                .processor(curWinStreakItemProcessor())
                .writer(commonItemWriter(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "curWinStreak_" + "itemReader")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> curWinStreakItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("s.SUMMONER_ID as SUMMONER_ID, " +
                "ROW_NUMBER() OVER (ORDER BY CUR_WIN_STREAK DESC) as RANKING_NUMBER, " +
                "st.CUR_WIN_STREAK as CUR_WIN_STREAK "
        );
        queryProviderFactoryBean.setFromClause("from SUMMONER as s, STATISTICS as st ");
        queryProviderFactoryBean.setWhereClause("WHERE st.SUMMONER_SUMMONER_ID = s.SUMMONER_ID ");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("SUMMONER_ID", Order.ASCENDING);

        queryProviderFactoryBean.setSortKeys(sortKeys);

        PagingQueryProvider queryProvider;

        try {
            queryProvider = queryProviderFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.warn(BEAN_PREFIX + "curWinStreak_" + "itemReader");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "curWinStreak_" + "itemReader")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "curWinStreak_" + "itemProcessor")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> curWinStreakItemProcessor() {
        log.warn(BEAN_PREFIX + "curWinStreak_" + "itemProcessor");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            Integer curWinStreak = (Integer) rankInfo.get("CUR_WIN_STREAK");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    RankType.CUR_WIN_STREAK.name(), // String으로 변환해서 저장
                    curWinStreak.toString(),
                    jobParameter.getDateTime()
            );
        };
    }

    @Bean(BEAN_PREFIX + "matchCount_" + "step")
    @JobScope
    public Step matchCountStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "matchCount_" + "step");
        return new StepBuilder(BEAN_PREFIX + "matchCount_" + "step", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(matchCountItemReader(dataSource))
                .processor(matchCountItemProcessor())
                .writer(commonItemWriter(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "matchCount_" + "itemReader")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> matchCountItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("s.SUMMONER_ID as SUMMONER_ID, " +
                "ROW_NUMBER() OVER (ORDER BY st.WIN_COUNT + st.LOSE_COUNT DESC) as RANKING_NUMBER, " +
                "st.WIN_COUNT + st.LOSE_COUNT as MATCH_COUNT "
        );
        queryProviderFactoryBean.setFromClause("from SUMMONER as s, STATISTICS as st ");
        queryProviderFactoryBean.setWhereClause("WHERE st.SUMMONER_SUMMONER_ID = s.SUMMONER_ID ");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("SUMMONER_ID", Order.ASCENDING);

        queryProviderFactoryBean.setSortKeys(sortKeys);

        PagingQueryProvider queryProvider;

        try {
            queryProvider = queryProviderFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.warn(BEAN_PREFIX + "matchCount_" + "itemReader");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "matchCount_" + "itemReader")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "matchCount_" + "itemProcessor")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> matchCountItemProcessor() {
        log.warn(BEAN_PREFIX + "matchCount_" + "itemProcessor");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            Long matchCount = (Long) rankInfo.get("MATCH_COUNT");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    RankType.MATCH_COUNT.name(), // String으로 변환해서 저장
                    matchCount.toString(),
                    jobParameter.getDateTime()
            );
        };
    }

    @Bean(BEAN_PREFIX + "summonerLevel_" + "step")
    @JobScope
    public Step summonerLevelStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "summonerLevel_" + "step");
        return new StepBuilder(BEAN_PREFIX + "summonerLevel_" + "step", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(summonerLevelItemReader(dataSource))
                .processor(summonerLevelItemProcessor())
                .writer(commonItemWriter(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "summonerLevel_" + "itemReader")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> summonerLevelItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("s.SUMMONER_ID as SUMMONER_ID, " +
                "ROW_NUMBER() OVER (ORDER BY s.SUMMONER_LEVEL DESC) as RANKING_NUMBER, " +
                "s.SUMMONER_LEVEL as SUMMONER_LEVEL "
        );
        queryProviderFactoryBean.setFromClause("from SUMMONER as s, STATISTICS as st ");
        queryProviderFactoryBean.setWhereClause("WHERE st.SUMMONER_SUMMONER_ID = s.SUMMONER_ID ");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("SUMMONER_ID", Order.ASCENDING);

        queryProviderFactoryBean.setSortKeys(sortKeys);

        PagingQueryProvider queryProvider;

        try {
            queryProvider = queryProviderFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.warn(BEAN_PREFIX + "summonerLevel_" + "itemReader");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "summonerLevel_" + "itemReader")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "summonerLevel_" + "itemProcessor")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> summonerLevelItemProcessor() {
        log.warn(BEAN_PREFIX + "summonerLevel_" + "itemProcessor");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            Integer summonerLevel = (Integer) rankInfo.get("SUMMONER_LEVEL");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    RankType.SUMMONER_LEVEL.name(), // String으로 변환해서 저장
                    summonerLevel.toString(),
                    jobParameter.getDateTime()
            );
        };
    }


    @Bean(BEAN_PREFIX + "commonItemWriter")
    @StepScope
    public ItemWriter<RankForJdbcDto> commonItemWriter(DataSource dataSource) {
        log.warn(BEAN_PREFIX + "commonItemWriter");
        return new JdbcBatchItemWriterBuilder<RankForJdbcDto>()
                .dataSource(dataSource)
                .sql("insert into RANK(RANK_ID, CREATE_AT, RANK_TYPE, RANK_VALUE, RANKING_NUMBER, SUMMONER_SUMMONER_ID) values (:id, :createAt, :rankType, :rankValue, :rankingNumber, :summonerId)")
                .beanMapped()
                .build();
    }
}

//TODO rank에 솔랭, 자랭 여부 추가
// 나머지는 LEAGUE 조인 안해야 함 - LEAGUE에 솔랭 자랭 있어서, 한 소롼사가 여러 컬럼 가질 수 있음
