package com.cao.flighttracker.common;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AdminKeyGuardTest {

    @Test
    void noConfiguredKeyLetsEverythingThrough() {
        AdminKeyGuard guard = new AdminKeyGuard("");

        assertThatCode(() -> guard.check(null)).doesNotThrowAnyException();
    }

    @Test
    void correctKeyIsAccepted() {
        AdminKeyGuard guard = new AdminKeyGuard("s3cret");

        assertThatCode(() -> guard.check("s3cret")).doesNotThrowAnyException();
    }

    @Test
    void wrongOrMissingKeyIsRejected() {
        AdminKeyGuard guard = new AdminKeyGuard("s3cret");

        assertThatThrownBy(() -> guard.check("wrong")).isInstanceOf(AdminKeyException.class);
        assertThatThrownBy(() -> guard.check(null)).isInstanceOf(AdminKeyException.class);
    }
}