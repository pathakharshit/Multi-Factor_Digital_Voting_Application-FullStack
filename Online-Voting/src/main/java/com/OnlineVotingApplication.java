package com.example.Online.Voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnlineVotingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineVotingApplication.class, args);
	}

}
