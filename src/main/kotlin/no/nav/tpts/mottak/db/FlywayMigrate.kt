package no.nav.tpts.mottak.db

import org.flywaydb.core.Flyway

fun flywayMigrate() {
    Flyway.configure()
}