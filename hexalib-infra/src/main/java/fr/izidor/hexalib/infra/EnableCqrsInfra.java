package fr.izidor.hexalib.infra;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CqrsInfraConfiguration.class, DomainScanRegistrar.class})
public @interface EnableCqrsInfra {

    String domainBasePackage();

}
