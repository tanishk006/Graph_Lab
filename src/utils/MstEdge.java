package utils;

public record MstEdge(int from, int to, double weight) {
    @Override
    public String toString() {
        return from + "-" + to + "(" + weight + ")";
    }
}
