package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ShortestPathFinder {

    private static final int[][] DIRECTIONS = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

    public static List<Coordinate> solve(final Area area, final Coordinate start, final Coordinate destination) {
        LinkedList<Node> nextToVisit = new LinkedList<>();
        nextToVisit.add(new Node(start.getX(), start.getY(), null));
        boolean[][] visited = new boolean[area.getHeight()][area.getWidth()];;


        while (!nextToVisit.isEmpty()) {
            Node cur = nextToVisit.remove();

            if (!area.isValidLocation(cur.getX(), cur.getY()) || visited[cur.getY()][cur.getX()]) {
                continue;
            }

            if (area.isWall(cur.getX(), cur.getY())) {
                visited[cur.getY()][cur.getX()] = true;
                continue;
            }

            if (cur.getX() == destination.getX() && cur.getY() == destination.getY()) {
                return backtrackPath(cur).stream()
                        .map(node -> new Coordinate(node.getX(), node.getY()))
                        .collect(Collectors.toList());
            }

            for (int[] direction : DIRECTIONS) {
                Node node = new Node(cur.getX() + direction[0], cur.getY() + direction[1], cur);
                nextToVisit.add(node);
                visited[cur.getY()][cur.getX()] = true;
            }
        }
        return Collections.emptyList();
    }

    private static List<Node> backtrackPath(final Node cur) {
        List<Node> path = new ArrayList<>();
        Node iter = cur;

        while (iter != null) {
            path.add(iter);
            iter = iter.parent;
        }

        return path;
    }

    private static class Node {
        private final int x;
        private final int y;
        private final Node parent;

        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }
    }
}
