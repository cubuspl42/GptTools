package io.github.cubuspl42.gpttools

fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> =
    pairs.filterNotNull().toMap()
