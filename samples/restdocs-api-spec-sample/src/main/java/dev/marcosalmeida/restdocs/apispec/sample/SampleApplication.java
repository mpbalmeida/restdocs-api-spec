package dev.marcosalmeida.restdocs.apispec.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	@Configuration
	static class ValidationConfiguration implements RepositoryRestConfigurer {

		@Override
		public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
			validatingListener.addValidator("beforeCreate", validator());
			validatingListener.addValidator("beforeSave", validator());
		}

		@Bean
		@Primary
		public Validator validator() {
			return new LocalValidatorFactoryBean();
		}

	}
}
