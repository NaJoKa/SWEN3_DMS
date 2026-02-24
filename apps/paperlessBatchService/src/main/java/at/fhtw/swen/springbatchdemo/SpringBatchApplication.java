package at.fhtw.swen.springbatchdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBatchDemoApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner runner() {
        return args -> System.out.println("SpringBatchDemo - run the unit-tests!");
    }
}
