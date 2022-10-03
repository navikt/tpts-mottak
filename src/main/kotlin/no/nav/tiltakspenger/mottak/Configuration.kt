package no.nav.tiltakspenger.mottak

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.getunleash.DefaultUnleash
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig

const val TPTS_RAPID_NAME = "tpts.rapid.v1"

enum class Profile {
    LOCAL, DEV, PROD
}

object Configuration {

    val kafka = mapOf(
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",  // earliest?
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-aiven-mottak-v2",
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_SCHEMA_REGISTRY" to System.getenv("KAFKA_SCHEMA_REGISTRY"),
        "KAFKA_SCHEMA_REGISTRY_USER" to System.getenv("KAFKA_SCHEMA_REGISTRY_USER"),
        "KAFKA_SCHEMA_REGISTRY_PASSWORD" to System.getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
    )

    private val otherDefaultProperties = mapOf(
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
    )
    private val defaultProperties = ConfigurationMap(kafka + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.LOCAL.toString(),
        )
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "safUrl" to "https://saf.dev-fss-pub.nais.io",
            "safScope" to "api://dev-fss.teamdokumenthandtering.saf/.default",
            "joarkTopicName" to "teamdokumenthandtering.aapen-dok-journalfoering-q1"
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "safUrl" to "https://saf.prod-fss-pub.nais.io",
            "safScope" to "api://prod-fss.teamdokumenthandtering.saf/.default",
            "joarkTopicName" to "teamdokumenthandtering.aapen-dok-journalfoering"
        )
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

        "prod-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

        else -> {
            systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }


    data class TokenVerificationConfig(
        val jwksUri: String = config()[Key("AZURE_OPENID_CONFIG_JWKS_URI", stringType)],
        val issuer: String = config()[Key("AZURE_OPENID_CONFIG_ISSUER", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val leeway: Long = 1000
    )
}


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
