package io.felipepoliveira.jserializer.plugins.spring;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JSerializationConfiguration implements WebMvcConfigurer{
	
	{
		System.out.println("[JSerializer-Plugin-Springweb] Loaded");
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jsonMessageConverter());
		WebMvcConfigurer.super.configureMessageConverters(converters);
	}
	
	@Bean
	public JSerializationJsonMessageConverter jsonMessageConverter() {
		return new JSerializationJsonMessageConverter();
	}

}
