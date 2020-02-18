package org.gradle.rewrite.checkstyle.check;

import com.netflix.rewrite.tree.Formatting;
import com.netflix.rewrite.tree.Tr;
import com.netflix.rewrite.tree.Tree;
import com.netflix.rewrite.tree.visitor.refactor.AstTransform;
import com.netflix.rewrite.tree.visitor.refactor.RefactorVisitor;

import java.util.List;

import static com.netflix.rewrite.tree.Formatting.EMPTY;

public class GenericWhitespace extends RefactorVisitor {
    @Override
    public String getRuleName() {
        return "GenericWhitespace";
    }

    @Override
    public List<AstTransform> visitTypeParameters(Tr.TypeParameters typeParams) {
        Tree tree = getCursor().getParentOrThrow().getTree();
        return maybeTransform(typeParams,
                !(tree instanceof Tr.MethodDecl) && !typeParams.getFormatting().getPrefix().isEmpty(),
                super::visitTypeParameters,
                tp -> tp.withFormatting(EMPTY)
        );
    }

    @Override
    public List<AstTransform> visitTypeParameter(Tr.TypeParameter typeParam) {
        List<Tr.TypeParameter> params = ((Tr.TypeParameters) getCursor().getParentOrThrow().getTree()).getParams();

        if (params.isEmpty()) {
            return super.visitTypeParameter(typeParam);
        } else if (params.size() == 1) {
            return maybeTransform(typeParam,
                    !typeParam.getFormatting().equals(EMPTY),
                    super::visitTypeParameter,
                    tp -> tp.withFormatting(EMPTY));
        } else if (params.get(0) == typeParam) {
            return maybeTransform(typeParam,
                    !typeParam.getFormatting().getPrefix().isEmpty(),
                    super::visitTypeParameter,
                    Formatting::stripPrefix);
        } else if (params.get(params.size() - 1) == typeParam) {
            return maybeTransform(typeParam,
                    !typeParam.getFormatting().getSuffix().isEmpty(),
                    super::visitTypeParameter,
                    Formatting::stripSuffix);
        }

        return super.visitTypeParameter(typeParam);
    }
}
