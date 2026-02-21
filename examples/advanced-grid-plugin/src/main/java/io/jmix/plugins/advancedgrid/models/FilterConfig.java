package io.jmix.plugins.advancedgrid.models;

import java.util.List;
import java.util.Objects;

/**
 * Composite filter description for the grid: a list of conditions
 * combined by a logical operator.
 */
public record FilterConfig(List<Condition> conditions, LogicalOperator combinator) {

    public FilterConfig {
        Objects.requireNonNull(conditions, "conditions");
        if (combinator == null) {
            combinator = LogicalOperator.AND;
        }
    }

    public enum LogicalOperator { AND, OR }

    public enum Operator {
        EQUALS, NOT_EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH,
        GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, BETWEEN, IN, IS_NULL
    }

    /**
     * Single filter condition.
     */
    public record Condition(String field, Operator operator, Object value) {

        public Condition {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("field must not be blank");
            }
            Objects.requireNonNull(operator, "operator");
        }
    }
}
