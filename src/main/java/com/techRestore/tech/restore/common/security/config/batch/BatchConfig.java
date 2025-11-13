package com.techRestore.tech.restore.common.security.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.shop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ProductRepository productRepository;
  private final ProductItemProcessor productItemProcessor;

  @Bean
  @StepScope
  public FlatFileItemReader<ProductCSV> productItemReader() {
    FlatFileItemReader<ProductCSV> reader = new FlatFileItemReader<>();
    String path=System.getProperty("uploadFile");
    reader.setResource(new FileSystemResource(path));
    reader.setLinesToSkip(1);
    reader.setLineMapper(lineMapper());
    return reader;
  }

  @Bean
  public RepositoryItemWriter<Product> productItemWriter() {
    RepositoryItemWriter<Product> writer = new RepositoryItemWriter<>();
    writer.setRepository(productRepository);
    writer.setMethodName("save");
    return writer;
  }

  @Bean
  public Step importStep(){
    return new StepBuilder("csvImport", jobRepository)
    .<ProductCSV, Product>chunk(10, transactionManager)
    .reader(productItemReader())
    .processor(productItemProcessor)
    .writer(productItemWriter())
    .build();
  }

  @Bean
  public Job runJob() {
      return new JobBuilder("importProducts", jobRepository)
              .start(importStep())
              .build();
  }

  private LineMapper<ProductCSV> lineMapper() {
    DefaultLineMapper<ProductCSV> lineMapper = new DefaultLineMapper<>();

    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setStrict(false);
    lineTokenizer.setNames("name", "description", "price", "stock", "condition", "imageUrl", "deleted", "category");

    BeanWrapperFieldSetMapper<ProductCSV> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(ProductCSV.class);

    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSetMapper);

    return lineMapper;
    }


}
