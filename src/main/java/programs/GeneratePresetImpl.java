package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {
    private final static int WIDTH = 3;
    private final static int HEIGHT = 21;
    private Random randomGenerator = new Random();

    @Override
    public Army generate(List<Unit> units, int pointsLimit) {

        Collections.sort(units, new Comparator<Unit>() {
            @Override
            public int compare(Unit first, Unit second) {
                double firstAttackValue = (double) first.getBaseAttack() / first.getCost();
                double secondAttackValue = (double) second.getBaseAttack() / second.getCost();
                if (firstAttackValue != secondAttackValue) {
                    return Double.compare(secondAttackValue, firstAttackValue);
                }
                double firstHealthValue = (double) first.getHealth() / first.getCost();
                double secondHealthValue = (double) second.getHealth() / second.getCost();
                return Double.compare(secondHealthValue, firstHealthValue);
            }
        });

        Army newArmy = new Army();
        int availablePoints = pointsLimit;
        int counter = 0;
        Set<Position> usedPositions = new HashSet<>();
        List<Unit> armyUnits = new ArrayList<>();

        for (Unit unitTemplate : units) {

            int maxCount = 11;
            int affordableByPoints = availablePoints / unitTemplate.getCost();
            if (affordableByPoints < maxCount) {
                maxCount = affordableByPoints;
            }

            for (int j = 0; j < maxCount; j++) {

                String name = unitTemplate.getName() + " " + (counter + 1);
                Position pos = new Position(
                        randomGenerator.nextInt(WIDTH),
                        randomGenerator.nextInt(HEIGHT)
                );

                while (usedPositions.contains(pos)) {
                    pos.x = randomGenerator.nextInt(WIDTH);
                    pos.y = randomGenerator.nextInt(HEIGHT);
                }
                usedPositions.add(pos);

                Unit createdUnit = new Unit(
                        name,
                        unitTemplate.getUnitType(),
                        unitTemplate.getHealth(),
                        unitTemplate.getBaseAttack(),
                        unitTemplate.getCost(),
                        unitTemplate.getAttackType(),
                        unitTemplate.getAttackBonuses(),
                        unitTemplate.getDefenceBonuses(),
                        pos.x,
                        pos.y
                );

                armyUnits.add(createdUnit);
                counter++;
            }

            availablePoints -= maxCount * unitTemplate.getCost();
        }

        newArmy.setUnits(armyUnits);
        return newArmy;
    }

    private static class Position {
        private int x;
        private int y;

        public Position(int xValue, int yValue) {
            this.x = xValue;
            this.y = yValue;
        }

        public void setX(int xValue) {
            this.x = xValue;
        }

        public void setY(int yValue) {
            this.y = yValue;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Position) {
                Position otherPosition = (Position) other;
                return this.x == otherPosition.x && this.y == otherPosition.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.x * 31 + this.y;
        }
    }
}