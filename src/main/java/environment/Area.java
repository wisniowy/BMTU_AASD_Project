package environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Area {
    private static int ID = 0;

    private final int id = ID++;

    public int getId() {
        return id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Area area = (Area) o;

        return id == area.id;
    }

    @Override
    public int hashCode() {
        return id;
    }


    private static final int ROAD = 0;
    private static final int WALL = 1;

    private int[][] area;
    private final List<Coordinate> conservatorsInitialPosition = new ArrayList<>();

    public Area(File maze) throws FileNotFoundException {
        String fileText = "";
        try (Scanner input = new Scanner(maze)) {
            while (input.hasNextLine()) {
                fileText += input.nextLine() + "\n";
            }
        }
        initializeArea(fileText);
    }

    private void initializeArea(String text) {
        if (text == null || (text = text.trim()).length() == 0) {
            throw new IllegalArgumentException("empty lines data");
        }

        String[] lines = text.split("[\r]?\n");
        area = new int[lines.length][lines[0].length()];

        for (int row = 0; row < getHeight(); row++) {
            if (lines[row].length() != getWidth()) {
                throw new IllegalArgumentException("line " + (row + 1) + " wrong length (was " + lines[row].length() + " but should be " + getWidth() + ")");
            }

            for (int col = 0; col < getWidth(); col++) {
                if (lines[row].charAt(col) == '#') {
                    area[row][col] = WALL;
                } else if (lines[row].charAt(col) == 'o') {
                    area[row][col] = ROAD;
                } else if (lines[row].charAt(col) == 'c') {
                    area[row][col] = ROAD;
                    conservatorsInitialPosition.add(new Coordinate(col, row));
                }
            }
        }
    }

    public int getHeight() {
        return area.length;
    }

    public int getWidth() {
        return area[0].length;
    }

    public List<Coordinate> getConservatorsInitialPosition() {
        return conservatorsInitialPosition;
    }

    public boolean isWall(int x, int y) {
        return area[y][x] == WALL;
    }


    public boolean isValidLocation(int x, int y) {
        if (y < 0 || y >= getHeight() || x < 0 || x >= getWidth()) {
            return false;
        }
        return true;
    }

    public String toString(int[][] maze) {
        StringBuilder result = new StringBuilder(getWidth() * (getHeight() + 1));
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (maze[row][col] == ROAD) {
                    result.append(' ');
                } else if (maze[row][col] == WALL) {
                    result.append('#');
                } else {
                    result.append('.');
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}
