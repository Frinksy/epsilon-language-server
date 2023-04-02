package io.github.frinksy.epsilonls.eol;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;
import org.eclipse.epsilon.eol.types.EolType;

import io.github.frinksy.epsilonls.eol.visitors.VariableDeclarationVisitor;

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

        NameExpression result = visitor.getDeclaration();

        // The visitor just looks for a declaration of a variable with the same name.
        // In the case of a object's property having the same name as a variable in the
        // same scope, the visitor will (wrongly) find the variable instead.
        //
        // Running visitor.getDeclaration() seems to have some side-effects, which need
        // to be investigated. Those side-effects seem to make things work for now.
        if (nameExpression.getParent() instanceof PropertyCallExpression) {

            PropertyCallExpression parentCallExpression = (PropertyCallExpression) nameExpression.getParent();
            if (parentCallExpression.getNameExpression() == nameExpression) {
                result = null;
            }

        }

        if (result == null) {

            EolType type = visitor.getResolvedType();

            if (type == null) {
                return null;
            }

            String variableName = nameExpression.getName();
            String variableType = type.getName();

            return constructVariableHoverText(variableName, variableType);

        }

        ModuleElement parent = result.getParent();

        if (parent instanceof VariableDeclaration) {
            return EolHover.getHoverContents((VariableDeclaration) parent);
        }

        if (parent instanceof Parameter) {
            return EolHover.getHoverContents((Parameter) parent);
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
