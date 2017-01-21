package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.beans.BooleanRelation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

final class FunctionUtils {

    @SafeVarargs
    static Set<String> oneOf(List<Object> args, java.util.function.Function<List<Object>, Set<String>>... conditions) {
        for (Function<List<Object>, Set<String>> condition : conditions) {
            Set<String> errors = condition.apply(args);
            if (!errors.isEmpty()) {
                return errors;
            }
        }
        return Collections.emptySet();
    }

    static java.util.function.Function<List<Object>, Set<String>> argsCount(int count) {
        return argsCount(BooleanRelation.EQUAL, count);
    }

    static java.util.function.Function<List<Object>, Set<String>> argsCount(BooleanRelation relation, int count) {
        return args -> {
            boolean condition;
            String format;
            switch (relation) {
                case EQUAL:
                    condition = args.size() == count;
                    format = String.format("%d arguments only", count);
                    break;
                case GREATER_THAN:
                    condition = args.size() > count;
                    format = String.format("%d or more arguments", count + 1);
                    break;
                case LESS_THAN:
                    condition = args.size() < count;
                    format = String.format("no more than %d arguments", count - 1);
                    break;
                case GREATER_THAN_EQUAL:
                    condition = args.size() >= count;
                    format = String.format("%d or more arguments", count);
                    break;
                case LESS_THAN_EQUAL:
                    condition = args.size() <= count;
                    format = String.format("no more than %d arguments", count);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported action. This is a bug.");
            }
            if (!condition) {
                return Collections.singleton(String.format("Function accepts %s", format));
            }
            return Collections.emptySet();
        };
    }

    static java.util.function.Function<List<Object>, Set<String>> isNumber(int argPosition) {
        return ifArgPresent(argPosition, args -> {
            Object arg = args.get(argPosition);
            if (!(arg instanceof Number)) {
                return Collections.singleton(String.format(
                        "Function argument %d should be a number but a %s is given",
                        argPosition, arg != null ? arg.getClass().getCanonicalName() : "NULL"
                ));
            }
            return Collections.emptySet();
        });
    }

    static java.util.function.Function<List<Object>, Set<String>> isInteger(int argPosition) {
        return isInteger(argPosition, 10);
    }

    static java.util.function.Function<List<Object>, Set<String>> isInteger(int argPosition, int radix) {
        return ifArgPresent(argPosition, args -> {
            Object arg = args.get(argPosition);
            try {
                Integer.parseInt(String.valueOf(arg), radix);
                return Collections.emptySet();
            } catch (NumberFormatException ignored) {
                return Collections.singleton(String.format(
                        "Function argument %d should be an integer but a %s is given",
                        argPosition, arg != null ? arg.getClass().getCanonicalName() : "NULL"
                ));
            }
        });
    }

    static java.util.function.Function<List<Object>, Set<String>> between(int argPosition, double left, double right) {
        return ifArgPresent(argPosition, args -> {
            Object arg = args.get(argPosition);
            Double value = Double.valueOf(String.valueOf(arg));
            if (value < left || value > right) {
                return Collections.singleton(String.format(
                        "Function argument %d should be a number between %f and %f but %f is given",
                        argPosition, left, right, value
                ));
            }
            return Collections.emptySet();
        });
    }

    static java.util.function.Function<List<Object>, Set<String>> numberRelation(int argPosition, BooleanRelation relation, double threshold) {
        return ifArgPresent(argPosition, args -> {
            Object arg = args.get(argPosition);
            Double value = Double.valueOf(String.valueOf(arg));
            switch (relation) {
                case EQUAL:
                    if (value != threshold) {
                        return Collections.singleton(String.format(
                                "Function argument %d should equal to %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                case GREATER_THAN:
                    if (value <= threshold) {
                        return Collections.singleton(String.format(
                                "Function argument %d should greater than %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                case LESS_THAN:
                    if (value >= threshold) {
                        return Collections.singleton(String.format(
                                "Function argument %d should less than %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                case GREATER_THAN_EQUAL:
                    if (value < threshold) {
                        return Collections.singleton(String.format(
                                "Function argument %d should greater than or equal %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                case LESS_THAN_EQUAL:
                    if (value > threshold) {
                        return Collections.singleton(String.format(
                                "Function argument %d should less than or equal %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                case NOT_EQUAL:
                    if (value.equals(threshold)) {
                        return Collections.singleton(String.format(
                                "Function argument %d should not be equal to %f",
                                argPosition, threshold
                        ));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported action. This is a bug.");
            }
            return Collections.emptySet();
        });
    }

    private static java.util.function.Function<List<Object>, Set<String>> ifArgPresent(int position, java.util.function.Function<List<Object>, Set<String>> function) {
        return args -> {
            if (args.size() > position) {
                return function.apply(args);
            }
            return Collections.emptySet();
        };
    }

}
