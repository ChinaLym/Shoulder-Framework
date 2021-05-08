package org.shoulder.autoconfigure.db.mybatis;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.shoulder.core.constant.PageConst;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.mybatis.config.handler.ModelMetaObjectHandler;
import org.shoulder.data.mybatis.injector.ShoulderSqlInjector;
import org.shoulder.data.mybatis.interceptor.ForbbidonWriteInterceptor;
import org.shoulder.data.mybatis.interceptor.tenants.SchemaModeInterceptor;
import org.shoulder.data.mybatis.interceptor.typehandler.FullLikeTypeHandler;
import org.shoulder.data.mybatis.interceptor.typehandler.LeftLikeTypeHandler;
import org.shoulder.data.mybatis.interceptor.typehandler.RightLikeTypeHandler;
import org.shoulder.data.uid.UidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * MybatisPlusConfig todo 【开发】根据 3.4 版本进行调整
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MybatisPlusVersion.class)
@EnableConfigurationProperties(DatabaseProperties.class)
public class MybatisPlusAutoConfiguration {

    @Autowired
    protected DatabaseProperties databaseProperties;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 开启分页插件
     */
    @Deprecated
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(PaginationInterceptor.class)
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置最大单页限制数量，默认 500 条，-1 不受限制，这里改为 PageConst.MAX_PAGE_SIZE
        paginationInterceptor.setLimit(PageConst.MAX_PAGE_SIZE);
        // 若查询目标页码大于总页码，则查询第一页
        paginationInterceptor.setOverflow(false);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     * <p>
     * 注意:
     * InnerPlugin 插件使用时需要注意顺序关系,建议使用如下顺序
     * 多租户插件,动态表名插件
     * 分页插件,乐观锁插件
     * sql性能规范插件,防止全表更新与删除插件
     * 总结: 对sql进行单次改造的优先放入,不对sql进行改造的最后放入
     * <p>
     * 参考：
     * https://mybatis.plus/guide/interceptor.html#%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F-%E4%BB%A5%E5%88%86%E9%A1%B5%E6%8F%92%E4%BB%B6%E4%B8%BE%E4%BE%8B
     */
    @Bean
    @Order(5)
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> innerInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        StringJoiner interceptorNames = new StringJoiner(",");
        innerInterceptors.stream().map(i -> i.getClass().getSimpleName()).forEach(interceptorNames::add);
        log.debug("Enable MybatisPlusInterceptors: {}", interceptorNames);
        if (!innerInterceptors.isEmpty()) {
            innerInterceptors.forEach(interceptor::addInnerInterceptor);
        }
        return interceptor;
    }

    @Bean
    @Order(0)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "tenantMode", havingValue = "SCHEMA")
    public InnerInterceptor schemaModeInterceptor() {
        return new SchemaModeInterceptor(databaseProperties.getTenantDatabasePrefix());
    }

    @Bean
    @Order(0)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "tenantMode", havingValue = "COLUMN")
    public InnerInterceptor tenantLineInnerInterceptor() {
        return new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public String getTenantIdColumn() {
                return databaseProperties.getTenantIdColumn();
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return false;
            }

            @Override
            public Expression getTenantId() {
                return new StringValue(AppContext.getTenantCode());
            }
        });
    }

    @Bean
    @Order(20)
    @ConditionalOnMissingBean
    public InnerInterceptor paginationInnerInterceptor() {
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 单页分页条数限制
        paginationInterceptor.setMaxLimit(databaseProperties.getLimit());
        // 数据库类型
        paginationInterceptor.setDbType(databaseProperties.getDbType());
        // 溢出总页数后是否进行处理
        paginationInterceptor.setOverflow(true);
        return paginationInterceptor;
    }

    /**
     * mybatis-plus 3.4.0开始采用新的分页插件,一缓和二缓遵循mybatis的规则,
     * 需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题
     * (该属性会在旧插件移除后一同移除)
     */
    @Bean
    @Deprecated
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }


    @Bean
    @Order(80)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "blockWriteFullTable", havingValue = "true")
    public InnerInterceptor blockWriteFullTable() {
        //防止全表更新与删除插件
        return new BlockAttackInnerInterceptor();
    }

    @Bean
    @Order(100)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "checkSqlPerformance", havingValue = "true")
    public InnerInterceptor illegalSQLInnerInterceptor() {
        // sql性能规范插件
        return new IllegalSQLInnerInterceptor();
    }

    /**
     * 演示环境权限拦截器
     */
    @Bean
    //@Order(15)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "forbiddenWrite", havingValue = "true")
    public ForbbidonWriteInterceptor forbbidonWriteInterceptor() {
        return new ForbbidonWriteInterceptor();
    }


    // ***********************************************************


    /**
     * 自动填充基础字段
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelMetaObjectHandler modelMetaObjectHandler() {
        ModelMetaObjectHandler metaObjectHandler = new ModelMetaObjectHandler();
        log.info("ModelMetaObjectHandler [{}]", metaObjectHandler);
        return metaObjectHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator uidGenerator() {
        // todo uid
        return (a, b) -> UUID.randomUUID().toString();
    }

    /**
     * Mybatis 自定义的类型处理器： 处理XML中  #{name,typeHandler=fullLike}
     */

    @Bean
    public LeftLikeTypeHandler getLeftLikeTypeHandler() {
        return new LeftLikeTypeHandler();
    }

    @Bean
    public RightLikeTypeHandler getRightLikeTypeHandler() {
        return new RightLikeTypeHandler();
    }

    @Bean
    public FullLikeTypeHandler getFullLikeTypeHandler() {
        return new FullLikeTypeHandler();
    }


    /**
     * mapper 额外方法
     */
    @Bean
    @ConditionalOnMissingBean
    public ShoulderSqlInjector getMySqlInjector() {
        return new ShoulderSqlInjector();
    }

}
