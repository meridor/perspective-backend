package org.meridor.perspective.sql.impl.function;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class CRC32Function implements Function<String> {

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1)
        );
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.CRC32;
    }

    @Override
    public String apply(List<Object> objects) {
        String strValue = String.valueOf(objects.get(0));
        CRC32 crc = new CRC32();
        crc.update(strValue.getBytes());
        return Long.toHexString(crc.getValue());
    }

    @Override
    public String getSignature() {
        return "CRC32(S)";
    }

    @Override
    public String getDescription() {
        return "Returns the CRC32 sum of string S.";
    }


}
