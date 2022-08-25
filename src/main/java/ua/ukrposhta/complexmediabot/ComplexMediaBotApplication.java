package ua.ukrposhta.complexmediabot;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = "ua.ukrposhta.complexmediabot")
public class ComplexMediaBotApplication extends AbstractAnnotationConfigDispatcherServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ComplexMediaBotApplication.class, args);
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[0];
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[0];
	}

	@NotNull
	@Override
	protected String[] getServletMappings() {
		return new String[]{
				"","/",
				"/Complex-Media-Bot/",
				"/Complex-Media-Bot"
		};
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(20))
				.build();
	}

	@Bean
	public WebClient webClient(){
		HttpClient httpClient =HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.responseTimeout(Duration.ofMillis(20000))
				.doOnConnected(conn ->
						conn.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
								.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}

}
