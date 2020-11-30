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

open class EmptyBlockTest : CheckstyleRefactorVisitorTest(EmptyBlock()) {
    private fun emptyBlock(vararg tokens: String) = setProperties("tokens" to tokens.joinToString(","))

    @Test
    fun emptySwitch() {
        emptyBlock("LITERAL_SWITCH")
        assertRefactored(
                before = """
                    public class A {
                        {
                            int i = 0;
                            switch(i) {
                            }
                        }
                    }
                """,
                after = """
                    public class A {
                        {
                            int i = 0;
                        }
                    }
                """
        )
    }

    @Test
    fun emptySynchronized() {
        emptyBlock("LITERAL_SYNCHRONIZED")
        assertRefactored(
                before = """
                    public class A {
                        {
                            final Object o = new Object();
                            synchronized(o) {
                            }
                        }
                    }
                """,
                after = """
                    public class A {
                        {
                            final Object o = new Object();
                        }
                    }
                """
        )
    }

    @Test
    fun emptyTry() {
        emptyBlock("LITERAL_TRY")
        assertRefactored(
                before = """
                    import java.io.*;
        
                    public class A {
                        {
                            try(FileInputStream fis = new FileInputStream("")) {
                                
                            } catch (IOException e) {
                            }
                        }
                    }
                """,
                after = """
                    public class A {
                        {
                        }
                    }
                """
        )
    }

    @Test
    fun emptyCatchBlockWithIOException() {
        emptyBlock("LITERAL_CATCH")
        assertRefactored(
                before = """
                import java.io.IOException;
                import java.nio.file.*;
                
                public class A {
                    public void foo() {
                        try {
                            Files.readString(Path.of("somewhere"));
                        } catch (IOException e) {
                        }
                    }
                }
            """,
                after = """
                import java.io.IOException;
                import java.io.UncheckedIOException;
                import java.nio.file.*;
                
                public class A {
                    public void foo() {
                        try {
                            Files.readString(Path.of("somewhere"));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            """
        )
    }

    @Test
    fun emptyCatchBlockWithExceptionAndEmptyFinally() {
        emptyBlock("LITERAL_CATCH", "LITERAL_FINALLY")
        assertRefactored(
                before = """
                    import java.nio.file.*;
                    
                    public class A {
                        public void foo() {
                            try {
                                Files.readString(Path.of("somewhere"));
                            } catch (Throwable t) {
                            } finally {
                            }
                        }
                    }
                """,
                after = """
                    import java.nio.file.*;
                    
                    public class A {
                        public void foo() {
                            try {
                                Files.readString(Path.of("somewhere"));
                            } catch (Throwable t) {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                """
        )
    }

    @Test
    fun emptyLoops() {
        emptyBlock("LITERAL_WHILE", "LITERAL_DO")
        assertRefactored(
                before = """
                public class A {
                    public void foo() {
                        while(true) {
                        }
                        do {
                        } while(true);
                    }
                }
            """,
                after = """
                public class A {
                    public void foo() {
                        while(true) {
                            continue;
                        }
                        do {
                            continue;
                        } while(true);
                    }
                }
            """
        )
    }

    @Test
    fun emptyStaticInit() {
        emptyBlock("STATIC_INIT")
        assertRefactored(
                before = """
                    public class A {
                        static {}
                        {}
                    }
                """,
                after = """
                    public class A {
                        {}
                    }
                """
        )
    }

    @Test
    fun emptyInstanceInit() {
        emptyBlock("INSTANCE_INIT")
        assertRefactored(
                before = """
                    public class A {
                        static {}
                        {}
                    }
                """,
                after = """
                    public class A {
                        static {}
                    }
                """
        )
    }

    @Test
    fun extractSideEffectsFromEmptyIfsWithNoElse() {
        emptyBlock("LITERAL_IF")
        assertRefactored(
                before = """
                    public class A {
                        int n = sideEffect();
                    
                        int sideEffect() {
                            return new java.util.Random().nextInt();
                        }
                    
                        boolean boolSideEffect() {
                            return sideEffect() == 0;
                        }
                    
                        public void lotsOfIfs() {
                            if(sideEffect() == 1) {}
                            if(sideEffect() == sideEffect()) {}
                            int n;
                            if((n = sideEffect()) == 1) {}
                            if((n /= sideEffect()) == 1) {}
                            if(new A().n == 1) {}
                            if(!boolSideEffect()) {}
                            if(1 == 2) {}
                        }
                    }
                """,
                after = """
                    public class A {
                        int n = sideEffect();
                    
                        int sideEffect() {
                            return new java.util.Random().nextInt();
                        }
                    
                        boolean boolSideEffect() {
                            return sideEffect() == 0;
                        }
                    
                        public void lotsOfIfs() {
                            sideEffect();
                            sideEffect();
                            sideEffect();
                            int n;
                            n = sideEffect();
                            n /= sideEffect();
                            new A();
                            boolSideEffect();
                        }
                    }
                """
        )
    }

    @Test
    fun invertIfWithOnlyElseClauseAndBinaryOperator() {
        emptyBlock("LITERAL_IF")

        assertRefactored(
                // extra spaces after the original if condition to ensure that we preserve the if statement's block formatting
                before = """
                    public class A {
                        {
                            if("foo".length() > 3)   {
                            } else {
                                System.out.println("this");
                            }
                        }
                    }
                """,
                after = """
                    public class A {
                        {
                            if("foo".length() <= 3)   {
                                System.out.println("this");
                            }
                        }
                    }
                """)
    }

    @Test
    fun invertIfWithElseIfElseClause() {
        emptyBlock("LITERAL_IF")
        assertRefactored(
                before = """
                    public class A {
                        {
                            if("foo".length() > 3) {
                            } else if("foo".length() > 4) {
                                System.out.println("longer");
                            }
                            else {
                                System.out.println("this");
                            }
                        }
                    }
                """,
                after = """
                    public class A {
                        {
                            if("foo".length() <= 3) {
                                if("foo".length() > 4) {
                                    System.out.println("longer");
                                }
                                else {
                                    System.out.println("this");
                                }
                            }
                        }
                    }
                """
        )
    }
}
