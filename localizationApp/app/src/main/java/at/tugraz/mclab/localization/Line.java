package at.tugraz.mclab.localization;

public class Line {

    public final Position start;
    public final Position end;

    public Line(Position start, Position end) {

        // make sure that the starting point has lower x coordinate
        if (start.getX() < end.getX()) {
            this.start = new Position(start);
            this.end = new Position(end);
        } else {
            this.start = new Position(end);
            this.end = new Position(start);
        }
    }

    public boolean intersects(Line line) {
        // check if x intervals overlap
        if (line.end.leftOf(start) || line.start.rightOf(end))
            return false;

        // compute point orientations
        int o1 = Position.orientation(start, end, line.start);
        int o2 = Position.orientation(start, end, line.end);
        int o3 = Position.orientation(line.start, line.end, start);
        int o4 = Position.orientation(line.start, line.end, end);

        // if no points are co-linear, orientations should be pairwise different if the lines intersect
        if (o1 * o2 < 0 && o3 * o4 < 0)
            return true;

        // if we have co-linear points we have to further check if the lines touch at any point
        if (o1 == 0 && start.leftOf(line.start) && end.rightOf(line.start))
            return true;

        if (o2 == 0 && start.leftOf(line.end) && end.rightOf(line.end))
            return true;

        if (o3 == 0 && line.start.leftOf(start) && line.end.rightOf(start))
            return true;

        if (o4 == 0 && line.start.leftOf(end) && line.end.rightOf(end))
            return true;

        return false;
    }

}
