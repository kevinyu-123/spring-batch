package com.study.batch.batch.job;

import com.study.batch.batch.domain.account.Account;
import com.study.batch.batch.domain.account.AccountRepository;
import com.study.batch.batch.domain.order.Order;
import com.study.batch.batch.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public Step migrationStep(ItemReader trOrderReader){
        return stepBuilderFactory.get("migrationStep")
                //db에서 읽어오는데이터 타입, writer 할 데이터 타입
                .<Order, Account>chunk(5) // chunk: 5개의 데이터 단위로 처리(transaction의 개수), 읽어오는 데이터 타입 : order, write 할 데이터 타입 : order
                .reader(trOrderReader)
                .processor(orderProcessor())
                .writer(repOrderWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Order,Account> orderProcessor(){
        return new ItemProcessor<Order, Account>() {
            @Override
            public Account process(Order item) throws Exception {
                return new Account(item);
            }
        };
    }

    @Bean
    @StepScope
    public RepositoryItemWriter<Account> repOrderWriter(){

        return new RepositoryItemWriterBuilder<Account>()
                .repository(accountRepository) //사용 레파지토리
                .methodName("save") // 레파지토리에서 사용하는 메소드
                .build();
    }

    /*
      RepositoryItemWriter를 사용하지 않고 직접 save 구현
    */
    @Bean
    @StepScope
    public ItemWriter<Account> orderWriter(){
        return new ItemWriter<Account>() {
            @Override
            public void write(List<? extends Account> items) throws Exception {
                      items.forEach(item -> accountRepository.save(item));
            }
        };
    }


    @Bean
    @StepScope
    public RepositoryItemReader<Order> trOrderReader(){
        return new RepositoryItemReaderBuilder<Order>()
                .name("trOrderReader")
                .repository(orderRepository)
                .methodName("findAll") //repository method
                .pageSize(5) // chunk 사이즈와 동일
                .arguments(Arrays.asList())
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC)) //정렬
                .build();
    }

}
