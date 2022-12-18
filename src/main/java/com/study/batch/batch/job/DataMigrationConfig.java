package com.study.batch.batch.job;

import com.study.batch.batch.domain.account.AccountRepository;
import com.study.batch.batch.domain.order.Order;
import com.study.batch.batch.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class DataMigrationConfig {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job migrationJob(Step migrationStep){
        return jobBuilderFactory.get("migrationJob")
                .incrementer(new RunIdIncrementer())
                .start(migrationStep)
                .build();

    }

    @Bean
    @JobScope
    public Step migrationStep(){
        return stepBuilderFactory.get("migrationStep")
                .<Order,Order>chunk(5)
                .build();
    }


}
