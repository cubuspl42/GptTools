@file:OptIn(ExperimentalPathApi::class)

package io.github.cubuspl42.gpttools

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText
import kotlin.io.path.walk

/**
 * Find files recursively in the given directory that contain the given text pattern.
 *
 * @return a collection of absolute paths to the found files
 */
fun findFilesRecursively(
    rootDirectoryPath: Path,
    pattern: String,
): List<Path> = rootDirectoryPath.walk().mapNotNull { filePath ->
    filePath.takeIf { it.readText().contains(pattern) }
}.toList()
