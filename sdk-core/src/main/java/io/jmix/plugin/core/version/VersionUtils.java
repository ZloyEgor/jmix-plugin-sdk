package io.jmix.plugin.core.version;

import org.semver4j.RangesList;
import org.semver4j.RangesListFactory;
import org.semver4j.Semver;
import org.semver4j.SemverException;

/**
 * Helper around the {@code semver4j} library matching the semantics of
 * the npm {@code semver} package that the TypeScript prototype relied
 * on.
 */
public final class VersionUtils {

    private VersionUtils() {
    }

    /**
     * Returns {@code true} when {@code version} satisfies the given
     * version range. Both arguments must use semantic versioning syntax;
     * invalid input yields {@code false}.
     */
    public static boolean satisfies(String version, String range) {
        if (version == null || range == null) {
            return false;
        }
        try {
            Semver semver = new Semver(version);
            RangesList ranges = RangesListFactory.create(range);
            return semver.satisfies(ranges);
        } catch (SemverException ex) {
            return false;
        }
    }

    /**
     * Returns {@code true} when the given string is a syntactically
     * valid semver version.
     */
    public static boolean isValid(String version) {
        return Semver.isValid(version);
    }
}
