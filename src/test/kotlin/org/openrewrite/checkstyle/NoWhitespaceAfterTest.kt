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

open class NoWhitespaceAfterTest: CheckstyleRefactorVisitorTest(NoWhitespaceAfter::class) {
    @Test
    fun noWhitespaceAfter() {
        val notNull = """
            import java.lang.annotation.*;

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.PARAMETER)
            public @interface NotNull {
              String value() default "";
            }
        """.trimIndent()

        val a = jp.parse("""
            public class A {
                int m;
            
                {
                    int [] [] a;
                    int [] n = { 1, 2};
                    int [] p = {1, 2 };
                    m = n [0];
                    ++ m;
                    -- m;
                    long o = - m;
                    o = + m;
                    o = ~ m;
                    boolean b;
                    b = ! b;
                    m = (int) o;
                    new A().
                        m = 2;
                    a().
                        a();
                    var a = Function:: identity;
                }
                
                @ Override
                public boolean equals(Object o) {}
                
                int [] [] foo() { return null; }
                
                A a() { return this; }
            }
        """.trimIndent(), notNull)

        val fixed = a.refactor().visit(configXml("tokens" to "ARRAY_INIT,AT,INC,DEC,UNARY_MINUS,UNARY_PLUS,BNOT,LNOT,DOT,TYPECAST,ARRAY_DECLARATOR,INDEX_OP,LITERAL_SYNCHRONIZED,METHOD_REF"))
                .fix().fixed

        assertRefactored(fixed, """
            public class A {
                int m;
            
                {
                    int[][] a;
                    int[] n = {1, 2};
                    int[] p = {1, 2};
                    m = n[0];
                    ++m;
                    --m;
                    long o = -m;
                    o = +m;
                    o = ~m;
                    boolean b;
                    b = !b;
                    m = (int)o;
                    new A().
                        m = 2;
                    a().
                        a();
                    var a = Function::identity;
                }
                
                @Override
                public boolean equals(Object o) {}
                
                int[][] foo() { return null; }
                
                A a() { return this; }
            }
        """)
    }

    @Test
    fun dontAllowLinebreaks() {
        val a = jp.parse("""
            public class A {
                int m;
            
                {
                    new A().
                        m = 2;
                    a().
                        a();
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml("tokens" to "DOT",
                "allowLineBreaks" to false)).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int m;
            
                {
                    new A().m = 2;
                    a().a();
                }
            }
        """)
    }

    /**
     * Evidently checkstyle doesn't recognize these as new arrays.
     */
    @Test
    fun dontChangeAnnotationValueNewArrays() {
        jp.assertUnchangedByRefactoring(configXml(), """
            @SuppressWarnings(value = {
                "all",
                "unchecked"
            })
            public class A {
            }
        """)
    }

    @Test
    fun dontChangeFirstAndLastValuesOfArrayInitializer() {
        jp.assertUnchangedByRefactoring(configXml(), """
            public class A {
                int[] ns = {
                    0,
                    1
                };
            }
        """)
    }
}
