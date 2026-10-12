package de.wortmonster.jbdtrigger;

/** Display conversion only; ride energy and range calculations remain in Wh and Wh/km. */
final class ConsumptionUnit {
    private ConsumptionUnit() {}

    static double percentPer10Km(double whPerKm,double fullCapacityAh,double nominalVoltage) {
        if(!Double.isFinite(whPerKm)||whPerKm<0||!Double.isFinite(fullCapacityAh)||fullCapacityAh<=0||!Double.isFinite(nominalVoltage)||nominalVoltage<=0)return Double.NaN;
        double packWh=fullCapacityAh*nominalVoltage;
        return Double.isFinite(packWh)&&packWh>0?whPerKm*1000d/packWh:Double.NaN;
    }

    static double normalizedProgress(double whPerKm,double scaleMaxWhPerKm) {
        if(!Double.isFinite(whPerKm)||!Double.isFinite(scaleMaxWhPerKm)||scaleMaxWhPerKm<=0)return Double.NaN;
        return Math.max(0,Math.min(1,whPerKm/scaleMaxWhPerKm));
    }
}
