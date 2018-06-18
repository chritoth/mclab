package at.tugraz.mclab.localization;

import java.util.ArrayList;

public class FloorPlan {

    public ArrayList<Line> walls;
    public ArrayList<Room> rooms;
    public final double totalArea;

    public FloorPlan() {

        // add walls on the floor plan
        walls.add(new Line(new Position(00.00, 00.00), new Position(84.17, 00.00)));
        walls.add(new Line(new Position(00.00, 05.33), new Position(16.50, 05.33)));
        walls.add(new Line(new Position(00.00, 00.00), new Position(00.00, 05.33)));
        walls.add(new Line(new Position(09.67, 00.00), new Position(09.67, 04.00)));
        walls.add(new Line(new Position(09.50, 04.00), new Position(16.50, 04.00)));
        walls.add(new Line(new Position(16.50, 00.00), new Position(16.50, 04.33)));
        walls.add(new Line(new Position(18.50, 00.00), new Position(18.50, 04.33)));
        walls.add(new Line(new Position(18.50, 04.33), new Position(45.50, 04.33)));
        walls.add(new Line(new Position(18.50, 05.67), new Position(43.50, 05.67)));
        walls.add(new Line(new Position(16.50, 05.67), new Position(16.50, 14.00)));
        walls.add(new Line(new Position(18.50, 05.67), new Position(18.50, 14.00)));

        // add rooms to the floor plan
        rooms.add(new Room(new Position(00.00, 00.00), new Position(09.67, 04.00))); // Lab south I
        rooms.add(new Room(new Position(00.00, 04.00), new Position(09.50, 05.33))); // Lab south II
        rooms.add(new Room(new Position(09.50, 04.33), new Position(16.50, 05.33))); // Aisle
        rooms.add(new Room(new Position(16.50, 00.00), new Position(18.50, 14.00))); // Aisle
        rooms.add(new Room(new Position(18.50, 04.33), new Position(43.50, 05.67))); // Aisle

        double area = 0.0;
        for (Room room : rooms) {
            area += room.area;
        }
        totalArea = area;
    }
}
