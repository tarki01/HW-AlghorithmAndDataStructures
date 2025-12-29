# Итоговый проект "Алгоритмы и структура данных"

---

Jar - файл (obf.jar) находиться в директории JarForTest

---

# Запуск проекта

## Требования
- Java (17)
- Для сборки проекта используется Gradle

## Сборка и запуск
Командная строка:

```bash
gradle jar
```

---

# Проект состоит из следующих модулей:

## 1. GeneratePresetImpl
**Назначение**: Генерация армии с использованием жадного алгоритма.

**Основной функционал**:
- Сортировка юнитов по эффективности (отношение атаки к стоимости, затем здоровья к стоимости)
- Распределение юнитов по игровому полю (3×21 клеток)
- Управление бюджетом (ограничение по очкам)
- Уникальное позиционирование без коллизий

## 2. SimulateBattleImpl
**Назначение**: Симуляция боя между двумя армиями.

**Основной функционал**:
- Поочередное выполнение раундов боя
- Сортировка юнитов по силе атаки для определения порядка ходов
- Расчет урона с учетом бонусов атаки и защиты
- Управление состоянием юнитов (здоровье, жизнь)

## 3. SuitableForAttackUnitsFinderImpl
**Назначение**: Поиск юнитов, доступных для атаки.

**Основной функционал**:
- Анализ позиций юнитов по строкам
- Определение свободных соседних позиций для атаки
- Поддержка направления атаки (влево/вправо)

## 4. UnitTargetPathFinderImpl
**Назначение**: Поиск пути от атакующего юнита к цели с использованием алгоритма A*.

**Основной функционал**:
- Реализация алгоритма A* для поиска пути
- Учет препятствий (других юнитов)
- 8-направленное движение (включая диагонали)
- Расчет оптимального пути с учетом стоимости перемещения

---

## Сложность алгоритмов

# 1. GeneratePresetImpl - Жадный алгоритм O(n log n)
Данная реализация использует стандартную сортировку Collections.sort(), которая в Java реализована алгоритмом TimSort со сложностью O(n log n) в среднем и худшем случаях. TimSort является гибридным алгоритмом, комбинирующим сортировку вставками для небольших массивов и сортировку слиянием для эффективного объединения упорядоченных последовательностей. Это обеспечивает стабильную производительность при работе с данными разного размера.

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

Данный фрагмент кода реализует жадный алгоритм формирования армии, при котором юниты с наилучшим соотношением эффективности выбираются первыми и размещаются в максимально возможном количестве до исчерпания бюджета. Количество экземпляров каждого типа юнитов ограничено сверху константой (11), поэтому вложенный цикл выполняется фиксированное число раз для каждого юнита. **Поскольку внешний цикл зависит от количества доступных типов юнитов (n), а внутренний — от константы (11), итоговая алгоритмическая сложность составляет O(n·11) = O(n).**

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

Таким образом, армия создаётся в рамках лимита очков а также учитывается стоимость. Помимо этого, юниты сортируются по убыванию baseAttack/cost, затем по health/cost.
**Получаем, что общая сложность данного кода = O(n log n) + O(n) = O(n log n).**

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

**Алгоритм имеет сложность O(n² log n) в худшем случае, так как в каждом из O(n) раундов выполняется сортировка всех активных юнитов за O(n log n).**

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

Таким образом, ходы сортируются по baseAttack, атака происходит, проверяется смерть и проверяется isAlive, мёртвые юниты пропускаются.

# 3. SuitableForAttackUnitsFinderImpl - Линейный алгоритм O(n)

Алгоритм состоит из:
 - Внешнего цикла по рядам (максимум 6 рядов - константа)
 - Двух внутренних циклов по юнитам в ряду
 - Каждый юнит обрабатывается дважды: один раз для заполнения множества занятых позиций, и один раз для проверки доступности атаки.
 - Получается, что общая сложность: O(n) + O(n) = O(n), где n - общее количество юнитов.

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

Таким образом, в коде проверяет свободную клетку перед юнитом а также если нет свободной клетки, юнит не добавляется.

# 4. UnitTargetPathFinderImpl - Алгоритм A* O(W·H·log(W·H))

Данный алгоритм включает следующие этапы:
 - Инициализация препятствий — один проход по всем юнитам: O(N)
 - Поиск пути (основной цикл A*) — исследует ячейки поля, но количество обрабатываемых узлов ограничено размером поля (W×H), а не количеством юнитов O(W·H·log(W·H))
 - Восстановление пути — линейно зависит от длины найденного пути, которая в худшем случае составляет O(W+H)

Где,
N — это количество юнитов
W — ширина поля (FIELD_WIDTH = 27)
H — высота поля (FIELD_HEIGHT = 21)

**Таким образом, общая сложность равна O(n) + O(W·H·log(W·H)) + O(W+H). Однако, учитывая, что:**
 - **O(W+H) поглощается O(W·H·log(W·H)), так как W+H ≤ 2·max(W,H), а W·H растёт быстрее.**
 - **O(n) также поглощается, поскольку W·H·log(W·H) обычно >> n для больших полей.**
**Итоговая сложность: O(W·H·log(W·H))**

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

    private boolean isPositionValid(Cell cellPosition, boolean[][] blockedPositions) {
        return cellPosition.xPos >= 0 && cellPosition.xPos < FIELD_WIDTH &&
                cellPosition.yPos >= 0 && cellPosition.yPos < FIELD_HEIGHT &&
                !blockedPositions[cellPosition.xPos][cellPosition.yPos];
    }

    private int estimateDistance(Cell fromCell, Cell toCell) {
        int horizontalDistance = Math.abs(fromCell.xPos - toCell.xPos);
        int verticalDistance = Math.abs(fromCell.yPos - toCell.yPos);
        return 10 * (horizontalDistance + verticalDistance);
    }

    private List<Edge> buildPath(Map<String, Cell> predecessorMap, Cell finalCell) {
        List<Edge> resultingPath = new ArrayList<>();
        String currentKey = finalCell.xPos + "," + finalCell.yPos;
        resultingPath.add(new Edge(finalCell.xPos, finalCell.yPos));
        while (predecessorMap.containsKey(currentKey)) {
            finalCell = predecessorMap.get(currentKey);
            currentKey = finalCell.xPos + "," + finalCell.yPos;
            resultingPath.add(0, new Edge(finalCell.xPos, finalCell.yPos));
        }
        return resultingPath;
    }
```
