package at.tugraz.mclab.localization;

import java.util.ArrayList;

public class FloorPlan {

    public ArrayList<Line> walls;
    public ArrayList<Room> rooms;
    public final double totalArea;

    public FloorPlan() {

        // add walls on the floor plan
        walls = new ArrayList<Line>();
        walls.add(new Line(new Position(00.00, 00.00), new Position(00.00, 84.17))); // Wall A
        walls.add(new Line(new Position(00.00, 00.00), new Position(14.00, 00.00))); // Wall B
        walls.add(new Line(new Position(05.33, 00.00), new Position(05.33, 16.50))); // Wall C
        walls.add(new Line(new Position(05.33, 16.50), new Position(14.00, 16.50))); // Wall D
        walls.add(new Line(new Position(14.00, 00.00), new Position(14.00, 84.17))); // Wall E
        walls.add(new Line(new Position(05.33, 18.50), new Position(14.00, 18.50))); // Wall F
        walls.add(new Line(new Position(05.33, 18.50), new Position(05.33, 43.50))); // Wall G
        walls.add(new Line(new Position(00.00, 43.50), new Position(14.00, 43.50))); // Wall H
        walls.add(new Line(new Position(04.33, 18.50), new Position(04.33, 47.50))); // Wall I
        walls.add(new Line(new Position(00.00, 18.50), new Position(04.33, 18.50))); // Wall J
        walls.add(new Line(new Position(00.00, 16.50), new Position(04.33, 16.50))); // Wall K
        walls.add(new Line(new Position(04.33, 08.50), new Position(04.33, 16.50))); // Wall L
        walls.add(new Line(new Position(00.00, 09.67), new Position(04.33, 09.67))); // Wall M

        // add rooms to the floor plan
        rooms = new ArrayList<Room>();
        rooms.add(new Room(new Position(00.00, 00.00), new Position(04.00, 09.67))); // Lab south I
        rooms.add(new Room(new Position(04.00, 00.00), new Position(05.33, 09.50))); // Lab south II
        rooms.add(new Room(new Position(04.33, 09.50), new Position(05.33, 16.50))); // Aisle
        rooms.add(new Room(new Position(00.00, 16.50), new Position(14.00, 18.50))); // Aisle
        rooms.add(new Room(new Position(04.33, 18.50), new Position(05.33, 43.50))); // Aisle

        double area = 0.0;
        for (Room room : rooms) {
            area += room.area;
        }
        totalArea = area;
    }
}
