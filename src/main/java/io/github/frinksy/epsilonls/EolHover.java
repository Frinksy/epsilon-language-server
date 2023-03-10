package io.github.frinksy.epsilonls;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.TypeExpression;

public class EolHover {

    /**
     * Get the Hover tooltip for an EOL operation.
     * 
     * @param operation
     * @return
     */
    public static String getOperationHover(Operation operation) {
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

}
