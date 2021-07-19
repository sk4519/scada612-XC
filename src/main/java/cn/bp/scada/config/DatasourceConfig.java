/*
package cn.bp.scada.configutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

*/
/**
 * druid 多数据源配置类
 *//*

@Configuration //标注这是一个配置类
public class DatasourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceConfig.class);

    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.oracle1")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mysql1DataSource")
    @Qualifier("mysql1DataSource")
    @ConfigurationProperties(prefix="spring.datasource.mysql1")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

}
*/
