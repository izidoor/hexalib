package fr.izidor.hexalib.infra;

import fr.izidor.hexalib.domain.cqrs.annotations.CommandHandlerComponent;
import fr.izidor.hexalib.domain.ddd.annotations.DomainEventListenerComponent;
import fr.izidor.hexalib.domain.ddd.annotations.DomainEventPublisherComponent;
import fr.izidor.hexalib.domain.ddd.annotations.DomainService;
import fr.izidor.hexalib.domain.hexagone.annotations.Stub;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class DomainScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attrs = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableCqrsInfra.class.getName()));
        String domainBasePackage = attrs.getString("domainBasePackage");

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CommandHandlerComponent.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(DomainService.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(DomainEventListenerComponent.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(DomainEventPublisherComponent.class));


        scanner.findCandidateComponents(domainBasePackage).forEach(bd ->
                registry.registerBeanDefinition(bd.getBeanClassName(), bd));

        if (environment.getProperty("fr.izidor.hexalib.infra.stubs.enabled", Boolean.class, false)) {
            ClassPathScanningCandidateComponentProvider stubScanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            stubScanner.addIncludeFilter(new AnnotationTypeFilter(Stub.class));
            stubScanner.findCandidateComponents(domainBasePackage).forEach(bd ->
                    registry.registerBeanDefinition(bd.getBeanClassName(), bd));
        }
    }
}
