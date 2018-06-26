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
        walls.add(new Line(new Position(05.33, 43.50), new Position(14.00, 43.50))); // Wall H
        walls.add(new Line(new Position(04.33, 18.50), new Position(04.33, 45.50))); // Wall I
        walls.add(new Line(new Position(00.00, 18.50), new Position(04.33, 18.50))); // Wall J
        walls.add(new Line(new Position(00.00, 16.50), new Position(04.33, 16.50))); // Wall K
        walls.add(new Line(new Position(04.33, 08.50), new Position(04.33, 16.50))); // Wall L
        walls.add(new Line(new Position(00.00, 09.67), new Position(04.33, 09.67))); // Wall M
        walls.add(new Line(new Position(00.00, 45.50), new Position(04.33, 45.50))); // Wall N
        walls.add(new Line(new Position(00.00, 47.50), new Position(14.00, 47.50))); // Wall O
        walls.add(new Line(new Position(10.00, 43.50), new Position(10.00, 45.50))); // Wall O
        walls.add(new Line(new Position(10.00, 45.50), new Position(14.00, 45.50))); // Wall Q

        walls.add(new Line(new Position(00.00, 00.90), new Position(03.75, 00.90))); // Furniture Lab South
        walls.add(new Line(new Position(00.00, 02.56), new Position(03.75, 02.56))); // Furniture Lab South
        walls.add(new Line(new Position(00.00, 04.22), new Position(03.75, 04.22))); // Furniture Lab South
        walls.add(new Line(new Position(00.00, 05.88), new Position(03.75, 05.88))); // Furniture Lab South
        walls.add(new Line(new Position(00.00, 07.54), new Position(03.75, 07.54))); // Furniture Lab South
        walls.add(new Line(new Position(03.75, 00.00), new Position(03.75, 00.90))); // Furniture Lab South
        walls.add(new Line(new Position(03.75, 02.56), new Position(03.75, 04.22))); // Furniture Lab South
        walls.add(new Line(new Position(03.75, 05.88), new Position(03.75, 07.54))); // Furniture Lab South

        // add rooms to the floor plan
        rooms = new ArrayList<Room>();
        rooms.add(new Room(new Position(00.00, 00.90), new Position(03.75, 02.56))); // Lab south
        rooms.add(new Room(new Position(00.00, 04.22), new Position(03.75, 05.88))); // Lab south
        rooms.add(new Room(new Position(00.00, 07.54), new Position(03.75, 09.67))); // Lab south
        rooms.add(new Room(new Position(03.75, 00.00), new Position(05.33, 09.50))); // Lab south
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
