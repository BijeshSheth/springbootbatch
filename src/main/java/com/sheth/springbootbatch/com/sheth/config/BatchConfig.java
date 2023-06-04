package com.sheth.springbootbatch.com.sheth.config;

import com.sheth.springbootbatch.com.sheth.entity.Invoice;
import com.sheth.springbootbatch.com.sheth.listner.InvoiceListener;
import com.sheth.springbootbatch.com.sheth.repository.InvoiceRepository;
import com.sheth.springbootbatch.com.sheth.utility.BlankLineRecordSeparatorPolicy;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    public final static String FILE_PATH = "invoice.csv";

    @Bean
    public FlatFileItemReader<Invoice> reader()
    {
        FlatFileItemReader<Invoice> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(FILE_PATH));

        reader.setLineMapper(new DefaultLineMapper<Invoice>(){{
            setLineTokenizer(new DelimitedLineTokenizer(){{
                    setDelimiter(DELIMITER_COMMA);
                    setNames("name","number","amount","discount","location");
            }});

            setFieldSetMapper(new BeanWrapperFieldSetMapper(){{
                setTargetType(Invoice.class);
            }});

        }});
        reader.setRecordSeparatorPolicy(new BlankLineRecordSeparatorPolicy());
        return reader;
    }

    //Autowire InvoiceRepository
    @Autowired
    InvoiceRepository repository;

    //Writer class Object
    @Bean
    public ItemWriter<Invoice> writer(){
        // return new InvoiceItemWriter(); // Using lambda expression code instead of a separate implementation
        return invoices -> {
            System.out.println("Saving Invoice Records: " +invoices);
            repository.saveAll(invoices);
        };
    }

    //Processor class Object
    @Bean
    public ItemProcessor<Invoice, Invoice> processor(){
        // return new InvoiceProcessor(); // Using lambda expression code instead of a separate implementation
       /* return invoice -> {
            Double discount = invoice.getAmount()*(invoice.getDiscount()/100.0);
            Double finalAmount= invoice.getAmount()-discount;
            invoice.setAmount(finalAmount);
            return invoice;
        };*/
        return invoice -> { return invoice;};

    }

    //Listener class Object
    @Bean
    public JobExecutionListener listener() {
        return new InvoiceListener();
    }

    //Autowire StepBuilderFactory
    @Autowired
    private StepBuilderFactory sbf;

    //Step Object
    @Bean
    public Step stepA() {
        return sbf.get("stepA")
                .<Invoice,Invoice>chunk(2)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
                ;
    }

    //Autowire JobBuilderFactory
    @Autowired
    private JobBuilderFactory jbf;

    //Job Object
    @Bean
    public Job jobA(){
        return jbf.get("jobA")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .start(stepA())
                // .next(stepB())
                // .next(stepC())
                .build();
    }
}
