package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class TruncateFunction extends BaseRoundFunction {

    @Override
    public FunctionName getName() {
        return FunctionName.TRUNCATE;
    }

    @Override
    protected RoundingMode getRoundingMode() {
        return RoundingMode.DOWN;
    }

    @Override
    public String getSignature() {
        return "TRUNCATE(X, D)";
    }

    @Override
    public String getDescription() {
        return "Returns the number X, truncated to D decimal places. D should be non-negative.";
    }

}
