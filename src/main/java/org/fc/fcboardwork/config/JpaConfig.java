package org.fc.fcboardwork.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class JpaConfig {
    // auditing 할 때 createdby나 modifiedby써서 사람 이름 정보 넣을 수 있게 하려함
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("jay"); // TODO: 스프링 시큐리티로 인증 기능 붙이게 되면 수정할것
    }
}
