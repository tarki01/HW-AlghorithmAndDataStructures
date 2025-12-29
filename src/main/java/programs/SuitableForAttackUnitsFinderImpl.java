package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> rowsOfUnits, boolean targetingLeftArmy) {
        List<Unit> appropriateUnits = new ArrayList<>();

        for (List<Unit> currentRow : rowsOfUnits) {
            Set<Integer> occupiedVerticalPositions = new HashSet<>();

            for (Unit currentUnit : currentRow) {
                occupiedVerticalPositions.add(currentUnit.getyCoordinate());
            }

            for (Unit currentUnit : currentRow) {
                if (targetingLeftArmy) {
                    if (!occupiedVerticalPositions.contains(currentUnit.getyCoordinate() - 1)) {
                        appropriateUnits.add(currentUnit);
                    }
                } else {
                    if (!occupiedVerticalPositions.contains(currentUnit.getyCoordinate() + 1)) {
                        appropriateUnits.add(currentUnit);
                    }
                }
            }
        }

        return appropriateUnits;
    }
}