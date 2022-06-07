package am.ejob.backend.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableMongoRepositories(
        basePackages = "am.ejob.backend.common.repository"
)
@ComponentScan(basePackages = {
		"am.ejob.backend.api", "am.ejob.backend.common"
})
@EnableWebSecurity
public class EJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(EJobApplication.class, args);
	}

}
