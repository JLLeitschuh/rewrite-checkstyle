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
package org.openrewrite.checkstyle.check

import org.junit.jupiter.api.Test
import org.openrewrite.config.MapConfigSource.mapConfig
import org.openrewrite.java.JavaParser

open class HiddenFieldTest : JavaParser() {
    private val defaultConfig = emptyModule("HiddenField")
    
    @Test
    fun renameHiddenFields() {
        val b = """
            public class B {
                protected int n2;
                int n3;
                private int n4;
            }
        """.trimIndent()

        val a = parse("""
            public class A extends B {
                int n;
                int n1;

                class C {
                    public void foo(int n) {
                        int n1 = 2;
                    }
                }
                
                static class D {
                    public void foo(int n) {
                    }
                }
            }
        """.trimIndent(), b)

        val fixed = a.refactor().visit(HiddenField.configure(defaultConfig)).fix().fixed

        assertRefactored(fixed, """
            public class A extends B {
                int n;
                int n1;

                class C {
                    public void foo(int n4) {
                        int n5 = 2;
                    }
                }
                
                static class D {
                    public void foo(int n) {
                    }
                }
            }
        """)
    }

    @Test
    fun ignorePattern() {
        val a = parse("""
            public class A {
                int n;
                
                public void foo(int n) {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(HiddenField.configure(mapConfig("checkstyle.config", """
                    <?xml version="1.0"?>
                    <!DOCTYPE module PUBLIC
                        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                        "https://checkstyle.org/dtds/configuration_1_3.dtd">
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="HiddenField">
                                <property name="ignoreFormat" value="\w+"/>
                            </module>
                        </module>
                    </module>
                """.trimIndent()))).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int n;
                
                public void foo(int n) {
                }
            }
        """)
    }

    @Test
    fun ignoreConstructorParameter() {
        val a = parse("""
            public class A {
                int n;
                
                A(int n) {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(HiddenField.configure(mapConfig("checkstyle.config", """
                    <?xml version="1.0"?>
                    <!DOCTYPE module PUBLIC
                        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                        "https://checkstyle.org/dtds/configuration_1_3.dtd">
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="HiddenField">
                                <property name="ignoreConstructorParameter" value="true"/>
                            </module>
                        </module>
                    </module>
                """.trimIndent()))).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int n;
                
                A(int n) {
                }
            }
        """)
    }

    @Test
    fun ignoreSetter() {
        val a = parse("""
            public class A {
                int n;
                
                public void setN(int n) {
                }
                
                public A setN(int n) {
                    return this;
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(HiddenField.configure(mapConfig("checkstyle.config", """
                    <?xml version="1.0"?>
                    <!DOCTYPE module PUBLIC
                        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                        "https://checkstyle.org/dtds/configuration_1_3.dtd">
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="HiddenField">
                                <property name="ignoreSetter" value="true"/>
                            </module>
                        </module>
                    </module>
                """.trimIndent()))).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int n;
                
                public void setN(int n) {
                }
                
                public A setN(int n1) {
                    return this;
                }
            }
        """)
    }

    @Test
    fun ignoreSetterThatReturnsItsClass() {
        val a = parse("""
            public class A {
                int n;
                
                public A setN(int n) {
                    return this;
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(HiddenField.configure(mapConfig("checkstyle.config", """
                    <?xml version="1.0"?>
                    <!DOCTYPE module PUBLIC
                        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                        "https://checkstyle.org/dtds/configuration_1_3.dtd">
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="HiddenField">
                                <property name="ignoreSetter" value="true"/>
                                <property name="setterCanReturnItsClass" value="true"/>
                            </module>
                        </module>
                    </module>
                """.trimIndent()))).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int n;
                
                public A setN(int n) {
                    return this;
                }
            }
        """)
    }

    @Test
    fun ignoreAbstractMethods() {
        val a = parse("""
            public abstract class A {
                int n;
                
                public abstract void foo(int n);
            }
        """.trimIndent())

        val fixed = a.refactor().visit(HiddenField.configure(mapConfig("checkstyle.config", """
                    <?xml version="1.0"?>
                    <!DOCTYPE module PUBLIC
                        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                        "https://checkstyle.org/dtds/configuration_1_3.dtd">
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="HiddenField">
                                <property name="ignoreAbstractMethods" value="true"/>
                            </module>
                        </module>
                    </module>
                """.trimIndent()))).fix().fixed

        assertRefactored(fixed, """
            public abstract class A {
                int n;
                
                public abstract void foo(int n);
            }
        """)
    }
}
