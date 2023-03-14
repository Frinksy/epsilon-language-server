package io.github.frinksy.epsilonls;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.AbortStatement;
import org.eclipse.epsilon.eol.dom.AndOperatorExpression;
import org.eclipse.epsilon.eol.dom.AnnotatableModuleElement;
import org.eclipse.epsilon.eol.dom.Annotation;
import org.eclipse.epsilon.eol.dom.AnnotationBlock;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.BooleanLiteral;
import org.eclipse.epsilon.eol.dom.BreakStatement;
import org.eclipse.epsilon.eol.dom.Case;
import org.eclipse.epsilon.eol.dom.CollectionLiteralExpression;
import org.eclipse.epsilon.eol.dom.ComplexOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ContinueStatement;
import org.eclipse.epsilon.eol.dom.DeleteStatement;
import org.eclipse.epsilon.eol.dom.DivOperatorExpression;
import org.eclipse.epsilon.eol.dom.DoubleEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ElvisOperatorExpression;
import org.eclipse.epsilon.eol.dom.EnumerationLiteralExpression;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ExecutableAnnotation;
import org.eclipse.epsilon.eol.dom.ExecutableBlock;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionInBrackets;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.GreaterEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.GreaterThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.IEolVisitor;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.ImpliesOperatorExpression;
import org.eclipse.epsilon.eol.dom.Import;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.ItemSelectorExpression;
import org.eclipse.epsilon.eol.dom.LessEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.LessThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.MapLiteralExpression;
import org.eclipse.epsilon.eol.dom.MinusOperatorExpression;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dom.ModelDeclarationParameter;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.NegativeOperatorExpression;
import org.eclipse.epsilon.eol.dom.NewInstanceExpression;
import org.eclipse.epsilon.eol.dom.NotEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.NotOperatorExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.PlusOperatorExpression;
import org.eclipse.epsilon.eol.dom.PostfixOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.RealLiteral;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.dom.SimpleAnnotation;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.dom.SwitchStatement;
import org.eclipse.epsilon.eol.dom.TernaryExpression;
import org.eclipse.epsilon.eol.dom.ThrowStatement;
import org.eclipse.epsilon.eol.dom.TimesOperatorExpression;
import org.eclipse.epsilon.eol.dom.TransactionStatement;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;
import org.eclipse.epsilon.eol.dom.WhileStatement;
import org.eclipse.epsilon.eol.dom.XorOperatorExpression;

public class VariableDeclarationVisitor implements IEolVisitor {

    String variableName;
    NameExpression rootNameExpression;

    ModuleElement originModuleElement = null;

    int currentDepth = 0;

    public VariableDeclarationVisitor(NameExpression nameExpression) {
        variableName = nameExpression.getName();
        rootNameExpression = nameExpression;
    }

    public ModuleElement getDeclaration() {
        rootNameExpression.accept(this);
        ModuleElement parent = rootNameExpression.getParent();
        while (parent != null && currentDepth < 100 && originModuleElement == null) {
            reAccept(parent);

            parent = parent.getParent();

            currentDepth += 1;
        }

        return originModuleElement;

    }

    private void reAccept(ModuleElement moduleElement) {

        if (moduleElement instanceof Expression) {

            ((Expression) moduleElement).accept(this);
        }

        if (moduleElement instanceof Statement) {
            ((Statement) moduleElement).accept(this);
        }

        if (moduleElement instanceof AnnotatableModuleElement) {
            ((AnnotatableModuleElement) moduleElement).accept(this);
        }

        if (moduleElement instanceof Annotation) {
            ((Annotation) moduleElement).accept(this);
        }

        if (moduleElement instanceof Parameter) {
            ((Parameter) moduleElement).accept(this);
        }

    }

    @Override
    public void visit(NameExpression nameExpression) {
        // Do nothing
    }

    @Override
    public void visit(AbortStatement abortStatement) {
        // Do nothing
    }

    @Override
    public void visit(AndOperatorExpression andOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(DeleteStatement deleteStatement) {
        // Do nothing
    }

    @Override
    public void visit(AnnotationBlock annotationBlock) {
        // Do nothing
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        // Do nothing
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        // Do nothing
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        // Do nothing
    }

    @Override
    public void visit(Case caseElement) {
        // Do nothing
    }

    @Override
    public void visit(CollectionLiteralExpression<?> collectionLiteralExpression) {
        // Do nothing
    }

    @Override
    public void visit(ComplexOperationCallExpression complexOperationCallExpression) {
        // Do nothing
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        // Do nothing
    }

    @Override
    public void visit(DivOperatorExpression divOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(DoubleEqualsOperatorExpression doubleEqualsOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(ElvisOperatorExpression elvisOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(EnumerationLiteralExpression enumerationLiteralExpression) {
        // Do nothing
    }

    @Override
    public void visit(EqualsOperatorExpression equalsOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(ExecutableAnnotation executableAnnotation) {
        // Do nothing
    }

    @Override
    public void visit(ExecutableBlock<?> executableBlock) {
        // Do nothing
    }

    @Override
    public void visit(ExpressionInBrackets expressionInBrackets) {
        // Do nothing
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        // Do nothing
    }

    @Override
    public void visit(FirstOrderOperationCallExpression firstOrderOperationCallExpression) {
        // Do nothing
    }

    @Override
    public void visit(ForStatement forStatement) {
        Parameter parameter = forStatement.getIteratorParameter();

        if (parameter.getName().equals(variableName)) {
            originModuleElement = parameter;
        }
        // Do nothing
    }

    @Override
    public void visit(GreaterEqualOperatorExpression greaterEqualOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(GreaterThanOperatorExpression greaterThanOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(IfStatement ifStatement) {
        // Do nothing
    }

    @Override
    public void visit(ImpliesOperatorExpression impliesOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(Import importElement) {
        // Do nothing
    }

    @Override
    public void visit(IntegerLiteral integerLiteral) {
        // Do nothing
    }

    @Override
    public void visit(ItemSelectorExpression itemSelectorExpression) {
        // Do nothing
    }

    @Override
    public void visit(LessEqualOperatorExpression lessEqualOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(LessThanOperatorExpression lessThanOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(MapLiteralExpression<?, ?> mapLiteralExpression) {
        // Do nothing
    }

    @Override
    public void visit(MinusOperatorExpression minusOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(ModelDeclaration modelDeclaration) {
        // Do nothing
    }

    @Override
    public void visit(ModelDeclarationParameter modelDeclarationParameter) {
        // Do nothing
    }

    @Override
    public void visit(NegativeOperatorExpression negativeOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(NewInstanceExpression newInstanceExpression) {
        // Do nothing
    }

    @Override
    public void visit(NotEqualsOperatorExpression notEqualsOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(NotOperatorExpression notOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(Operation operation) {

        for (Parameter parameter : operation.getFormalParameters()) {

            parameter.accept(this);

        }

    }

    @Override
    public void visit(OperationCallExpression operationCallExpression) {
        // Do nothing
    }

    @Override
    public void visit(OrOperatorExpression orOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(Parameter parameter) {

        if (parameter.getName().equals(variableName)) {
            originModuleElement = parameter;
        }

    }

    @Override
    public void visit(PlusOperatorExpression plusOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(PostfixOperatorExpression postfixOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(PropertyCallExpression propertyCallExpression) {
        // Do nothing
    }

    @Override
    public void visit(RealLiteral realLiteral) {
        // Do nothing
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        // Do nothing
    }

    @Override
    public void visit(SimpleAnnotation simpleAnnotation) {
        // Do nothing
    }

    @Override
    public void visit(StatementBlock statementBlock) {
        for (Statement statement : statementBlock.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        // Do nothing
    }

    @Override
    public void visit(SwitchStatement switchStatement) {
        // Do nothing
    }

    @Override
    public void visit(TernaryExpression ternaryExpression) {
        // Do nothing
    }

    @Override
    public void visit(ThrowStatement throwStatement) {
        throwStatement.getThrown().accept(this);
    }

    @Override
    public void visit(TimesOperatorExpression timesOperatorExpression) {
        // Do nothing
    }

    @Override
    public void visit(TransactionStatement transactionStatement) {
        transactionStatement.getBody().accept(this);
    }

    @Override
    public void visit(TypeExpression typeExpression) {
        // Do nothing
    }

    @Override
    public void visit(VariableDeclaration variableDeclaration) {

        originModuleElement = variableDeclaration;
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.getBodyStatementBlock().accept(this);
    }

    @Override
    public void visit(XorOperatorExpression xorOperatorExpression) {
        // Do nothing
    }

}