package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private final static int FIELD_WIDTH = 27;
    private final static int FIELD_HEIGHT = 21;

    private final static int[][] MOVEMENT_DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @Override
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

    private static class Cell {
        final int xPos;
        final int yPos;
        int movementCost;
        int totalCost;

        Cell(int x, int y) {
            this.xPos = x;
            this.yPos = y;
            this.movementCost = 0;
            this.totalCost = 0;
        }

        int getTotalCost() {
            return totalCost;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Cell otherCell = (Cell) object;
            return xPos == otherCell.xPos && yPos == otherCell.yPos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(xPos, yPos);
        }
    }
}