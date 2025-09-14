package com.example.redis_practice.aop;

import com.example.redis_practice.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 분산 락을 위한 AOP 클래스
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = createDynamicKey(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock lock = redissonClient.getLock(key);

        try {
            if (!lock.tryLock(10, 1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Failed to acquire lock.");
            }

            // 별도 트랜잭션으로 비즈니스 로직 수행
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new InterruptedException("Thread interrupted while waiting for lock.");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * SpEL을 사용해 동적인 락 키 생성
     *
     * @param parameterNames 메서드 파라미터 이름
     * @param args 실제 전달된 인수 값
     * @param keyExpression 동적 키 표현식
     * @return 락 키
     */
    private String createDynamicKey(String[] parameterNames, Object[] args, String keyExpression) {
        // SpEL 표현식이 없으면 정적 키로 판단하고 그대로 반환
        if (!keyExpression.contains("#{")) {
            return keyExpression;
        }

        // SpEL 파서와 컨텍스트 생성
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        // 메서드 파라미터를 SpEL 컨텍스트에 등록
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        try {
            // SpEL 표현식 파싱 및 평가
            Expression expression = parser.parseExpression(keyExpression, new TemplateParserContext());
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse lock key expression: " + keyExpression, e);
        }
    }
}