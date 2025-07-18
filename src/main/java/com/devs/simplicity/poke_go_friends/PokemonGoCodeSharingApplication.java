package com.devs.simplicity.poke_go_friends;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PokemonGoCodeSharingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokemonGoCodeSharingApplication.class, args);
	}

}
