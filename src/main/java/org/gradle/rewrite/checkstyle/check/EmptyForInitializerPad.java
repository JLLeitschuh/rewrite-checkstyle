package org.gradle.rewrite.checkstyle.check;

import com.netflix.rewrite.tree.Statement;
import com.netflix.rewrite.tree.Tr;
import com.netflix.rewrite.tree.visitor.refactor.AstTransform;
import com.netflix.rewrite.tree.visitor.refactor.RefactorVisitor;
import lombok.RequiredArgsConstructor;
import org.gradle.rewrite.checkstyle.policy.PadPolicy;

import java.util.List;

@RequiredArgsConstructor
public class EmptyForInitializerPad extends RefactorVisitor {
    private final PadPolicy option;

    public EmptyForInitializerPad() {
        this(PadPolicy.NOSPACE);
    }

    @Override
    public String getRuleName() {
        return "EmptyForInitializerPad";
    }

    @Override
    public List<AstTransform> visitForLoop(Tr.ForLoop forLoop) {
        String prefix = forLoop.getControl().getInit().getFormatting().getPrefix();
        return maybeTransform(!prefix.startsWith("\n") && option == PadPolicy.NOSPACE ?
                        prefix.startsWith(" ") || prefix.startsWith("\t") :
                        prefix.isEmpty(),
                super.visitForLoop(forLoop),
                transform(forLoop, f -> {
                    Statement init = f.getControl().getInit();
                    String fixedPrefix = option == PadPolicy.NOSPACE ? "" : " ";
                    return f.withControl(f.getControl().withInit(init.withPrefix(fixedPrefix)));
                })
        );
    }
}