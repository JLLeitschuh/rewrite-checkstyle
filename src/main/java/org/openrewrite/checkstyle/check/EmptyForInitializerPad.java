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
package org.openrewrite.checkstyle.check;

import org.eclipse.microprofile.config.Config;
import org.openrewrite.config.AutoConfigure;
import org.openrewrite.checkstyle.policy.PadPolicy;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class EmptyForInitializerPad extends CheckstyleRefactorVisitor {
    private final PadPolicy option;

    public EmptyForInitializerPad(PadPolicy option) {
        super("checkstyle.EmptyForInitializerPad");
        this.option = option;
    }

    @AutoConfigure
    public static EmptyForInitializerPad configure(Config config) {
        return fromModule(
                config,
                "EmptyForInitializerPad",
                m -> new EmptyForInitializerPad(m.propAsOptionValue(PadPolicy::valueOf, PadPolicy.NOSPACE))
        );
    }

    @Override
    public J visitForLoop(J.ForLoop forLoop) {
        J.ForLoop f = refactor(forLoop, super::visitForLoop);
        String prefix = forLoop.getControl().getInit().getFormatting().getPrefix();

        if (!prefix.startsWith("\n") &&
                (option == PadPolicy.NOSPACE ? prefix.startsWith(" ") || prefix.startsWith("\t") : prefix.isEmpty()) &&
                forLoop.getControl().getInit() instanceof J.Empty) {
            Statement init = f.getControl().getInit();
            String fixedPrefix = option == PadPolicy.NOSPACE ? "" : " ";
            f = f.withControl(f.getControl().withInit(init.withPrefix(fixedPrefix)));
        }

        return f;
    }
}
