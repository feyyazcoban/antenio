package dev.iptvplayer.tv.data

/**
 * The features NEXTV Plus would open (a proposal, not a decision). Each is checked once where it is entered: a Settings
 * page names its feature, and the access gate answers [GateResult.NeedsPlus] for a target that needs one.
 */
enum class Feature { CATCHUP, REMINDERS, FAVORITE_GROUPS, CUSTOM_ORDER, RENAME, RECORDING, MULTI_VIEW, ALWAYS_BUFFER }

/** Which [Feature]s this device may use. */
interface Entitlements { fun has(feature: Feature): Boolean }

/** Every feature for everyone: what the app uses wherever it asks, until sales open (gap plan S37). */
object AllFree : Entitlements { override fun has(feature: Feature) = true }
