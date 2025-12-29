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
    public void simulate(Army armyOne, Army armyTwo) throws InterruptedException {
        int currentRound = 1;

        while (true) {

            List<Unit> livingUnitsFirst = new ArrayList<>();
            List<Unit> livingUnitsSecond = new ArrayList<>();

            for (Unit unit : armyOne.getUnits()) {
                if (unit.isAlive()) {
                    livingUnitsFirst.add(unit);
                }
            }

            for (Unit unit : armyTwo.getUnits()) {
                if (unit.isAlive()) {
                    livingUnitsSecond.add(unit);
                }
            }

            if (livingUnitsFirst.isEmpty() || livingUnitsSecond.isEmpty()) {
                break;
            }

            List<Unit> allActiveUnits = new ArrayList<>();
            allActiveUnits.addAll(livingUnitsFirst);
            allActiveUnits.addAll(livingUnitsSecond);

            allActiveUnits.sort(new java.util.Comparator<Unit>() {
                @Override
                public int compare(Unit first, Unit second) {
                    return Integer.compare(second.getBaseAttack(), first.getBaseAttack());
                }
            });

            List<Unit> eliminatedThisRound = new ArrayList<>();

            for (Unit attackingUnit : allActiveUnits) {
                if (!attackingUnit.isAlive() || eliminatedThisRound.contains(attackingUnit)) {
                    continue;
                }

                Unit targetUnit = attackingUnit.getProgram().attack();

                if (targetUnit != null && (targetUnit.isAlive() || !eliminatedThisRound.contains(targetUnit))) {

                    logBattleAction(attackingUnit, targetUnit);

                    executeAttack(attackingUnit, targetUnit);

                    if (targetUnit.getHealth() <= 0) {
                        targetUnit.setHealth(0);
                        targetUnit.setAlive(false);
                        eliminatedThisRound.add(targetUnit);
                    }
                }
            }

            currentRound++;
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