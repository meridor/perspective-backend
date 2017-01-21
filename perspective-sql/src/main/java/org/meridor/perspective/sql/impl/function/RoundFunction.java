package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class RoundFunction extends BaseRoundFunction {

    @Override
    public FunctionName getName() {
        return FunctionName.ROUND;
    }

    protected RoundingMode getRoundingMode() {
        return RoundingMode.UP;
    }

    @Override
    public String getSignature() {
        return "ROUND(X, D)";
    }

    @Override
    public String getDescription() {
        return "Rounds the argument X to D decimal places. D should be non-negative.";
    }

}
