package com.openclassrooms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;

import com.openclassrooms.controllers.LoginController;

@SpringBootTest
class LearnSpringSecurityAuthApplicationTests {
	
	@Autowired
	private LoginController controller;

	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
	}

}
