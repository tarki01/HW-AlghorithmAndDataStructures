# Итоговый проект "Алгоритмы и структура данных"

---

Jar - файл (obf.jar) находиться в директории JarForTest

---

# Запуск проекта

## Требования
- Java (версия 21 или выше)
- Для сборки проекта используется Gradle

## Сборка и запуск
Командная строка:

```bash
gradle jar
```

---

Проект состоит из следующих модулей:

## 1. GeneratePresetImpl
**Назначение**: Генерация армии с использованием жадного алгоритма.

**Основной функционал**:
- Сортировка юнитов по эффективности (отношение атаки к стоимости, затем здоровья к стоимости)
- Распределение юнитов по игровому полю (3×21 клеток)
- Управление бюджетом (ограничение по очкам)
- Уникальное позиционирование без коллизий

**Ключевые особенности**:
- Использует жадный алгоритм для оптимального распределения юнитов
- Проверка уникальности координат через Set
- Ограничение максимального количества юнитов одного типа (11 единиц)

## 2. SimulateBattleImpl
**Назначение**: Симуляция боя между двумя армиями.

**Основной функционал**:
- Поочередное выполнение раундов боя
- Сортировка юнитов по силе атаки для определения порядка ходов
- Расчет урона с учетом бонусов атаки и защиты
- Управление состоянием юнитов (здоровье, жизнь)

**Ключевые особенности**:
- Учет бонусов атаки против определенных типов юнитов
- Учет бонусов защиты против определенных типов атак
- Логирование боевых действий

## 3. SuitableForAttackUnitsFinderImpl
**Назначение**: Поиск юнитов, доступных для атаки.

**Основной функционал**:
- Анализ позиций юнитов по строкам
- Определение свободных соседних позиций для атаки
- Поддержка направления атаки (влево/вправо)

**Ключевые особенности**:
- Работа с группировкой юнитов по рядам
- Проверка возможности атаки на основе свободных позиций
- Простой и эффективный алгоритм поиска

## 4. UnitTargetPathFinderImpl
**Назначение**: Поиск пути от атакующего юнита к цели с использованием алгоритма A*.

**Основной функционал**:
- Реализация алгоритма A* для поиска пути
- Учет препятствий (других юнитов)
- 8-направленное движение (включая диагонали)
- Расчет оптимального пути с учетом стоимости перемещения

**Ключевые особенности**:
- Эвристика Манхэттенского расстояния
- Приоритетная очередь для оптимизации поиска
- Восстановление пути от цели к началу

---

## Сложность алгоритмов

# 1. GeneratePresetImpl - Жадный алгоритм O(n log n)
Данный участок кода использует метод sort из класса Collections. Здесь применяется сортировка TimSort - сложность данного алгоритма O(n log n). Сортировка TimSort - гибридный алгоритм, который использует алгоритм бинарной вставки для малых подмассивов и сортировку слиянием для объединения отсортированных участков.

```java
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
```

Данный участок кода реализует жадный алгоритм для наполнения массива юнитами. Юниты с большей стоимостью берутся раньше и заполняются до максимума, вплоть до последнего момента. В коде есть ограничение количества юнитов - принято считать её константой, и в конечном рассчете не учитывать. В коде выше был подобран критерий самого объемного юнита. Далее будет заполнение для каждого типа. Учитывая что здесь один цикл, зависящий от списка unit, а второй цикл равен константе то результатом данного кода будет O(n*11) = O(n)

```java
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
```

# 2. SimulateBattleImpl - Алгоритм симуляции боя O(n² log n)

Алгоритм имеет несколько уровней вложенности:
Цикл while (O(n)) - количество раундов
Два цикла по армиям (O(n) каждый) - сбор живых юнитов
Сортировка TimSort (O(n log n)) - сортировка всех активных юнитов
Цикл по всем юнитам (O(n)) - выполнение атак

С учетом вложенности:
Сбор живых юнитов: O(n) + O(n) = O(n)
Сортировка: O(n log n) (внутри цикла раундов)
Выполнение атак: O(n) (внутри цикла раундов)
Наихудший случай, когда в каждом раунде остаются почти все юниты: O(n² log n)

```java
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
```
# 3. SuitableForAttackUnitsFinderImpl - Линейный алгоритм O(n)

Алгоритм состоит из:
 - Внешнего цикла по рядам (максимум 6 рядов - константа)
 - Двух внутренних циклов по юнитам в ряду
 - Каждый юнит обрабатывается дважды: один раз для заполнения множества занятых позиций, и один раз для проверки доступности атаки.

```java
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
```

Общая сложность: O(n), где n - общее количество юнитов.

# 4. UnitTargetPathFinderImpl - Алгоритм A* O(n)

Алгоритм состоит из:
 - Инициализация препятствий: O(n) - проход по всем юнитам
 - Алгоритм A*: В худшем случае O(bᵈ), где b - коэффициент ветвления (8 направлений), d - глубина пути
 - Восстановление пути: O(p), где p - длина пути

```java
public List<Edge> getTargetPath(Unit attackingUnit, Unit targetUnit, List<Unit> allUnitsOnField) {
        boolean[][] blockedPositions = new boolean[FIELD_WIDTH][FIELD_HEIGHT];
        if (!targetUnit.isAlive()) {
            return Collections.emptyList();
        }
        for (Unit unit : allUnitsOnField) {
            if (unit == attackingUnit || unit == targetUnit) {
                continue;
            }
            if (unit.isAlive()) {
                blockedPositions[unit.getxCoordinate()][unit.getyCoordinate()] = true;
            }
        }
        Cell startingCell = new Cell(attackingUnit.getxCoordinate(), attackingUnit.getyCoordinate());
        Cell destinationCell = new Cell(targetUnit.getxCoordinate(), targetUnit.getyCoordinate());

        if (!isPositionValid(startingCell, blockedPositions) || !isPositionValid(destinationCell, blockedPositions)) {
            return Collections.emptyList();
        }
        if (startingCell.xPos == destinationCell.xPos && startingCell.yPos == destinationCell.yPos) {
            List<Edge> singleCellPath = new ArrayList<>();
            singleCellPath.add(new Edge(startingCell.xPos, startingCell.yPos));
            return singleCellPath;
        }
        PriorityQueue<Cell> cellsToExamine = new PriorityQueue<>(Comparator.comparingInt(Cell::getTotalCost));
        Map<String, Cell> predecessorCells = new HashMap<>();
        Map<String, Integer> movementCosts = new HashMap<>();
        Map<String, Integer> totalCostEstimates = new HashMap<>();
        String startingKey = startingCell.xPos + "," + startingCell.yPos;
        String destinationKey = destinationCell.xPos + "," + destinationCell.yPos;
        startingCell.movementCost = 0;
        startingCell.totalCost = estimateDistance(startingCell, destinationCell);
        cellsToExamine.add(startingCell);
        movementCosts.put(startingKey, 0);
        totalCostEstimates.put(startingKey, startingCell.totalCost);
        while (!cellsToExamine.isEmpty()) {
            Cell currentCell = cellsToExamine.poll();
            String currentKey = currentCell.xPos + "," + currentCell.yPos;
            if (currentCell.xPos == destinationCell.xPos && currentCell.yPos == destinationCell.yPos) {
                return buildPath(predecessorCells, currentCell);
            }
            for (int[] direction : MOVEMENT_DIRECTIONS) {
                Cell adjacentCell = new Cell(currentCell.xPos + direction[0], currentCell.yPos + direction[1]);
                String adjacentKey = adjacentCell.xPos + "," + adjacentCell.yPos;
                if (!isPositionValid(adjacentCell, blockedPositions)) {
                    continue;
                }
                int stepCost = (Math.abs(direction[0]) == 1 && Math.abs(direction[1]) == 1) ? 14 : 10;
                int potentialMovementCost = movementCosts.getOrDefault(currentKey, Integer.MAX_VALUE) + stepCost;
                if (potentialMovementCost < movementCosts.getOrDefault(adjacentKey, Integer.MAX_VALUE)) {
                    predecessorCells.put(adjacentKey, currentCell);
                    movementCosts.put(adjacentKey, potentialMovementCost);
                    int distanceEstimate = estimateDistance(adjacentCell, destinationCell);
                    int combinedCost = potentialMovementCost + distanceEstimate;
                    totalCostEstimates.put(adjacentKey, combinedCost);
                    Cell adjacentCellNode = new Cell(adjacentCell.xPos, adjacentCell.yPos);
                    adjacentCellNode.movementCost = potentialMovementCost;
                    adjacentCellNode.totalCost = combinedCost;
                    cellsToExamine.removeIf(cell -> cell.xPos == adjacentCell.xPos && cell.yPos == adjacentCell.yPos);
                    cellsToExamine.add(adjacentCellNode);
                }
            }
        }
        return Collections.emptyList();
    }
```

В контексте игрового поля 27×21 (567 клеток), алгоритм работает эффективно. Поскольку количество юнитов ограничено и распределено по полю, практическая сложность близка к O(n) для инициализации, а поиск пути зависит от размера поля, а не от количества юнитов.


