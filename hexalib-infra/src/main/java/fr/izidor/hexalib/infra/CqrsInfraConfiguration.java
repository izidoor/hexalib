package fr.izidor.hexalib.infra;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("fr.izidor.hexalib.infra.commandBus")
public class CqrsInfraConfiguration {
}
