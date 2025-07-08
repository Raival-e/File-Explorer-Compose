package com.raival.compose.file.explorer.common.extension

fun <T> ArrayList<T>.addIf(item: T, condition: T.() -> Boolean) {
    if (condition(item)) {
        add(item)
    }
}

fun <T> List<T>.getIf(condition: T.() -> Boolean): T? {
    forEach {
        if (condition(it)) {
            return it
        }
    }
    return null
}

fun <T> List<T>.getIndexIf(condition: T.() -> Boolean): Int {
    forEachIndexed { index, item ->
        if (condition(item)) {
            return index
        }
    }
    return -1
}

fun ArrayList<String>.addIfAbsent(toAdd: String) {
    if (!contains(toAdd)) add(toAdd)
}

fun <A, B> HashMap<A, B>.removeIf(condition: (A, B) -> Boolean) {
    keys.forEach {
        if (condition(it, this[it]!!)) {
            remove(it)
        }
    }
}