package de.wortmonster.jbdtrigger;

public class ConsumptionUnitTest {
    public static void main(String[] args) {
        near(16.0256410256,ConsumptionUnit.percentPer10Km(20,26,48));
        near(32.0512820513,ConsumptionUnit.percentPer10Km(40,26,48));
        near(0,ConsumptionUnit.percentPer10Km(0,26,48));
        if(!Double.isNaN(ConsumptionUnit.percentPer10Km(20,0,48)))throw new AssertionError("zero capacity");
        if(!Double.isNaN(ConsumptionUnit.percentPer10Km(Double.NaN,26,48)))throw new AssertionError("missing consumption");
        near(.5,ConsumptionUnit.normalizedProgress(15,30));
        near(1,ConsumptionUnit.normalizedProgress(45,30));
        if(!Double.isNaN(ConsumptionUnit.normalizedProgress(20,0)))throw new AssertionError("zero scale");
        System.out.println("Consumption display conversion: OK");
    }

    private static void near(double expected,double actual) {
        if(!Double.isFinite(actual)||Math.abs(expected-actual)>.000001)throw new AssertionError(actual+" != "+expected);
    }
}
