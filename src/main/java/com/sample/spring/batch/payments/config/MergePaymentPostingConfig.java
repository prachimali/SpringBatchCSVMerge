package com.sample.spring.batch.payments.config;

/*import javax.batch.api.chunk.ItemReader;*/
import org.springframework.batch.item.*;

import java.io.IOException;
import java.io.Writer;

import javax.sound.sampled.Line;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.sample.spring.batch.payments.dto.PaymentDTO;
import com.sample.spring.batch.payments.processor.PaymentEntityProcessor;
import com.sample.spring.batch.payments.tasklet.MergePaymentPostingTasklet;
import com.sample.spring.batch.payments.writer.PaymentsCSVFileWriter;

/**
 * Configuration for MergePaymentPosting
 */
@Configuration
@EnableBatchProcessing
@Import(StandaloneInfrastructureConfiguration.class)
public class MergePaymentPostingConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public InfrastructureConfiguration infrastructureConfiguration;

	@Autowired
	public DataSource dataSource;

	@Value("payment-posting*.csv")
	public Resource[] resources;

	@Bean
	public PaymentsCSVFileWriter fileWriter() {
		return new PaymentsCSVFileWriter();
	}

	@Bean
	public MultiResourceItemReader<PaymentDTO> paymentFilesReader() {
		MultiResourceItemReader<PaymentDTO> paymentFileReader = new MultiResourceItemReader<PaymentDTO>();
		paymentFileReader.setResources(resources);
		paymentFileReader.setDelegate(paymentFileEntityReader());
		return paymentFileReader;
	}

	@Bean
	FlatFileItemReader<PaymentDTO> paymentFileEntityReader() {
		System.out.println("In reader");
		FlatFileItemReader<PaymentDTO> paymnetFileReader = new FlatFileItemReader<PaymentDTO>();
		paymnetFileReader.setLinesToSkip(1);
		paymnetFileReader.setLineMapper(paymentEntityLineMapper());
		return paymnetFileReader;
	}

	private LineMapper<PaymentDTO> paymentEntityLineMapper() {
		DefaultLineMapper<PaymentDTO> paymentEntityLineMapper = new DefaultLineMapper<PaymentDTO>();
		paymentEntityLineMapper.setFieldSetMapper(createPaymentEntityMapper());
		paymentEntityLineMapper.setLineTokenizer(createPaymentEntityTokenizer());
		return paymentEntityLineMapper;
	}

	private FieldSetMapper<PaymentDTO> createPaymentEntityMapper() {
		BeanWrapperFieldSetMapper<PaymentDTO> paymentEntityMapper = new BeanWrapperFieldSetMapper<PaymentDTO>();
		paymentEntityMapper.setTargetType(PaymentDTO.class);
		return paymentEntityMapper;
	}

	private LineTokenizer createPaymentEntityTokenizer() {
		DelimitedLineTokenizer paymentEntityTokenizer = new DelimitedLineTokenizer();
		paymentEntityTokenizer.setDelimiter(";");
		paymentEntityTokenizer.setNames(new String[] { "paymentId", "agencyCode", "paymentAmount", "requestDateTime",
				"paymentDate", "paymentMethod", "accountId" });
		return paymentEntityTokenizer;
	}

	@Bean
	public Job MergePaymentPosting() {
		return jobBuilderFactory.get("MergePaymentPosting").incrementer(new RunIdIncrementer()).flow(step())
				.next(step2()).end().build();
	}

	@Bean
	public PaymentEntityProcessor processor() {
		return new PaymentEntityProcessor();
	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("step").<PaymentDTO, PaymentDTO> chunk(3).reader(paymentFilesReader())
				.processor(processor()).writer(fileWriter()).build();
	}

	@Bean
	public Step step2() {
        return stepBuilderFactory.get("step2").tasklet(tasklet()).build();
	}

	@Bean
	public Tasklet tasklet() {
		return new MergePaymentPostingTasklet();
	}
}
