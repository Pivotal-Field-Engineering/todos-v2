package io.todos.cloudcache;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.geode.config.annotation.EnableDurableClient;
import org.springframework.geode.config.annotation.UseMemberName;

@Configuration
@EnablePdx
@EnableDurableClient(id = "todos-store")
@EnableEntityDefinedRegions(basePackageClasses = Todo.class)
@EnableLogging
@UseMemberName("TodosPCC")
public class Config {

}
