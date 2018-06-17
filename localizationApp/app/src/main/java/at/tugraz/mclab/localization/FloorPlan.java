package at.tugraz.mclab.localization;

import java.util.ArrayList;

public class FloorPlan {

    public ArrayList<Line> walls;
    public ArrayList<Room> rooms;
    public final double totalArea;

    public FloorPlan() {

        // TODO: add walls on the floor plan
        walls.add(new Line(new Position(0, 0), new Position(0, 0)));

        // TODO: add rooms to the floor plan
        rooms.add(new Room(new Position(0, 0), new Position(0, 0)));

        double area = 0.0;
        for (Room room : rooms) {
            area += room.area;
        }
        totalArea = area;
    }
}
