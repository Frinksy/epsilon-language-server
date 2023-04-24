package io.github.frinksy.epsilonls.eol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.lsp4j.Location;

import io.github.frinksy.epsilonls.EolDocument;
import io.github.frinksy.epsilonls.EpsilonDocument;
import io.github.frinksy.epsilonls.Util;

public class EolReferences {

    private EolReferences() {
    }

    public static List<Location> getReferences(ModuleElement resolvedModule) {
        return getReferences(resolvedModule, null);
    }

    public static List<Location> getReferences(ModuleElement resolvedModule,
            Collection<EpsilonDocument> otherDocuments) {

        if (!(resolvedModule instanceof NameExpression)) {
            return Collections.emptyList();
        }

        EolStaticAnalyser analyser = new EolStaticAnalyser();
        analyser.validate(resolvedModule.getModule());

        NameExpression nameExpression = (NameExpression) resolvedModule;

        ModuleElement resolvedDeclaration = EolDeclaration.getDeclaration(nameExpression, analyser);

        if (resolvedDeclaration == null) {
            return Collections.emptyList();
        }

        List<NameExpression> expressions = getNameOccurences(nameExpression.getName(), resolvedModule.getModule());

        List<Location> locations = expressions.stream()
                .filter(expr -> resolvedDeclaration.equals(EolDeclaration.getDeclaration(expr, analyser)))
                .map(EolReferences::getLocation)
                .collect(Collectors.toList());

        if (otherDocuments == null) {
            return locations;
        }

        if (!(resolvedModule.getParent() instanceof Operation
                || resolvedModule.getParent() instanceof OperationCallExpression)) {
            return locations;
        }

        String rootFileUri = resolvedModule.getFile().toPath().toUri().toString();

        otherDocuments.stream().filter(
                EolDocument.class::isInstance)
                .filter(doc -> !doc.getFilename().equals(rootFileUri))
                .map(EolDocument.class::cast)
                .forEach(
                        document -> locations.addAll(
                                getReferencesInDocument(document, resolvedDeclaration, nameExpression.getName())));

        return locations;
    }

    public static List<Location> getReferencesInDocument(EolDocument doc, ModuleElement declaration,
            String variableName) {

        EolStaticAnalyser tempAnalyser = new EolStaticAnalyser();
        tempAnalyser.validate(doc.getEolModule());

        return getNameOccurences(variableName, doc.getEolModule())
                .stream()
                .filter(expr -> declaration.equals(EolDeclaration.getDeclaration(expr, tempAnalyser)))
                .map(EolReferences::getLocation)
                .collect(Collectors.toList());

    }

    public static Location getLocation(ModuleElement moduleElement) {

        if (moduleElement == null) {
            return null;
        }

        Location location = new Location();
        location.setUri(moduleElement.getFile().toPath().toUri().toString());
        location.setRange(Util.getRangeFromRegion(moduleElement.getRegion()));

        return location;
    }

    public static List<NameExpression> getNameOccurences(String name, ModuleElement root) {

        List<NameExpression> result = new ArrayList<>();

        if (root instanceof NameExpression) {
            NameExpression nameExpression = (NameExpression) root;
            if (nameExpression.getName().equals(name)) {
                result.add(nameExpression);
            }
            return result;
        }

        for (ModuleElement child : root.getChildren()) {
            result.addAll(getNameOccurences(name, child));
        }

        return result;

    }

}
