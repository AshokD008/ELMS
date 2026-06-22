//package com.lms.usermanagementservice.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.redis.core.ListOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.lang.reflect.Proxy;
//import java.util.Collections;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//// ///////////TODO: Redis is temporarily disabled. Remove this configuration when Redis is re-enabled.
//@Profile("!redis")
//public class RedisDisabledConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//
//        return new NoOpRedisTemplate();
//    }
//
//    private static class NoOpRedisTemplate extends RedisTemplate<String, Object> {
//
//        private final ValueOperations<String, Object> valueOperations =
//                noOpOperations(ValueOperations.class);
//
//        private final ListOperations<String, Object> listOperations =
//                noOpOperations(ListOperations.class);
//
//        @Override
//        public void afterPropertiesSet() {
//            ////////////// Redis is intentionally disabled for local startup without a Redis server.
//        }
//
//        @Override
//        public ValueOperations<String, Object> opsForValue() {
//
//            return valueOperations;
//        }
//
//        @Override
//        public ListOperations<String, Object> opsForList() {
//
//            return listOperations;
//        }
//
//        @Override
//        public Boolean hasKey(String key) {
//
//            return false;
//        }
//
//        @Override
//        public Boolean delete(String key) {
//
//            return false;
//        }
//
//        @Override
//        public Long delete(java.util.Collection<String> keys) {
//
//            return 0L;
//        }
//
//        @Override
//        public Boolean expire(
//                String key,
//                long timeout,
//                TimeUnit unit
//        ) {
//
//            return false;
//        }
//
//        @SuppressWarnings("unchecked")
//        private static <T> T noOpOperations(Class<T> type) {
//
//            return (T) Proxy.newProxyInstance(
//                    type.getClassLoader(),
//                    new Class<?>[]{type},
//                    (proxy, method, args) -> defaultValue(method.getReturnType())
//            );
//        }
//
//        private static Object defaultValue(Class<?> returnType) {
//
//            if (returnType == Void.TYPE) {
//                return null;
//            }
//
//            if (returnType == Boolean.TYPE) {
//                return false;
//            }
//
//            if (returnType == Long.TYPE) {
//                return 0L;
//            }
//
//            if (returnType == Integer.TYPE) {
//                return 0;
//            }
//
//            if (returnType == Double.TYPE) {
//                return 0D;
//            }
//
//            if (returnType == Float.TYPE) {
//                return 0F;
//            }
//
//            if (returnType == Short.TYPE) {
//                return (short) 0;
//            }
//
//            if (returnType == Byte.TYPE) {
//                return (byte) 0;
//            }
//
//            if (returnType == Character.TYPE) {
//                return '\0';
//            }
//
//            if (returnType == java.util.List.class) {
//                return Collections.emptyList();
//            }
//
//            if (returnType == Set.class) {
//                return Collections.emptySet();
//            }
//
//            if (returnType == Map.class) {
//                return Collections.emptyMap();
//            }
//
//            return null;
//        }
//    }
//}
