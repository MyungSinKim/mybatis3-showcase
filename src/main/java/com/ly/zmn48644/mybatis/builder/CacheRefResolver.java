
package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.cache.Cache;

public class CacheRefResolver {
    private final MapperBuilderAssistant assistant;
    private final String cacheRefNamespace;

    public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
        this.assistant = assistant;
        this.cacheRefNamespace = cacheRefNamespace;
    }

    public Cache resolveCacheRef() {
        return assistant.useCacheRef(cacheRefNamespace);
    }
}