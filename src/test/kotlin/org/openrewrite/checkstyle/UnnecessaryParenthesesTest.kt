/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.checkstyle

import org.junit.jupiter.api.Test

open class UnnecessaryParenthesesTest: CheckstyleRefactorVisitorTest(UnnecessaryParentheses()) {
    @Test
    fun simpleUnwrapping() = assertRefactored(
            before = """
                import java.util.*;
                public class A {
                    int square(int a, int b) {
                        int square = (a * b);
    
                        int sumOfSquares = 0;
                        for(int i = (0); i < 10; i++) {
                          sumOfSquares += (square(i * i, i));
                        }
                        double num = (10.0);
    
                        List<String> list = Arrays.asList("a1", "b1", "c1");
                        list.stream()
                          .filter((s) -> s.startsWith("c"))
                          .forEach(System.out::println);
    
                        return (square);
                    }
                }
            """,
            after = """
                import java.util.*;
                public class A {
                    int square(int a, int b) {
                        int square = a * b;
    
                        int sumOfSquares = 0;
                        for(int i = 0; i < 10; i++) {
                          sumOfSquares += square(i * i, i);
                        }
                        double num = 10.0;
    
                        List<String> list = Arrays.asList("a1", "b1", "c1");
                        list.stream()
                          .filter(s -> s.startsWith("c"))
                          .forEach(System.out::println);
    
                        return square;
                    }
                }
            """
    )
}
