package com.example.jwttest.global.batch.job;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.global.riot.service.SummonerRiotApiService;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalMatchJobConfiguration {

    private int CHUNK_SIZE = 1;
    private final String JOB_NAME = "renewMatchJob";
    private final String BEAN_PREFIX = JOB_NAME + "_";

    private final SummonerRiotApiService summonerRiotApiService;

    @Getter
    @AllArgsConstructor
    public class JobParameter {
        @NonNull
        private final LocalDateTime standardDateTime;

        // @NonNull을 필드에 지정하면 롬복이 생성한 메서드에만 null check 들어감
        // 내가 직접 allArg 생성자를 만든다고 해도, 그거까지 null check를 해주지 않는다는 소리임
    }

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[date]}") LocalDateTime date
    ) {
        return new JobParameter(date);
    }

    @Bean(BEAN_PREFIX + "_step")
    @JobScope
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      EntityManagerFactory entityManagerFactory,
                      ItemReader<Match> reader,
                      ItemWriter<Match> writer
    ) {
        return new StepBuilder("step1", jobRepository)
                .<Match, Match>chunk(100, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean(BEAN_PREFIX + "_itemReader")
    @JobScope
    public ItemReader<Match> itemReader() {
        return new ItemReader<Match>() {
            @Override
            public Match read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                return null;
            }
        };
    }

    @Bean(BEAN_PREFIX + "_itemWriter")
    @JobScope
    public JpaItemWriter<Match> itemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Match>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }


}
