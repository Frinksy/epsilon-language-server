package io.github.frinksy.epsilonls;

import org.eclipse.epsilon.common.parse.Region;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class Util {
    private Util() {
    }

    public static Position convertPosition(org.eclipse.epsilon.common.parse.Position position) {
        return new Position(position.getLine() - 1, position.getColumn());
    }

    public static org.eclipse.epsilon.common.parse.Position convertPosition(Position position) {
        return new org.eclipse.epsilon.common.parse.Position(position.getLine() + 1, position.getCharacter());
    }

    public static Range getRangeFromRegion(Region region) {
        return new Range(convertPosition(region.getStart()), convertPosition(region.getEnd()));
    }

    public static boolean regionContainsPosition(Region region, org.eclipse.epsilon.common.parse.Position position) {

        return (region.getStart()
                .isBefore(position)
                && region.getEnd().isAfter(position) || region.getStart().equals(position));
    }

}
