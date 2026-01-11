package org.example.be.resolver;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecodedPathVariable {
    String value() default "";  // 기존 @PathVariable처럼 사용할 수 있게 value지원. "" 비워두면 파라미터 이름으로 매핑된다.
}
