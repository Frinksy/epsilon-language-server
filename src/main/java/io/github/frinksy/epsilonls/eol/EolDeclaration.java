package io.github.frinksy.epsilonls.eol;

import java.io.File;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import io.github.frinksy.epsilonls.EolDocument;
import io.github.frinksy.epsilonls.eol.visitors.VariableDeclarationVisitor;

public class EolDeclaration {

    private EolDeclaration() {}

    public static ModuleElement getDeclaration(NameExpression nameExpression, EolStaticAnalyser analyser) {

        if (nameExpression.getParent() instanceof OperationCallExpression) {

            OperationCallExpression operationCall = (OperationCallExpression) nameExpression.getParent();

            Operation operation = analyser.getExactMatchedOperation(operationCall);

            if (operation == null) {
                return null;
            }
            if (operation.getName().equals(nameExpression.getName())) {
                // Only return the operation declaration if it matches, otherwise we could
                // be on the instance on which the operation is being invoked.
                return operation.getNameExpression();
            }

        }

        if (nameExpression.getParent() instanceof Operation) {
            Operation operation = (Operation) nameExpression.getParent();

            return operation.getNameExpression();
        }

        return new VariableDeclarationVisitor(nameExpression).getDeclaration();

    }

    public static Location getDeclaration(Position position, EolModule eolModule, EolStaticAnalyser staticAnalyser) {

        ModuleElement resolvedModule = EolDocument.getModuleElementAtPosition(eolModule, position);

        if (!(resolvedModule instanceof NameExpression)) {
            return null;
        }

        ModuleElement declarationModuleElement = EolDeclaration.getDeclaration((NameExpression) resolvedModule,
                staticAnalyser);

        if (declarationModuleElement == null) {
            return null;
        }

        File declarationFile = declarationModuleElement.getFile();
        if (declarationFile == null) {
            return null;
        }
        String uri = null;
        uri = declarationFile.toPath().toUri().toString();

        Range region = EolDocument.getRangeFromRegion(declarationModuleElement.getRegion());

        return new Location(
                uri,
                region);

    }

}
