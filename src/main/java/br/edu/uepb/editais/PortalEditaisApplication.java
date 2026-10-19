package br.edu.uepb.editais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PortalEditaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortalEditaisApplication.class, args);
	}

}
