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

open class NeedBracesTest: CheckstyleRefactorVisitorTest(NeedBraces()) {
    @Test
    fun addBraces() = assertRefactored(
            before = """
                public class A {
                    int n;
                    void foo() {
                        while (true);
                        if (n == 1) return;
                        else return;
                        while (true) return;
                        do this.notify(); while (true);
                        for (int i = 0; ; ) this.notify();
                    }
                }
            """,
            after = """
                public class A {
                    int n;
                    void foo() {
                        while (true) {
                        }
                        if (n == 1) {
                            return;
                        }
                        else {
                            return;
                        }
                        while (true) {
                            return;
                        }
                        do {
                            this.notify();
                        } while (true);
                        for (int i = 0; ; ) {
                            this.notify();
                        }
                    }
                }
            """
    )

    @Test
    fun allowEmptyLoopBody() {
        setProperties("allowEmptyLoopBody" to true)
        assertUnchanged(
                before = """
                    public class A {
                        {
                            while (true);
                            for(int i = 0; i < 10; i++);
                        }
                    }
                """
        )
    }

    @Test
    fun allowSingleLineStatement() {
        setProperties("allowSingleLineStatement" to true)
        assertUnchanged(
                before = """
                    public class A {
                        int n;
                        void foo() {
                            if (n == 1) return;
                            while (true) return;
                            do this.notify(); while (true);
                            for (int i = 0; ; ) this.notify();
                        }
                    }
                """
        )
    }

    @Test
    fun allowSingleLineStatementInSwitch() {
        setProperties(
                "allowSingleLineStatements" to true,
                "tokens" to "LITERAL_CASE,LITERAL_DEFAULT"
        )
        assertUnchanged(
                before = """
                    public class A {
                        {
                            int n = 1;
                            switch (n) {
                              case 1: counter++; break;
                              case 6: counter += 10; break;
                              default: counter = 100; break;
                            }
                        }
                    }
                """
        )
    }

    @Test
    fun dontSplitElseIf() = assertUnchanged(
            before = """
                public class A {
                    int n;
                    {
                        if (n == 1) {
                        }
                        else if (n == 2) {
                        }
                        else {
                        }
                    }
                }
            """
    )
}
