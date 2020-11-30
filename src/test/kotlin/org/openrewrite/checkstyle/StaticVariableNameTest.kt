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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

open class StaticVariableNameTest: CheckstyleRefactorVisitorTest(StaticVariableName()) {
    @Test
    fun snakeName() {
        assertThat(StaticVariableName.snakeCaseToCamel("CAMEL_CASE_NAME_1"))
                .isEqualTo("camelCaseName1")
    }

    @Test
    fun dontChangeEveryField() = assertUnchanged(
            before = """
                import java.util.List;
                public class A {
                   List MY_LIST;
                }
            """
    )

    @Test
    fun changeSingleVariableFieldAndReference() = assertRefactored(
            before = """
                import java.util.*;
                public class A {
                   static List<String> MY_LIST;
                   
                   static {
                       MY_LIST = new ArrayList<>();
                   }
                }
            """,
            after = """
                import java.util.*;
                public class A {
                   static List<String> myList;
                   
                   static {
                       myList = new ArrayList<>();
                   }
                }
            """
    )

    @Test
    fun changeOnlyMatchingVisibility() {
        setProperties(
                "applyToPublic" to false,
                "applyToPackage" to false,
                "applyToPrivate" to false
        )
        assertRefactored(
                before = """
                    import java.util.List;
                    public class A {
                       static List MY_LIST;
                       private static List MY_PRIVATE_LIST;
                       public static List MY_PUBLIC_LIST;
                       protected static List MY_PROTECTED_LIST;
                    }
                """,
                after = """
                    import java.util.List;
                    public class A {
                       static List MY_LIST;
                       private static List MY_PRIVATE_LIST;
                       public static List MY_PUBLIC_LIST;
                       protected static List myProtectedList;
                    }
                """
        )
    }
}
