package at.tugraz.mclab.localization;

public class Line {

    public final Position start;
    public final Position end;

    public Line() {
        start = new Position();
        end = new Position();
    }

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

        // TODO: do intersection check..
        return true;
    }
}
