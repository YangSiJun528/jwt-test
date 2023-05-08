package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.match.dto.MatchSummonerDto;
import com.example.jwttest.domain.rank.domain.Rank;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.batch.InMemCacheStatistics;
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
                      DataSource dataSource
    ) {
        log.warn(BEAN_PREFIX + "step1");
        return new StepBuilder(BEAN_PREFIX + "step1", jobRepository)
                .<Map<String, Object>, RankForJdbcDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader1(dataSource))
                .processor(itemProcessor1())
                .writer(itemWriter1(dataSource))
                .build();
    }

    @Bean(BEAN_PREFIX + "itemReader1")
    @StepScope
    public JdbcPagingItemReader<Map<String, Object>> itemReader1(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        String rankCase = "CASE l.RANK " +
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
                "ROW_NUMBER() OVER (ORDER BY "+ rankCase +" ASC, "+ tierCase +" ASC, LEAGUE_POINTS DESC) as RANKING_NUMBER, " +
                "l.RANK as RANK, " +
                "l.TIER as TIER_TYPE, " +
                "l.LEAGUE_POINTS as LEAGUE_POINTS "
        );
        queryProviderFactoryBean.setFromClause("from SUMMONER as s, STATISTICS as st, LEAGUE as l ");
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

        log.warn(BEAN_PREFIX + "itemReader1");
        return new JdbcPagingItemReaderBuilder<Map<String, Object>>()
                .name(BEAN_PREFIX + "itemReader1")
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(columnMapRowMapper)
                .queryProvider(queryProvider)
                .maxItemCount(100)
                .build();
    }

    @Bean(BEAN_PREFIX + "itemProcessor1")
    @StepScope
    public ItemProcessor<Map<String, Object>, RankForJdbcDto> itemProcessor1() {
        log.warn(BEAN_PREFIX + "itemProcessor1");
        return rankInfo -> {
            UUID summonerId = (UUID) rankInfo.get("SUMMONER_ID");
            String tierType = (String) rankInfo.get("TIER_TYPE");
            String rankValue = (String) rankInfo.get("RANK");
            Integer leaguePoints = (Integer) rankInfo.get("LEAGUE_POINTS");
            Long rankingNumber = (Long) rankInfo.get("RANKING_NUMBER");

            return new RankForJdbcDto(
                    UUID.randomUUID(),
                    summonerId,
                    rankingNumber,
                    RankType.TIER.name(), // String으로 변환해서 저장
                    tierType + "_" + rankValue + "_" + leaguePoints,
                    jobParameter.getDateTime()
            );
        };
    }

    @Bean(BEAN_PREFIX + "itemWriter1")
    @StepScope
    public ItemWriter<RankForJdbcDto> itemWriter1(DataSource dataSource) {
        log.warn(BEAN_PREFIX + "itemWriter1");
        return new JdbcBatchItemWriterBuilder<RankForJdbcDto>()
                .dataSource(dataSource)
                .sql("insert into RANK(RANK_ID, CREATE_AT, RANK_TYPE, RANK_VALUE, RANKING_NUMBER, SUMMONER_SUMMONER_ID) values (:id, :createAt, :rankType, :rankValue, :rankingNumber, :summonerId)")
                .beanMapped()
                .build();
    }
}

