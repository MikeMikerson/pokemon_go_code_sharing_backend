package com.devs.simplicity.poke_go_friends;

import com.devs.simplicity.poke_go_friends.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PokemonGoCodeSharingApplicationTests {

	@Test
	void contextLoads() {
		// Test context loading with H2 database
		// This validates that all beans can be created properly
		// Individual components are tested in their respective test classes
	}

}
