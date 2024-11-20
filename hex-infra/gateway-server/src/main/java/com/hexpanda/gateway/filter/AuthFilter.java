package com.hexpanda.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexpanda.constant.AuthConstants;
import com.hexpanda.enums.BusinessEnum;
import com.hexpanda.gateway.config.AuthWhitelistConfig;
import com.hexpanda.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;


@Slf4j
@RequiredArgsConstructor
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private final AuthWhitelistConfig authWhitelistConfig;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 校验token
     * 1. 获取请求路径
     * 2. 判断请求路径是否可以放行
     *  放行：不需要验证身份
     *  不放行：需要对其进行身份的认证
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取请求路径
        String path = request.getURI().getPath();
        // 判断当前请求路径是否需要放行，是否存在于白名单中
        if (authWhitelistConfig.getAllowUrls().contains(path)) {
            // 包含：请求路径包含在白名单中，即需要放行
            return chain.filter(exchange);
        }

        // 请求路径不包含在白名单中，需要对其进行身份的验证
        // 从约定好的位置获取Authorization的值，值的格式为：bearer token
        String authorizationValue = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // 判断是否有值
        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith(AuthConstants.BEARER)) {
            // 从Authorization的值中获取token
            String tokenValue = authorizationValue.replaceFirst(AuthConstants.BEARER, "");
            // 判断token值是否有值且是否在redis中存在
            if (StringUtils.hasText(tokenValue)
                    && Boolean.TRUE.equals(stringRedisTemplate.hasKey(AuthConstants.LOGIN_TOKEN_PREFIX + tokenValue))) {
                // 身份验证通过，放行
                return chain.filter(exchange);
            }
        }
        // 流程如果走到这：说明验证身份没有通过或请求不合法
        log.error("Unauthorized request, uri path: {}", path);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 设置响应消息
        ResultVO<Void> result = ResultVO.fail(BusinessEnum.UN_AUTHORIZATION);

        // 创建一个objectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = new byte[0];
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
