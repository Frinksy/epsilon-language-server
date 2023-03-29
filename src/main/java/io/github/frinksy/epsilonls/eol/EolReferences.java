package io.github.frinksy.epsilonls.eol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.lsp4j.Location;

import io.github.frinksy.epsilonls.Util;

public class EolReferences {

    private EolReferences() {
    }

    public static List<Location> getReferences(ModuleElement resolvedModule) {

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

        List<Location> locations = new ArrayList<>();

        for (NameExpression expr : expressions) {
            if (resolvedDeclaration.equals(EolDeclaration.getDeclaration(expr, analyser))) {
                locations.add(getLocation(expr));
            }
        }

        return locations;
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
