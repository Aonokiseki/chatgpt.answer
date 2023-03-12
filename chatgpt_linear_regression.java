public class LinearRegression {
    private double slope;
    private double intercept;

    public void fit(double[] x, double[] y) {
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        int n = x.length;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }

        double denom = n * sumXX - sumX * sumX;
        if (denom == 0) {
            throw new IllegalArgumentException("Cannot compute linear regression with a constant input variable.");
        }
        slope = (n * sumXY - sumX * sumY) / denom;
        intercept = (sumY - slope * sumX) / n;
    }

    public double predict(double x) {
        return slope * x + intercept;
    }

    public static void main(String[] args) {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 5, 4, 5};

        LinearRegression lr = new LinearRegression();
        lr.fit(x, y);

        System.out.println("Slope: " + lr.slope);
        System.out.println("Intercept: " + lr.intercept);
        System.out.println("Prediction for x=6: " + lr.predict(6));
    }
}
