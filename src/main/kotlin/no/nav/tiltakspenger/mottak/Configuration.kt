package no.nav.tiltakspenger.mottak

import io.getunleash.DefaultUnleash
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig

const val TPTS_RAPID_NAME = "tpts.rapid.v1"

private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
    return when (System.getenv("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> devValue
        "prod-gcp" -> prodValue
        else -> devValue
    }
}

fun getSafUrl(): String = getPropertyValueByEnvironment(
    devValue = "https://saf.dev-fss-pub.nais.io", prodValue = "https://saf.prod-fss-pub.nais.io"
)

fun getSafScope(): String = getPropertyValueByEnvironment(
    devValue = "api://dev-fss.teamdokumenthandtering.saf/.default",
    prodValue = "api://prod-fss.teamdokumenthandtering.saf/.default"
)

fun joarkTopicName(): String = getPropertyValueByEnvironment(
    devValue = "teamdokumenthandtering.aapen-dok-journalfoering-q1",
    prodValue = "teamdokumenthandtering.aapen-dok-journalfoering"
)

val unleash by lazy {
    DefaultUnleash(
        UnleashConfig.builder()
            .appName(requireNotNull(System.getenv("NAIS_APP_NAME")) { "Expected NAIS_APP_NAME" })
            .instanceId(requireNotNull(System.getenv("HOSTNAME")) { "Expected HOSTNAME" })
            .environment(requireNotNull(System.getenv("NAIS_CLUSTER_NAME")) { "Expected NAIS_CLUSTER_NAME" })
            .unleashAPI("https://unleash.nais.io/api/")
            .build(), ByClusterStrategy(System.getenv("NAIS_CLUSTER_NAME"))
    )
}

class ByClusterStrategy(private val cluster: String) : Strategy {
    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: Map<String, String>) =
        if (parameters["cluster"] == null) false else parameters["cluster"]!!.contains(cluster, ignoreCase = true)
}
