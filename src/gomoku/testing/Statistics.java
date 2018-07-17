package gomoku.testing;

// Keep track of match wins, losses, etc

public class Statistics {

    private int wins, losses, draws, Xwins, Xlosses, Xdraws;
    private int totalGameLength; // sum of individual game lengths

    private int lastGameLength;

    private int size; // Match size
    private int current; // current match number

    public Statistics(int size) {
        this.size = size;
    }

    // result in the perspective of the X player
    public void addData(int result, boolean switchSides, int gameLength) {
        if (result == 1) {
            Xwins++;

            if (!switchSides) {
                wins++;
            } else {
                losses++;
            }
        } else if (result == -1) {
            Xlosses++;

            if (!switchSides) {
                losses++;
            } else {
                wins++;
            }
        } else {
            Xdraws++;
            draws++;
        }
        current++;
        totalGameLength += gameLength;
        lastGameLength = gameLength;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getCurrent() {
        return current;
    }

    public void changeSize(int size) {
        this.size = size;
    }

    public void print(int gameLength) {
        System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + current + " out of " + size + " games completed.");
        //Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
        System.out.print("Elo difference: " + format(2, elo((wins + 0.5 * draws) / current)));
        System.out.print(" +" + format(2,(double) Math.round(1000 * (elo((wins + 0.5 * draws) / current + 1.95996 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000));
        System.out.print(" " + format(2, (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current - 1.95996 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000));
        System.out.println(" (95% confidence)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("LOS: " + format(5, (0.5 + 0.5 * erf((wins - losses) / Math.sqrt(2.0 * (wins + losses))))));
        System.out.println("Game length: " + gameLength);
        System.out.println("Average game length: " + format(2, (double) totalGameLength / current));
        System.out.println("(X Perspective) W-L-D " + Xwins + "-" + Xlosses + "-" + Xdraws);
        System.out.print("    Elo difference: " + format(2, elo((Xwins + 0.5 * Xdraws) / current)));
        System.out.print(" +" + format(2,(double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current + 1.95996 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000));
        System.out.print(" " + format(2,(double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current - 1.95996 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000));
        System.out.println(" (95% confidence)");
        System.out.println("    LOS: " + format(5, (0.5 + 0.5 * erf((Xwins - Xlosses) / Math.sqrt(2.0 * (Xwins + Xlosses))))));

        System.out.println();
    }

    public void print() {
        print(lastGameLength);
    }

    public void printSPRT(int gameLength) {
        System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + current + " games completed.");
        //Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
        System.out.print("Elo difference: " + format(2, elo((wins + 0.5 * draws) / current)));
        System.out.print(" +" + format(2,(double) Math.round(1000 * (elo((wins + 0.5 * draws) / current + 1.95996 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000));
        System.out.print(" " + format(2, (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current - 1.95996 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000));
        System.out.println(" (95% confidence)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("LOS: " + format(5, (0.5 + 0.5 * erf((wins - losses) / Math.sqrt(2.0 * (wins + losses))))));
        System.out.println("Game length: " + gameLength);
        System.out.println("Average game length: " + format(2, (double) totalGameLength / current));
        System.out.println("(X Perspective) W-L-D " + Xwins + "-" + Xlosses + "-" + Xdraws);
        System.out.print("    Elo difference: " + format(2, elo((Xwins + 0.5 * Xdraws) / current)));
        System.out.print(" +" + format(2,(double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current + 1.95996 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000));
        System.out.print(" " + format(2,(double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current - 1.95996 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000));
        System.out.println(" (95% confidence)");
        System.out.println("    LOS: " + format(5, (0.5 + 0.5 * erf((Xwins - Xlosses) / Math.sqrt(2.0 * (Xwins + Xlosses))))));

        System.out.println();
    }

    public void printSPRT() {
        printSPRT(lastGameLength);
    }

    // round
    private String format(int places, double num) {
        return String.format("%." + Integer.toString(places) + "f", num);
    }

    private double elo(double mean) {
        return -400.0 * Math.log(1.0 / mean - 1) / Math.log(10.0);
    }

    private static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                t * ( 1.00002368 +
                        t * ( 0.37409196 +
                                t * ( 0.09678418 +
                                        t * (-0.18628806 +
                                                t * ( 0.27886807 +
                                                        t * (-1.13520398 +
                                                                t * ( 1.48851587 +
                                                                        t * (-0.82215223 +
                                                                                t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;

        // source: https://introcs.cs.princeton.edu/java/21function/ErrorFunction.java.html
    }

    private double standardErrorOfMeanScore() {
        double mean = (wins + 0.5 * draws) / current;
        double standardDeviation = Math.sqrt((wins * Math.pow(1 - mean, 2) + draws * Math.pow(0.5 - mean, 2) + losses * Math.pow(mean, 2)) / (current + 1));
        return standardDeviation / Math.sqrt(current);
    }

    private double XstandardErrorOfMeanScore() {
        double mean = (Xwins + 0.5 * Xdraws) / current;
        double standardDeviation = Math.sqrt((Xwins * Math.pow(1 - mean, 2) + Xdraws * Math.pow(0.5 - mean, 2) + Xlosses * Math.pow(mean, 2)) / (current + 1));
        return standardDeviation / Math.sqrt(current);
    }
}
