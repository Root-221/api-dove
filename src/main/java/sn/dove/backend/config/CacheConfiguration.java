package sn.dove.backend.config;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Caffeine caffeine = jHipsterProperties.getCache().getCaffeine();

        CaffeineConfiguration<Object, Object> caffeineConfiguration = new CaffeineConfiguration<>();
        caffeineConfiguration.setMaximumSize(OptionalLong.of(caffeine.getMaxEntries()));
        caffeineConfiguration.setExpireAfterWrite(OptionalLong.of(TimeUnit.SECONDS.toNanos(caffeine.getTimeToLiveSeconds())));
        caffeineConfiguration.setStatisticsEnabled(true);
        jcacheConfiguration = caffeineConfiguration;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, sn.dove.backend.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, sn.dove.backend.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, sn.dove.backend.domain.User.class.getName());
            createCache(cm, sn.dove.backend.domain.Authority.class.getName());
            createCache(cm, sn.dove.backend.domain.User.class.getName() + ".authorities");
            createCache(cm, sn.dove.backend.domain.Utilisateur.class.getName());
            createCache(cm, sn.dove.backend.domain.Utilisateur.class.getName() + ".communauteNandites");
            createCache(cm, sn.dove.backend.domain.Role.class.getName());
            createCache(cm, sn.dove.backend.domain.UtilisateurRole.class.getName());
            createCache(cm, sn.dove.backend.domain.Signalement.class.getName());
            createCache(cm, sn.dove.backend.domain.Application.class.getName());
            createCache(cm, sn.dove.backend.domain.Application.class.getName() + ".moduleses");
            createCache(cm, sn.dove.backend.domain.Module.class.getName());
            createCache(cm, sn.dove.backend.domain.Contenu.class.getName());
            createCache(cm, sn.dove.backend.domain.FAQ.class.getName());
            createCache(cm, sn.dove.backend.domain.Media.class.getName());
            createCache(cm, sn.dove.backend.domain.FeedBack.class.getName());
            createCache(cm, sn.dove.backend.domain.Metier.class.getName());
            createCache(cm, sn.dove.backend.domain.Message.class.getName());
            createCache(cm, sn.dove.backend.domain.Message.class.getName() + ".reponseses");
            createCache(cm, sn.dove.backend.domain.Message.class.getName() + ".signalementses");
            createCache(cm, sn.dove.backend.domain.Discussion.class.getName());
            createCache(cm, sn.dove.backend.domain.Discussion.class.getName() + ".messageses");
            createCache(cm, sn.dove.backend.domain.CommunauteNandite.class.getName());
            createCache(cm, sn.dove.backend.domain.CommunauteNandite.class.getName() + ".discussionses");
            createCache(cm, sn.dove.backend.domain.CommunauteNandite.class.getName() + ".membreses");
            createCache(cm, sn.dove.backend.domain.Reaction.class.getName());
            createCache(cm, sn.dove.backend.domain.SignalementMessage.class.getName());
            // jhipster-needle-caffeine-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
