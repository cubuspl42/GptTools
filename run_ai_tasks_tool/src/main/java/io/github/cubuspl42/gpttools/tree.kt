package io.github.cubuspl42.gpttools

import java.nio.file.Path

fun executeTree(
    path: Path,
): String {
    val pathString = path.toString()
    val process = ProcessBuilder("tree", pathString).start()
    val treeOutput = process.inputStream.bufferedReader().readText()

    return treeOutput
}
