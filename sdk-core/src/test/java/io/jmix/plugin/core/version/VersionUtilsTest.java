package io.jmix.plugin.core.version;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionUtilsTest {

    @Test
    void satisfiesAcceptsExactMatch() {
        assertThat(VersionUtils.satisfies("2.1.0", "2.1.0")).isTrue();
    }

    @Test
    void satisfiesAcceptsRangeOperators() {
        assertThat(VersionUtils.satisfies("2.5.3", ">=2.0.0 <3.0.0")).isTrue();
        assertThat(VersionUtils.satisfies("3.0.0", ">=2.0.0 <3.0.0")).isFalse();
        assertThat(VersionUtils.satisfies("2.1.5", "^2.0.0")).isTrue();
        assertThat(VersionUtils.satisfies("3.0.0", "^2.0.0")).isFalse();
    }

    @Test
    void satisfiesReturnsFalseForInvalidInput() {
        assertThat(VersionUtils.satisfies(null, "1.0.0")).isFalse();
        assertThat(VersionUtils.satisfies("not-a-version", "1.0.0")).isFalse();
        assertThat(VersionUtils.satisfies("1.0.0", "?broken")).isFalse();
    }

    @Test
    void isValidRecognisesSemver() {
        assertThat(VersionUtils.isValid("1.0.0")).isTrue();
        assertThat(VersionUtils.isValid("2.1.3-rc.1")).isTrue();
        assertThat(VersionUtils.isValid("not-a-version")).isFalse();
    }
}
