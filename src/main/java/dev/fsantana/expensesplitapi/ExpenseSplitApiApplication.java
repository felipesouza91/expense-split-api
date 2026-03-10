package dev.fsantana.expensesplitapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ExpenseSplitApiApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication application = new SpringApplication(ExpenseSplitApiApplication.class);
		application.run(args);
	}

}
