package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army firstArmy, Army secondArmy) throws InterruptedException {
        int roundCounter = 1;

        while (true) {

            List<Unit> activeFirstArmyUnits = new ArrayList<>();
            List<Unit> activeSecondArmyUnits = new ArrayList<>();

            for (Unit unitObject : firstArmy.getUnits()) {
                if (unitObject.isAlive()) {
                    activeFirstArmyUnits.add(unitObject);
                }
            }

            for (Unit unitObject : secondArmy.getUnits()) {
                if (unitObject.isAlive()) {
                    activeSecondArmyUnits.add(unitObject);
                }
            }

            if (activeFirstArmyUnits.isEmpty() || activeSecondArmyUnits.isEmpty()) {
                break;
            }

            List<Unit> allAvailableUnits = new ArrayList<>();
            allAvailableUnits.addAll(activeFirstArmyUnits);
            allAvailableUnits.addAll(activeSecondArmyUnits);

            allAvailableUnits.sort(new java.util.Comparator<Unit>() {
                @Override
                public int compare(Unit unitA, Unit unitB) {
                    return Integer.compare(unitB.getBaseAttack(), unitA.getBaseAttack());
                }
            });

            List<Unit> destroyedInCurrentRound = new ArrayList<>();

            for (Unit currentAttacker : allAvailableUnits) {
                if (!currentAttacker.isAlive() || destroyedInCurrentRound.contains(currentAttacker)) {
                    continue;
                }

                Unit selectedTarget = currentAttacker.getProgram().attack();

                if (selectedTarget != null && (selectedTarget.isAlive() || !destroyedInCurrentRound.contains(selectedTarget))) {

                    logBattleAction(currentAttacker, selectedTarget);

                    executeAttack(currentAttacker, selectedTarget);

                    if (selectedTarget.getHealth() <= 0) {
                        selectedTarget.setHealth(0);
                        selectedTarget.setAlive(false);
                        destroyedInCurrentRound.add(selectedTarget);
                    }
                }
            }

            roundCounter++;
        }
    }

    private void executeAttack(Unit attacker, Unit defender) {

        int initialDamage = attacker.getBaseAttack();
        double damageModifier = 1.0;

        Map<String, Double> attackerAdvantages = attacker.getAttackBonuses();
        if (attackerAdvantages != null && attackerAdvantages.containsKey(defender.getUnitType())) {
            damageModifier *= attackerAdvantages.get(defender.getUnitType());
        }

        Map<String, Double> defenderResistances = defender.getDefenceBonuses();
        if (defenderResistances != null && defenderResistances.containsKey(attacker.getAttackType())) {
            damageModifier /= defenderResistances.get(attacker.getAttackType());
        }

        int resultingDamage = (int) Math.round(initialDamage * damageModifier);

        defender.setHealth(defender.getHealth() - resultingDamage);
    }

    private void logBattleAction(Unit attacker, Unit target) {
        printBattleLog.printBattleLog(attacker, target);
    }
}