package io.github.frinksy.epsilonls;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;

public class EolHover {

    /**
     * Get the Hover tooltip for an EOL operation.
     * 
     * @param operation
     * @return
     */
    public static String getHoverContents(Operation operation) {
        TypeExpression returnType = operation.getReturnTypeExpression();

        String returnTypeName = "Any";
        if (returnType != null) {
            returnTypeName = returnType.getName();
        }

        String signature = "(";

        List<Parameter> parameters = operation.getFormalParameters();
        if (parameters != null) {
            List<String> formattedParameters = parameters.stream()
                    .map(parameter -> parameter.getName() + ": " + parameter.getTypeName())
                    .collect(Collectors.toList());

            signature += String.join(", ", formattedParameters);
        }

        signature += ")";

        return operation.getName() + signature + " -> " + returnTypeName;

    }

    public static String getNameExpressionHover(NameExpression nameExpression) {

        VariableDeclarationVisitor visitor = new VariableDeclarationVisitor(nameExpression);

        ModuleElement result = visitor.getDeclaration();

        if (result instanceof VariableDeclaration) {
            return EolHover.getHoverContents((VariableDeclaration) result);
        }

        if (result instanceof Parameter) {
            return EolHover.getHoverContents((Parameter) result);
        }

        return null;

    }

    public static String getHoverContents(VariableDeclaration declaration) {

        String name = declaration.getName();
        String type = "Any";

        if (declaration.getTypeExpression() != null) {
            TypeExpression tExpression = declaration.getTypeExpression();
            type = tExpression.getName();
        }

        return EolHover.constructVariableHoverText(name, type);
    }

    public static String getHoverContents(Parameter parameter) {
        return EolHover.constructVariableHoverText(parameter.getName(), parameter.getTypeName());

    }

    public static String constructVariableHoverText(String variableName, String variableType) {

        return variableName + " : " + variableType;

    }

}
