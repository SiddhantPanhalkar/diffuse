package com.jakewharton.diffuse.diff

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.Manifest
import com.jakewharton.diffuse.diffuseTable

internal class ManifestDiff(
  val oldManifest: Manifest,
  val newManifest: Manifest
) {
  internal val parsedPropertiesChanged = oldManifest.packageName != newManifest.packageName ||
      oldManifest.versionName != newManifest.versionName ||
      oldManifest.versionCode != newManifest.versionCode

  val diff: List<String> = run {
    val oldLines = oldManifest.xml.lines()
    val newLines = newManifest.xml.lines()
    val diff = DiffUtils.diff(oldLines, newLines)
    UnifiedDiffUtils.generateUnifiedDiff(Apk.manifestFileName, Apk.manifestFileName, oldLines, diff, 1)
  }

  val changed = parsedPropertiesChanged || diff.isNotEmpty()
}

internal fun ManifestDiff.toDetailReport() = buildString {
  if (parsedPropertiesChanged) {
    appendln()
    appendln(diffuseTable {
      header {
        row("", "old", "new")
      }
      row("package", oldManifest.packageName, newManifest.packageName)
      row("version code", oldManifest.versionCode, newManifest.versionCode)
      row("version name", oldManifest.versionName, newManifest.versionName)
    })
  }
  if (diff.isNotEmpty()) {
    appendln()
    diff.drop(2) // Skip file name headers
        .forEach { appendln(it) }
    appendln()
  }
}
