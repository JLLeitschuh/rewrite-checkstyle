package org.gradle.rewrite.checkstyle.check;

import com.netflix.rewrite.tree.Formatting;
import com.netflix.rewrite.tree.Tr;
import com.netflix.rewrite.tree.Tree;
import com.netflix.rewrite.tree.visitor.refactor.AstTransform;
import com.netflix.rewrite.tree.visitor.refactor.RefactorVisitor;
import com.netflix.rewrite.tree.visitor.search.FindReferencesToVariable;

import java.util.ArrayList;
import java.util.List;

import static com.netflix.rewrite.tree.Formatting.format;
import static com.netflix.rewrite.tree.Tr.randomId;

public class FinalLocalVariable extends RefactorVisitor {
    @Override
    public String getRuleName() {
        return "FinalLocalVariable";
    }

    @Override
    public List<AstTransform> visitMultiVariable(Tr.VariableDecls multiVariable) {
        Tree variableScope = getCursor().getParentOrThrow().getTree();

        if (variableScope instanceof Tr.ClassDecl) {
            // we don't care about fields here
            super.visitMultiVariable(multiVariable);
        }

        return maybeTransform(!multiVariable.hasModifier("final") && multiVariable.getVars().stream()
                        .anyMatch(variable -> new FindReferencesToVariable(variable.getName()).visit(variableScope).size() +
                                (variable.getInitializer() == null ? -1 : 0) <= 0),
                super.visitMultiVariable(multiVariable),
                transform(multiVariable, mv -> {
                    List<Tr.Modifier> modifiers = new ArrayList<>();
                    modifiers.add(new Tr.Modifier.Final(randomId(), mv.getTypeExpr() == null ? Formatting.EMPTY :
                            format(mv.getTypeExpr().getFormatting().getPrefix())));

                    List<Tr.Modifier> mvModifiers = mv.getModifiers();
                    for (int i = 0; i < mvModifiers.size(); i++) {
                        Tr.Modifier modifier = mvModifiers.get(i);
                        modifiers.add(i == 0 ? modifier.withFormatting(modifier.getFormatting().withPrefix(" "))
                                : modifier);
                    }

                    return mv.withModifiers(modifiers).withTypeExpr(mv.getTypeExpr().withFormatting(mv.getTypeExpr()
                            .getFormatting().withPrefix(" ")));
                })
        );
    }
}