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

open class HideUtilityClassConstructorTest: CheckstyleRefactorVisitorTest(HideUtilityClassConstructor::class) {
    @Test
    fun hideUtilityConstructor() {
        val a = jp.parse("""
            public class A {
                public A() {
                }
                
                public static void utility() {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                private A() {
                }
                
                public static void utility() {
                }
            }
        """)
    }
}
