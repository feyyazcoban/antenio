package dev.iptvplayer.tv.data

/**
 * The features NEXTV Plus would open (a proposal, not a decision). Each is checked once where it is entered. Only the
 * names exist so far, for a Settings page to name its feature; the entitlement check arrives with the access gate.
 */
enum class Feature { CATCHUP, REMINDERS, FAVORITE_GROUPS, CUSTOM_ORDER, RENAME, RECORDING, MULTI_VIEW, ALWAYS_BUFFER }
