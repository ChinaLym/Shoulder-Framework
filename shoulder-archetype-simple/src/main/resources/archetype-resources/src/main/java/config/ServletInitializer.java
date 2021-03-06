package ${package}.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import ${package}.${StartClassName};

/**
 * 打成 war 包部署到外部tomcat需要这个
 *
 * @author ${author}
 */
//@Configuration
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(${StartClassName}.class);
	}

}
