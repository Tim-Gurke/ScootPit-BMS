package de.wortmonster.jbdtrigger;

public class DisplayCurrentLearnerTest {
    public static void main(String[] args) {
        DisplayCurrentLearner learner=new DisplayCurrentLearner(0,0);
        for(int i=0;i<11;i++)if(learner.observe(-0.20,0.01,true))throw new AssertionError("Learned before stable window");
        if(!learner.observe(-0.20,0.01,true)||learner.learnedAmps()!=0.20)throw new AssertionError("Stable powered-at-rest baseline");
        if(learner.observe(-0.02,0.01,false)||learner.learnedAmps()!=0.20)throw new AssertionError("Ineligible sample changed baseline");
        for(int i=0;i<12;i++)learner.observe(-0.18,0.01,true);
        if(learner.learnedAmps()!=0.18)throw new AssertionError("Lower stable baseline must be more conservative");
        if(!DisplayCurrentLearner.safelyBelow(0,0.01,0.18,0.01))throw new AssertionError("Zero current is below learned display draw");
        if(DisplayCurrentLearner.safelyBelow(-0.17,0.01,0.18,0.01))throw new AssertionError("Current too close to baseline");
        if(DisplayCurrentLearner.safelyBelow(0,0.1,0.1,0.1))throw new AssertionError("Coarse BMS resolution cannot distinguish baseline");
        if(DisplayCurrentLearner.safelyBelow(Double.NaN,0.01,0.18,0.01))throw new AssertionError("Invalid current accepted");
        for(int i=0;i<20;i++)learner.observe(-0.01,0.01,true);
        if(learner.learnedAmps()!=0.18 || !DisplayCurrentLearner.safelyBelow(0,0.01,learner.learnedAmps(),learner.learnedResolution()))
            throw new AssertionError("A residual current at the BMS resolution poisoned the powered-at-rest baseline");
        DisplayCurrentLearner unusable=new DisplayCurrentLearner(0.01,0.01);
        if(unusable.learnedAmps()!=0)throw new AssertionError("A saved baseline indistinguishable from zero must be relearned");
        for(int i=0;i<12;i++)unusable.observe(-0.20,0.01,true);
        if(unusable.learnedAmps()!=0.20)throw new AssertionError("An unusable saved baseline prevented learning a valid value");
        DisplayCurrentLearner firstResidual=new DisplayCurrentLearner(0,0);
        for(int i=0;i<20;i++)firstResidual.observe(-0.01,0.01,true);
        if(firstResidual.learnedAmps()!=0)throw new AssertionError("An initial residual current must not become the reference");
        if(new DisplayCurrentLearner(0.1,0.1).learnedAmps()!=0)throw new AssertionError("Coarse saved reference must be relearned");
        if(new DisplayCurrentLearner(0.3,0.01).learnedAmps()!=0.3)throw new AssertionError("Valid saved reference was lost");
        System.out.println("Display-current learning and conservative cutoff margin: OK");
    }
}
